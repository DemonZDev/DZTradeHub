package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
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
import java.util.UUID;

public class AuctionManageGUI {
    private final DZTradeHub plugin;

    public AuctionManageGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void openManageGUI(Player player, Auction auction) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lManage Auction #" + auction.getItemNumber());

        // Display the item being sold (slot 13)
        gui.setItem(13, auction.getItemStack());

        // Currency type (slot 20)
        gui.setItem(20, createGuiItem(
            getCurrencyMaterial(auction.getCurrencyType()),
            "§6Currency: §e" + auction.getCurrencyType(),
            "§7Click to cycle currencies",
            "§7Current: " + auction.getCurrencyType()
        ));

        // Current price (slot 21)
        gui.setItem(21, createGuiItem(Material.EMERALD, "§aActual Price: §f" + auction.getActualPrice(),
            "§7Left: +100", "§7Right: +10", "§7Shift+Left: +1000", "§7Shift+Right: -100"));

        // Min drop price (slot 22)
        gui.setItem(22, createGuiItem(Material.REDSTONE, "§cMin Drop Price: §f" + auction.getMaxDropPrice(),
            "§7Left: +50", "§7Right: +5", "§7Shift+Left: +500", "§7Shift+Right: -50"));

        // Drop per interval (slot 23)
        gui.setItem(23, createGuiItem(Material.ARROW, "§eDrop Per Interval: §f" + auction.getDropPerUnit(),
            "§7Left: +10", "§7Right: +1", "§7Shift+Left: +50", "§7Shift+Right: -10"));

        // Drop interval (slot 29)
        gui.setItem(29, createGuiItem(Material.CLOCK, "§bDrop Interval: §f" + auction.getDropIntervalHours() + "h",
            "§7Left: +1h", "§7Right: +12h", "§7Shift+Left: +24h", "§7Shift+Right: -1h"));

        // Queue system (slot 30)
        gui.setItem(30, createGuiItem(Material.PAPER, "§dMax Queue: §f" + auction.getMaxQueue(),
            "§7Current queue size: " + auction.getQueueSize(),
            "§7Left: +1", "§7Right: +5", "§7Shift+Left: +10", "§7Shift+Right: -1"));

        // Price increase (slot 31)
        gui.setItem(31, createGuiItem(Material.GOLD_BLOCK, "§6Price Increase: §f" + auction.getPriceIncreasePercent() + "%",
            "§7Left: +5%", "§7Right: +1%", "§7Shift+Left: +10%", "§7Shift+Right: -5%"));

        // Freeze/Unfreeze toggle (slot 32)
        Material freezeMaterial = auction.isFrozen() ? Material.BLUE_ICE : Material.PACKED_ICE;
        String freezeStatus = auction.isFrozen() ? "§c[FROZEN]" : "§a[ACTIVE]";
        gui.setItem(32, createGuiItem(freezeMaterial, "§bFreeze Status: " + freezeStatus,
            "§7Click to toggle", "§7Frozen auctions don't drop price"));

        // Current stats (slot 40)
        gui.setItem(40, createGuiItem(Material.BOOK, "§e§lAuction Statistics",
            "§7Current Price: §a" + String.format("%.2f", auction.getCurrentPrice()),
            "§7Original Price: §f" + auction.getActualPrice(),
            "§7Queue Size: §e" + auction.getQueueSize() + "/" + auction.getMaxQueue(),
            "§7Status: " + (auction.isFrozen() ? "§cFrozen" : "§aActive"),
            "§7Created: " + formatTime(auction.getCreatedTime())));

        // Queue viewer (slot 41)
        if (auction.getMaxQueue() > 0 && auction.getQueueSize() > 0) {
            List<String> queueInfo = new ArrayList<>();
            queueInfo.add("§7Queue Information:");
            queueInfo.add("");
            int pos = 1;
            for (Auction.QueueEntry entry : auction.getQueue()) {
                Player queuedPlayer = plugin.getServer().getPlayer(entry.playerUUID());
                String playerName = queuedPlayer != null ? queuedPlayer.getName() : "Unknown";
                queueInfo.add("§e" + pos + ". §f" + playerName + " §7- §a" + String.format("%.2f", entry.paidPrice()));
                pos++;
                if (pos > 10) break; // Show max 10 entries
            }
            gui.setItem(41, createGuiItem(Material.PLAYER_HEAD, "§6§lQueue Viewer",
                queueInfo.toArray(new String[0])));
        }

        // Save changes (slot 48)
        gui.setItem(48, createGuiItem(Material.LIME_CONCRETE, "§a§lSave Changes",
            "§7Click to save and close"));

        // Cancel/Remove auction (slot 49)
        gui.setItem(49, createGuiItem(Material.RED_CONCRETE, "§c§lRemove Auction",
            "§7Click to cancel auction",
            "§7Item will be returned",
            "§7All buyers will be refunded"));

        // Close without saving (slot 50)
        gui.setItem(50, createGuiItem(Material.BARRIER, "§c§lClose",
            "§7Close without saving changes"));

        player.openInventory(gui);
    }

    public void handleClick(Player player, Auction auction, int slot, boolean leftClick, boolean shift) {
        switch (slot) {
            case 20 -> { // Currency
                String current = auction.getCurrencyType();
                String newCurrency = switch (current) {
                    case "MONEY" -> "MOBCOIN";
                    case "MOBCOIN" -> "GEM";
                    default -> "MONEY";
                };
                auction.setCurrencyType(newCurrency);
                openManageGUI(player, auction);
            }
            case 21 -> { // Actual price
                if (shift && leftClick) auction.setActualPrice(auction.getActualPrice() + 1000);
                else if (shift) auction.setActualPrice(Math.max(1, auction.getActualPrice() - 100));
                else if (leftClick) auction.setActualPrice(auction.getActualPrice() + 100);
                else auction.setActualPrice(auction.getActualPrice() + 10);
                openManageGUI(player, auction);
            }
            case 22 -> { // Max drop price
                if (shift && leftClick) auction.setMaxDropPrice(auction.getMaxDropPrice() + 500);
                else if (shift) auction.setMaxDropPrice(Math.max(0.1, auction.getMaxDropPrice() - 50));
                else if (leftClick) auction.setMaxDropPrice(auction.getMaxDropPrice() + 50);
                else auction.setMaxDropPrice(auction.getMaxDropPrice() + 5);
                openManageGUI(player, auction);
            }
            case 23 -> { // Drop per unit
                if (shift && leftClick) auction.setDropPerUnit(auction.getDropPerUnit() + 50);
                else if (shift) auction.setDropPerUnit(Math.max(0, auction.getDropPerUnit() - 10));
                else if (leftClick) auction.setDropPerUnit(auction.getDropPerUnit() + 10);
                else auction.setDropPerUnit(auction.getDropPerUnit() + 1);
                openManageGUI(player, auction);
            }
            case 29 -> { // Drop interval
                if (shift && leftClick) auction.setDropIntervalHours(auction.getDropIntervalHours() + 24);
                else if (shift) auction.setDropIntervalHours(Math.max(1, auction.getDropIntervalHours() - 1));
                else if (leftClick) auction.setDropIntervalHours(auction.getDropIntervalHours() + 1);
                else auction.setDropIntervalHours(auction.getDropIntervalHours() + 12);
                openManageGUI(player, auction);
            }
            case 32 -> { // Freeze toggle
                auction.setFrozen(!auction.isFrozen());
                openManageGUI(player, auction);
                MessageUtil.sendInfo(player, "Auction " + (auction.isFrozen() ? "frozen" : "resumed"));
            }
            case 48 -> { // Save
                plugin.getFileStorageManager().saveAuction(auction);
                player.closeInventory();
                MessageUtil.sendSuccess(player, "Changes saved successfully!");
            }
            case 49 -> { // Remove
                plugin.getAuctionManager().cancelAuction(auction.getId());
                player.getInventory().addItem(auction.getItemStack());
                player.closeInventory();
                MessageUtil.sendSuccess(player, "Auction cancelled and item returned");
            }
            case 50 -> { // Close
                player.closeInventory();
                MessageUtil.sendInfo(player, "Closed without saving");
            }
        }
    }

    private Material getCurrencyMaterial(String currency) {
        return switch (currency.toUpperCase()) {
            case "MOBCOIN" -> Material.GHAST_TEAR;
            case "GEM" -> Material.EMERALD;
            default -> Material.GOLD_INGOT;
        };
    }

    private String formatTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long hours = diff / (1000 * 60 * 60);
        long days = hours / 24;
        
        if (days > 0) return days + " days ago";
        if (hours > 0) return hours + " hours ago";
        return "Less than an hour ago";
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
        return title.startsWith("§6§lManage Auction #");
    }
}
