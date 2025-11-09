package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamicPricingManager {
    private final DZTradeHub plugin;
    
    // Track purchase/sell counts for demand calculation
    private final Map<UUID, Integer> purchaseCount; // itemId -> count
    private final Map<UUID, Integer> sellCount; // itemId -> count
    private final Map<UUID, Long> lastPriceUpdate; // itemId -> timestamp

    public DynamicPricingManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.purchaseCount = new HashMap<>();
        this.sellCount = new HashMap<>();
        this.lastPriceUpdate = new HashMap<>();
    }

    /**
     * Record a purchase transaction
     */
    public void recordPurchase(ShopItem item, int quantity) {
        UUID itemId = item.getId();
        purchaseCount.put(itemId, purchaseCount.getOrDefault(itemId, 0) + quantity);
    }

    /**
     * Record a sell transaction
     */
    public void recordSell(ShopItem item, int quantity) {
        UUID itemId = item.getId();
        sellCount.put(itemId, sellCount.getOrDefault(itemId, 0) + quantity);
    }

    /**
     * Update prices for all items in all shops
     */
    public void updateAllPrices() {
        long currentTime = System.currentTimeMillis();
        
        for (Area area : plugin.getShopManager().getAllAreas()) {
            for (Shop shop : plugin.getShopManager().getShopsInArea(area.getName())) {
                for (ShopItem item : shop.getItems()) {
                    if (item.isDynamicPricingEnabled()) {
                        updateItemPrice(area.getName(), shop, item, currentTime);
                    }
                }
            }
        }
    }

    /**
     * Update price for a specific item based on demand
     */
    private void updateItemPrice(String areaName, Shop shop, ShopItem item, long currentTime) {
        UUID itemId = item.getId();
        
        // Check if enough time has passed since last update
        Long lastUpdate = lastPriceUpdate.get(itemId);
        if (lastUpdate != null && (currentTime - lastUpdate) < 1800000L) { // 30 minutes
            return;
        }
        
        int purchases = purchaseCount.getOrDefault(itemId, 0);
        int sells = sellCount.getOrDefault(itemId, 0);
        
        // Calculate demand ratio
        int totalTransactions = purchases + sells;
        if (totalTransactions == 0) {
            return; // No activity, no price change
        }
        
        double demandRatio = (double) purchases / totalTransactions;
        
        // Adjust prices based on demand
        double currentBuyPrice = item.getBuyPrice();
        double currentSellPrice = item.getSellPrice();
        
        // High demand (more purchases) -> increase prices
        // Low demand (more sells) -> decrease prices
        double priceChange = 0.0;
        
        if (demandRatio > 0.6) {
            // High demand: increase price by 5-10%
            priceChange = 0.05 + (demandRatio - 0.6) * 0.125; // 5-10% increase
        } else if (demandRatio < 0.4) {
            // Low demand: decrease price by 5-10%
            priceChange = -(0.05 + (0.4 - demandRatio) * 0.125); // 5-10% decrease
        }
        
        // Apply price change
        if (Math.abs(priceChange) > 0.01) {
            double newBuyPrice = currentBuyPrice * (1 + priceChange);
            double newSellPrice = currentSellPrice * (1 + priceChange);
            
            // Ensure prices stay within min/max bounds
            newBuyPrice = Math.max(item.getMinPrice(), Math.min(item.getMaxPrice(), newBuyPrice));
            newSellPrice = Math.max(item.getMinPrice() * 0.5, Math.min(item.getMaxPrice() * 0.8, newSellPrice));
            
            item.setBuyPrice(newBuyPrice);
            item.setSellPrice(newSellPrice);
            
            plugin.getLogger().fine("Updated price for " + item.getItemStack().getType().name() + 
                " in shop " + shop.getName() + ": Buy=" + String.format("%.2f", newBuyPrice) + 
                ", Sell=" + String.format("%.2f", newSellPrice) + 
                " (demand: " + String.format("%.2f", demandRatio) + ")");
            
            // Save updated prices
            plugin.getFileStorageManager().saveShopItems(areaName, shop);
        }
        
        // Reset counters and update timestamp
        purchaseCount.put(itemId, 0);
        sellCount.put(itemId, 0);
        lastPriceUpdate.put(itemId, currentTime);
    }

    /**
     * Get current demand ratio for an item (0.0 to 1.0)
     * Higher value = higher demand
     */
    public double getDemandRatio(ShopItem item) {
        UUID itemId = item.getId();
        int purchases = purchaseCount.getOrDefault(itemId, 0);
        int sells = sellCount.getOrDefault(itemId, 0);
        int total = purchases + sells;
        
        if (total == 0) {
            return 0.5; // Neutral demand
        }
        
        return (double) purchases / total;
    }

    /**
     * Reset all pricing data
     */
    public void reset() {
        purchaseCount.clear();
        sellCount.clear();
        lastPriceUpdate.clear();
    }
}
