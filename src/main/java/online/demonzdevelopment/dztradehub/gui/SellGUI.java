package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
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

public class SellGUI {
    private final DZTradeHub plugin;

    public static class SellAreaHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }

    public static class SellShopHolder implements InventoryHolder {
        private final Area area;

        public SellShopHolder(Area area) {
            this.area = area;
        }

        public Area getArea() { return area; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public static class SellInterfaceHolder implements InventoryHolder {
        private final Area area;
        private final Shop shop;
        private final List<Integer> selectedSlots = new ArrayList<>();

        public SellInterfaceHolder(Area area, Shop shop) {
            this.area = area;
            this.shop = shop;
        }

        public Area getArea() { return area; }
        public Shop getShop() { return shop; }
        public List<Integer> getSelectedSlots() { return selectedSlots; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public SellGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Open area selection GUI
     */
    public void openAreaSelection(Player player) {
        Inventory inv = Bukkit.createInventory(
            new SellAreaHolder(),
            54,
            Component.text("§6§lSelect Area to Sell")
        );

        List<Area> areas = plugin.getShopManager().getAllAreas();
        int slot = 9;

        for (Area area : areas) {
            if (slot >= 45) break;

            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§e§l" + area.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§7Shops: §f" + plugin.getShopManager().getShopsInArea(area.getName()).size()));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to select shop"));
            meta.lore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("§e§lHow to Sell"));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text(""));
        infoLore.add(Component.text("§71. Select an area"));
        infoLore.add(Component.text("§72. Select a shop"));
        infoLore.add(Component.text("§73. Choose items to sell"));
        infoLore.add(Component.text("§74. Confirm sale"));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);

        // Close
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);

        player.openInventory(inv);
    }

    /**
     * Open shop selection GUI
     */
    public void openShopSelection(Player player, Area area) {
        Inventory inv = Bukkit.createInventory(
            new SellShopHolder(area),
            54,
            Component.text("§6§lSelect Shop - " + area.getDisplayName())
        );

        List<Shop> shops = plugin.getShopManager().getShopsInArea(area.getName());
        int slot = 9;

        for (Shop shop : shops) {
            if (slot >= 45) break;

            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§a§l" + shop.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            
            // Count sellable items
            int sellableCount = 0;
            for (ShopItem shopItem : shop.getItems()) {
                if (shopItem.canSell()) {
                    sellableCount++;
                }
            }
            
            lore.add(Component.text("§7Accepts §e" + sellableCount + "§7 item types"));
            lore.add(Component.text("§7Currency: §f" + shop.getCurrencyType().name()));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to sell items"));
            meta.lore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot++, item);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(45, back);

        // Close
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);

        player.openInventory(inv);
    }

    /**
     * Open sell interface - player puts items to sell
     */
    public void openSellInterface(Player player, Area area, Shop shop) {
        Inventory inv = Bukkit.createInventory(
            new SellInterfaceHolder(area, shop),
            54,
            Component.text("§a§lSell Items - " + shop.getDisplayName())
        );

        // Instructions (slot 4)
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("§e§lInstructions"));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text(""));
        infoLore.add(Component.text("§71. Place items in empty slots"));
        infoLore.add(Component.text("§72. Only sellable items accepted"));
        infoLore.add(Component.text("§73. Click §aSell All§7 to confirm"));
        infoLore.add(Component.text(""));
        infoLore.add(Component.text("§7Unsellable items returned"));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Item placement area (slots 9-44)
        // Players can place items here

        // Sell all button (slot 48)
        ItemStack sellAll = new ItemStack(Material.LIME_DYE);
        ItemMeta sellMeta = sellAll.getItemMeta();
        sellMeta.displayName(Component.text("§a§lSell All Items"));
        List<Component> sellLore = new ArrayList<>();
        sellLore.add(Component.text(""));
        sellLore.add(Component.text("§7Click to sell all items"));
        sellLore.add(Component.text("§7in this interface"));
        sellMeta.lore(sellLore);
        sellAll.setItemMeta(sellMeta);
        inv.setItem(48, sellAll);

        // Back button (slot 45)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(45, back);

        // Close button (slot 53)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(53, close);

        player.openInventory(inv);
    }
}
