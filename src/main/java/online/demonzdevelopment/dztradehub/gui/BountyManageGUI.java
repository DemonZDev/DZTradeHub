package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Bounty;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class BountyManageGUI {
    private final DZTradeHub plugin;

    public BountyManageGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens GUI showing all player's bounties for management
     */
    public void openBountyListGUI(Player player) {
        List<Bounty> bounties = plugin.getBountyManager().getBountiesCreatedBy(player.getUniqueId());
        
        if (bounties.isEmpty()) {
            MessageUtil.sendInfo(player, "You have no active bounties to manage");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§6§lManage Your Bounties");
        
        int slot = 0;
        for (Bounty bounty : bounties) {
            if (slot >= 45) break; // Leave room for navigation
            
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
            
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§e§lBounty #" + bounty.getBountyNumber());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Target: §c" + target.getName());
            lore.add("");
            lore.add("§7Rewards:");
            if (bounty.getMoneyReward() > 0) {
                lore.add("  §a$" + String.format("%.2f", bounty.getMoneyReward()));
            }
            if (bounty.getMobcoinReward() > 0) {
                lore.add("  §a" + String.format("%.2f", bounty.getMobcoinReward()) + " MC");
            }
            if (bounty.getGemReward() > 0) {
                lore.add("  §a" + String.format("%.2f", bounty.getGemReward()) + " G");
            }
            if (!bounty.getRewardItems().isEmpty()) {
                lore.add("  §a" + bounty.getRewardItems().size() + " items");
            }
            lore.add("");
            lore.add("§eClick to manage!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        // Exit button
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§c§lClose");
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);
        
        player.openInventory(inv);
    }

    /**
     * Opens management GUI for a specific bounty
     */
    public void openManageGUI(Player player, Bounty bounty) {
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
        
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lManage Bounty #" + bounty.getBountyNumber());
        
        // Target info
        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) targetHead.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName("§e§lTarget: §c" + target.getName());
        targetHead.setItemMeta(skullMeta);
        inv.setItem(4, targetHead);
        
        // Current rewards display
        ItemStack moneyDisplay = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = moneyDisplay.getItemMeta();
        moneyMeta.setDisplayName("§e§lMoney Reward");
        List<String> moneyLore = new ArrayList<>();
        moneyLore.add("§7Current: §a$" + String.format("%.2f", bounty.getMoneyReward()));
        moneyLore.add("");
        moneyLore.add("§eClick to change amount");
        moneyMeta.setLore(moneyLore);
        moneyDisplay.setItemMeta(moneyMeta);
        inv.setItem(20, moneyDisplay);
        
        ItemStack mobcoinDisplay = new ItemStack(Material.EMERALD);
        ItemMeta mobcoinMeta = mobcoinDisplay.getItemMeta();
        mobcoinMeta.setDisplayName("§a§lMobcoin Reward");
        List<String> mobcoinLore = new ArrayList<>();
        mobcoinLore.add("§7Current: §a" + String.format("%.2f", bounty.getMobcoinReward()) + " MC");
        mobcoinLore.add("");
        mobcoinLore.add("§eClick to change amount");
        mobcoinMeta.setLore(mobcoinLore);
        mobcoinDisplay.setItemMeta(mobcoinMeta);
        inv.setItem(22, mobcoinDisplay);
        
        ItemStack gemDisplay = new ItemStack(Material.DIAMOND);
        ItemMeta gemMeta = gemDisplay.getItemMeta();
        gemMeta.setDisplayName("§b§lGem Reward");
        List<String> gemLore = new ArrayList<>();
        gemLore.add("§7Current: §a" + String.format("%.2f", bounty.getGemReward()) + " G");
        gemLore.add("");
        gemLore.add("§eClick to change amount");
        gemMeta.setLore(gemLore);
        gemDisplay.setItemMeta(gemMeta);
        inv.setItem(24, gemDisplay);
        
        // Item rewards (display first 5)
        int itemSlot = 29;
        int displayCount = 0;
        for (ItemStack rewardItem : bounty.getRewardItems()) {
            if (displayCount >= 5) break;
            inv.setItem(itemSlot++, rewardItem.clone());
            displayCount++;
        }
        
        // More items indicator
        if (bounty.getRewardItems().size() > 5) {
            ItemStack moreItems = new ItemStack(Material.CHEST);
            ItemMeta moreMeta = moreItems.getItemMeta();
            moreMeta.setDisplayName("§e§l+" + (bounty.getRewardItems().size() - 5) + " more items");
            List<String> moreLore = new ArrayList<>();
            moreLore.add("§7Total items: §a" + bounty.getRewardItems().size());
            moreMeta.setLore(moreLore);
            moreItems.setItemMeta(moreMeta);
            inv.setItem(34, moreItems);
        }
        
        // Manage items button
        ItemStack manageItems = new ItemStack(Material.HOPPER);
        ItemMeta manageItemsMeta = manageItems.getItemMeta();
        manageItemsMeta.setDisplayName("§e§lManage Reward Items");
        List<String> manageItemsLore = new ArrayList<>();
        manageItemsLore.add("§7Click to add/remove items");
        manageItemsLore.add("§7Current items: §a" + bounty.getRewardItems().size());
        manageItemsMeta.setLore(manageItemsLore);
        manageItems.setItemMeta(manageItemsMeta);
        inv.setItem(40, manageItems);
        
        // Delete bounty button
        ItemStack delete = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName("§c§lDelete Bounty");
        List<String> deleteLore = new ArrayList<>();
        deleteLore.add("§7Delete this bounty");
        deleteLore.add("§7All rewards will be refunded");
        deleteLore.add("");
        deleteLore.add("§cClick to delete!");
        deleteMeta.setLore(deleteLore);
        delete.setItemMeta(deleteMeta);
        inv.setItem(48, delete);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§e§lBack to List");
        back.setItemMeta(backMeta);
        inv.setItem(45, back);
        
        // Exit button
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§c§lClose");
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);
        
        player.openInventory(inv);
    }
}
