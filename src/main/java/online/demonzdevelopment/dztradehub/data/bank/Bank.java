package online.demonzdevelopment.dztradehub.data.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.*;

public class Bank {
    private UUID bankId;
    private String bankName;
    private String displayName;
    private long createdTime;
    
    // Enabled currencies
    private Set<CurrencyType> enabledCurrencies;
    
    // Features
    private boolean currencyConversionEnabled;
    private boolean accountTransferEnabled;
    private boolean bankTransferEnabled;
    
    // Account types
    private Set<AccountType> availableAccountTypes;
    
    // Loan settings
    private boolean loansEnabled;
    private double minLoanAmount;
    private double maxLoanAmount;
    private double loanInterestRate; // percentage per payment period
    
    // Global taxes (percentage)
    private double globalDepositTax;
    private double globalWithdrawalTax;
    
    // Global max storage per currency
    private Map<CurrencyType, Double> maxStoragePerCurrency;
    
    // Leveling
    private int maxLevel;
    private Map<Integer, BankLevelConfig> levelConfigs;
    
    // Queue settings
    private int receptionSlots;
    private int receptionTimeSeconds;
    
    // Account creation cost
    private Map<CurrencyType, Double> accountCreationCost;
    
    public Bank(String bankName) {
        this.bankId = UUID.randomUUID();
        this.bankName = bankName;
        this.displayName = bankName;
        this.createdTime = System.currentTimeMillis();
        this.enabledCurrencies = new HashSet<>();
        this.availableAccountTypes = new HashSet<>();
        this.maxStoragePerCurrency = new HashMap<>();
        this.levelConfigs = new HashMap<>();
        this.accountCreationCost = new HashMap<>();
    }
    
    public Bank(UUID bankId, String bankName) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.displayName = bankName;
        this.createdTime = System.currentTimeMillis();
        this.enabledCurrencies = new HashSet<>();
        this.availableAccountTypes = new HashSet<>();
        this.maxStoragePerCurrency = new HashMap<>();
        this.levelConfigs = new HashMap<>();
        this.accountCreationCost = new HashMap<>();
    }

    // Getters and Setters
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public Set<CurrencyType> getEnabledCurrencies() { return enabledCurrencies; }
    public void setEnabledCurrencies(Set<CurrencyType> enabledCurrencies) { this.enabledCurrencies = enabledCurrencies; }
    public void addEnabledCurrency(CurrencyType currency) { this.enabledCurrencies.add(currency); }
    public void removeEnabledCurrency(CurrencyType currency) { this.enabledCurrencies.remove(currency); }
    public boolean isCurrencyEnabled(CurrencyType currency) { return enabledCurrencies.contains(currency); }
    
    public boolean isCurrencyConversionEnabled() { return currencyConversionEnabled && enabledCurrencies.size() >= 2; }
    public void setCurrencyConversionEnabled(boolean currencyConversionEnabled) { this.currencyConversionEnabled = currencyConversionEnabled; }
    
    public boolean isAccountTransferEnabled() { return accountTransferEnabled; }
    public void setAccountTransferEnabled(boolean accountTransferEnabled) { this.accountTransferEnabled = accountTransferEnabled; }
    
    public boolean isBankTransferEnabled() { return bankTransferEnabled; }
    public void setBankTransferEnabled(boolean bankTransferEnabled) { this.bankTransferEnabled = bankTransferEnabled; }
    
    public Set<AccountType> getAvailableAccountTypes() { return availableAccountTypes; }
    public void setAvailableAccountTypes(Set<AccountType> availableAccountTypes) { this.availableAccountTypes = availableAccountTypes; }
    public void addAccountType(AccountType type) { this.availableAccountTypes.add(type); }
    public void removeAccountType(AccountType type) { this.availableAccountTypes.remove(type); }
    public boolean isAccountTypeAvailable(AccountType type) { return availableAccountTypes.contains(type); }
    
    public boolean isLoansEnabled() { return loansEnabled; }
    public void setLoansEnabled(boolean loansEnabled) { this.loansEnabled = loansEnabled; }
    
    public double getMinLoanAmount() { return minLoanAmount; }
    public void setMinLoanAmount(double minLoanAmount) { this.minLoanAmount = minLoanAmount; }
    
    public double getMaxLoanAmount() { return maxLoanAmount; }
    public void setMaxLoanAmount(double maxLoanAmount) { this.maxLoanAmount = maxLoanAmount; }
    
    public double getLoanInterestRate() { return loanInterestRate; }
    public void setLoanInterestRate(double loanInterestRate) { this.loanInterestRate = loanInterestRate; }
    
    public double getGlobalDepositTax() { return globalDepositTax; }
    public void setGlobalDepositTax(double globalDepositTax) { this.globalDepositTax = globalDepositTax; }
    
    public double getGlobalWithdrawalTax() { return globalWithdrawalTax; }
    public void setGlobalWithdrawalTax(double globalWithdrawalTax) { this.globalWithdrawalTax = globalWithdrawalTax; }
    
    public Map<CurrencyType, Double> getMaxStoragePerCurrency() { return maxStoragePerCurrency; }
    public void setMaxStoragePerCurrency(Map<CurrencyType, Double> maxStoragePerCurrency) { this.maxStoragePerCurrency = maxStoragePerCurrency; }
    public double getMaxStorage(CurrencyType currency) { return maxStoragePerCurrency.getOrDefault(currency, 0.0); }
    public void setMaxStorage(CurrencyType currency, double amount) { this.maxStoragePerCurrency.put(currency, amount); }
    
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    
    public Map<Integer, BankLevelConfig> getLevelConfigs() { return levelConfigs; }
    public void setLevelConfigs(Map<Integer, BankLevelConfig> levelConfigs) { this.levelConfigs = levelConfigs; }
    public BankLevelConfig getLevelConfig(int level) { return levelConfigs.get(level); }
    public void setLevelConfig(int level, BankLevelConfig config) { this.levelConfigs.put(level, config); }
    
    public int getReceptionSlots() { return receptionSlots; }
    public void setReceptionSlots(int receptionSlots) { this.receptionSlots = receptionSlots; }
    
    public int getReceptionTimeSeconds() { return receptionTimeSeconds; }
    public void setReceptionTimeSeconds(int receptionTimeSeconds) { this.receptionTimeSeconds = receptionTimeSeconds; }
    
    public Map<CurrencyType, Double> getAccountCreationCost() { return accountCreationCost; }
    public void setAccountCreationCost(Map<CurrencyType, Double> accountCreationCost) { this.accountCreationCost = accountCreationCost; }
    public double getCreationCost(CurrencyType currency) { return accountCreationCost.getOrDefault(currency, 0.0); }
    public void setCreationCost(CurrencyType currency, double cost) { this.accountCreationCost.put(currency, cost); }
}