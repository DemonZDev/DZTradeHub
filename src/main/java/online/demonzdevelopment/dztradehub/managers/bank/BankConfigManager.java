package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.AccountType;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.data.bank.BankLevelConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class BankConfigManager {
    private final DZTradeHub plugin;
    private FileConfiguration mainConfig;
    private final Map<String, FileConfiguration> bankConfigs;
    
    // Global settings
    private int maxAccountsPerPlayer;
    private int loanPaymentPeriodMinutes;
    private int interestPayoutIntervalMinutes;
    private Map<String, Double> currencyConversionRates;
    private double conversionFeePercent;
    
    public BankConfigManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.bankConfigs = new HashMap<>();
        this.currencyConversionRates = new HashMap<>();
        loadMainConfig();
    }
    
    private void loadMainConfig() {
        File configFile = new File(plugin.getDataFolder(), "banks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("banks.yml", false);
        }
        
        mainConfig = YamlConfiguration.loadConfiguration(configFile);
        
        // Load global settings
        maxAccountsPerPlayer = mainConfig.getInt("global.max-accounts-per-player", 1);
        loanPaymentPeriodMinutes = mainConfig.getInt("global.loan-payment-period-minutes", 600);
        interestPayoutIntervalMinutes = mainConfig.getInt("global.interest-payout-interval-minutes", 60);
        
        // Load currency conversion rates
        ConfigurationSection conversionSection = mainConfig.getConfigurationSection("global.currency-conversion");
        if (conversionSection != null) {
            for (String key : conversionSection.getKeys(false)) {
                if (!key.equals("conversion-fee")) {
                    currencyConversionRates.put(key, conversionSection.getDouble(key));
                }
            }
            conversionFeePercent = conversionSection.getDouble("conversion-fee", 2.0);
        }
        
        plugin.getLogger().info("Loaded banks.yml configuration");
    }
    
    /**
     * Load a bank configuration from file
     */
    public Bank loadBankFromConfig(String bankName) {
        FileConfiguration config = getBankConfig(bankName);
        if (config == null) {
            plugin.getLogger().warning("Could not load config for bank: " + bankName);
            return null;
        }
        
        Bank bank = new Bank(bankName);
        
        // Basic info
        ConfigurationSection info = config.getConfigurationSection("bank-info");
        if (info != null) {
            bank.setDisplayName(info.getString("display-name", bankName));
        }
        
        // Enabled currencies
        List<String> currencyList = config.getStringList("enabled-currencies");
        for (String currencyName : currencyList) {
            try {
                CurrencyType currency = CurrencyType.valueOf(currencyName);
                bank.addEnabledCurrency(currency);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid currency: " + currencyName);
            }
        }
        
        // Features
        ConfigurationSection features = config.getConfigurationSection("features");
        if (features != null) {
            bank.setCurrencyConversionEnabled(
                features.getBoolean("currency-conversion.enabled", false)
            );
            bank.setAccountTransferEnabled(
                features.getBoolean("account-transfer.enabled", true)
            );
            bank.setBankTransferEnabled(
                features.getBoolean("bank-transfer.enabled", true)
            );
        }
        
        // Account types
        List<String> accountTypeList = config.getStringList("account-types");
        for (String typeName : accountTypeList) {
            try {
                AccountType type = AccountType.valueOf(typeName);
                bank.addAccountType(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid account type: " + typeName);
            }
        }
        
        // Loans
        ConfigurationSection loans = config.getConfigurationSection("loans");
        if (loans != null) {
            bank.setLoansEnabled(loans.getBoolean("enabled", false));
            bank.setMinLoanAmount(loans.getDouble("min-amount", 1000));
            bank.setMaxLoanAmount(loans.getDouble("max-amount", 100000));
            bank.setLoanInterestRate(loans.getDouble("interest-rate", 5.0));
        }
        
        // Taxes
        ConfigurationSection taxes = config.getConfigurationSection("taxes");
        if (taxes != null) {
            bank.setGlobalDepositTax(taxes.getDouble("deposit", 0.5));
            bank.setGlobalWithdrawalTax(taxes.getDouble("withdrawal", 1.0));
        }
        
        // Max storage
        ConfigurationSection maxStorage = config.getConfigurationSection("max-storage");
        if (maxStorage != null) {
            for (String currencyName : maxStorage.getKeys(false)) {
                try {
                    CurrencyType currency = CurrencyType.valueOf(currencyName);
                    ConfigurationSection currencySection = maxStorage.getConfigurationSection(currencyName);
                    if (currencySection != null) {
                        // Get max storage for highest level
                        double maxAmount = 0;
                        for (String key : currencySection.getKeys(false)) {
                            maxAmount = Math.max(maxAmount, currencySection.getDouble(key));
                        }
                        bank.setMaxStorage(currency, maxAmount);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid currency in max-storage: " + currencyName);
                }
            }
        }
        
        // Leveling
        ConfigurationSection leveling = config.getConfigurationSection("leveling");
        if (leveling != null) {
            bank.setMaxLevel(leveling.getInt("max-level", 1));
            
            ConfigurationSection levels = leveling.getConfigurationSection("levels");
            if (levels != null) {
                for (String levelStr : levels.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(levelStr);
                        ConfigurationSection levelSection = levels.getConfigurationSection(levelStr);
                        
                        BankLevelConfig levelConfig = new BankLevelConfig(level);
                        levelConfig.setDepositTaxReduction(levelSection.getDouble("deposit-tax-reduction", 0));
                        levelConfig.setWithdrawalTaxReduction(levelSection.getDouble("withdrawal-tax-reduction", 0));
                        levelConfig.setInterestRateBonus(levelSection.getDouble("interest-rate-bonus", 0));
                        
                        // Load upgrade costs
                        ConfigurationSection upgradeCost = levelSection.getConfigurationSection("upgrade-cost");
                        if (upgradeCost != null) {
                            for (String currencyName : upgradeCost.getKeys(false)) {
                                try {
                                    CurrencyType currency = CurrencyType.valueOf(currencyName);
                                    levelConfig.setUpgradeCost(currency, upgradeCost.getDouble(currencyName));
                                } catch (IllegalArgumentException e) {
                                    // Ignore invalid currency
                                }
                            }
                        }
                        
                        bank.setLevelConfig(level, levelConfig);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level number: " + levelStr);
                    }
                }
            }
        }
        
        // Reception
        ConfigurationSection reception = config.getConfigurationSection("reception");
        if (reception != null) {
            bank.setReceptionSlots(reception.getInt("slots", 10));
            bank.setReceptionTimeSeconds(reception.getInt("time-limit-seconds", 300));
        }
        
        // Account creation cost
        ConfigurationSection creationCost = config.getConfigurationSection("account-creation-cost");
        if (creationCost != null) {
            for (String currencyName : creationCost.getKeys(false)) {
                try {
                    CurrencyType currency = CurrencyType.valueOf(currencyName);
                    bank.setCreationCost(currency, creationCost.getDouble(currencyName));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid currency in creation cost: " + currencyName);
                }
            }
        }
        
        return bank;
    }
    
    /**
     * Get bank configuration file
     */
    private FileConfiguration getBankConfig(String bankName) {
        if (bankConfigs.containsKey(bankName)) {
            return bankConfigs.get(bankName);
        }
        
        File banksFolder = new File(plugin.getDataFolder(), "banks");
        if (!banksFolder.exists()) {
            banksFolder.mkdirs();
        }
        
        File configFile = new File(banksFolder, bankName + ".yml");
        
        // Try to extract from resources if doesn't exist
        if (!configFile.exists()) {
            try {
                plugin.saveResource("banks/" + bankName + ".yml", false);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not find config file for bank: " + bankName);
                return null;
            }
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        bankConfigs.put(bankName, config);
        return config;
    }
    
    /**
     * Save bank configuration to file
     */
    public void saveBankToConfig(Bank bank) {
        File banksFolder = new File(plugin.getDataFolder(), "banks");
        File configFile = new File(banksFolder, bank.getBankName() + ".yml");
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        // Update toggleable settings
        config.set("features.currency-conversion.enabled", bank.isCurrencyConversionEnabled());
        config.set("features.account-transfer.enabled", bank.isAccountTransferEnabled());
        config.set("features.bank-transfer.enabled", bank.isBankTransferEnabled());
        config.set("loans.enabled", bank.isLoansEnabled());
        
        try {
            config.save(configFile);
            plugin.getLogger().info("Saved configuration for bank: " + bank.getBankName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save bank config", e);
        }
    }
    
    /**
     * Get list of default banks to create
     */
    public List<String> getDefaultBanks() {
        return mainConfig.getStringList("default-banks.banks");
    }
    
    public boolean shouldAutoCreateDefaultBanks() {
        return mainConfig.getBoolean("default-banks.auto-create-on-first-run", true);
    }
    
    // Getters
    public int getMaxAccountsPerPlayer() { return maxAccountsPerPlayer; }
    public int getLoanPaymentPeriodMinutes() { return loanPaymentPeriodMinutes; }
    public int getInterestPayoutIntervalMinutes() { return interestPayoutIntervalMinutes; }
    public Map<String, Double> getCurrencyConversionRates() { return currencyConversionRates; }
    public double getConversionFeePercent() { return conversionFeePercent; }
}