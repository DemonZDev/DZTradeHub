package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.CoinFlipRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CoinFlipRequestsGUI {
    private final DZTradeHub plugin;
    
    public CoinFlipRequestsGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        List<CoinFlipRequest> requests = plugin.getCasinoManager().getPendingRequests(player.getUniqueId());
        
        Inventory inv = Bukkit.createInventory(null, 54, "§e§lCoin Flip Requests");
        
        if (requests.isEmpty()) {
            ItemStack noRequests = new ItemStack(Material.BARRIER);
            ItemMeta meta = noRequests.getItemMeta();
            meta.setDisplayName("§cNo pending requests");
            noRequests.setItemMeta(meta);
            inv.setItem(22, noRequests);
        } else {
            int slot = 0;
            for (CoinFlipRequest request : requests) {
                if (slot >= 54) break;
                
                Player challenger = Bukkit.getPlayer(request.getFromPlayer());
                String challengerName = challenger != null ? challenger.getName() : "Unknown";
                
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§eRequest from §a" + challengerName);
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Amount: §e" + request.getAmount() + " " + request.getCurrencyType().toLowerCase());
                lore.add("§7Their side: §6" + request.getSide());
                lore.add("§7Your side: §6" + (request.getSide() == CoinFlipRequest.CoinSide.HEAD ? 
                         CoinFlipRequest.CoinSide.TAIL : CoinFlipRequest.CoinSide.HEAD));
                lore.add("");
                lore.add("§aLeft-Click to Accept");
                lore.add("§cRight-Click to Deny");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(slot++, item);
            }
        }
        
        player.openInventory(inv);
    }
}
