package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.*;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class BankInterestManager {
    private final DZTradeHub plugin;
    private final BankManager bankManager;
    private final BankAccountManager accountManager;
    
    public BankInterestManager(DZTradeHub plugin, BankManager bankManager, BankAccountManager accountManager) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.accountManager = accountManager;
        
        startInterestTask();
    }
    
    /**
     * Start interest payout task
     */
    private void startInterestTask() {
        int intervalMinutes = bankManager.getConfigManager().getInterestPayoutIntervalMinutes();
        long intervalTicks = intervalMinutes * 60 * 20L; // Convert minutes to ticks
        
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            processInterestPayouts();
        }, intervalTicks, intervalTicks);
        
        plugin.getLogger().info("Interest payout task started (every " + intervalMinutes + " minutes)");
    }
    
    /**
     * Process interest payouts for all eligible accounts
     */
    private void processInterestPayouts() {
        int accountsProcessed = 0;
        double totalInterestPaid = 0;
        
        for (Bank bank : bankManager.getAllBanks()) {
            for (CurrencyType currency : bank.getEnabledCurrencies()) {
                // Process all accounts for this bank and currency
                List<BankAccount> accounts = getAllAccountsForBank(bank.getBankId());
                
                for (BankAccount account : accounts) {
                    // Only INTEREST account type earns interest
                    if (account.getAccountType() != AccountType.INTEREST) {
                        continue;
                    }
                    
                    double balance = account.getBalance(currency);
                    if (balance <= 0) {
                        continue;
                    }
                    
                    // Calculate interest
                    double interestRate = calculateInterestRate(bank, account);
                    double interestAmount = balance * (interestRate / 100.0);
                    
                    if (interestAmount > 0) {
                        // Add interest to account
                        double balanceBefore = account.getBalance(currency);
                        account.addBalance(currency, interestAmount);
                        account.addInterestEarned((long) interestAmount);
                        
                        // Update account
                        accountManager.updateAccount(account);
                        
                        // Record transaction
                        recordInterestTransaction(account, currency, interestAmount, balanceBefore, account.getBalance(currency));
                        
                        // Notify player if online
                        Player player = plugin.getServer().getPlayer(account.getPlayerUUID());
                        if (player != null && player.isOnline()) {
                            player.sendMessage("§a✓ Interest earned: " + String.format("%.2f", interestAmount) + " " + currency.name());
                            player.sendMessage("§eNew balance: " + String.format("%.2f", account.getBalance(currency)) + " " + currency.name());
                        }
                        
                        accountsProcessed++;
                        totalInterestPaid += interestAmount;
                    }
                }
            }
        }
        
        if (accountsProcessed > 0) {
            plugin.getLogger().info("Interest payout complete: " + accountsProcessed + " accounts, total: " + String.format("%.2f", totalInterestPaid));
        }
    }
    
    /**
     * Calculate interest rate for account
     */
    private double calculateInterestRate(Bank bank, BankAccount account) {
        // Base rate from config (would need to be added to bank config loading)
        double baseRate = 2.0; // Default 2%
        
        // Add level bonus
        double levelBonus = 0;
        BankLevelConfig levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            levelBonus = levelConfig.getInterestRateBonus();
        }
        
        return baseRate + levelBonus;
    }
    
    /**
     * Get all accounts for a bank
     */
    private List<BankAccount> getAllAccountsForBank(UUID bankId) {
        // This is a simplified version - in production you'd query from database or cache
        List<BankAccount> result = new ArrayList<>();
        
        // Get all accounts from account manager
        // Note: This is inefficient, in production you'd maintain a bank->accounts index
        for (UUID playerUUID : getAllPlayerUUIDs()) {
            List<BankAccount> playerAccounts = accountManager.getPlayerAccounts(playerUUID);
            for (BankAccount account : playerAccounts) {
                if (account.getBankId().equals(bankId)) {
                    result.add(account);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Get all player UUIDs (helper method)
     */
    private Set<UUID> getAllPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        plugin.getServer().getOnlinePlayers().forEach(p -> uuids.add(p.getUniqueId()));
        // TODO: Also load from database for offline players
        return uuids;
    }
    
    /**
     * Record interest transaction
     */
    private void recordInterestTransaction(BankAccount account, CurrencyType currency, 
                                          double amount, double balanceBefore, double balanceAfter) {
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
                stmt.setString(5, BankTransactionType.INTEREST_EARNED.name());
                stmt.setString(6, currency.name());
                stmt.setDouble(7, amount);
                stmt.setDouble(8, balanceBefore);
                stmt.setDouble(9, balanceAfter);
                stmt.setLong(10, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to record interest transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Calculate potential interest for preview
     */
    public double calculatePotentialInterest(BankAccount account, CurrencyType currency) {
        Bank bank = bankManager.getBankById(account.getBankId());
        if (bank == null || account.getAccountType() != AccountType.INTEREST) {
            return 0;
        }
        
        double balance = account.getBalance(currency);
        double interestRate = calculateInterestRate(bank, account);
        return balance * (interestRate / 100.0);
    }
}