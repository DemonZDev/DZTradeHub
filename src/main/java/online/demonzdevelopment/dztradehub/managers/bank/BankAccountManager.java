package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.*;
import online.demonzdevelopment.dztradehub.utils.PasswordUtil;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bank account manager with secure password handling.
 * 
 * ⚠️ SECURITY: Never log sensitive data ⚠️
 * - Passwords (plaintext or hashed)
 * - Session tokens
 * - Bank balances in error messages shown to other players
 * 
 * Only log:
 * - Account IDs
 * - Player UUIDs/names
 * - Operation results (success/failure)
 * - Lockout events (without credentials)
 */
public class BankAccountManager {
    private final DZTradeHub plugin;
    private final BankManager bankManager;
    private final Map<UUID, BankAccount> accountsById;
    private final Map<UUID, List<BankAccount>> accountsByPlayer; // player UUID -> accounts
    
    // Rate limiting for brute-force protection
    private final Map<UUID, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lockoutTime = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutes
    
    // Account operation locks for thread safety
    private final Map<UUID, Object> accountLocks = new ConcurrentHashMap<>();
    
    private volatile boolean accountsLoaded = false;
    
    public BankAccountManager(DZTradeHub plugin, BankManager bankManager) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.accountsById = new ConcurrentHashMap<>();
        this.accountsByPlayer = new ConcurrentHashMap<>();
        
        // MEDIUM-004 FIX: Load accounts asynchronously to prevent main thread blocking
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            loadAccountsFromDatabase();
            accountsLoaded = true;
            plugin.getLogger().info("§aBank accounts loaded asynchronously");
        });
        
        // Periodic cleanup of expired lockouts
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            lockoutTime.entrySet().removeIf(entry -> now > entry.getValue());
        }, 6000L, 6000L); // Every 5 minutes
    }
    
    /**
     * Check if accounts have finished loading.
     * MEDIUM-004: Allows checking load status for async operations.
     */
    public boolean isLoaded() {
        return accountsLoaded;
    }
    
    /**
     * Load all accounts from database
     */
    private void loadAccountsFromDatabase() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM bank_accounts WHERE active = 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BankAccount account = loadAccountFromResultSet(rs);
                if (account != null) {
                    registerAccount(account);
                }
            }
            
            plugin.getLogger().info("Loaded " + accountsById.size() + " bank accounts");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load bank accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load account from ResultSet
     */
    private BankAccount loadAccountFromResultSet(ResultSet rs) throws SQLException {
        UUID accountId = UUID.fromString(rs.getString("account_id"));
        UUID bankId = UUID.fromString(rs.getString("bank_id"));
        UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
        String playerName = rs.getString("player_name");
        
        BankAccount account = new BankAccount(accountId, bankId, playerUUID, playerName);
        
        try {
            account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        } catch (IllegalArgumentException e) {
            account.setAccountType(AccountType.SAVINGS);
        }
        
        account.setPasswordHash(rs.getString("password_hash"));
        account.setAccountLevel(rs.getInt("account_level"));
        account.setBalance(CurrencyType.MONEY, rs.getDouble("money_balance"));
        account.setBalance(CurrencyType.MOBCOIN, rs.getDouble("mobcoin_balance"));
        account.setBalance(CurrencyType.GEM, rs.getDouble("gem_balance"));
        account.setCreatedTime(rs.getLong("created_time"));
        account.setLastAccessTime(rs.getLong("last_access_time"));
        account.setTotalTransactions(rs.getInt("total_transactions"));
        account.setTotalInterestEarned(rs.getLong("total_interest_earned"));
        account.setActive(rs.getInt("active") == 1);
        account.setLocked(rs.getInt("locked") == 1);
        
        return account;
    }
    
    /**
     * Create a new bank account
     */
    public BankAccount createAccount(UUID playerUUID, String playerName, Bank bank, AccountType accountType, String password) {
        // Validate password
        if (!PasswordUtil.isPasswordValid(password)) {
            return null;
        }
        
        // Check if player already has account at this bank
        if (hasAccountAtBank(playerUUID, bank.getBankId())) {
            return null;
        }
        
        // Check max accounts limit
        if (getPlayerAccountCount(playerUUID) >= bankManager.getConfigManager().getMaxAccountsPerPlayer()) {
            return null;
        }
        
        // Check if account type is available
        if (!bank.isAccountTypeAvailable(accountType)) {
            return null;
        }
        
        // Hash password
        String passwordHash = PasswordUtil.hashPassword(password);
        
        // Create account
        BankAccount account = new BankAccount(bank.getBankId(), playerUUID, playerName, accountType, passwordHash);
        
        // Save to database
        if (saveAccountToDatabase(account)) {
            registerAccount(account);
            return account;
        }
        
        return null;
    }
    
    /**
     * Save account to database
     */
    private boolean saveAccountToDatabase(BankAccount account) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO bank_accounts (account_id, bank_id, player_uuid, player_name, " +
                        "account_type, password_hash, account_level, money_balance, mobcoin_balance, " +
                        "gem_balance, created_time, last_access_time, total_transactions, " +
                        "total_interest_earned, active, locked) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, account.getAccountId().toString());
            stmt.setString(2, account.getBankId().toString());
            stmt.setString(3, account.getPlayerUUID().toString());
            stmt.setString(4, account.getPlayerName());
            stmt.setString(5, account.getAccountType().name());
            stmt.setString(6, account.getPasswordHash());
            stmt.setInt(7, account.getAccountLevel());
            stmt.setDouble(8, account.getBalance(CurrencyType.MONEY));
            stmt.setDouble(9, account.getBalance(CurrencyType.MOBCOIN));
            stmt.setDouble(10, account.getBalance(CurrencyType.GEM));
            stmt.setLong(11, account.getCreatedTime());
            stmt.setLong(12, account.getLastAccessTime());
            stmt.setInt(13, account.getTotalTransactions());
            stmt.setLong(14, account.getTotalInterestEarned());
            stmt.setInt(15, account.isActive() ? 1 : 0);
            stmt.setInt(16, account.isLocked() ? 1 : 0);
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save bank account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update account in database
     */
    public void updateAccount(BankAccount account) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "UPDATE bank_accounts SET account_type = ?, account_level = ?, " +
                            "money_balance = ?, mobcoin_balance = ?, gem_balance = ?, " +
                            "last_access_time = ?, total_transactions = ?, total_interest_earned = ?, " +
                            "locked = ? WHERE account_id = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, account.getAccountType().name());
                stmt.setInt(2, account.getAccountLevel());
                stmt.setDouble(3, account.getBalance(CurrencyType.MONEY));
                stmt.setDouble(4, account.getBalance(CurrencyType.MOBCOIN));
                stmt.setDouble(5, account.getBalance(CurrencyType.GEM));
                stmt.setLong(6, account.getLastAccessTime());
                stmt.setInt(7, account.getTotalTransactions());
                stmt.setLong(8, account.getTotalInterestEarned());
                stmt.setInt(9, account.isLocked() ? 1 : 0);
                stmt.setString(10, account.getAccountId().toString());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update bank account: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete account
     */
    public boolean deleteAccount(UUID accountId) {
        BankAccount account = getAccountById(accountId);
        if (account == null) {
            return false;
        }
        
        // Check for active loans
        // TODO: Check if account has active loans
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE bank_accounts SET active = 0 WHERE account_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountId.toString());
            stmt.executeUpdate();
            
            unregisterAccount(account);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete account: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify password (with account object)
     * Includes brute-force protection and auto-migration from legacy hashes.
     */
    public boolean verifyPassword(BankAccount account, String password) {
        if (account == null) {
            return false;
        }
        
        UUID accountId = account.getAccountId();
        
        // Check if account is locked out due to failed attempts
        Long lockout = lockoutTime.get(accountId);
        if (lockout != null && System.currentTimeMillis() < lockout) {
            long remainingSeconds = (lockout - System.currentTimeMillis()) / 1000;
            return false; // Still locked out
        }
        
        // Verify the password
        boolean valid = PasswordUtil.verifyPassword(password, account.getPasswordHash());
        
        if (!valid) {
            // Increment failed attempts
            int attempts = failedAttempts.getOrDefault(accountId, 0) + 1;
            failedAttempts.put(accountId, attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                // Lock account for 5 minutes
                lockoutTime.put(accountId, System.currentTimeMillis() + LOCKOUT_DURATION_MS);
                failedAttempts.remove(accountId);
                plugin.getLogger().warning("Account " + accountId + " locked due to " + MAX_FAILED_ATTEMPTS + " failed password attempts");
            }
            
            return false;
        }
        
        // Password is correct - clear failed attempts
        failedAttempts.remove(accountId);
        lockoutTime.remove(accountId);
        
        // Auto-migrate from legacy SHA-256 to PBKDF2 if needed
        if (PasswordUtil.isLegacyHash(account.getPasswordHash())) {
            plugin.getLogger().info("Auto-migrating legacy password hash to PBKDF2 for account: " + accountId);
            String newHash = PasswordUtil.hashPassword(password);
            account.setPasswordHash(newHash);
            updatePasswordHash(account);
        }
        
        return true;
    }
    
    /**
     * Verify password (with account ID)
     */
    public boolean verifyPassword(UUID accountId, String password) {
        BankAccount account = getAccountById(accountId);
        if (account == null) {
            return false;
        }
        return verifyPassword(account, password);
    }
    
    /**
     * Check if an account is currently locked out.
     */
    public boolean isAccountLockedOut(UUID accountId) {
        Long lockout = lockoutTime.get(accountId);
        return lockout != null && System.currentTimeMillis() < lockout;
    }
    
    /**
     * Get remaining lockout time in seconds.
     */
    public long getRemainingLockoutTime(UUID accountId) {
        Long lockout = lockoutTime.get(accountId);
        if (lockout == null || System.currentTimeMillis() >= lockout) {
            return 0;
        }
        return (lockout - System.currentTimeMillis()) / 1000;
    }
    
    /**
     * Update only the password hash in database (for auto-migration).
     */
    private void updatePasswordHash(BankAccount account) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "UPDATE bank_accounts SET password_hash = ? WHERE account_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, account.getPasswordHash());
                stmt.setString(2, account.getAccountId().toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to update password hash: " + e.getMessage());
            }
        });
    }
    
    /**
     * Save account (sync)
     */
    public void saveAccount(BankAccount account) {
        updateAccount(account);
    }
    
    /**
     * Record a transaction
     */
    public void recordTransaction(BankAccount account, CurrencyType currency, double amount, 
                                 double afterTax, BankTransactionType type) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "INSERT INTO bank_transactions (transaction_id, account_id, " +
                            "transaction_type, currency_type, amount, after_tax, timestamp) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, account.getAccountId().toString());
                stmt.setString(3, type.name());
                stmt.setString(4, currency.name());
                stmt.setDouble(5, amount);
                stmt.setDouble(6, afterTax);
                stmt.setLong(7, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to record transaction: " + e.getMessage());
            }
        });
    }
    
    /**
     * Change password
     */
    public boolean changePassword(UUID accountId, String oldPassword, String newPassword) {
        if (!verifyPassword(accountId, oldPassword)) {
            return false;
        }
        
        if (!PasswordUtil.isPasswordValid(newPassword)) {
            return false;
        }
        
        BankAccount account = getAccountById(accountId);
        if (account == null) {
            return false;
        }
        
        String newHash = PasswordUtil.hashPassword(newPassword);
        account.setPasswordHash(newHash);
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE bank_accounts SET password_hash = ? WHERE account_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newHash);
            stmt.setString(2, accountId.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to change password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deposit currency from player wallet to bank account.
     * Thread-safe with transaction atomicity.
     */
    public boolean deposit(Player player, BankAccount account, CurrencyType currency, double amount) {
        Bank bank = bankManager.getBankById(account.getBankId());
        if (bank == null) {
            return false;
        }
        
        // Get account-specific lock for thread safety
        Object lock = accountLocks.computeIfAbsent(account.getAccountId(), k -> new Object());
        
        synchronized (lock) {
            // Check if currency is enabled
            if (!bank.isCurrencyEnabled(currency)) {
                player.sendMessage("§c" + currency.name() + " is not accepted by this bank!");
                return false;
            }
            
            // Check player balance
            double playerBalance = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
            if (playerBalance < amount) {
                player.sendMessage("§cYou don't have enough " + currency.name() + "!");
                return false;
            }
            
            // Calculate tax
            double taxRate = calculateDepositTax(bank, account);
            double taxAmount = amount * (taxRate / 100.0);
            double netAmount = amount - taxAmount;
            
            // Check max storage
            double maxStorage = getMaxStorage(bank, account, currency);
            double currentBalance = account.getBalance(currency);
            if (currentBalance + netAmount > maxStorage) {
                player.sendMessage("§cThis would exceed your maximum storage limit!");
                player.sendMessage("§cMax: " + maxStorage + " | Current: " + currentBalance);
                return false;
            }
            
            // Check wallet balance again (double-check)
            double walletBalance = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
            if (walletBalance < amount) {
                player.sendMessage("§cInsufficient funds in your wallet!");
                return false;
            }
            
            // Execute transaction atomically
            Connection conn = null;
            try {
                conn = plugin.getDatabaseManager().getConnection();
                conn.setAutoCommit(false);
                
                // Deduct from wallet
                try {
                    plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), currency, amount);
                } catch (Exception e) {
                    player.sendMessage("§cFailed to deduct from wallet!");
                    throw e;
                }
                
                // Add to bank account
                double balanceBefore = account.getBalance(currency);
                account.addBalance(currency, netAmount);
                account.incrementTransactions();
                account.updateAccessTime();
                
                // Update in database
                updateAccountSync(conn, account);
                
                // Record transaction
                recordTransactionSync(conn, account, BankTransactionType.DEPOSIT, currency, netAmount, balanceBefore, account.getBalance(currency));
                if (taxAmount > 0) {
                    recordTransactionSync(conn, account, BankTransactionType.TAX_DEDUCTED, currency, taxAmount, 0, 0);
                }
                
                // Commit transaction
                conn.commit();
                
                // Notify player
                player.sendMessage("§a✓ Deposited " + String.format("%.2f", amount) + " " + currency.name());
                if (taxAmount > 0) {
                    player.sendMessage("§7Tax: " + String.format("%.2f", taxAmount) + " (" + String.format("%.1f", taxRate) + "%)");
                }
                player.sendMessage("§7New balance: " + String.format("%.2f", account.getBalance(currency)) + " " + currency.name());
                
                return true;
                
            } catch (Exception e) {
                // Rollback on error
                if (conn != null) {
                    try {
                        conn.rollback();
                        // Refund to wallet
                        plugin.getEconomyAPI().addCurrency(player.getUniqueId(), currency, amount);
                    } catch (SQLException rollbackEx) {
                        plugin.getLogger().severe("CRITICAL: Failed to rollback deposit transaction!");
                        rollbackEx.printStackTrace();
                    }
                }
                player.sendMessage("§cDeposit failed! Transaction rolled back.");
                plugin.getLogger().severe("Deposit transaction failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Failed to close connection: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Withdraw currency from bank account to player wallet.
     * Thread-safe with transaction atomicity.
     */
    public boolean withdraw(Player player, BankAccount account, CurrencyType currency, double amount) {
        Bank bank = bankManager.getBankById(account.getBankId());
        if (bank == null) {
            return false;
        }
        
        // Get account-specific lock for thread safety
        Object lock = accountLocks.computeIfAbsent(account.getAccountId(), k -> new Object());
        
        synchronized (lock) {
            // Check if currency is enabled
            if (!bank.isCurrencyEnabled(currency)) {
                player.sendMessage("§c" + currency.name() + " is not accepted by this bank!");
                return false;
            }
            
            // Calculate tax
            double taxRate = calculateWithdrawalTax(bank, account);
            double totalNeeded = amount * (1 + taxRate / 100.0);
            
            // Check bank balance
            if (!account.hasBalance(currency, totalNeeded)) {
                player.sendMessage("§cInsufficient bank balance!");
                player.sendMessage("§cRequired: " + String.format("%.2f", totalNeeded) + " (including tax)");
                player.sendMessage("§cBalance: " + String.format("%.2f", account.getBalance(currency)));
                return false;
            }
            
            Connection conn = null;
            try {
                conn = plugin.getDatabaseManager().getConnection();
                conn.setAutoCommit(false);
                
                // Deduct from bank account
                double balanceBefore = account.getBalance(currency);
                account.deductBalance(currency, totalNeeded);
                account.incrementTransactions();
                account.updateAccessTime();
                
                // Update in database
                updateAccountSync(conn, account);
                
                // Add to wallet
                try {
                    plugin.getEconomyAPI().addCurrency(player.getUniqueId(), currency, amount);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to add currency to wallet", e);
                }
                
                // Record transaction
                double taxAmount = totalNeeded - amount;
                recordTransactionSync(conn, account, BankTransactionType.WITHDRAWAL, currency, amount, balanceBefore, account.getBalance(currency));
                if (taxAmount > 0) {
                    recordTransactionSync(conn, account, BankTransactionType.TAX_DEDUCTED, currency, taxAmount, 0, 0);
                }
                
                // Commit transaction
                conn.commit();
                
                // Notify player
                player.sendMessage("§a✓ Withdrew " + String.format("%.2f", amount) + " " + currency.name());
                if (taxAmount > 0) {
                    player.sendMessage("§7Tax: " + String.format("%.2f", taxAmount) + " (" + String.format("%.1f", taxRate) + "%)");
                }
                player.sendMessage("§7New balance: " + String.format("%.2f", account.getBalance(currency)) + " " + currency.name());
                
                return true;
                
            } catch (Exception e) {
                // Rollback on error
                if (conn != null) {
                    try {
                        conn.rollback();
                        // Restore bank balance
                        account.addBalance(currency, totalNeeded);
                    } catch (SQLException rollbackEx) {
                        plugin.getLogger().severe("CRITICAL: Failed to rollback withdrawal transaction!");
                        rollbackEx.printStackTrace();
                    }
                }
                player.sendMessage("§cWithdrawal failed! Transaction rolled back.");
                plugin.getLogger().severe("Withdrawal transaction failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        plugin.getLogger().severe("Failed to close connection: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Calculate deposit tax rate for account
     */
    private double calculateDepositTax(Bank bank, BankAccount account) {
        double baseTax = bank.getGlobalDepositTax();
        double multiplier = account.getAccountType().getDepositTaxMultiplier();
        double levelReduction = 0;
        
        BankLevelConfig levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            levelReduction = levelConfig.getDepositTaxReduction();
        }
        
        double finalTax = baseTax * multiplier;
        finalTax -= (finalTax * (levelReduction / 100.0));
        return Math.max(0, finalTax);
    }
    
    /**
     * Calculate withdrawal tax rate for account
     */
    private double calculateWithdrawalTax(Bank bank, BankAccount account) {
        double baseTax = bank.getGlobalWithdrawalTax();
        double multiplier = account.getAccountType().getWithdrawalTaxMultiplier();
        double levelReduction = 0;
        
        BankLevelConfig levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            levelReduction = levelConfig.getWithdrawalTaxReduction();
        }
        
        double finalTax = baseTax * multiplier;
        finalTax -= (finalTax * (levelReduction / 100.0));
        return Math.max(0, finalTax);
    }
    
    /**
     * Get max storage for account
     */
    private double getMaxStorage(Bank bank, BankAccount account, CurrencyType currency) {
        double baseMax = bank.getMaxStorage(currency);
        BankLevelConfig levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            baseMax += levelConfig.getMaxBalanceIncrease(currency);
        }
        return baseMax;
    }
    
    /**
     * Record transaction
     */
    private void recordTransaction(BankAccount account, BankTransactionType type, CurrencyType currency, 
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
     * Register account in memory
     */
    private void registerAccount(BankAccount account) {
        accountsById.put(account.getAccountId(), account);
        accountsByPlayer.computeIfAbsent(account.getPlayerUUID(), k -> new ArrayList<>()).add(account);
    }
    
    /**
     * Unregister account from memory
     */
    private void unregisterAccount(BankAccount account) {
        accountsById.remove(account.getAccountId());
        List<BankAccount> playerAccounts = accountsByPlayer.get(account.getPlayerUUID());
        if (playerAccounts != null) {
            playerAccounts.remove(account);
        }
    }
    
    // Getters and utility methods
    public BankAccount getAccountById(UUID accountId) {
        return accountsById.get(accountId);
    }
    
    public List<BankAccount> getPlayerAccounts(UUID playerUUID) {
        return accountsByPlayer.getOrDefault(playerUUID, new ArrayList<>());
    }
    
    public BankAccount getPlayerAccountAtBank(UUID playerUUID, UUID bankId) {
        List<BankAccount> accounts = getPlayerAccounts(playerUUID);
        return accounts.stream()
            .filter(acc -> acc.getBankId().equals(bankId))
            .findFirst()
            .orElse(null);
    }
    
    public boolean hasAccountAtBank(UUID playerUUID, UUID bankId) {
        return getPlayerAccountAtBank(playerUUID, bankId) != null;
    }
    
    public int getPlayerAccountCount(UUID playerUUID) {
        return getPlayerAccounts(playerUUID).size();
    }
    
    /**
     * Synchronous account update for use within transactions.
     * MUST be called within an existing database transaction.
     */
    private void updateAccountSync(Connection conn, BankAccount account) throws SQLException {
        String sql = "UPDATE bank_accounts SET account_type = ?, account_level = ?, " +
                    "money_balance = ?, mobcoin_balance = ?, gem_balance = ?, " +
                    "last_access_time = ?, total_transactions = ?, total_interest_earned = ?, " +
                    "locked = ? WHERE account_id = ?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, account.getAccountType().name());
        stmt.setInt(2, account.getAccountLevel());
        stmt.setDouble(3, account.getBalance(CurrencyType.MONEY));
        stmt.setDouble(4, account.getBalance(CurrencyType.MOBCOIN));
        stmt.setDouble(5, account.getBalance(CurrencyType.GEM));
        stmt.setLong(6, account.getLastAccessTime());
        stmt.setInt(7, account.getTotalTransactions());
        stmt.setLong(8, account.getTotalInterestEarned());
        stmt.setInt(9, account.isLocked() ? 1 : 0);
        stmt.setString(10, account.getAccountId().toString());
        
        stmt.executeUpdate();
    }
    
    /**
     * Synchronous transaction recording for use within transactions.
     * MUST be called within an existing database transaction.
     */
    private void recordTransactionSync(Connection conn, BankAccount account, BankTransactionType type,
                                      CurrencyType currency, double amount, double balanceBefore, double balanceAfter) throws SQLException {
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
    }
}
