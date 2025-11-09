package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import online.demonzdevelopment.dztradehub.gui.ShopGUI;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopGUIListener implements Listener {
    private final DZTradeHub plugin;

    public ShopGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI.ShopHolder holder)) {
            return;
        }

        event.setCancelled(true); // Cancel all clicks by default

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.use")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            return;
        }

        // Update activity for reception queue if in one
        if (plugin.getQueueManager().isInActiveReception(player, holder.getShop())) {
            plugin.getQueueManager().updateReceptionActivity(player, holder.getShop());
        }

        int slot = event.getRawSlot();
        Shop shop = holder.getShop();
        int page = holder.getPage();

        // Handle navigation
        if (slot == 45 && page > 0) {
            // Previous page
            plugin.getShopGUI().openShop(player, shop, page - 1);
            return;
        }

        if (slot == 53) {
            // Next page
            List<ShopItem> items = shop.getItems();
            if ((page + 1) * 36 < items.size()) {
                plugin.getShopGUI().openShop(player, shop, page + 1);
            }
            return;
        }

        if (slot == 49) {
            // Close
            player.closeInventory();
            return;
        }

        // Handle item clicks (slots 9-44)
        if (slot >= 9 && slot < 45) {
            int itemIndex = page * 36 + (slot - 9);
            List<ShopItem> items = shop.getItems();
            
            if (itemIndex >= items.size()) {
                return;
            }

            ShopItem shopItem = items.get(itemIndex);
            ClickType clickType = event.getClick();

            if (clickType == ClickType.LEFT) {
                // Buy item
                handleBuy(player, shopItem);
            } else if (clickType == ClickType.RIGHT) {
                // Sell item
                handleSell(player, shopItem);
            }

            // Refresh GUI
            plugin.getShopGUI().openShop(player, shop, page);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI.ShopHolder holder)) {
            return;
        }

        if (event.getPlayer() instanceof Player player) {
            // Leave reception queue if player was in one
            if (plugin.getQueueManager().isInActiveReception(player, holder.getShop())) {
                plugin.getQueueManager().leaveReceptionQueue(player, holder.getShop());
            }
        }
    }

    private void handleBuy(Player player, ShopItem shopItem) {
        // Check if item can be bought
        if (!shopItem.canBuy()) {
            MessageUtil.sendError(player, "This item cannot be purchased!");
            return;
        }

        // Check stock
        if (shopItem.getCurrentStock() <= 0) {
            MessageUtil.sendError(player, "This item is out of stock!");
            return;
        }

        // Check if player can buy
        if (!plugin.getPermissionManager().canBuyItems(player)) {
            MessageUtil.sendError(player, "Your rank cannot purchase items!");
            return;
        }

        double price = shopItem.getBuyPrice();
        
        // Check balance
        if (!plugin.getEconomyAPI().hasBalance(
                player.getUniqueId(),
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
                price)) {
            MessageUtil.sendError(player, "You need $" + String.format("%.2f", price) + " to purchase this item!");
            MessageUtil.sendInfo(player, "Your balance: $" + 
                String.format("%.2f", plugin.getEconomyAPI().getBalance(
                    player.getUniqueId(),
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY
                )));
            return;
        }

        // Remove money
        plugin.getEconomyAPI().removeCurrency(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
            price
        );

        // Give item
        ItemStack item = shopItem.getItemStack().clone();
        player.getInventory().addItem(item);

        // Remove stock
        shopItem.removeStock(1);

        MessageUtil.sendSuccess(player, "Purchased " + item.getType().name() + " for $" + String.format("%.2f", price));
    }

    private void handleSell(Player player, ShopItem shopItem) {
        // Check if item can be sold
        if (!shopItem.canSell()) {
            MessageUtil.sendError(player, "This item cannot be sold!");
            return;
        }

        // Check if player can sell
        if (!plugin.getPermissionManager().canSellItems(player)) {
            MessageUtil.sendError(player, "Your rank cannot sell items!");
            return;
        }

        // Check if player has item
        ItemStack sellItem = shopItem.getItemStack();
        if (!player.getInventory().contains(sellItem.getType(), 1)) {
            MessageUtil.sendError(player, "You don't have this item to sell!");
            return;
        }

        double price = shopItem.getSellPrice();

        // Remove item from player
        ItemStack toRemove = new ItemStack(sellItem.getType(), 1);
        player.getInventory().removeItem(toRemove);

        // Add money
        plugin.getEconomyAPI().addCurrency(
            player.getUniqueId(),
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
            price
        );

        // Add to stock
        shopItem.addStock(1);

        MessageUtil.sendSuccess(player, "Sold " + sellItem.getType().name() + " for $" + String.format("%.2f", price));
    }
}