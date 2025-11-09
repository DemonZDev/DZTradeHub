package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CoinFlipGUI {
    private final DZTradeHub plugin;
    
    public CoinFlipGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§e§lCoin Flip");
        
        // Single Player
        ItemStack single = new ItemStack(Material.GOLD_INGOT);
        ItemMeta singleMeta = single.getItemMeta();
        singleMeta.setDisplayName("§e§lSingle Player");
        singleMeta.setLore(Arrays.asList(
            "§7Play against the house",
            "§7Win 2x your bet!",
            "",
            "§aClick to open!",
            "§7Or use: /coinflip single"
        ));
        single.setItemMeta(singleMeta);
        inv.setItem(11, single);
        
        // Double Player
        ItemStack doublePlayer = new ItemStack(Material.DIAMOND);
        ItemMeta doubleMeta = doublePlayer.getItemMeta();
        doubleMeta.setDisplayName("§b§lDouble Player");
        doubleMeta.setLore(Arrays.asList(
            "§7Challenge another player",
            "§7Winner takes all!",
            "",
            "§aClick to open!",
            "§7Or use: /coinflip double"
        ));
        doublePlayer.setItemMeta(doubleMeta);
        inv.setItem(15, doublePlayer);
        
        player.openInventory(inv);
    }
    
    public void openSingleMode(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§e§lSingle Coin Flip");
        
        player.sendMessage("§6§l[Coin Flip]");
        player.sendMessage("§eSelect your options:");
        player.sendMessage("§e1. Choose currency: §aMONEY §7| §bMOBCOIN §7| §dGEM");
        player.sendMessage("§e2. Enter amount in chat");
        player.sendMessage("§e3. Choose side: §6HEADS §7or §6TAILS");
        player.sendMessage("");
        player.sendMessage("§7Or use command: §e/coinflip single <currency> <amount> <heads|tails>");
        
        player.closeInventory();
    }
    
    public void openDoubleMode(Player player) {
        player.sendMessage("§6§l[Double Coin Flip]");
        player.sendMessage("§eChallenge another player:");
        player.sendMessage("§e1. Choose currency: §aMONEY §7| §bMOBCOIN §7| §dGEM");
        player.sendMessage("§e2. Enter amount");
        player.sendMessage("§e3. Choose your side: §6HEADS §7or §6TAILS");
        player.sendMessage("§e4. Enter player name");
        player.sendMessage("");
        player.sendMessage("§7Use command: §e/coinflip double <currency> <amount> <heads|tails> <player>");
        player.sendMessage("§7Example: §e/coinflip double money 1000 heads Steve");
        
        player.closeInventory();
    }
}
