package online.demonzdevelopment.dztradehub.data.bank;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

import java.util.UUID;

public class BankLoan {
    private UUID loanId;
    private UUID accountId;
    private UUID bankId;
    private UUID playerUUID;
    private CurrencyType currency;
    private double principalAmount;
    private double remainingAmount;
    private double interestRate;
    private long issuedTime;
    private long dueTime; // in milliseconds (real time)
    private int paymentPeriodMinutes; // payment period in minutes
    private int missedPayments;
    private double currentInterestRate; // increases with missed payments
    private boolean active;
    private boolean defaulted;
    
    public BankLoan(UUID accountId, UUID bankId, UUID playerUUID, CurrencyType currency, 
                    double amount, double interestRate, int paymentPeriodMinutes) {
        this.loanId = UUID.randomUUID();
        this.accountId = accountId;
        this.bankId = bankId;
        this.playerUUID = playerUUID;
        this.currency = currency;
        this.principalAmount = amount;
        this.remainingAmount = amount;
        this.interestRate = interestRate;
        this.currentInterestRate = interestRate;
        this.issuedTime = System.currentTimeMillis();
        this.paymentPeriodMinutes = paymentPeriodMinutes;
        this.dueTime = issuedTime + (paymentPeriodMinutes * 60 * 1000L);
        this.missedPayments = 0;
        this.active = true;
        this.defaulted = false;
    }
    
    public BankLoan(UUID loanId, UUID accountId, UUID bankId, UUID playerUUID) {
        this.loanId = loanId;
        this.accountId = accountId;
        this.bankId = bankId;
        this.playerUUID = playerUUID;
    }
    
    /**
     * Calculate the required payment amount including interest
     */
    public double calculatePaymentAmount() {
        return remainingAmount * (1 + currentInterestRate / 100.0);
    }
    
    /**
     * Process a payment
     */
    public boolean makePayment(double amount) {
        double required = calculatePaymentAmount();
        if (amount >= required) {
            remainingAmount = 0;
            active = false;
            return true;
        } else {
            remainingAmount -= amount;
            if (remainingAmount < 0) remainingAmount = 0;
            return remainingAmount == 0;
        }
    }
    
    /**
     * Check if payment is overdue
     */
    public boolean isOverdue() {
        return System.currentTimeMillis() > dueTime && active;
    }
    
    /**
     * Handle missed payment - increase interest and extend due date
     */
    public void handleMissedPayment() {
        missedPayments++;
        currentInterestRate = interestRate * (1 + (missedPayments * 0.1)); // 10% increase per missed payment
        dueTime = System.currentTimeMillis() + (paymentPeriodMinutes * 60 * 1000L);
    }
    
    /**
     * Get time remaining until due (in milliseconds)
     */
    public long getTimeRemaining() {
        return Math.max(0, dueTime - System.currentTimeMillis());
    }
    
    // Getters and Setters
    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    
    public UUID getBankId() { return bankId; }
    public void setBankId(UUID bankId) { this.bankId = bankId; }
    
    public UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }
    
    public CurrencyType getCurrency() { return currency; }
    public void setCurrency(CurrencyType currency) { this.currency = currency; }
    
    public double getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(double principalAmount) { this.principalAmount = principalAmount; }
    
    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }
    
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    
    public long getIssuedTime() { return issuedTime; }
    public void setIssuedTime(long issuedTime) { this.issuedTime = issuedTime; }
    
    public long getDueTime() { return dueTime; }
    public void setDueTime(long dueTime) { this.dueTime = dueTime; }
    
    public int getPaymentPeriodMinutes() { return paymentPeriodMinutes; }
    public void setPaymentPeriodMinutes(int paymentPeriodMinutes) { this.paymentPeriodMinutes = paymentPeriodMinutes; }
    
    public int getMissedPayments() { return missedPayments; }
    public void setMissedPayments(int missedPayments) { this.missedPayments = missedPayments; }
    
    public double getCurrentInterestRate() { return currentInterestRate; }
    public void setCurrentInterestRate(double currentInterestRate) { this.currentInterestRate = currentInterestRate; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean isDefaulted() { return defaulted; }
    public void setDefaulted(boolean defaulted) { this.defaulted = defaulted; }
}