package online.demonzdevelopment.dztradehub.data.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.UUID;

public class BankTransaction {
    private UUID transactionId;
    private UUID accountId;
    private UUID bankId;
    private UUID playerUUID;
    private BankTransactionType transactionType;
    private CurrencyType currency;
    private double amount;
    private double balanceBefore;
    private double balanceAfter;
    private long timestamp;
    private String notes;
    
    // For transfers
    private UUID targetAccountId;
    private UUID targetPlayerUUID;
    private UUID targetBankId;
    
    public BankTransaction(UUID accountId, UUID bankId, UUID playerUUID, 
                          BankTransactionType transactionType, CurrencyType currency, 
                          double amount, double balanceBefore, double balanceAfter) {
        this.transactionId = UUID.randomUUID();
        this.accountId = accountId;
        this.bankId = bankId;
        this.playerUUID = playerUUID;
        this.transactionType = transactionType;
        this.currency = currency;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BankTransaction(UUID transactionId) {
        this.transactionId = transactionId;
    }
    
    // Getters and Setters
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    
    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }
    
    public BankTransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(BankTransactionType transactionType) { this.transactionType = transactionType; }
    
    public CurrencyType getCurrency() { return currency; }
    public void setCurrency(CurrencyType currency) { this.currency = currency; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public double getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(double balanceBefore) { this.balanceBefore = balanceBefore; }
    
    public double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public UUID getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(UUID targetAccountId) { this.targetAccountId = targetAccountId; }
    
    public UUID getTargetPlayerUUID() { return targetPlayerUUID; }
    public void setTargetPlayerUUID(UUID targetPlayerUUID) { this.targetPlayerUUID = targetPlayerUUID; }
    
    public UUID getTargetBankId() { return targetBankId; }
    public void setTargetBankId(UUID targetBankId) { this.targetBankId = targetBankId; }
}