package online.demonzdevelopment.dztradehub.data;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Auction {
    private final UUID id;
    private final UUID sellerUUID;
    private ItemStack itemStack;
    private String currencyType; // MONEY, MOBCOIN, GEM
    private double actualPrice;
    private double maxDropPrice;
    private double dropPerUnit;
    private int dropIntervalHours;
    private int maxQueue;
    private double priceIncreasePercent;
    private long createdTime;
    private long lastDropTime;
    private boolean frozen;
    private final Queue<QueueEntry> queue;
    private int itemNumber; // For /ah list and /ah remove <number>

    public record QueueEntry(UUID playerUUID, double paidPrice, long joinTime) {}

    public Auction(UUID sellerUUID, ItemStack itemStack, String currencyType, double actualPrice, double maxDropPrice,
                   double dropPerUnit, int dropIntervalHours, int maxQueue, double priceIncreasePercent) {
        this.id = UUID.randomUUID();
        this.sellerUUID = sellerUUID;
        this.itemStack = itemStack.clone();
        this.currencyType = currencyType;
        this.actualPrice = actualPrice;
        this.maxDropPrice = maxDropPrice;
        this.dropPerUnit = dropPerUnit;
        this.dropIntervalHours = dropIntervalHours;
        this.maxQueue = maxQueue;
        this.priceIncreasePercent = priceIncreasePercent;
        this.createdTime = System.currentTimeMillis();
        this.lastDropTime = System.currentTimeMillis();
        this.frozen = false;
        this.queue = new LinkedList<>();
        this.itemNumber = 0;
    }

    // Constructor with item number
    public Auction(UUID sellerUUID, ItemStack itemStack, String currencyType, double actualPrice, double maxDropPrice,
                   double dropPerUnit, int dropIntervalHours, int maxQueue, double priceIncreasePercent, int itemNumber) {
        this(sellerUUID, itemStack, currencyType, actualPrice, maxDropPrice, dropPerUnit, 
             dropIntervalHours, maxQueue, priceIncreasePercent);
        this.itemNumber = itemNumber;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSellerUUID() { return sellerUUID; }
    public ItemStack getItemStack() { return itemStack.clone(); }
    public String getCurrencyType() { return currencyType; }
    public double getActualPrice() { return actualPrice; }
    public double getMaxDropPrice() { return maxDropPrice; }
    public double getDropPerUnit() { return dropPerUnit; }
    public int getDropIntervalHours() { return dropIntervalHours; }
    public int getMaxQueue() { return maxQueue; }
    public double getPriceIncreasePercent() { return priceIncreasePercent; }
    public long getCreatedTime() { return createdTime; }
    public long getLastDropTime() { return lastDropTime; }
    public boolean isFrozen() { return frozen; }
    public Queue<QueueEntry> getQueue() { return new LinkedList<>(queue); }
    public int getItemNumber() { return itemNumber; }

    // Setters
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    public void setActualPrice(double actualPrice) { this.actualPrice = actualPrice; }
    public void setMaxDropPrice(double maxDropPrice) { this.maxDropPrice = maxDropPrice; }
    public void setDropPerUnit(double dropPerUnit) { this.dropPerUnit = dropPerUnit; }
    public void setDropIntervalHours(int hours) { this.dropIntervalHours = hours; }
    public void setLastDropTime(long time) { this.lastDropTime = time; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public void setItemNumber(int itemNumber) { this.itemNumber = itemNumber; }

    // Queue methods
    public void addToQueue(UUID playerUUID, double paidPrice) {
        queue.add(new QueueEntry(playerUUID, paidPrice, System.currentTimeMillis()));
    }

    public QueueEntry removeFromQueue() {
        return queue.poll();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean isQueueFull() {
        return maxQueue > 0 && queue.size() >= maxQueue;
    }

    // Calculate current price based on time elapsed
    public double getCurrentPrice() {
        if (frozen || dropPerUnit <= 0) {
            return actualPrice;
        }

        long elapsed = System.currentTimeMillis() - lastDropTime;
        long hoursElapsed = elapsed / (1000 * 60 * 60);
        
        if (hoursElapsed >= dropIntervalHours && dropIntervalHours > 0) {
            int drops = (int) (hoursElapsed / dropIntervalHours);
            double newPrice = actualPrice - (dropPerUnit * drops);
            return Math.max(newPrice, maxDropPrice);
        }

        return actualPrice;
    }

    // Update price after drop
    public void updatePriceAfterDrop() {
        if (!frozen && dropPerUnit > 0) {
            double newPrice = getCurrentPrice();
            this.actualPrice = Math.max(newPrice, maxDropPrice);
            this.lastDropTime = System.currentTimeMillis();
        }
    }

    // Increase price after queue fill
    public void increasePriceAfterQueueFill() {
        if (priceIncreasePercent > 0) {
            this.actualPrice *= (1 + priceIncreasePercent / 100.0);
        }
    }
}