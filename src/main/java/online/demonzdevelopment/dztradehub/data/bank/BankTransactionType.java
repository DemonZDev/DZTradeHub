package online.demonzdevelopment.dztradehub.data.bank;

public enum BankTransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_SENT("Transfer Sent"),
    TRANSFER_RECEIVED("Transfer Received"),
    BANK_TRANSFER_SENT("Bank Transfer Sent"),
    BANK_TRANSFER_RECEIVED("Bank Transfer Received"),
    CURRENCY_CONVERSION("Currency Conversion"),
    LOAN_ISSUED("Loan Issued"),
    LOAN_PAYMENT("Loan Payment"),
    INTEREST_EARNED("Interest Earned"),
    TAX_DEDUCTED("Tax Deducted"),
    ACCOUNT_CREATION("Account Creation"),
    ACCOUNT_UPGRADE("Account Upgrade"),
    LEVEL_UPGRADE("Level Upgrade");

    private final String displayName;

    BankTransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}