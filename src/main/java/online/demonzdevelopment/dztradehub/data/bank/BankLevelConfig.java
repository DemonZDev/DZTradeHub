package online.demonzdevelopment.dztradehub.data.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.HashMap;
import java.util.Map;

public class BankLevelConfig {
    private int level;
    private double depositTaxReduction; // percentage reduction
    private double withdrawalTaxReduction; // percentage reduction
    private double interestRateBonus; // percentage bonus
    private Map<CurrencyType, Double> maxBalanceIncrease; // additional balance limit
    private Map<CurrencyType, Double> upgradeCost; // cost to upgrade to this level
    
    public BankLevelConfig(int level) {
        this.level = level;
        this.maxBalanceIncrease = new HashMap<>();
        this.upgradeCost = new HashMap<>();
    }
    
    // Getters and Setters
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public double getDepositTaxReduction() { return depositTaxReduction; }
    public void setDepositTaxReduction(double depositTaxReduction) { this.depositTaxReduction = depositTaxReduction; }
    
    public double getWithdrawalTaxReduction() { return withdrawalTaxReduction; }
    public void setWithdrawalTaxReduction(double withdrawalTaxReduction) { this.withdrawalTaxReduction = withdrawalTaxReduction; }
    
    public double getInterestRateBonus() { return interestRateBonus; }
    public void setInterestRateBonus(double interestRateBonus) { this.interestRateBonus = interestRateBonus; }
    
    public Map<CurrencyType, Double> getMaxBalanceIncrease() { return maxBalanceIncrease; }
    public void setMaxBalanceIncrease(Map<CurrencyType, Double> maxBalanceIncrease) { this.maxBalanceIncrease = maxBalanceIncrease; }
    public double getMaxBalanceIncrease(CurrencyType currency) { return maxBalanceIncrease.getOrDefault(currency, 0.0); }
    public void setMaxBalanceIncrease(CurrencyType currency, double increase) { this.maxBalanceIncrease.put(currency, increase); }
    
    public Map<CurrencyType, Double> getUpgradeCost() { return upgradeCost; }
    public void setUpgradeCost(Map<CurrencyType, Double> upgradeCost) { this.upgradeCost = upgradeCost; }
    public double getUpgradeCost(CurrencyType currency) { return upgradeCost.getOrDefault(currency, 0.0); }
    public void setUpgradeCost(CurrencyType currency, double cost) { this.upgradeCost.put(currency, cost); }
}