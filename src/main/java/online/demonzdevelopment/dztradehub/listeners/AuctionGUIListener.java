package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
import online.demonzdevelopment.dztradehub.gui.AuctionBrowserGUI;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class AuctionGUIListener implements Listener {
    private final DZTradeHub plugin;

    public AuctionGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.auction")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            event.setCancelled(true);
            return;
        }

        String title = event.getView().getTitle();
        
        // Handle AuctionAddGUI
        if (plugin.getAuctionBrowserGUI().getAuctionAddGUI().isAuctionAddGUI(title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            boolean leftClick = event.getClick() == ClickType.LEFT;
            boolean shift = event.getClick().isShiftClick();
            
            if (slot < event.getInventory().getSize()) {
                plugin.getAuctionBrowserGUI().getAuctionAddGUI().handleClick(player, slot, leftClick, shift);
            }
            return;
        }

        if (!(event.getInventory().getHolder() instanceof AuctionBrowserGUI.AuctionHolder holder)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        int page = holder.getPage();

        // Handle navigation
        if (slot == 45 && page > 0) {
            // Previous page
            plugin.getAuctionBrowserGUI().openBrowser(player, page - 1);
            return;
        }

        if (slot == 53) {
            // Next page
            List<Auction> auctions = plugin.getAuctionManager().getAllAuctions();
            if ((page + 1) * 36 < auctions.size()) {
                plugin.getAuctionBrowserGUI().openBrowser(player, page + 1);
            }
            return;
        }

        if (slot == 49) {
            // My Auctions - show player's auctions
            player.closeInventory();
            List<Auction> myAuctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId());
            MessageUtil.sendInfo(player, "You have " + myAuctions.size() + " active auctions.");
            return;
        }

        // Handle auction clicks (slots 9-44)
        if (slot >= 9 && slot < 45) {
            int auctionIndex = page * 36 + (slot - 9);
            List<Auction> auctions = plugin.getAuctionManager().getAllAuctions();
            
            if (auctionIndex >= auctions.size()) {
                return;
            }

            Auction auction = auctions.get(auctionIndex);
            handleAuctionPurchase(player, auction);
        }
    }

    private void handleAuctionPurchase(Player player, Auction auction) {
        // Prevent buying own auction
        if (auction.getSellerUUID().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "You cannot buy your own auction!");
            return;
        }

        // Check if frozen
        if (auction.isFrozen()) {
            MessageUtil.sendError(player, "This auction is currently frozen!");
            return;
        }

        double currentPrice = auction.getCurrentPrice();

        // Check balance
        if (!plugin.getEconomyAPI().hasBalance(
                player.getUniqueId(),
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
                currentPrice)) {
            MessageUtil.sendError(player, "You need $" + String.format("%.2f", currentPrice) + " to purchase this auction!");
            return;
        }

        // Deduct money
        plugin.getEconomyAPI().removeCurrency(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
            currentPrice
        );

        // Handle purchase through manager
        plugin.getAuctionManager().purchaseAuction(player, auction);

        // Close and refresh
        player.closeInventory();
        MessageUtil.sendSuccess(player, "Purchase successful!");
    }
}