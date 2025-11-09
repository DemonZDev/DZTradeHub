package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShopManageGUI {
    private final DZTradeHub plugin;

    public ShopManageGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void openManageGUI(Player player, String areaName, Shop shop) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lManage: " + shop.getName());

        // Shop info (slot 4)
        gui.setItem(4, createGuiItem(Material.CHEST, "§e§l" + shop.getDisplayName(),
            "§7Area: §f" + areaName,
            "§7Type: §e" + shop.getShopType().name(),
            "§7Items: §a" + shop.getItems().size()));

        // Reception system toggle (slot 19)
        Material recMaterial = shop.isReceptionEnabled() ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        String recStatus = shop.isReceptionEnabled() ? "§a[ENABLED]" : "§c[DISABLED]";
        gui.setItem(19, createGuiItem(recMaterial, "§6Reception System " + recStatus,
            "§7Click to toggle",
            "§7Current slots: " + shop.getReceptionNumber(),
            "§7Time limit: " + shop.getReceptionTimeKick() + "s",
            "§7AFK kick: " + shop.getReceptionAfkKick() + "s"));

        // Reception number (slot 20)
        if (shop.isReceptionEnabled()) {
            gui.setItem(20, createGuiItem(Material.IRON_DOOR, "§eReception Slots: §f" + shop.getReceptionNumber(),
                "§7Left: +1", "§7Right: -1", "§7Shift+Left: +5", "§7Shift+Right: -5"));
        }

        // Reception time kick (slot 21)
        if (shop.isReceptionEnabled()) {
            gui.setItem(21, createGuiItem(Material.CLOCK, "§eTime Limit: §f" + shop.getReceptionTimeKick() + "s",
                "§7Left: +60s", "§7Right: -60s", "§7Shift+Left: +300s", "§7Shift+Right: -300s"));
        }

        // Reception AFK kick (slot 22)
        if (shop.isReceptionEnabled()) {
            gui.setItem(22, createGuiItem(Material.REDSTONE_TORCH, "§eAFK Kick: §f" + shop.getReceptionAfkKick() + "s",
                "§7Left: +10s", "§7Right: -10s", "§7Shift+Left: +60s", "§7Shift+Right: -60s"));
        }

        // Checkout system toggle (slot 28)
        Material checkMaterial = shop.isCheckoutEnabled() ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        String checkStatus = shop.isCheckoutEnabled() ? "§a[ENABLED]" : "§c[DISABLED]";
        gui.setItem(28, createGuiItem(checkMaterial, "§6Checkout System " + checkStatus,
            "§7Click to toggle",
            "§7Current counters: " + shop.getCheckoutNumber(),
            "§7Time per item: " + shop.getCheckoutTimeKick() + "s"));

        // Checkout number (slot 29)
        if (shop.isCheckoutEnabled()) {
            gui.setItem(29, createGuiItem(Material.HOPPER, "§eCheckout Counters: §f" + shop.getCheckoutNumber(),
                "§7Left: +1", "§7Right: -1", "§7Shift+Left: +5", "§7Shift+Right: -5"));
        }

        // Checkout time per item (slot 30)
        if (shop.isCheckoutEnabled()) {
            gui.setItem(30, createGuiItem(Material.CLOCK, "§eTime/Item: §f" + shop.getCheckoutTimeKick() + "s",
                "§7Left: +1s", "§7Right: -1s", "§7Shift+Left: +5s", "§7Shift+Right: -5s"));
        }

        // Shop type toggle (slot 37)
        gui.setItem(37, createGuiItem(Material.GOLD_INGOT, "§eShop Type: §f" + shop.getShopType().name(),
            "§7Click to cycle",
            "§7Options:",
            "§7  - BUY_ONLY",
            "§7  - SELL_ONLY",
            "§7  - BUY_SELL"));

        // Linked shop (slot 38)
        String linkedShop = shop.getLinkedShopName() != null ? shop.getLinkedShopName() : "None";
        gui.setItem(38, createGuiItem(Material.CHAIN, "§eLinked Shop: §f" + linkedShop,
            "§7Sold items transfer to linked shop",
            "§7Use /dzth link-shop to change"));

        // Current queue stats (slot 40)
        Map<String, Object> queueStats = plugin.getQueueManager().getQueueStats(shop);
        int recQueue = (int) queueStats.getOrDefault("receptionQueue", 0);
        int recActive = (int) queueStats.getOrDefault("receptionActive", 0);
        int checkQueue = (int) queueStats.getOrDefault("checkoutQueue", 0);
        int checkActive = (int) queueStats.getOrDefault("checkoutActive", 0);

        List<String> statsLore = new ArrayList<>();
        statsLore.add("§7Reception Queue: §e" + recQueue + " waiting");
        statsLore.add("§7Reception Active: §a" + recActive + " shopping");
        statsLore.add("§7Checkout Queue: §e" + checkQueue + " waiting");
        statsLore.add("§7Checkout Active: §a" + checkActive + " processing");
        
        gui.setItem(40, createGuiItem(Material.BOOK, "§e§lCurrent Statistics",
            statsLore.toArray(new String[0])));

        // View items (slot 41)
        gui.setItem(41, createGuiItem(Material.CHEST, "§e§lView Items",
            "§7Click to see all shop items",
            "§7Total: §a" + shop.getItems().size()));

        // Add item (slot 42)
        gui.setItem(42, createGuiItem(Material.EMERALD, "§a§lAdd Item",
            "§7Click to add new item",
            "§7Opens item selection GUI"));

        // Save changes (slot 48)
        gui.setItem(48, createGuiItem(Material.LIME_CONCRETE, "§a§lSave Changes",
            "§7Click to save and close"));

        // Clear queues (slot 49)
        gui.setItem(49, createGuiItem(Material.RED_CONCRETE, "§c§lClear All Queues",
            "§7Click to clear all queues",
            "§7WARNING: Will kick all players"));

        // Close (slot 50)
        gui.setItem(50, createGuiItem(Material.BARRIER, "§cClose",
            "§7Close without saving"));

        player.openInventory(gui);
    }

    public void handleClick(Player player, String areaName, Shop shop, int slot, boolean leftClick, boolean shift) {
        switch (slot) {
            case 19 -> { // Reception toggle
                if (shop.isCheckoutEnabled()) {
                    MessageUtil.sendError(player, "Cannot enable both systems! Disable checkout first.");
                    return;
                }
                shop.setReceptionEnabled(!shop.isReceptionEnabled());
                if (shop.isReceptionEnabled()) {
                    shop.setQueueType(Shop.QueueType.RECEPTION);
                } else {
                    shop.setQueueType(Shop.QueueType.NONE);
                }
                openManageGUI(player, areaName, shop);
            }
            case 20 -> { // Reception number
                if (shift && leftClick) shop.setReceptionNumber(Math.min(10, shop.getReceptionNumber() + 5));
                else if (shift) shop.setReceptionNumber(Math.max(1, shop.getReceptionNumber() - 5));
                else if (leftClick) shop.setReceptionNumber(shop.getReceptionNumber() + 1);
                else shop.setReceptionNumber(Math.max(1, shop.getReceptionNumber() - 1));
                openManageGUI(player, areaName, shop);
            }
            case 21 -> { // Reception time kick
                if (shift && leftClick) shop.setReceptionTimeKick(shop.getReceptionTimeKick() + 300);
                else if (shift) shop.setReceptionTimeKick(Math.max(60, shop.getReceptionTimeKick() - 300));
                else if (leftClick) shop.setReceptionTimeKick(shop.getReceptionTimeKick() + 60);
                else shop.setReceptionTimeKick(Math.max(60, shop.getReceptionTimeKick() - 60));
                openManageGUI(player, areaName, shop);
            }
            case 22 -> { // Reception AFK kick
                if (shift && leftClick) shop.setReceptionAfkKick(shop.getReceptionAfkKick() + 60);
                else if (shift) shop.setReceptionAfkKick(Math.max(10, shop.getReceptionAfkKick() - 60));
                else if (leftClick) shop.setReceptionAfkKick(shop.getReceptionAfkKick() + 10);
                else shop.setReceptionAfkKick(Math.max(10, shop.getReceptionAfkKick() - 10));
                openManageGUI(player, areaName, shop);
            }
            case 28 -> { // Checkout toggle
                if (shop.isReceptionEnabled()) {
                    MessageUtil.sendError(player, "Cannot enable both systems! Disable reception first.");
                    return;
                }
                shop.setCheckoutEnabled(!shop.isCheckoutEnabled());
                if (shop.isCheckoutEnabled()) {
                    shop.setQueueType(Shop.QueueType.CASH_COUNTER);
                } else {
                    shop.setQueueType(Shop.QueueType.NONE);
                }
                openManageGUI(player, areaName, shop);
            }
            case 29 -> { // Checkout number
                if (shift && leftClick) shop.setCheckoutNumber(Math.min(10, shop.getCheckoutNumber() + 5));
                else if (shift) shop.setCheckoutNumber(Math.max(1, shop.getCheckoutNumber() - 5));
                else if (leftClick) shop.setCheckoutNumber(shop.getCheckoutNumber() + 1);
                else shop.setCheckoutNumber(Math.max(1, shop.getCheckoutNumber() - 1));
                openManageGUI(player, areaName, shop);
            }
            case 30 -> { // Checkout time per item
                if (shift && leftClick) shop.setCheckoutTimeKick(Math.min(60, shop.getCheckoutTimeKick() + 5));
                else if (shift) shop.setCheckoutTimeKick(Math.max(1, shop.getCheckoutTimeKick() - 5));
                else if (leftClick) shop.setCheckoutTimeKick(shop.getCheckoutTimeKick() + 1);
                else shop.setCheckoutTimeKick(Math.max(1, shop.getCheckoutTimeKick() - 1));
                openManageGUI(player, areaName, shop);
            }
            case 37 -> { // Shop type cycle
                shop.setShopType(switch (shop.getShopType()) {
                    case BUY_ONLY -> Shop.ShopType.SELL_ONLY;
                    case SELL_ONLY -> Shop.ShopType.BUY_SELL;
                    case BUY_SELL -> Shop.ShopType.BUY_ONLY;
                });
                openManageGUI(player, areaName, shop);
            }
            case 41 -> { // View items
                player.closeInventory();
                player.performCommand("dzth " + areaName + " " + shop.getName() + " items list");
            }
            case 42 -> { // Add item
                player.closeInventory();
                plugin.getShopGUI().openAddItemGUI(player, shop, areaName);
            }
            case 48 -> { // Save
                plugin.getFileStorageManager().saveShop(areaName, shop);
                player.closeInventory();
                MessageUtil.sendSuccess(player, "Shop settings saved successfully!");
            }
            case 49 -> { // Clear queues
                plugin.getQueueManager().clearQueues(shop);
                MessageUtil.sendSuccess(player, "All queues cleared!");
                openManageGUI(player, areaName, shop);
            }
            case 50 -> { // Close
                player.closeInventory();
                MessageUtil.sendInfo(player, "Closed without saving");
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isManageGUI(String title) {
        return title.startsWith("§6§lManage: ");
    }
}
