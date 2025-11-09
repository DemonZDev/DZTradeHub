package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final DZTradeHub plugin;

    public static class ShopHolder implements InventoryHolder {
        private final Shop shop;
        private int page;

        public ShopHolder(Shop shop, int page) {
            this.shop = shop;
            this.page = page;
        }

        public Shop getShop() { return shop; }
        public int getPage() { return page; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public ShopGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player, Shop shop) {
        openShop(player, shop, 0);
    }

    public void openShop(Player player, Shop shop, int page) {
        Inventory inv = Bukkit.createInventory(
            new ShopHolder(shop, page),
            54,
            Component.text(shop.getDisplayName())
        );

        // Get items for this page
        List<ShopItem> items = shop.getItems();
        int startIndex = page * 36;
        int endIndex = Math.min(startIndex + 36, items.size());

        // Fill items (slots 9-44)
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            ItemStack display = createShopItemDisplay(shopItem);
            inv.setItem(9 + (i - startIndex), display);
        }

        // Navigation and control items
        // Previous page (slot 45)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("§ePrevious Page"));
            prev.setItemMeta(prevMeta);
            inv.setItem(45, prev);
        }

        // Balance display (slot 47)
        double balance = plugin.getEconomyAPI().getBalance(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY
        );
        ItemStack balanceItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balanceItem.getItemMeta();
        balanceMeta.displayName(Component.text("§eYour Balance"));
        List<Component> balanceLore = new ArrayList<>();
        balanceLore.add(Component.text("§a$" + String.format("%.2f", balance)));
        balanceMeta.lore(balanceLore);
        balanceItem.setItemMeta(balanceMeta);
        inv.setItem(47, balanceItem);

        // Close button (slot 49)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        // Next page (slot 53)
        if (endIndex < items.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("§eNext Page"));
            next.setItemMeta(nextMeta);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    private ItemStack createShopItemDisplay(ShopItem shopItem) {
        ItemStack display = shopItem.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();
        
        List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore.addAll(meta.lore());
        }
        
        lore.add(Component.text(""));
        
        if (shopItem.canBuy()) {
            lore.add(Component.text("§7Buy Price: §a$" + String.format("%.2f", shopItem.getBuyPrice())));
            lore.add(Component.text("§7Stock: §e" + shopItem.getCurrentStock() + "/" + shopItem.getMaxStock()));
        }
        
        if (shopItem.canSell()) {
            lore.add(Component.text("§7Sell Price: §e$" + String.format("%.2f", shopItem.getSellPrice())));
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§eLeft-click to buy"));
        lore.add(Component.text("§eRight-click to sell"));
        
        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    /**
     * Open shop configuration GUI
     */
    public void openShopConfig(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§6Configure: " + shop.getDisplayName()));

        // Reception settings
        ItemStack receptionItem = new ItemStack(shop.isReceptionEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta receptionMeta = receptionItem.getItemMeta();
        receptionMeta.displayName(Component.text("§eReception System"));
        List<Component> receptionLore = new ArrayList<>();
        receptionLore.add(Component.text(shop.isReceptionEnabled() ? "§aEnabled" : "§cDisabled"));
        receptionLore.add(Component.text("§7Slots: " + shop.getReceptionNumber()));
        receptionLore.add(Component.text("§7Time: " + shop.getReceptionTimeKick() + "s"));
        receptionLore.add(Component.text("§7AFK: " + shop.getReceptionAfkKick() + "s"));
        receptionLore.add(Component.text(""));
        receptionLore.add(Component.text("§eClick to toggle"));
        receptionMeta.lore(receptionLore);
        receptionItem.setItemMeta(receptionMeta);
        inv.setItem(11, receptionItem);

        // Checkout settings
        ItemStack checkoutItem = new ItemStack(shop.isCheckoutEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta checkoutMeta = checkoutItem.getItemMeta();
        checkoutMeta.displayName(Component.text("§eCheckout System"));
        List<Component> checkoutLore = new ArrayList<>();
        checkoutLore.add(Component.text(shop.isCheckoutEnabled() ? "§aEnabled" : "§cDisabled"));
        checkoutLore.add(Component.text("§7Counters: " + shop.getCheckoutNumber()));
        checkoutLore.add(Component.text("§7Time/item: " + shop.getCheckoutTimeKick() + "s"));
        checkoutLore.add(Component.text(""));
        checkoutLore.add(Component.text("§eClick to toggle"));
        checkoutMeta.lore(checkoutLore);
        checkoutItem.setItemMeta(checkoutMeta);
        inv.setItem(13, checkoutItem);

        // Shop type
        ItemStack typeItem = new ItemStack(Material.PAPER);
        ItemMeta typeMeta = typeItem.getItemMeta();
        typeMeta.displayName(Component.text("§eShop Type"));
        List<Component> typeLore = new ArrayList<>();
        typeLore.add(Component.text("§7" + shop.getShopType().name()));
        typeLore.add(Component.text(""));
        typeLore.add(Component.text("§eClick to change"));
        typeMeta.lore(typeLore);
        typeItem.setItemMeta(typeMeta);
        inv.setItem(15, typeItem);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(22, close);

        player.openInventory(inv);
    }

    /**
     * Open add item GUI
     */
    public void openAddItemGUI(Player player, Shop shop, String areaName) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("§6Add Item: " + shop.getDisplayName()));

        // Add common items as selection
        Material[] commonItems = {
            Material.DIAMOND, Material.EMERALD, Material.IRON_INGOT, Material.GOLD_INGOT,
            Material.COAL, Material.STONE, Material.COBBLESTONE, Material.OAK_LOG,
            Material.WHEAT, Material.BREAD, Material.COOKED_BEEF, Material.APPLE,
            Material.IRON_PICKAXE, Material.IRON_SWORD, Material.BOW, Material.ARROW
        };

        for (int i = 0; i < Math.min(commonItems.length, 45); i++) {
            ItemStack item = new ItemStack(commonItems[i]);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§eClick to add this item"));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // Instructions
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("§eHow to Add Items"));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text("§71. Click an item to select"));
        infoLore.add(Component.text("§72. Configure in chat"));
        infoLore.add(Component.text("§7or use command:"));
        infoLore.add(Component.text("§e/dzth add-item " + areaName + " " + shop.getName() + " <item>"));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);

        player.openInventory(inv);
    }
}