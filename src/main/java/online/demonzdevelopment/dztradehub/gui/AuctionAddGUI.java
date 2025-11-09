package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
import online.demonzdevelopment.dztradehub.data.RankData;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuctionAddGUI {
    private final DZTradeHub plugin;
    private final Map<UUID, AuctionCreationData> creationData;

    public AuctionAddGUI(DZTradeHub plugin) {
        this.plugin = plugin;
        this.creationData = new HashMap<>();
    }

    public void openAddGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lCreate Auction");

        // Get or create creation data
        AuctionCreationData data = creationData.computeIfAbsent(player.getUniqueId(), k -> new AuctionCreationData());

        // Item slot (middle top)
        if (data.itemStack == null) {
            gui.setItem(13, createGuiItem(Material.CHEST, "§e§lPlace Item Here", 
                "§7Click to place the item", "§7you want to auction"));
        } else {
            gui.setItem(13, data.itemStack);
        }

        // Currency selection
        gui.setItem(20, createGuiItem(
            data.currencyType.equals("MONEY") ? Material.GOLD_INGOT : Material.IRON_INGOT,
            "§6Currency: §e" + data.currencyType,
            "§7Click to change currency",
            "§7Options: MONEY, MOBCOIN, GEM"
        ));

        // Starting price
        gui.setItem(21, createGuiItem(Material.EMERALD, "§aStarting Price: §f" + data.actualPrice,
            "§7Left click: +100", "§7Right click: +10", "§7Shift+Left: +1000", "§7Shift+Right: -100"));

        // Min drop price
        gui.setItem(22, createGuiItem(Material.REDSTONE, "§cMin Drop Price: §f" + data.maxDropPrice,
            "§7Left click: +50", "§7Right click: +5", "§7Shift+Left: +500", "§7Shift+Right: -50"));

        // Drop per interval
        gui.setItem(23, createGuiItem(Material.ARROW, "§eDrop Per Interval: §f" + data.dropPerUnit,
            "§7Left click: +10", "§7Right click: +1", "§7Shift+Left: +50", "§7Shift+Right: -10"));

        // Drop interval
        gui.setItem(29, createGuiItem(Material.CLOCK, "§bDrop Interval: §f" + data.dropIntervalHours + "h",
            "§7Left click: +1h", "§7Right click: +12h", "§7Shift+Left: +24h", "§7Shift+Right: -1h"));

        // Queue system
        gui.setItem(30, createGuiItem(Material.PAPER, "§dMax Queue: §f" + data.maxQueue,
            "§7Left click: +1", "§7Right click: +5", "§7Shift+Left: +10", "§7Shift+Right: -1",
            "§70 = Price reduction auction"));

        // Price increase
        gui.setItem(31, createGuiItem(Material.GOLD_BLOCK, "§6Price Increase: §f" + data.priceIncreasePercent + "%",
            "§7Left click: +5%", "§7Right click: +1%", "§7Shift+Left: +10%", "§7Shift+Right: -5%"));

        // Confirm button
        gui.setItem(48, createGuiItem(Material.LIME_CONCRETE, "§a§lConfirm & Create Auction",
            "§7Click to create auction", "§7Fee: $" + getListingFee(player)));

        // Cancel button
        gui.setItem(50, createGuiItem(Material.RED_CONCRETE, "§c§lCancel",
            "§7Click to cancel"));

        // Info
        gui.setItem(49, createGuiItem(Material.BOOK, "§e§lAuction Info",
            "§7Your auctions: §f" + plugin.getAuctionManager().getPlayerListingCount(player.getUniqueId()),
            "§7Max auctions: §f" + getMaxListings(player),
            "§7Listing fee: §f$" + getListingFee(player)));

        player.openInventory(gui);
    }

    public void handleClick(Player player, int slot, boolean leftClick, boolean shift) {
        AuctionCreationData data = creationData.get(player.getUniqueId());
        if (data == null) return;

        switch (slot) {
            case 13 -> { // Item slot
                ItemStack cursor = player.getItemOnCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    data.itemStack = cursor.clone();
                    player.setItemOnCursor(null);
                    openAddGUI(player);
                }
            }
            case 20 -> { // Currency
                data.currencyType = switch (data.currencyType) {
                    case "MONEY" -> "MOBCOIN";
                    case "MOBCOIN" -> "GEM";
                    default -> "MONEY";
                };
                openAddGUI(player);
            }
            case 21 -> { // Starting price
                if (shift && leftClick) data.actualPrice += 1000;
                else if (shift) data.actualPrice = Math.max(1, data.actualPrice - 100);
                else if (leftClick) data.actualPrice += 100;
                else data.actualPrice += 10;
                openAddGUI(player);
            }
            case 22 -> { // Min drop price
                if (shift && leftClick) data.maxDropPrice += 500;
                else if (shift) data.maxDropPrice = Math.max(0.1, data.maxDropPrice - 50);
                else if (leftClick) data.maxDropPrice += 50;
                else data.maxDropPrice += 5;
                openAddGUI(player);
            }
            case 23 -> { // Drop per unit
                if (shift && leftClick) data.dropPerUnit += 50;
                else if (shift) data.dropPerUnit = Math.max(0, data.dropPerUnit - 10);
                else if (leftClick) data.dropPerUnit += 10;
                else data.dropPerUnit += 1;
                openAddGUI(player);
            }
            case 29 -> { // Drop interval
                if (shift && leftClick) data.dropIntervalHours += 24;
                else if (shift) data.dropIntervalHours = Math.max(1, data.dropIntervalHours - 1);
                else if (leftClick) data.dropIntervalHours += 1;
                else data.dropIntervalHours += 12;
                openAddGUI(player);
            }
            case 30 -> { // Max queue
                if (shift && leftClick) data.maxQueue += 10;
                else if (shift) data.maxQueue = Math.max(0, data.maxQueue - 1);
                else if (leftClick) data.maxQueue += 1;
                else data.maxQueue += 5;
                openAddGUI(player);
            }
            case 31 -> { // Price increase
                if (shift && leftClick) data.priceIncreasePercent += 10;
                else if (shift) data.priceIncreasePercent = Math.max(0, data.priceIncreasePercent - 5);
                else if (leftClick) data.priceIncreasePercent += 5;
                else data.priceIncreasePercent += 1;
                openAddGUI(player);
            }
            case 48 -> { // Confirm
                createAuction(player, data);
            }
            case 50 -> { // Cancel
                player.closeInventory();
                creationData.remove(player.getUniqueId());
                MessageUtil.sendInfo(player, "Auction creation cancelled");
            }
        }
    }

    private void createAuction(Player player, AuctionCreationData data) {
        // Validate
        if (data.itemStack == null) {
            MessageUtil.sendError(player, "Please place an item first!");
            return;
        }

        // Check listing limit
        RankData rank = plugin.getPermissionManager().getPlayerRank(player);
        int currentListings = plugin.getAuctionManager().getPlayerListingCount(player.getUniqueId());
        
        if (currentListings >= rank.auctionSettings().maxListings()) {
            MessageUtil.sendError(player, "Listing limit reached!");
            return;
        }

        // Charge listing fee
        double listingFee = rank.auctionSettings().listingFee();
        if (!plugin.getEconomyAPI().hasBalance(
                player.getUniqueId(),
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
                listingFee)) {
            MessageUtil.sendError(player, "You need $" + listingFee + " listing fee!");
            return;
        }

        plugin.getEconomyAPI().removeCurrency(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
            listingFee
        );

        // Create auction
        Auction auction = new Auction(
            player.getUniqueId(),
            data.itemStack,
            data.currencyType,
            data.actualPrice,
            data.maxDropPrice,
            data.dropPerUnit,
            data.dropIntervalHours,
            data.maxQueue,
            data.priceIncreasePercent
        );

        plugin.getAuctionManager().createAuction(auction);
        
        player.closeInventory();
        creationData.remove(player.getUniqueId());
        
        MessageUtil.sendSuccess(player, "Auction #" + auction.getItemNumber() + " created successfully!");
        MessageUtil.sendInfo(player, "Type: " + (data.maxQueue > 0 ? "Queue Bidding" : "Price Reduction"));
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

    private double getListingFee(Player player) {
        RankData rank = plugin.getPermissionManager().getPlayerRank(player);
        return rank.auctionSettings().listingFee();
    }

    private int getMaxListings(Player player) {
        RankData rank = plugin.getPermissionManager().getPlayerRank(player);
        return rank.auctionSettings().maxListings();
    }

    public boolean isAuctionAddGUI(String title) {
        return title.equals("§6§lCreate Auction");
    }

    private static class AuctionCreationData {
        ItemStack itemStack = null;
        String currencyType = "MONEY";
        double actualPrice = 100.0;
        double maxDropPrice = 50.0;
        double dropPerUnit = 10.0;
        int dropIntervalHours = 2;
        int maxQueue = 0;
        double priceIncreasePercent = 20.0;
    }
}
