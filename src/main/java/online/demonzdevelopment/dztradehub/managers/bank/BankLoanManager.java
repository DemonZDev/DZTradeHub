package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.*;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankLoanManager {
    private final DZTradeHub plugin;
    private final BankManager bankManager;
    private final BankAccountManager accountManager;
    private final Map<UUID, BankLoan> loansById;
    private final Map<UUID, List<BankLoan>> loansByPlayer;
    
    public BankLoanManager(DZTradeHub plugin, BankManager bankManager, BankAccountManager accountManager) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.accountManager = accountManager;
        this.loansById = new ConcurrentHashMap<>();
        this.loansByPlayer = new ConcurrentHashMap<>();
        
        loadLoansFromDatabase();
        startLoanChecker();
    }
    
    /**
     * Load all active loans from database
     */
    private void loadLoansFromDatabase() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM bank_loans WHERE active = 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BankLoan loan = loadLoanFromResultSet(rs);
                if (loan != null) {
                    registerLoan(loan);
                }
            }
            
            plugin.getLogger().info("Loaded " + loansById.size() + " active loans");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load loans: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load loan from ResultSet
     */
    private BankLoan loadLoanFromResultSet(ResultSet rs) throws SQLException {
        UUID loanId = UUID.fromString(rs.getString("loan_id"));
        UUID accountId = UUID.fromString(rs.getString("account_id"));
        UUID bankId = UUID.fromString(rs.getString("bank_id"));
        UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
        
        CurrencyType currency;
        try {
            currency = CurrencyType.valueOf(rs.getString("currency"));
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        double principal = rs.getDouble("principal_amount");
        double interestRate = rs.getDouble("interest_rate");
        int paymentPeriod = rs.getInt("payment_period_minutes");
        
        BankLoan loan = new BankLoan(accountId, bankId, playerUUID, currency, principal, interestRate, paymentPeriod);
        loan.setLoanId(loanId);
        loan.setRemainingAmount(rs.getDouble("remaining_amount"));
        loan.setCurrentInterestRate(rs.getDouble("current_interest_rate"));
        loan.setIssuedTime(rs.getLong("issued_time"));
        loan.setDueTime(rs.getLong("due_time"));
        loan.setMissedPayments(rs.getInt("missed_payments"));
        loan.setActive(rs.getInt("active") == 1);
        loan.setDefaulted(rs.getInt("defaulted") == 1);
        
        return loan;
    }
    
    /**
     * Issue a new loan
     */
    public BankLoan issueLoan(Player player, BankAccount account, CurrencyType currency, double amount) {
        Bank bank = bankManager.getBankById(account.getBankId());
        if (bank == null) {
            player.sendMessage("§cBank not found!");
            return null;
        }
        
        // Check if loans are enabled
        if (!bank.isLoansEnabled()) {
            player.sendMessage("§cThis bank does not offer loans!");
            return null;
        }
        
        // Check currency enabled
        if (!bank.isCurrencyEnabled(currency)) {
            player.sendMessage("§cThis bank does not accept " + currency.name() + "!");
            return null;
        }
        
        // Check amount limits
        if (amount < bank.getMinLoanAmount()) {
            player.sendMessage("§cMinimum loan amount: " + String.format("%.2f", bank.getMinLoanAmount()));
            return null;
        }
        if (amount > bank.getMaxLoanAmount()) {
            player.sendMessage("§cMaximum loan amount: " + String.format("%.2f", bank.getMaxLoanAmount()));
            return null;
        }
        
        // Check for existing active loans
        List<BankLoan> activeLoans = getActiveLoans(player.getUniqueId());
        if (!activeLoans.isEmpty()) {
            player.sendMessage("§cYou already have an active loan!");
            player.sendMessage("§cPay off your existing loan first.");
            return null;
        }
        
        // Create loan
        int paymentPeriod = bankManager.getConfigManager().getLoanPaymentPeriodMinutes();
        BankLoan loan = new BankLoan(
            account.getAccountId(),
            bank.getBankId(),
            player.getUniqueId(),
            currency,
            amount,
            bank.getLoanInterestRate(),
            paymentPeriod
        );
        
        // Deposit loan amount to account
        double balanceBefore = account.getBalance(currency);
        account.addBalance(currency, amount);
        accountManager.updateAccount(account);
        
        // Save loan
        if (saveLoanToDatabase(loan)) {
            registerLoan(loan);
            
            // Record transaction
            recordLoanTransaction(account, BankTransactionType.LOAN_ISSUED, currency, amount, balanceBefore, account.getBalance(currency));
            
            // Notify player
            player.sendMessage("§a✓ Loan approved!");
            player.sendMessage("§eAmount: " + String.format("%.2f", amount) + " " + currency.name());
            player.sendMessage("§eInterest Rate: " + String.format("%.1f", loan.getInterestRate()) + "%");
            player.sendMessage("§eDue in: " + (paymentPeriod / 60) + " hours");
            player.sendMessage("§cMake sure to deposit money before due date!");
            
            return loan;
        }
        
        return null;
    }
    
    /**
     * Make a loan payment
     */
    public boolean makePayment(Player player, BankLoan loan) {
        BankAccount account = accountManager.getAccountById(loan.getAccountId());
        if (account == null) {
            return false;
        }
        
        double paymentAmount = loan.calculatePaymentAmount();
        CurrencyType currency = loan.getCurrency();
        
        // Check if account has balance
        if (!account.hasBalance(currency, paymentAmount)) {
            player.sendMessage("§cInsufficient balance in account!");
            player.sendMessage("§cRequired: " + String.format("%.2f", paymentAmount) + " " + currency.name());
            player.sendMessage("§cCurrent: " + String.format("%.2f", account.getBalance(currency)) + " " + currency.name());
            return false;
        }
        
        // Deduct payment
        double balanceBefore = account.getBalance(currency);
        account.deductBalance(currency, paymentAmount);
        
        // Process payment
        boolean paidOff = loan.makePayment(paymentAmount);
        
        if (paidOff) {
            loan.setActive(false);
            player.sendMessage("§a✓ Loan paid off completely!");
        } else {
            player.sendMessage("§a✓ Payment received!");
            player.sendMessage("§eRemaining: " + String.format("%.2f", loan.getRemainingAmount()) + " " + currency.name());
        }
        
        // Update database
        accountManager.updateAccount(account);
        updateLoanInDatabase(loan);
        
        // Record transaction
        recordLoanTransaction(account, BankTransactionType.LOAN_PAYMENT, currency, paymentAmount, balanceBefore, account.getBalance(currency));
        
        if (paidOff) {
            unregisterLoan(loan);
        }
        
        return true;
    }
    
    /**
     * Save loan to database
     */
    private boolean saveLoanToDatabase(BankLoan loan) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO bank_loans (loan_id, account_id, bank_id, player_uuid, " +
                        "currency, principal_amount, remaining_amount, interest_rate, " +
                        "current_interest_rate, issued_time, due_time, payment_period_minutes, " +
                        "missed_payments, active, defaulted) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, loan.getLoanId().toString());
            stmt.setString(2, loan.getAccountId().toString());
            stmt.setString(3, loan.getBankId().toString());
            stmt.setString(4, loan.getPlayerUUID().toString());
            stmt.setString(5, loan.getCurrency().name());
            stmt.setDouble(6, loan.getPrincipalAmount());
            stmt.setDouble(7, loan.getRemainingAmount());
            stmt.setDouble(8, loan.getInterestRate());
            stmt.setDouble(9, loan.getCurrentInterestRate());
            stmt.setLong(10, loan.getIssuedTime());
            stmt.setLong(11, loan.getDueTime());
            stmt.setInt(12, loan.getPaymentPeriodMinutes());
            stmt.setInt(13, loan.getMissedPayments());
            stmt.setInt(14, loan.isActive() ? 1 : 0);
            stmt.setInt(15, loan.isDefaulted() ? 1 : 0);
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save loan: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update loan in database
     */
    private void updateLoanInDatabase(BankLoan loan) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "UPDATE bank_loans SET remaining_amount = ?, current_interest_rate = ?, " +
                            "due_time = ?, missed_payments = ?, active = ?, defaulted = ? " +
                            "WHERE loan_id = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setDouble(1, loan.getRemainingAmount());
                stmt.setDouble(2, loan.getCurrentInterestRate());
                stmt.setLong(3, loan.getDueTime());
                stmt.setInt(4, loan.getMissedPayments());
                stmt.setInt(5, loan.isActive() ? 1 : 0);
                stmt.setInt(6, loan.isDefaulted() ? 1 : 0);
                stmt.setString(7, loan.getLoanId().toString());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update loan: " + e.getMessage());
            }
        });
    }
    
    /**
     * Record loan transaction
     */
    private void recordLoanTransaction(BankAccount account, BankTransactionType type, 
                                      CurrencyType currency, double amount, 
                                      double balanceBefore, double balanceAfter) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "INSERT INTO bank_transactions (transaction_id, account_id, bank_id, " +
                            "player_uuid, transaction_type, currency, amount, balance_before, " +
                            "balance_after, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, account.getAccountId().toString());
                stmt.setString(3, account.getBankId().toString());
                stmt.setString(4, account.getPlayerUUID().toString());
                stmt.setString(5, type.name());
                stmt.setString(6, currency.name());
                stmt.setDouble(7, amount);
                stmt.setDouble(8, balanceBefore);
                stmt.setDouble(9, balanceAfter);
                stmt.setLong(10, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to record transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Start loan checker task
     */
    private void startLoanChecker() {
        // Check every 5 minutes
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            checkOverdueLoans();
        }, 6000L, 6000L); // 6000 ticks = 5 minutes
    }
    
    /**
     * Check for overdue loans
     */
    private void checkOverdueLoans() {
        List<BankLoan> overdueLoans = new ArrayList<>();
        
        for (BankLoan loan : loansById.values()) {
            if (loan.isOverdue() && !loan.isDefaulted()) {
                overdueLoans.add(loan);
            }
        }
        
        for (BankLoan loan : overdueLoans) {
            handleOverdueLoan(loan);
        }
    }
    
    /**
     * Handle overdue loan
     */
    private void handleOverdueLoan(BankLoan loan) {
        BankAccount account = accountManager.getAccountById(loan.getAccountId());
        if (account == null) {
            return;
        }
        
        // Check if player deposited money
        double paymentAmount = loan.calculatePaymentAmount();
        
        if (account.hasBalance(loan.getCurrency(), paymentAmount)) {
            // Auto-deduct payment
            double balanceBefore = account.getBalance(loan.getCurrency());
            account.deductBalance(loan.getCurrency(), paymentAmount);
            
            boolean paidOff = loan.makePayment(paymentAmount);
            
            accountManager.updateAccount(account);
            updateLoanInDatabase(loan);
            
            recordLoanTransaction(account, BankTransactionType.LOAN_PAYMENT, 
                loan.getCurrency(), paymentAmount, balanceBefore, account.getBalance(loan.getCurrency()));
            
            // Notify player if online
            Player player = plugin.getServer().getPlayer(loan.getPlayerUUID());
            if (player != null && player.isOnline()) {
                if (paidOff) {
                    player.sendMessage("§a✓ Loan payment deducted - Loan paid off!");
                    unregisterLoan(loan);
                } else {
                    player.sendMessage("§a✓ Loan payment deducted automatically");
                    player.sendMessage("§eRemaining: " + String.format("%.2f", loan.getRemainingAmount()));
                }
            }
            
            if (paidOff) {
                loan.setActive(false);
            }
        } else {
            // Player didn't deposit - increase interest
            loan.handleMissedPayment();
            updateLoanInDatabase(loan);
            
            // Notify player if online
            Player player = plugin.getServer().getPlayer(loan.getPlayerUUID());
            if (player != null && player.isOnline()) {
                player.sendMessage("§c§l⚠ LOAN PAYMENT MISSED!");
                player.sendMessage("§cInterest rate increased to " + String.format("%.1f", loan.getCurrentInterestRate()) + "%");
                player.sendMessage("§cDeposit " + String.format("%.2f", loan.calculatePaymentAmount()) + " " + loan.getCurrency().name() + " before next due date!");
            }
            
            plugin.getLogger().warning("Player " + account.getPlayerName() + " missed loan payment. Interest increased.");
        }
    }
    
    /**
     * Register loan in memory
     */
    private void registerLoan(BankLoan loan) {
        loansById.put(loan.getLoanId(), loan);
        loansByPlayer.computeIfAbsent(loan.getPlayerUUID(), k -> new ArrayList<>()).add(loan);
    }
    
    /**
     * Unregister loan from memory
     */
    private void unregisterLoan(BankLoan loan) {
        loansById.remove(loan.getLoanId());
        List<BankLoan> playerLoans = loansByPlayer.get(loan.getPlayerUUID());
        if (playerLoans != null) {
            playerLoans.remove(loan);
        }
    }
    
    // Getters
    public BankLoan getLoanById(UUID loanId) {
        return loansById.get(loanId);
    }
    
    public List<BankLoan> getPlayerLoans(UUID playerUUID) {
        return loansByPlayer.getOrDefault(playerUUID, new ArrayList<>());
    }
    
    public List<BankLoan> getActiveLoans(UUID playerUUID) {
        return getPlayerLoans(playerUUID).stream()
            .filter(BankLoan::isActive)
            .toList();
    }
    
    public boolean hasActiveLoan(UUID playerUUID) {
        return !getActiveLoans(playerUUID).isEmpty();
    }
}