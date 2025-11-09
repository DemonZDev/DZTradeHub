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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyGUI {
    private final DZTradeHub plugin;
    private final Map<UUID, Bounty> activeBountyCreation;
    
    public BountyGUI(DZTradeHub plugin) {
        this.plugin = plugin;
        this.activeBountyCreation = new HashMap<>();
    }
    
    /**
     * Opens main bounty browser showing all active bounties
     */
    public void openBountyBrowser(Player player) {
        var allBounties = plugin.getBountyManager().getAllBounties();
        
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lBounty Board");
        
        int slot = 0;
        for (var entry : allBounties.entrySet()) {
            if (slot >= 45) break; // Leave room for navigation
            
            for (online.demonzdevelopment.dztradehub.data.Bounty bounty : entry.getValue()) {
                if (slot >= 45) break;
                
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
                
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
                meta.setOwningPlayer(target);
                meta.setDisplayName("§c§lBounty: " + target.getName());
                
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add("§7Target: §c" + target.getName());
                lore.add("");
                lore.add("§7Total Bounties: §e" + entry.getValue().size());
                
                // Calculate total rewards
                double totalMoney = 0;
                double totalMobcoin = 0;
                double totalGem = 0;
                int totalItems = 0;
                
                for (online.demonzdevelopment.dztradehub.data.Bounty b : entry.getValue()) {
                    totalMoney += b.getMoneyReward();
                    totalMobcoin += b.getMobcoinReward();
                    totalGem += b.getGemReward();
                    totalItems += b.getRewardItems().size();
                }
                
                lore.add("§7Total Rewards:");
                if (totalMoney > 0) {
                    lore.add("  §a$" + String.format("%.2f", totalMoney));
                }
                if (totalMobcoin > 0) {
                    lore.add("  §a" + String.format("%.2f", totalMobcoin) + " MC");
                }
                if (totalGem > 0) {
                    lore.add("  §a" + String.format("%.2f", totalGem) + " G");
                }
                if (totalItems > 0) {
                    lore.add("  §a" + totalItems + " items");
                }
                lore.add("");
                lore.add("§e§lKill to claim rewards!");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
                
                inv.setItem(slot++, item);
            }
        }
        
        if (slot == 0) {
            ItemStack noB = new ItemStack(Material.BARRIER);
            ItemMeta noMeta = noB.getItemMeta();
            noMeta.setDisplayName("§c§lNo Active Bounties");
            noMeta.setLore(java.util.Arrays.asList("§7Use /bounty create <player>", "§7to place a bounty!"));
            noB.setItemMeta(noMeta);
            inv.setItem(22, noB);
        }
        
        // Exit button
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§c§lClose");
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);
        
        player.openInventory(inv);
    }

    
    public void openForTarget(Player player, UUID targetUUID, String targetName) {
        // Create new bounty
        Bounty bounty = new Bounty(targetUUID, player.getUniqueId());
        activeBountyCreation.put(player.getUniqueId(), bounty);
        
        Inventory inv = Bukkit.createInventory(null, 54, "§c§lBounty: " + targetName);
        
        // Money input
        ItemStack money = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = money.getItemMeta();
        moneyMeta.setDisplayName("§e§lMoney Reward");
        moneyMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getMoneyReward(),
            "",
            "§eLeft-Click: §a+100",
            "§eRight-Click: §a+1000",
            "§eShift-Left: §c-100",
            "§eShift-Right: §c-1000"
        ));
        money.setItemMeta(moneyMeta);
        inv.setItem(45, money);
        
        // Mobcoin input
        ItemStack mobcoin = new ItemStack(Material.EMERALD);
        ItemMeta mobcoinMeta = mobcoin.getItemMeta();
        mobcoinMeta.setDisplayName("§a§lMobcoin Reward");
        mobcoinMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getMobcoinReward(),
            "",
            "§eLeft-Click: §a+100",
            "§eRight-Click: §a+1000",
            "§eShift-Left: §c-100",
            "§eShift-Right: §c-1000"
        ));
        mobcoin.setItemMeta(mobcoinMeta);
        inv.setItem(46, mobcoin);
        
        // Gem input
        ItemStack gem = new ItemStack(Material.DIAMOND);
        ItemMeta gemMeta = gem.getItemMeta();
        gemMeta.setDisplayName("§b§lGem Reward");
        gemMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getGemReward(),
            "",
            "§eLeft-Click: §a+10",
            "§eRight-Click: §a+100",
            "§eShift-Left: §c-10",
            "§eShift-Right: §c-100"
        ));
        gem.setItemMeta(gemMeta);
        inv.setItem(47, gem);
        
        // Confirm button
        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§a§lConfirm Bounty");
        confirmMeta.setLore(Arrays.asList(
            "§7Click to place bounty",
            "",
            "§7Add items by placing them",
            "§7in the inventory slots above"
        ));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(53, confirm);
        
        // Cancel button
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§c§lCancel");
        cancel.setItemMeta(cancelMeta);
        inv.setItem(52, cancel);
        
        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lBounty Info");
        infoMeta.setLore(Arrays.asList(
            "§7Target: §e" + targetName,
            "",
            "§7Place items in the slots above",
            "§7Set currency rewards below",
            "§7Then click confirm"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);
        
        player.openInventory(inv);
    }
    
    public Bounty getActiveBounty(UUID playerUUID) {
        return activeBountyCreation.get(playerUUID);
    }
    
    public void removeActiveBounty(UUID playerUUID) {
        activeBountyCreation.remove(playerUUID);
    }
    
    /**
     * Opens main bounty GUI - /bounty
     */
    public void openMainGUI(Player player) {
        openBountyBrowser(player);
    }
    
    /**
     * Opens bounty creation GUI - /bounty create <player>
     */
    public void openCreateGUI(Player player, UUID targetUUID, String targetName) {
        openForTarget(player, targetUUID, targetName);
    }
    
    /**
     * Opens GUI showing all player's bounties - /bounty manage
     */
    public void openManageAllGUI(Player player) {
        java.util.List<Bounty> playerBounties = plugin.getBountyManager().getBountiesCreatedBy(player.getUniqueId());
        
        if (playerBounties.isEmpty()) {
            MessageUtil.sendInfo(player, "You have no bounties to manage!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lManage Your Bounties");
        
        int slot = 0;
        for (Bounty bounty : playerBounties) {
            if (slot >= 45) break; // Leave room for navigation
            
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
            
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§c§lBounty #" + bounty.getBountyNumber());
            
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Target: §e" + target.getName());
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
     * Opens management GUI for specific bounty - /bounty manage <bounty_number>
     */
    public void openManageSingleGUI(Player player, Bounty bounty) {
        @SuppressWarnings("deprecation")
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
        
        Inventory inv = Bukkit.createInventory(null, 54, "§c§lManage Bounty #" + bounty.getBountyNumber());
        
        // Money input
        ItemStack money = new ItemStack(Material.GOLD_INGOT);
        ItemMeta moneyMeta = money.getItemMeta();
        moneyMeta.setDisplayName("§e§lMoney Reward");
        moneyMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getMoneyReward(),
            "",
            "§eLeft-Click: §a+100",
            "§eRight-Click: §a+1000",
            "§eShift-Left: §c-100",
            "§eShift-Right: §c-1000"
        ));
        money.setItemMeta(moneyMeta);
        inv.setItem(45, money);
        
        // Mobcoin input
        ItemStack mobcoin = new ItemStack(Material.EMERALD);
        ItemMeta mobcoinMeta = mobcoin.getItemMeta();
        mobcoinMeta.setDisplayName("§a§lMobcoin Reward");
        mobcoinMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getMobcoinReward(),
            "",
            "§eLeft-Click: §a+100",
            "§eRight-Click: §a+1000",
            "§eShift-Left: §c-100",
            "§eShift-Right: §c-1000"
        ));
        mobcoin.setItemMeta(mobcoinMeta);
        inv.setItem(46, mobcoin);
        
        // Gem input
        ItemStack gem = new ItemStack(Material.DIAMOND);
        ItemMeta gemMeta = gem.getItemMeta();
        gemMeta.setDisplayName("§b§lGem Reward");
        gemMeta.setLore(Arrays.asList(
            "§7Current: §a" + bounty.getGemReward(),
            "",
            "§eLeft-Click: §a+10",
            "§eRight-Click: §a+100",
            "§eShift-Left: §c-10",
            "§eShift-Right: §c-100"
        ));
        gem.setItemMeta(gemMeta);
        inv.setItem(47, gem);
        
        // Show current reward items
        int itemSlot = 0;
        for (ItemStack rewardItem : bounty.getRewardItems()) {
            if (itemSlot >= 36) break;
            inv.setItem(itemSlot++, rewardItem.clone());
        }
        
        // Save changes button
        ItemStack save = new ItemStack(Material.LIME_WOOL);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName("§a§lSave Changes");
        saveMeta.setLore(Arrays.asList(
            "§7Click to update bounty",
            "",
            "§eYou can add/remove items",
            "§eand adjust currency rewards"
        ));
        save.setItemMeta(saveMeta);
        inv.setItem(53, save);
        
        // Delete bounty button
        ItemStack delete = new ItemStack(Material.RED_WOOL);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName("§c§lDelete Bounty");
        deleteMeta.setLore(Arrays.asList(
            "§7Click to delete this bounty",
            "§7Rewards will be refunded"
        ));
        delete.setItemMeta(deleteMeta);
        inv.setItem(52, delete);
        
        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lBounty Info");
        infoMeta.setLore(Arrays.asList(
            "§7Target: §e" + target.getName(),
            "§7Bounty #: §e" + bounty.getBountyNumber(),
            "",
            "§7Modify items and currencies",
            "§7Then click save"
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);
        
        player.openInventory(inv);
    }
    
    public void confirmBounty(Player player) {
        Bounty bounty = activeBountyCreation.get(player.getUniqueId());
        if (bounty == null) {
            MessageUtil.sendError(player, "No active bounty creation!");
            return;
        }
        
        // Check if has any rewards
        if (!bounty.hasRewards()) {
            MessageUtil.sendError(player, "You must add at least one reward!");
            return;
        }
        
        // Deduct currency from player
        if (bounty.getMoneyReward() > 0) {
            if (!plugin.getEconomyAPI().hasBalance(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY, bounty.getMoneyReward())) {
                MessageUtil.sendError(player, "You don't have enough money!");
                return;
            }
            plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY, bounty.getMoneyReward());
        }
        
        if (bounty.getMobcoinReward() > 0) {
            if (!plugin.getEconomyAPI().hasBalance(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN, bounty.getMobcoinReward())) {
                MessageUtil.sendError(player, "You don't have enough mobcoin!");
                return;
            }
            plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN, bounty.getMobcoinReward());
        }
        
        if (bounty.getGemReward() > 0) {
            if (!plugin.getEconomyAPI().hasBalance(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM, bounty.getGemReward())) {
                MessageUtil.sendError(player, "You don't have enough gems!");
                return;
            }
            plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM, bounty.getGemReward());
        }
        
        // Add bounty
        plugin.getBountyManager().addBounty(bounty).thenAccept(success -> {
            if (success) {
                MessageUtil.sendSuccess(player, "Bounty placed successfully!");
                player.closeInventory();
                removeActiveBounty(player.getUniqueId());
            } else {
                MessageUtil.sendError(player, "Failed to place bounty!");
            }
        });
    }
}
