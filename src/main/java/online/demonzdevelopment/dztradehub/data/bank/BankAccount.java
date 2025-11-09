package online.demonzdevelopment.dztradehub.data.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.*;

public class BankAccount {
    private UUID accountId;
    private UUID bankId;
    private UUID playerUUID;
    private String playerName;
    private AccountType accountType;
    private String passwordHash;
    private long createdTime;
    private long lastAccessTime;
    private int accountLevel;
    
    // Balances per currency (stored in bank, separate from wallet)
    private Map<CurrencyType, Double> balances;
    
    // Account status
    private boolean active;
    private boolean locked;
    
    // Statistics
    private int totalTransactions;
    private long totalInterestEarned;
    
    public BankAccount(UUID bankId, UUID playerUUID, String playerName, AccountType accountType, String passwordHash) {
        this.accountId = UUID.randomUUID();
        this.bankId = bankId;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.accountType = accountType;
        this.passwordHash = passwordHash;
        this.createdTime = System.currentTimeMillis();
        this.lastAccessTime = System.currentTimeMillis();
        this.accountLevel = 1;
        this.balances = new HashMap<>();
        this.active = true;
        this.locked = false;
        this.totalTransactions = 0;
        this.totalInterestEarned = 0;
    }
    
    public BankAccount(UUID accountId, UUID bankId, UUID playerUUID, String playerName) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.balances = new HashMap<>();
        this.active = true;
        this.locked = false;
    }
    
    // Balance operations
    public double getBalance(CurrencyType currency) {
        return balances.getOrDefault(currency, 0.0);
    }
    
    public void setBalance(CurrencyType currency, double amount) {
        balances.put(currency, Math.max(0, amount));
    }
    
    public void addBalance(CurrencyType currency, double amount) {
        double current = getBalance(currency);
        setBalance(currency, current + amount);
    }
    
    public boolean hasBalance(CurrencyType currency, double amount) {
        return getBalance(currency) >= amount;
    }
    
    public boolean deductBalance(CurrencyType currency, double amount) {
        if (!hasBalance(currency, amount)) {
            return false;
        }
        setBalance(currency, getBalance(currency) - amount);
        return true;
    }
    
    public double getTotalBalance(CurrencyType currency) {
        return getBalance(currency);
    }
    
    // Getters and Setters
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    
    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public long getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    public void updateAccessTime() { this.lastAccessTime = System.currentTimeMillis(); }
    
    public int getAccountLevel() { return accountLevel; }
    public void setAccountLevel(int accountLevel) { this.accountLevel = accountLevel; }
    
    public Map<CurrencyType, Double> getBalances() { return balances; }
    public void setBalances(Map<CurrencyType, Double> balances) { this.balances = balances; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    
    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
    public void incrementTransactions() { this.totalTransactions++; }
    
    public long getTotalInterestEarned() { return totalInterestEarned; }
    public void setTotalInterestEarned(long totalInterestEarned) { this.totalInterestEarned = totalInterestEarned; }
    public void addInterestEarned(long amount) { this.totalInterestEarned += amount; }
}