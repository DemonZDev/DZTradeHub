package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.commands.SellCommand;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.gui.SellGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SellGUIListener implements Listener {
    private final DZTradeHub plugin;
    private final SellGUI sellGUI;
    private final SellCommand sellCommand;

    public SellGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
        this.sellGUI = new SellGUI(plugin);
        this.sellCommand = new SellCommand(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.use")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            event.setCancelled(true);
            return;
        }

        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        int slot = event.getSlot();

        // Handle Area Selection GUI
        if (inv.getHolder() instanceof SellGUI.SellAreaHolder) {
            event.setCancelled(true);
            handleAreaSelection(player, slot);
        }
        // Handle Shop Selection GUI
        else if (inv.getHolder() instanceof SellGUI.SellShopHolder holder) {
            event.setCancelled(true);
            handleShopSelection(player, holder, slot);
        }
        // Handle Sell Interface GUI
        else if (inv.getHolder() instanceof SellGUI.SellInterfaceHolder holder) {
            handleSellInterface(player, holder, event, slot);
        }
    }

    private void handleAreaSelection(Player player, int slot) {
        if (slot == 53) { // Close
            player.closeInventory();
            return;
        }

        if (slot >= 9 && slot <= 44) {
            List<Area> areas = plugin.getShopManager().getAllAreas();
            int index = slot - 9;

            if (index < areas.size()) {
                Area area = areas.get(index);
                sellGUI.openShopSelection(player, area);
            }
        }
    }

    private void handleShopSelection(Player player, SellGUI.SellShopHolder holder, int slot) {
        Area area = holder.getArea();

        if (slot == 45) { // Back
            sellGUI.openAreaSelection(player);
            return;
        }

        if (slot == 53) { // Close
            player.closeInventory();
            return;
        }

        if (slot >= 9 && slot <= 44) {
            List<Shop> shops = plugin.getShopManager().getShopsInArea(area.getName());
            int index = slot - 9;

            if (index < shops.size()) {
                Shop shop = shops.get(index);
                sellGUI.openSellInterface(player, area, shop);
            }
        }
    }

    private void handleSellInterface(Player player, SellGUI.SellInterfaceHolder holder, 
                                    InventoryClickEvent event, int slot) {
        Area area = holder.getArea();
        Shop shop = holder.getShop();

        // Control buttons (cancel interaction with these slots)
        if (slot == 4 || slot == 45 || slot == 48 || slot == 53) {
            event.setCancelled(true);

            if (slot == 45) { // Back
                player.closeInventory();
                sellGUI.openShopSelection(player, area);
            } else if (slot == 48) { // Sell All
                executeSellFromGUI(player, holder);
            } else if (slot == 53) { // Close
                returnItemsToPlayer(player, event.getInventory());
                player.closeInventory();
            }
            return;
        }

        // Allow players to place/remove items in slots 9-44
        if (slot >= 9 && slot <= 44) {
            // Allow normal interaction (placing/removing items)
            return;
        }

        // Cancel clicks on other slots
        event.setCancelled(true);
    }

    private void executeSellFromGUI(Player player, SellGUI.SellInterfaceHolder holder) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        List<ItemStack> itemsToSell = new ArrayList<>();

        // Collect items from slots 9-44
        for (int i = 9; i <= 44; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemsToSell.add(item.clone());
                inv.setItem(i, null); // Clear slot
            }
        }

        if (itemsToSell.isEmpty()) {
            player.sendMessage("§cNo items to sell!");
            return;
        }

        player.closeInventory();

        // Execute sell
        sellCommand.executeSell(player, holder.getShop(), itemsToSell, false);
    }

    private void returnItemsToPlayer(Player player, Inventory inv) {
        // Return any items left in the sell interface
        for (int i = 9; i <= 44; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Return items when closing sell interface
        if (event.getInventory().getHolder() instanceof SellGUI.SellInterfaceHolder) {
            returnItemsToPlayer(player, event.getInventory());
        }
    }
}
