package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StockManager {
    private final DZTradeHub plugin;
    private final Map<UUID, Long> lastRestockTime; // shopItemId -> timestamp

    public StockManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.lastRestockTime = new HashMap<>();
    }

    /**
     * Process restocking for all shops
     */
    public void processRestocking() {
        for (Area area : plugin.getShopManager().getAllAreas()) {
            for (Shop shop : plugin.getShopManager().getShopsInArea(area.getName())) {
                processShopRestocking(area.getName(), shop);
            }
        }
    }

    /**
     * Process restocking for a specific shop
     */
    private void processShopRestocking(String areaName, Shop shop) {
        long currentTime = System.currentTimeMillis();
        
        for (ShopItem item : shop.getItems()) {
            Long lastRestock = lastRestockTime.get(item.getId());
            
            if (lastRestock == null) {
                lastRestockTime.put(item.getId(), currentTime);
                continue;
            }
            
            long timeSinceRestock = currentTime - lastRestock;
            long restockInterval = getRestockIntervalMillis(item.getRefillInterval());
            
            if (timeSinceRestock >= restockInterval) {
                // Restock item
                int amountToAdd = item.getRefillAmount();
                item.addStock(amountToAdd);
                lastRestockTime.put(item.getId(), currentTime);
                
                plugin.getLogger().fine("Restocked " + item.getItemStack().getType().name() + 
                    " in shop " + shop.getName() + " by " + amountToAdd);
                
                // Save updated item
                plugin.getFileStorageManager().saveShopItems(areaName, shop);
            }
        }
    }

    /**
     * Transfer stock from sell shop to buy shop (when items are sold)
     */
    public void transferStock(String areaName, Shop sellShop, ShopItem soldItem, int quantity) {
        String linkedShopName = sellShop.getLinkedShopName();
        
        if (linkedShopName == null || linkedShopName.isEmpty()) {
            // Link to self if no linked shop specified
            linkedShopName = sellShop.getName();
        }
        
        Shop targetShop = plugin.getShopManager().getShop(areaName, linkedShopName);
        
        if (targetShop == null) {
            plugin.getLogger().warning("Linked shop '" + linkedShopName + "' not found for stock transfer");
            return;
        }
        
        // Find matching item in target shop
        for (ShopItem targetItem : targetShop.getItems()) {
            if (isSameItem(soldItem, targetItem)) {
                targetItem.addStock(quantity);
                plugin.getFileStorageManager().saveShopItems(areaName, targetShop);
                
                plugin.getLogger().fine("Transferred " + quantity + " " + 
                    soldItem.getItemStack().getType().name() + " from " + 
                    sellShop.getName() + " to " + targetShop.getName());
                return;
            }
        }
        
        plugin.getLogger().fine("No matching item found in " + linkedShopName + 
            " for stock transfer from " + sellShop.getName());
    }

    /**
     * Check if two shop items are the same (same material)
     */
    private boolean isSameItem(ShopItem item1, ShopItem item2) {
        return item1.getItemStack().getType() == item2.getItemStack().getType();
    }

    /**
     * Convert restock interval string to milliseconds
     * Format examples: "1h", "2d", "1w", "30m", "DAILY", "HOURLY", "WEEKLY"
     */
    private long getRestockIntervalMillis(String interval) {
        if (interval == null || interval.isEmpty()) {
            return 24 * 60 * 60 * 1000L; // Default: 1 day
        }
        
        interval = interval.toUpperCase();
        
        // Handle predefined intervals
        switch (interval) {
            case "HOURLY":
                return 60 * 60 * 1000L;
            case "DAILY":
                return 24 * 60 * 60 * 1000L;
            case "WEEKLY":
                return 7 * 24 * 60 * 60 * 1000L;
            case "MONTHLY":
                return 30L * 24 * 60 * 60 * 1000L;
        }
        
        // Parse custom format (e.g., "2h", "30m", "3d")
        try {
            if (interval.endsWith("M")) {
                int minutes = Integer.parseInt(interval.substring(0, interval.length() - 1));
                return minutes * 60 * 1000L;
            } else if (interval.endsWith("H")) {
                int hours = Integer.parseInt(interval.substring(0, interval.length() - 1));
                return hours * 60 * 60 * 1000L;
            } else if (interval.endsWith("D")) {
                int days = Integer.parseInt(interval.substring(0, interval.length() - 1));
                return days * 24 * 60 * 60 * 1000L;
            } else if (interval.endsWith("W")) {
                int weeks = Integer.parseInt(interval.substring(0, interval.length() - 1));
                return weeks * 7 * 24 * 60 * 60 * 1000L;
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid restock interval format: " + interval);
        }
        
        return 24 * 60 * 60 * 1000L; // Default: 1 day
    }

    /**
     * Force restock for a specific shop
     */
    public void forceRestockShop(String areaName, Shop shop) {
        for (ShopItem item : shop.getItems()) {
            item.addStock(item.getRefillAmount());
            lastRestockTime.put(item.getId(), System.currentTimeMillis());
        }
        plugin.getFileStorageManager().saveShopItems(areaName, shop);
        plugin.getLogger().info("Force restocked shop: " + shop.getName());
    }
}
