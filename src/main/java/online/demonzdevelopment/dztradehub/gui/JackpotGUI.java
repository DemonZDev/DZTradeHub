package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JackpotGUI {
    private final DZTradeHub plugin;
    private final Map<UUID, Integer> selectedRows; // Track row selection per player
    
    public JackpotGUI(DZTradeHub plugin) {
        this.plugin = plugin;
        this.selectedRows = new HashMap<>();
    }
    
    public void open(Player player) {
        openRowSelection(player);
    }
    
    /**
     * Opens row selection GUI
     */
    public void openRowSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lSelect Jackpot Rows");
        
        // 3 Row option
        ItemStack row3 = new ItemStack(Material.IRON_BLOCK, 3);
        ItemMeta row3Meta = row3.getItemMeta();
        row3Meta.setDisplayName("§e§l3 Row Jackpot");
        row3Meta.setLore(Arrays.asList(
            "§7Spin with 3 symbols",
            "",
            "§7Multipliers:",
            "§e2 matches: §a0.8x",
            "§e3 matches: §a2x",
            "",
            "§aClick to select!"
        ));
        row3.setItemMeta(row3Meta);
        inv.setItem(11, row3);
        
        // 4 Row option
        ItemStack row4 = new ItemStack(Material.GOLD_BLOCK, 4);
        ItemMeta row4Meta = row4.getItemMeta();
        row4Meta.setDisplayName("§6§l4 Row Jackpot");
        row4Meta.setLore(Arrays.asList(
            "§7Spin with 4 symbols",
            "",
            "§7Multipliers:",
            "§e2 matches: §a0.5x",
            "§e3 matches: §a1x",
            "§e4 matches: §a2x",
            "",
            "§aClick to select!"
        ));
        row4.setItemMeta(row4Meta);
        inv.setItem(13, row4);
        
        // 5 Row option
        ItemStack row5 = new ItemStack(Material.DIAMOND_BLOCK, 5);
        ItemMeta row5Meta = row5.getItemMeta();
        row5Meta.setDisplayName("§b§l5 Row Jackpot");
        row5Meta.setLore(Arrays.asList(
            "§7Spin with 5 symbols",
            "",
            "§7Multipliers:",
            "§e2 matches: §a0.4x",
            "§e3 matches: §a0.8x",
            "§e4 matches: §a1.5x",
            "§e5 matches: §a3x",
            "",
            "§aClick to select!"
        ));
        row5.setItemMeta(row5Meta);
        inv.setItem(15, row5);
        
        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lJackpot Info");
        infoMeta.setLore(Arrays.asList(
            "§7Match symbols to win!",
            "",
            "§e7️⃣ 7️⃣ 7️⃣ §7- §a10x-20x",
            "§e💎 💎 💎 §7- §a7x-15x",
            "§e⭐ ⭐ ⭐ §7- §a5x-10x",
            "",
            "§7Higher rows = Higher rewards!"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(22, info);
        
        player.openInventory(inv);
    }
    
    /**
     * Opens currency selection for specific row count
     */
    public void openCurrencySelection(Player player, int rows) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lJackpot - " + rows + " Rows");
        
        // Money bet
        ItemStack money = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = money.getItemMeta();
        moneyMeta.setDisplayName("§e§lBet Money");
        moneyMeta.setLore(Arrays.asList(
            "§7Spin " + rows + " rows with money",
            "",
            "§eLeft-Click: §a100",
            "§eRight-Click: §a1000",
            "§eShift-Left: §a10000"
        ));
        money.setItemMeta(moneyMeta);
        inv.setItem(11, money);
        
        // Mobcoin bet
        ItemStack mobcoin = new ItemStack(Material.EMERALD);
        ItemMeta mobcoinMeta = mobcoin.getItemMeta();
        mobcoinMeta.setDisplayName("§a§lBet Mobcoin");
        mobcoinMeta.setLore(Arrays.asList(
            "§7Spin " + rows + " rows with mobcoin",
            "",
            "§eLeft-Click: §a100",
            "§eRight-Click: §a1000",
            "§eShift-Left: §a10000"
        ));
        mobcoin.setItemMeta(mobcoinMeta);
        inv.setItem(13, mobcoin);
        
        // Gem bet
        ItemStack gem = new ItemStack(Material.DIAMOND);
        ItemMeta gemMeta = gem.getItemMeta();
        gemMeta.setDisplayName("§b§lBet Gem");
        gemMeta.setLore(Arrays.asList(
            "§7Spin " + rows + " rows with gem",
            "",
            "§eLeft-Click: §a10",
            "§eRight-Click: §a100",
            "§eShift-Left: §a1000"
        ));
        gem.setItemMeta(gemMeta);
        inv.setItem(15, gem);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§e§lBack to Row Selection");
        back.setItemMeta(backMeta);
        inv.setItem(22, back);
        
        player.openInventory(inv);
    }
    
    public void handleClick(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String title = event.getView().getTitle();
        
        // Handle row selection
        if (title.equals("§6§lSelect Jackpot Rows")) {
            switch (clicked.getType()) {
                case IRON_BLOCK:
                    selectedRows.put(player.getUniqueId(), 3);
                    player.closeInventory();
                    openCurrencySelection(player, 3);
                    break;
                case GOLD_BLOCK:
                    selectedRows.put(player.getUniqueId(), 4);
                    player.closeInventory();
                    openCurrencySelection(player, 4);
                    break;
                case DIAMOND_BLOCK:
                    selectedRows.put(player.getUniqueId(), 5);
                    player.closeInventory();
                    openCurrencySelection(player, 5);
                    break;
            }
            return;
        }
        
        // Handle back button
        if (clicked.getType() == Material.ARROW) {
            player.closeInventory();
            openRowSelection(player);
            return;
        }
        
        // Handle currency selection
        boolean isShift = event.getClick().isShiftClick();
        boolean isRight = event.getClick().isRightClick();
        
        double amount = 0;
        String currency = "";
        
        switch (clicked.getType()) {
            case GOLD_INGOT:
                currency = "MONEY";
                amount = isShift ? 10000 : (isRight ? 1000 : 100);
                break;
            case EMERALD:
                currency = "MOBCOIN";
                amount = isShift ? 10000 : (isRight ? 1000 : 100);
                break;
            case DIAMOND:
                currency = "GEM";
                amount = isShift ? 1000 : (isRight ? 100 : 10);
                break;
            default:
                return;
        }
        
        int rows = selectedRows.getOrDefault(player.getUniqueId(), 3);
        player.closeInventory();
        plugin.getCasinoManager().performJackpotSpin(player, currency, amount, rows);
    }
}
