package online.demonzdevelopment.dztradehub.storage;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileStorageManager {
    private final DZTradeHub plugin;
    private final File areasFolder;
    private final File auctionsFolder;

    public FileStorageManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.areasFolder = new File(plugin.getDataFolder(), "Areas");
        this.auctionsFolder = new File(plugin.getDataFolder(), "Auctions");
        if (!areasFolder.exists()) {
            areasFolder.mkdirs();
        }
        if (!auctionsFolder.exists()) {
            auctionsFolder.mkdirs();
        }
    }

    public void saveArea(Area area) {
        File areaFolder = new File(areasFolder, area.getName());
        if (!areaFolder.exists()) {
            areaFolder.mkdirs();
        }

        File areaFile = new File(areaFolder, area.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", area.getName());
        config.set("displayName", area.getDisplayName());
        config.set("type", area.getType().name());
        config.set("description", area.getDescription());
        
        if (area.getLocation() != null) {
            config.set("location.world", area.getLocation().getWorld().getName());
            config.set("location.x", area.getLocation().getX());
            config.set("location.y", area.getLocation().getY());
            config.set("location.z", area.getLocation().getZ());
        }

        try {
            config.save(areaFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save area: " + area.getName());
            e.printStackTrace();
        }
    }

    public void saveShop(String areaName, Shop shop) {
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        File shopFolder = new File(shopsFolder, shop.getName());
        
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
        }

        File shopFile = new File(shopFolder, shop.getName().toLowerCase() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", shop.getName());
        config.set("displayName", shop.getDisplayName());
        config.set("shopType", shop.getShopType().name());
        config.set("queueType", shop.getQueueType().name());
        
        // Reception settings
        config.set("reception.enabled", shop.isReceptionEnabled());
        config.set("reception.number", shop.getReceptionNumber());
        config.set("reception.timeKick", shop.getReceptionTimeKick());
        config.set("reception.afkKick", shop.getReceptionAfkKick());
        
        // Checkout settings
        config.set("checkout.enabled", shop.isCheckoutEnabled());
        config.set("checkout.number", shop.getCheckoutNumber());
        config.set("checkout.timeKick", shop.getCheckoutTimeKick());
        
        // Stock settings
        config.set("stock.linkedShop", shop.getLinkedShopName());
        config.set("stock.restockInterval", shop.getRestockInterval());
        config.set("stock.restockAmount", shop.getRestockAmount());

        try {
            config.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save shop: " + shop.getName());
            e.printStackTrace();
        }
    }

    public void saveShopItems(String areaName, Shop shop) {
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        File shopFolder = new File(shopsFolder, shop.getName());
        File itemsFile = new File(shopFolder, "items.yml");

        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (ShopItem item : shop.getItems()) {
            String path = "items." + index;
            config.set(path + ".material", item.getItemStack().getType().name());
            config.set(path + ".amount", item.getItemStack().getAmount());
            config.set(path + ".currency", item.getCurrency().name());
            config.set(path + ".minPrice", item.getMinPrice());
            config.set(path + ".maxPrice", item.getMaxPrice());
            config.set(path + ".currentBuyPrice", item.getBuyPrice());
            config.set(path + ".currentSellPrice", item.getSellPrice());
            config.set(path + ".transactionType", item.getTransactionType().name());
            config.set(path + ".currentStock", item.getCurrentStock());
            config.set(path + ".maxStock", item.getMaxStock());
            config.set(path + ".dynamicPricing", item.isDynamicPricingEnabled());
            index++;
        }

        try {
            config.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save items for shop: " + shop.getName());
            e.printStackTrace();
        }
    }

    public Area loadArea(String areaName) {
        File areaFolder = new File(areasFolder, areaName);
        File areaFile = new File(areaFolder, areaName + ".yml");

        if (!areaFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(areaFile);
        
        Area area = new Area(
            config.getString("name"),
            config.getString("displayName"),
            Area.AreaType.valueOf(config.getString("type")),
            null // Location loaded separately if needed
        );
        
        area.setDescription(config.getStringList("description"));
        return area;
    }

    public Shop loadShop(String areaName, String shopName) {
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        File shopFolder = new File(shopsFolder, shopName);
        File shopFile = new File(shopFolder, shopName.toLowerCase() + ".yml");

        if (!shopFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
        
        Shop shop = new Shop(
            config.getString("name"),
            config.getString("displayName"),
            Shop.ShopType.valueOf(config.getString("shopType"))
        );
        
        shop.setQueueType(Shop.QueueType.valueOf(config.getString("queueType", "NONE")));
        
        // Load reception settings
        shop.setReceptionEnabled(config.getBoolean("reception.enabled", false));
        shop.setReceptionNumber(config.getInt("reception.number", 1));
        shop.setReceptionTimeKick(config.getInt("reception.timeKick", 300));
        shop.setReceptionAfkKick(config.getInt("reception.afkKick", 60));
        
        // Load checkout settings
        shop.setCheckoutEnabled(config.getBoolean("checkout.enabled", false));
        shop.setCheckoutNumber(config.getInt("checkout.number", 3));
        shop.setCheckoutTimeKick(config.getInt("checkout.timeKick", 5));
        
        // Load stock settings
        shop.setLinkedShopName(config.getString("stock.linkedShop"));
        shop.setRestockInterval(config.getString("stock.restockInterval", "DAILY"));
        shop.setRestockAmount(config.getInt("stock.restockAmount", 64));
        
        return shop;
    }

    public List<ShopItem> loadShopItems(String areaName, String shopName) {
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        File shopFolder = new File(shopsFolder, shopName);
        File itemsFile = new File(shopFolder, "items.yml");

        List<ShopItem> items = new ArrayList<>();

        if (!itemsFile.exists()) {
            return items;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");

        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                String path = "items." + key;
                Material material = Material.valueOf(config.getString(path + ".material"));
                int amount = config.getInt(path + ".amount", 1);
                ItemStack itemStack = new ItemStack(material, amount);

                ShopItem item = new ShopItem(
                    itemStack,
                    config.getDouble(path + ".currentBuyPrice"),
                    config.getDouble(path + ".currentSellPrice")
                );

                item.setCurrency(ShopItem.Currency.valueOf(config.getString(path + ".currency", "MONEY")));
                item.setMinPrice(config.getDouble(path + ".minPrice"));
                item.setMaxPrice(config.getDouble(path + ".maxPrice"));
                item.setTransactionType(ShopItem.TransactionType.valueOf(config.getString(path + ".transactionType")));
                item.setCurrentStock(config.getInt(path + ".currentStock"));
                item.setMaxStock(config.getInt(path + ".maxStock", 640));
                item.setDynamicPricingEnabled(config.getBoolean(path + ".dynamicPricing", false));

                items.add(item);
            }
        }

        return items;
    }

    public List<String> getAllAreaNames() {
        List<String> areaNames = new ArrayList<>();
        File[] files = areasFolder.listFiles(File::isDirectory);
        if (files != null) {
            for (File file : files) {
                areaNames.add(file.getName());
            }
        }
        return areaNames;
    }

    public List<String> getAllShopNames(String areaName) {
        List<String> shopNames = new ArrayList<>();
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        
        if (shopsFolder.exists()) {
            File[] files = shopsFolder.listFiles(File::isDirectory);
            if (files != null) {
                for (File file : files) {
                    shopNames.add(file.getName());
                }
            }
        }
        return shopNames;
    }

    public void deleteArea(String areaName) {
        File areaFolder = new File(areasFolder, areaName);
        deleteDirectory(areaFolder);
    }

    public void deleteShop(String areaName, String shopName) {
        File areaFolder = new File(areasFolder, areaName);
        File shopsFolder = new File(areaFolder, "Shops");
        File shopFolder = new File(shopsFolder, shopName);
        deleteDirectory(shopFolder);
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    // ==================== AUCTION STORAGE METHODS ====================
    
    public void saveAuction(online.demonzdevelopment.dztradehub.data.Auction auction) {
        File auctionFile = new File(auctionsFolder, auction.getId().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("id", auction.getId().toString());
        config.set("sellerUUID", auction.getSellerUUID().toString());
        config.set("itemNumber", auction.getItemNumber());
        config.set("currencyType", auction.getCurrencyType());
        config.set("actualPrice", auction.getActualPrice());
        config.set("maxDropPrice", auction.getMaxDropPrice());
        config.set("dropPerUnit", auction.getDropPerUnit());
        config.set("dropIntervalHours", auction.getDropIntervalHours());
        config.set("maxQueue", auction.getMaxQueue());
        config.set("priceIncreasePercent", auction.getPriceIncreasePercent());
        config.set("createdTime", auction.getCreatedTime());
        config.set("lastDropTime", auction.getLastDropTime());
        config.set("frozen", auction.isFrozen());
        
        // Save item
        ItemStack item = auction.getItemStack();
        config.set("item.material", item.getType().name());
        config.set("item.amount", item.getAmount());
        if (item.hasItemMeta()) {
            config.set("item.displayName", item.getItemMeta().getDisplayName());
            config.set("item.lore", item.getItemMeta().getLore());
        }
        
        // Save queue
        int queueIndex = 0;
        for (online.demonzdevelopment.dztradehub.data.Auction.QueueEntry entry : auction.getQueue()) {
            String path = "queue." + queueIndex;
            config.set(path + ".playerUUID", entry.playerUUID().toString());
            config.set(path + ".paidPrice", entry.paidPrice());
            config.set(path + ".joinTime", entry.joinTime());
            queueIndex++;
        }
        
        try {
            config.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save auction: " + auction.getId());
            e.printStackTrace();
        }
    }
    
    public online.demonzdevelopment.dztradehub.data.Auction loadAuction(java.util.UUID auctionId) {
        File auctionFile = new File(auctionsFolder, auctionId.toString() + ".yml");
        if (!auctionFile.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(auctionFile);
        
        try {
            java.util.UUID sellerUUID = java.util.UUID.fromString(config.getString("sellerUUID"));
            String currencyType = config.getString("currencyType", "MONEY");
            double actualPrice = config.getDouble("actualPrice");
            double maxDropPrice = config.getDouble("maxDropPrice");
            double dropPerUnit = config.getDouble("dropPerUnit");
            int dropIntervalHours = config.getInt("dropIntervalHours");
            int maxQueue = config.getInt("maxQueue");
            double priceIncreasePercent = config.getDouble("priceIncreasePercent");
            int itemNumber = config.getInt("itemNumber");
            
            // Load item
            Material material = Material.valueOf(config.getString("item.material"));
            int amount = config.getInt("item.amount", 1);
            ItemStack itemStack = new ItemStack(material, amount);
            
            if (config.contains("item.displayName")) {
                org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(config.getString("item.displayName"));
                    if (config.contains("item.lore")) {
                        meta.setLore(config.getStringList("item.lore"));
                    }
                    itemStack.setItemMeta(meta);
                }
            }
            
            online.demonzdevelopment.dztradehub.data.Auction auction = 
                new online.demonzdevelopment.dztradehub.data.Auction(
                    sellerUUID, itemStack, currencyType, actualPrice, maxDropPrice,
                    dropPerUnit, dropIntervalHours, maxQueue, priceIncreasePercent, itemNumber
                );
            
            auction.setLastDropTime(config.getLong("lastDropTime"));
            auction.setFrozen(config.getBoolean("frozen"));
            
            // Load queue
            ConfigurationSection queueSection = config.getConfigurationSection("queue");
            if (queueSection != null) {
                for (String key : queueSection.getKeys(false)) {
                    String path = "queue." + key;
                    java.util.UUID playerUUID = java.util.UUID.fromString(config.getString(path + ".playerUUID"));
                    double paidPrice = config.getDouble(path + ".paidPrice");
                    long joinTime = config.getLong(path + ".joinTime");
                    auction.addToQueue(playerUUID, paidPrice);
                }
            }
            
            return auction;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load auction: " + auctionId);
            e.printStackTrace();
            return null;
        }
    }
    
    public java.util.List<online.demonzdevelopment.dztradehub.data.Auction> loadAllAuctions() {
        java.util.List<online.demonzdevelopment.dztradehub.data.Auction> auctions = new java.util.ArrayList<>();
        File[] files = auctionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                String idStr = file.getName().replace(".yml", "");
                try {
                    java.util.UUID auctionId = java.util.UUID.fromString(idStr);
                    online.demonzdevelopment.dztradehub.data.Auction auction = loadAuction(auctionId);
                    if (auction != null) {
                        auctions.add(auction);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid auction file: " + file.getName());
                }
            }
        }
        
        return auctions;
    }
    
    public void deleteAuction(java.util.UUID auctionId) {
        File auctionFile = new File(auctionsFolder, auctionId.toString() + ".yml");
        if (auctionFile.exists()) {
            auctionFile.delete();
        }
    }
    
    public int cleanupOldAuctions(long retentionMillis) {
        int cleaned = 0;
        long now = System.currentTimeMillis();
        File[] files = auctionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                String idStr = file.getName().replace(".yml", "");
                try {
                    java.util.UUID auctionId = java.util.UUID.fromString(idStr);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    long createdTime = config.getLong("createdTime");
                    
                    if ((now - createdTime) > retentionMillis) {
                        file.delete();
                        cleaned++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error cleaning auction file: " + file.getName());
                }
            }
        }
        
        return cleaned;
    }
}
