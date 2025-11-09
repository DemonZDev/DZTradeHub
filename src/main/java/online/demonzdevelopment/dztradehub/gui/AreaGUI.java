package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AreaGUI {
    private final DZTradeHub plugin;

    public static class AreaHolder implements InventoryHolder {
        private final Area area;
        private int page;

        public AreaHolder(Area area, int page) {
            this.area = area;
            this.page = page;
        }

        public Area getArea() { return area; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public AreaGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Open area browser showing all available areas
     */
    public void openAreaBrowser(Player player) {
        openAreaBrowser(player, 0);
    }

    /**
     * Open area browser with pagination
     */
    public void openAreaBrowser(Player player, int page) {
        List<Area> allAreas = plugin.getShopManager().getAllAreas();
        
        Inventory inv = Bukkit.createInventory(
            null,
            54,
            Component.text("§6§lMarketplace Areas")
        );

        // Calculate pagination
        int itemsPerPage = 36; // Slots 9-44
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allAreas.size());

        // Fill areas (slots 9-44)
        for (int i = startIndex; i < endIndex; i++) {
            Area area = allAreas.get(i);
            ItemStack areaItem = createAreaBrowserDisplay(area);
            inv.setItem(9 + (i - startIndex), areaItem);
        }

        // Previous page button (slot 45)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("§ePrevious Page"));
            prev.setItemMeta(prevMeta);
            inv.setItem(45, prev);
        }

        // Balance displays (slots 46, 47, 48)
        addBalanceDisplays(player, inv);

        // Exit button (slot 49)
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.displayName(Component.text("§cClose"));
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);

        // Next page button (slot 53)
        if (endIndex < allAreas.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("§eNext Page"));
            next.setItemMeta(nextMeta);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    /**
     * Create display item for area in browser
     */
    private ItemStack createAreaBrowserDisplay(Area area) {
        Material displayMaterial = getAreaMaterial(area);
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(area.getDisplayName()));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Type: §e" + area.getType().name()));
        lore.add(Component.text("§7Shops: §e" + area.getShops().size()));
        
        // Add description
        if (!area.getDescription().isEmpty()) {
            lore.add(Component.text(""));
            for (String desc : area.getDescription()) {
                lore.add(Component.text(desc));
            }
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§eClick to browse shops"));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get material icon for area based on type
     */
    private Material getAreaMaterial(Area area) {
        return switch (area.getType()) {
            case SUPERMARKET -> Material.EMERALD_BLOCK;
            case BAZAR -> Material.GOLD_BLOCK;
            case JUNKYARD -> Material.IRON_BLOCK;
            case PAWNAREA -> Material.DIAMOND_BLOCK;
            case BLACKMARKET -> Material.NETHERITE_BLOCK;
            case KITS -> Material.CHEST;
            case AUCTIONHOUSE -> Material.NETHER_STAR;
        };
    }

    /**
     * Open area GUI showing all shops in the area
     */
    public void openArea(Player player, Area area) {
        openArea(player, area, 0);
    }

    /**
     * Open area GUI with specific page
     */
    public void openArea(Player player, Area area, int page) {
        Inventory inv = Bukkit.createInventory(
            new AreaHolder(area, page),
            54,
            Component.text(area.getDisplayName())
        );

        // Get shops sorted alphabetically by name
        List<Shop> shops = area.getShops().stream()
            .sorted(Comparator.comparing(Shop::getName))
            .collect(Collectors.toList());

        // Calculate pagination
        int itemsPerPage = 36; // Slots 9-44 (36 slots)
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, shops.size());

        // Fill shops (slots 9-44)
        for (int i = startIndex; i < endIndex; i++) {
            Shop shop = shops.get(i);
            ItemStack shopItem = createShopDisplay(shop);
            inv.setItem(9 + (i - startIndex), shopItem);
        }

        // Previous page button (slot 45)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("§ePrevious Page"));
            List<Component> prevLore = new ArrayList<>();
            prevLore.add(Component.text("§7Page " + page + " of " + ((shops.size() - 1) / itemsPerPage + 1)));
            prevMeta.lore(prevLore);
            prev.setItemMeta(prevMeta);
            inv.setItem(45, prev);
        }

        // Balance displays (slots 46, 47, 48)
        addBalanceDisplays(player, inv);

        // Exit button (slot 49)
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.displayName(Component.text("§cExit"));
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);

        // Next page button (slot 53)
        if (endIndex < shops.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("§eNext Page"));
            List<Component> nextLore = new ArrayList<>();
            nextLore.add(Component.text("§7Page " + (page + 2) + " of " + ((shops.size() - 1) / itemsPerPage + 1)));
            nextMeta.lore(nextLore);
            next.setItemMeta(nextMeta);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    /**
     * Create display item for a shop
     */
    private ItemStack createShopDisplay(Shop shop) {
        Material displayMaterial = getShopMaterial(shop);
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(shop.getDisplayName()));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Type: §e" + getShopTypeDisplay(shop.getShopType())));
        lore.add(Component.text("§7Items: §e" + shop.getItems().size()));
        lore.add(Component.text(""));
        
        // Queue info
        if (shop.isReceptionEnabled()) {
            lore.add(Component.text("§6⏰ Reception System"));
            lore.add(Component.text("§7Slots: §e" + shop.getReceptionNumber()));
            lore.add(Component.text("§7Time limit: §e" + shop.getReceptionTimeKick() + "s"));
            
            // Add queue stats
            var stats = plugin.getQueueManager().getQueueStats(shop);
            int queueSize = (int) stats.getOrDefault("receptionQueue", 0);
            int activeSize = (int) stats.getOrDefault("receptionActive", 0);
            if (queueSize > 0 || activeSize > 0) {
                lore.add(Component.text("§7Queue: §e" + queueSize + " waiting, " + activeSize + " active"));
            }
        } else if (shop.isCheckoutEnabled()) {
            lore.add(Component.text("§6🛒 Checkout System"));
            lore.add(Component.text("§7Counters: §e" + shop.getCheckoutNumber()));
            lore.add(Component.text("§7Time/item: §e" + shop.getCheckoutTimeKick() + "s"));
            
            // Add queue stats
            var stats = plugin.getQueueManager().getQueueStats(shop);
            int queueSize = (int) stats.getOrDefault("checkoutQueue", 0);
            int activeSize = (int) stats.getOrDefault("checkoutActive", 0);
            if (queueSize > 0 || activeSize > 0) {
                lore.add(Component.text("§7Queue: §e" + queueSize + " waiting, " + activeSize + " processing"));
            }
        } else {
            lore.add(Component.text("§a✓ Instant Access"));
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§eClick to enter shop"));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get material icon for shop based on type
     */
    private Material getShopMaterial(Shop shop) {
        String shopName = shop.getName().toLowerCase();
        
        // Food related
        if (shopName.contains("food") || shopName.contains("meat")) return Material.COOKED_BEEF;
        if (shopName.contains("vegetable") || shopName.contains("farm")) return Material.CARROT;
        if (shopName.contains("bakery") || shopName.contains("bread")) return Material.BREAD;
        
        // Building related
        if (shopName.contains("building") || shopName.contains("construction")) return Material.BRICKS;
        if (shopName.contains("wood") || shopName.contains("lumber")) return Material.OAK_LOG;
        if (shopName.contains("stone") || shopName.contains("mason")) return Material.STONE;
        
        // Tools and equipment
        if (shopName.contains("tool")) return Material.IRON_PICKAXE;
        if (shopName.contains("weapon") || shopName.contains("armor")) return Material.IRON_SWORD;
        if (shopName.contains("enchant")) return Material.ENCHANTING_TABLE;
        
        // Ores and minerals
        if (shopName.contains("ore") || shopName.contains("mineral")) return Material.DIAMOND;
        if (shopName.contains("gem") || shopName.contains("jewel")) return Material.EMERALD;
        
        // Special shops
        if (shopName.contains("rare") || shopName.contains("special")) return Material.NETHER_STAR;
        if (shopName.contains("potion") || shopName.contains("brew")) return Material.BREWING_STAND;
        if (shopName.contains("kit")) return Material.CHEST;
        if (shopName.contains("spawn")) return Material.CHICKEN_SPAWN_EGG;
        if (shopName.contains("decor")) return Material.PAINTING;
        if (shopName.contains("plant") || shopName.contains("flower")) return Material.POPPY;
        if (shopName.contains("mob")) return Material.BONE;
        if (shopName.contains("scrap") || shopName.contains("junk")) return Material.COBBLESTONE;
        if (shopName.contains("redstone") || shopName.contains("tech")) return Material.REDSTONE;
        if (shopName.contains("magic") || shopName.contains("mystic")) return Material.ENDER_EYE;
        if (shopName.contains("fish")) return Material.COD;
        if (shopName.contains("animal")) return Material.WHEAT;
        if (shopName.contains("nether")) return Material.NETHERRACK;
        if (shopName.contains("end")) return Material.END_STONE;
        
        // Default
        return Material.CHEST;
    }

    /**
     * Get display text for shop type
     */
    private String getShopTypeDisplay(Shop.ShopType type) {
        return switch (type) {
            case BUY_ONLY -> "Buy Only";
            case SELL_ONLY -> "Sell Only";
            case BUY_SELL -> "Buy & Sell";
        };
    }

    /**
     * Add balance displays for all 3 currencies
     */
    private void addBalanceDisplays(Player player, Inventory inv) {
        var economyAPI = plugin.getEconomyAPI();
        
        // Money balance (slot 46)
        double moneyBalance = economyAPI.getBalance(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY
        );
        ItemStack moneyItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = moneyItem.getItemMeta();
        moneyMeta.displayName(Component.text("§e💰 Money"));
        List<Component> moneyLore = new ArrayList<>();
        moneyLore.add(Component.text("§a$" + String.format("%.2f", moneyBalance)));
        moneyMeta.lore(moneyLore);
        moneyItem.setItemMeta(moneyMeta);
        inv.setItem(46, moneyItem);

        // MobCoin balance (slot 47)
        double mobcoinBalance = economyAPI.getBalance(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN
        );
        ItemStack mobcoinItem = new ItemStack(Material.GHAST_TEAR);
        ItemMeta mobcoinMeta = mobcoinItem.getItemMeta();
        mobcoinMeta.displayName(Component.text("§b🪙 MobCoin"));
        List<Component> mobcoinLore = new ArrayList<>();
        mobcoinLore.add(Component.text("§b" + String.format("%.0f", mobcoinBalance)));
        mobcoinMeta.lore(mobcoinLore);
        mobcoinItem.setItemMeta(mobcoinMeta);
        inv.setItem(47, mobcoinItem);

        // Gem balance (slot 48)
        double gemBalance = economyAPI.getBalance(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM
        );
        ItemStack gemItem = new ItemStack(Material.EMERALD);
        ItemMeta gemMeta = gemItem.getItemMeta();
        gemMeta.displayName(Component.text("§a💎 Gems"));
        List<Component> gemLore = new ArrayList<>();
        gemLore.add(Component.text("§a" + String.format("%.0f", gemBalance)));
        gemMeta.lore(gemLore);
        gemItem.setItemMeta(gemMeta);
        inv.setItem(48, gemItem);
    }
}
