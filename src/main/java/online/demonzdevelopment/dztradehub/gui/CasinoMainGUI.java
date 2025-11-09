package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CasinoMainGUI {
    private final DZTradeHub plugin;
    
    public CasinoMainGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lCasino");
        
        // Coin Flip
        ItemStack coinFlip = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta coinMeta = coinFlip.getItemMeta();
        coinMeta.setDisplayName("§e§lCoin Flip");
        coinMeta.setLore(Arrays.asList(
            "§7Test your luck!",
            "§7Single or double player mode",
            "",
            "§eClick to play!"
        ));
        coinFlip.setItemMeta(coinMeta);
        inv.setItem(11, coinFlip);
        
        // Jackpot Machine
        ItemStack jackpot = new ItemStack(Material.EMERALD);
        ItemMeta jackpotMeta = jackpot.getItemMeta();
        jackpotMeta.setDisplayName("§a§lJackpot Machine");
        jackpotMeta.setLore(Arrays.asList(
            "§7Spin to win!",
            "§7Match 3 symbols for prizes",
            "",
            "§eClick to spin!"
        ));
        jackpot.setItemMeta(jackpotMeta);
        inv.setItem(15, jackpot);
        
        player.openInventory(inv);
    }
}
