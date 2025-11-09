package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.gui.AreaGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AreaGUIListener implements Listener {
    private final DZTradeHub plugin;

    public AreaGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof AreaGUI.AreaHolder holder)) return;

        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.use")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        int slot = event.getRawSlot();
        var area = holder.getArea();
        int currentPage = holder.getPage();

        // Navigation buttons
        if (slot == 45) {
            // Previous page
            if (currentPage > 0) {
                plugin.getAreaGUI().openArea(player, area, currentPage - 1);
            }
            return;
        }

        if (slot == 53) {
            // Next page
            List<Shop> shops = area.getShops();
            int totalPages = (shops.size() - 1) / 36 + 1;
            if (currentPage < totalPages - 1) {
                plugin.getAreaGUI().openArea(player, area, currentPage + 1);
            }
            return;
        }

        if (slot == 49) {
            // Exit button
            player.closeInventory();
            return;
        }

        // Balance displays (46, 47, 48) - no action
        if (slot == 46 || slot == 47 || slot == 48) {
            return;
        }

        // Shop selection (slots 9-44)
        if (slot >= 9 && slot <= 44) {
            int shopIndex = (currentPage * 36) + (slot - 9);
            List<Shop> shops = area.getShops().stream()
                .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                .toList();

            if (shopIndex >= 0 && shopIndex < shops.size()) {
                Shop shop = shops.get(shopIndex);
                
                // Check if shop has reception queue system
                if (shop.isReceptionEnabled()) {
                    // Try to join reception queue
                    boolean joined = plugin.getQueueManager().joinReceptionQueue(player, shop);
                    if (joined) {
                        player.closeInventory();
                    }
                } else if (shop.isCheckoutEnabled()) {
                    // For checkout shops, directly open shop (checkout happens after)
                    player.closeInventory();
                    plugin.getShopGUI().openShop(player, shop);
                } else {
                    // No queue system, direct access
                    player.closeInventory();
                    plugin.getShopGUI().openShop(player, shop);
                }
            }
        }
    }
}
