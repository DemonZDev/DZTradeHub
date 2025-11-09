package online.demonzdevelopment.dztradehub.data;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopItem {
    private final UUID id;
    private ItemStack itemStack;
    private Currency currency;
    private double buyPrice;
    private double sellPrice;
    private double minPrice;
    private double maxPrice;
    private int currentStock;
    private int maxStock;
    private String refillInterval; // HOURLY, DAILY, WEEKLY
    private int refillAmount;
    private boolean dynamicPricingEnabled;
    private TransactionType transactionType; // BUY_ONLY, SELL_ONLY, BOTH

    public enum TransactionType {
        BUY_ONLY, SELL_ONLY, BOTH
    }
    
    public enum Currency {
        MONEY, MOBCOIN, GEM
    }

    public ShopItem(ItemStack itemStack, double buyPrice, double sellPrice) {
        this.id = UUID.randomUUID();
        this.itemStack = itemStack.clone();
        this.currency = Currency.MONEY;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.minPrice = buyPrice * 0.5;
        this.maxPrice = buyPrice * 2.0;
        this.currentStock = 0;
        this.maxStock = 640;
        this.refillInterval = "DAILY";
        this.refillAmount = 64;
        this.dynamicPricingEnabled = false;
        this.transactionType = TransactionType.BOTH;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public ItemStack getItemStack() { return itemStack.clone(); }
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack.clone(); }
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }
    public double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public int getMaxStock() { return maxStock; }
    public void setMaxStock(int maxStock) { this.maxStock = maxStock; }
    public String getRefillInterval() { return refillInterval; }
    public void setRefillInterval(String refillInterval) { this.refillInterval = refillInterval; }
    public int getRefillAmount() { return refillAmount; }
    public void setRefillAmount(int refillAmount) { this.refillAmount = refillAmount; }
    public boolean isDynamicPricingEnabled() { return dynamicPricingEnabled; }
    public void setDynamicPricingEnabled(boolean enabled) { this.dynamicPricingEnabled = enabled; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public void addStock(int amount) {
        this.currentStock = Math.min(this.currentStock + amount, this.maxStock);
    }

    public boolean removeStock(int amount) {
        if (this.currentStock >= amount) {
            this.currentStock -= amount;
            return true;
        }
        return false;
    }

    public boolean canBuy() {
        return buyPrice >= 0 && (transactionType == TransactionType.BUY_ONLY || transactionType == TransactionType.BOTH);
    }

    public boolean canSell() {
        return sellPrice >= 0 && (transactionType == TransactionType.SELL_ONLY || transactionType == TransactionType.BOTH);
    }
}