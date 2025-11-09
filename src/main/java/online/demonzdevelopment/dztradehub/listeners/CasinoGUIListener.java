package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.CoinFlipRequest;
import online.demonzdevelopment.dztradehub.gui.BountyGUI;
import online.demonzdevelopment.dztradehub.gui.CoinFlipGUI;
import online.demonzdevelopment.dztradehub.gui.JackpotGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CasinoGUIListener implements Listener {
    private final DZTradeHub plugin;
    private final CoinFlipGUI coinFlipGUI;
    private final JackpotGUI jackpotGUI;
    private final BountyGUI bountyGUI;
    
    public CasinoGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
        this.coinFlipGUI = new CoinFlipGUI(plugin);
        this.jackpotGUI = new JackpotGUI(plugin);
        this.bountyGUI = plugin.getBountyGUI();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.casino")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            event.setCancelled(true);
            return;
        }
        
        String title = event.getView().getTitle();
        
        // Casino Main GUI
        if (title.equals("§6§lCasino")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.GOLD_NUGGET) {
                coinFlipGUI.open(player);
            } else if (clicked.getType() == Material.EMERALD) {
                jackpotGUI.open(player);
            }
        }
        
        // Coin Flip GUI
        else if (title.equals("§e§lCoin Flip")) {
            event.setCancelled(true);
            // Just info GUI, no action needed
        }
        
        // Coin Flip Requests GUI
        else if (title.equals("§e§lCoin Flip Requests")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PAPER) return;
            
            List<CoinFlipRequest> requests = plugin.getCasinoManager().getPendingRequests(player.getUniqueId());
            int slot = event.getSlot();
            if (slot >= 0 && slot < requests.size()) {
                CoinFlipRequest request = requests.get(slot);
                
                if (event.getClick() == ClickType.LEFT) {
                    plugin.getCasinoManager().acceptCoinFlipRequest(player, request);
                    player.closeInventory();
                } else if (event.getClick() == ClickType.RIGHT) {
                    plugin.getCasinoManager().denyCoinFlipRequest(player, request);
                    player.closeInventory();
                }
            }
        }
        
        // Jackpot GUI
        else if (title.equals("§6§lJackpot Machine")) {
            event.setCancelled(true);
            jackpotGUI.handleClick(event, player);
        }
        
        // Bounty GUI
        else if (title.startsWith("§c§lBounty:")) {
            int slot = event.getSlot();
            
            // Currency buttons
            if (slot >= 45 && slot <= 47) {
                event.setCancelled(true);
                var bounty = bountyGUI.getActiveBounty(player.getUniqueId());
                if (bounty == null) return;
                
                boolean isShift = event.getClick().isShiftClick();
                boolean isRight = event.getClick().isRightClick();
                
                switch (slot) {
                    case 45: // Money
                        double moneyChange = isRight ? (isShift ? -1000 : 1000) : (isShift ? -100 : 100);
                        bounty.setMoneyReward(Math.max(0, bounty.getMoneyReward() + moneyChange));
                        break;
                    case 46: // Mobcoin
                        double mobcoinChange = isRight ? (isShift ? -1000 : 1000) : (isShift ? -100 : 100);
                        bounty.setMobcoinReward(Math.max(0, bounty.getMobcoinReward() + mobcoinChange));
                        break;
                    case 47: // Gem
                        double gemChange = isRight ? (isShift ? -100 : 100) : (isShift ? -10 : 10);
                        bounty.setGemReward(Math.max(0, bounty.getGemReward() + gemChange));
                        break;
                }
                
                // Refresh GUI
                bountyGUI.openForTarget(player, bounty.getTargetPlayer(), 
                    event.getView().getTitle().replace("§c§lBounty: ", ""));
            }
            
            // Confirm/Cancel buttons
            else if (slot == 53) { // Confirm
                event.setCancelled(true);
                bountyGUI.confirmBounty(player);
            }
            else if (slot == 52) { // Cancel
                event.setCancelled(true);
                bountyGUI.removeActiveBounty(player.getUniqueId());
                player.closeInventory();
            }
            else if (slot == 49) { // Info
                event.setCancelled(true);
            }
            // Allow item placement in slots 0-44
            else if (slot >= 0 && slot < 45) {
                // Items can be placed/taken freely
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        
        // Bounty GUI - collect items
        if (title.startsWith("§c§lBounty:")) {
            var bounty = bountyGUI.getActiveBounty(player.getUniqueId());
            if (bounty == null) return;
            
            Inventory inv = event.getInventory();
            for (int i = 0; i < 45; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    bounty.addRewardItem(item);
                }
            }
        }
    }
}
