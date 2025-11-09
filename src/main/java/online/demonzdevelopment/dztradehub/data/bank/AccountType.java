package online.demonzdevelopment.dztradehub.data.bank;

public enum AccountType {
    SAVINGS("Savings Account", "Store your money safely", 1.2, 1.3),
    INTEREST("Interest Account", "Earn interest on deposits", 0.5, 0.8),
    BUSINESS("Business Account", "Higher limits for trading", 1.5, 1.8);

    private final String displayName;
    private final String description;
    private final double depositTaxMultiplier;
    private final double withdrawalTaxMultiplier;

    AccountType(String displayName, String description, double depositTaxMultiplier, double withdrawalTaxMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.depositTaxMultiplier = depositTaxMultiplier;
        this.withdrawalTaxMultiplier = withdrawalTaxMultiplier;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public double getDepositTaxMultiplier() { return depositTaxMultiplier; }
    public double getWithdrawalTaxMultiplier() { return withdrawalTaxMultiplier; }
}