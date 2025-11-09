package online.demonzdevelopment.dztradehub.gui;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Kit;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitsGUI {
    private final DZTradeHub plugin;
    
    public KitsGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        List<Kit> kits = plugin.getKitManager().getAllKits();
        
        int size = ((kits.size() + 8) / 9) * 9;
        size = Math.min(54, Math.max(9, size));
        
        Inventory inv = Bukkit.createInventory(null, size, "§6§lAvailable Kits");
        
        int slot = 0;
        for (Kit kit : kits) {
            if (slot >= size) break;
            
            ItemStack item = new ItemStack(kit.getIconMaterial());
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName("§e§l" + kit.getDisplayName());
            
            List<String> lore = new ArrayList<>(kit.getDescription());
            lore.add("");
            lore.add("§7Price: §a" + kit.getPrice());
            lore.add("§7Cooldown: §a" + formatCooldown(kit.getCooldown()));
            
            // Check if player can claim
            boolean canClaim = plugin.getKitManager().canClaimKit(player, kit);
            if (!canClaim) {
                lore.add("");
                lore.add("§c§lON COOLDOWN");
                long remaining = plugin.getKitManager().getRemainingCooldown(player.getUniqueId(), kit.getName());
                lore.add("§7Remaining: §c" + formatTime(remaining));
            } else {
                lore.add("");
                lore.add("§a» Click to claim");
            }
            
            // Check permission
            if (kit.getPermission() != null && !kit.getPermission().isEmpty()) {
                if (!player.hasPermission(kit.getPermission())) {
                    lore.add("");
                    lore.add("§c§lNO PERMISSION");
                }
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inv.setItem(slot++, item);
        }
        
        player.openInventory(inv);
    }
    
    public void handleClick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        if (displayName == null) return;
        
        // Find kit by display name
        Kit kit = null;
        for (Kit k : plugin.getKitManager().getAllKits()) {
            if (("§e§l" + k.getDisplayName()).equals(displayName)) {
                kit = k;
                break;
            }
        }
        
        if (kit == null) return;
        
        // Check if can claim
        if (!plugin.getKitManager().canClaimKit(player, kit)) {
            long remaining = plugin.getKitManager().getRemainingCooldown(player.getUniqueId(), kit.getName());
            MessageUtil.sendError(player, "This kit is on cooldown!");
            player.sendMessage("§7Remaining: " + formatTime(remaining));
            player.closeInventory();
            return;
        }
        
        // Check permission
        if (kit.getPermission() != null && !kit.getPermission().isEmpty()) {
            if (!player.hasPermission(kit.getPermission())) {
                MessageUtil.sendError(player, "You don't have permission to claim this kit!");
                player.closeInventory();
                return;
            }
        }
        
        // Claim kit
        if (plugin.getKitManager().claimKit(player, kit)) {
            MessageUtil.sendSuccess(player, "Claimed kit: " + kit.getDisplayName());
            player.closeInventory();
        } else {
            MessageUtil.sendError(player, "Failed to claim kit! Your inventory may be full.");
        }
    }
    
    private String formatCooldown(long seconds) {
        if (seconds == 0) return "None";
        return formatTime(seconds);
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m ";
        } else {
            return seconds + "s";
        }
    }
}
