package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KitManager {
    private final DZTradeHub plugin;
    private final Map<String, Kit> kits;
    private final Map<String, KitLinkData> kitLinks; // kit name -> shop link

    public KitManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.kitLinks = new HashMap<>();
        loadKits();
    }

    public void loadKits() {
        kits.clear();
        kits.putAll(plugin.getConfigManager().loadKits());
        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public List<Kit> getAllKits() {
        return new ArrayList<>(kits.values());
    }

    /**
     * Check if player can claim kit (sync version for GUI)
     */
    public boolean canClaimKit(Player player, Kit kit) {
        try {
            return canClaimKitAsync(player, kit).get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if player can claim kit (async)
     */
    public CompletableFuture<Boolean> canClaimKitAsync(Player player, Kit kit) {
        return plugin.getDatabaseManager().getKitCooldownAsync(player.getUniqueId(), kit.getName())
            .thenApply(lastClaim -> {
                if (lastClaim == 0) return true;
                long elapsed = System.currentTimeMillis() - lastClaim;
                return elapsed >= (kit.getCooldown() * 1000);
            });
    }

    /**
     * Get remaining cooldown (sync for GUI)
     */
    public long getRemainingCooldown(UUID playerUUID, String kitName) {
        try {
            return getRemainingCooldownAsync(playerUUID, kitName).get();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Get remaining cooldown (async)
     */
    public CompletableFuture<Long> getRemainingCooldownAsync(UUID playerUUID, String kitName) {
        return plugin.getDatabaseManager().getKitCooldownAsync(playerUUID, kitName)
            .thenApply(lastClaim -> {
                if (lastClaim == 0) return 0L;
                long elapsed = System.currentTimeMillis() - lastClaim;
                Kit kit = kits.get(kitName);
                if (kit == null) return 0L;
                long remaining = (kit.getCooldown() * 1000) - elapsed;
                return Math.max(0, remaining); // Return milliseconds
            });
    }

    /**
     * Claim a kit
     */
    public boolean claimKit(Player player, Kit kit) {
        // Check cooldown
        if (!canClaimKit(player, kit)) {
            return false;
        }

        // Give kit
        giveKit(player, kit);
        return true;
    }

    public void giveKit(Player player, Kit kit) {
        // Give items
        for (ItemStack item : kit.getItems()) {
            if (item != null) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                // Drop items that don't fit
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
        }

        // Execute commands
        kit.getCommands().forEach(cmd -> {
            String command = cmd.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        });

        // Set cooldown
        plugin.getDatabaseManager().setKitCooldownAsync(
            player.getUniqueId(), 
            kit.getName(), 
            System.currentTimeMillis()
        );
    }

    /**
     * Create a new kit
     */
    public boolean createKit(Kit kit) {
        if (kits.containsKey(kit.getName())) {
            return false;
        }
        
        kits.put(kit.getName(), kit);
        plugin.getConfigManager().saveKit(kit);
        return true;
    }

    /**
     * Delete a kit
     */
    public boolean deleteKit(String kitName) {
        if (!kits.containsKey(kitName)) {
            return false;
        }
        
        kits.remove(kitName);
        kitLinks.remove(kitName);
        plugin.getConfigManager().deleteKit(kitName);
        return true;
    }

    /**
     * Link kit to shop
     */
    public boolean linkKitToShop(String kitName, String areaName, String shopName) {
        Kit kit = kits.get(kitName);
        if (kit == null) {
            return false;
        }
        
        // Check if shop exists
        if (plugin.getShopManager().getShop(areaName, shopName) == null) {
            return false;
        }
        
        kitLinks.put(kitName, new KitLinkData(areaName, shopName));
        plugin.getConfigManager().saveKitLink(kitName, areaName, shopName);
        return true;
    }

    /**
     * Unlink kit from shop
     */
    public boolean unlinkKit(String kitName) {
        if (!kitLinks.containsKey(kitName)) {
            return false;
        }
        
        kitLinks.remove(kitName);
        plugin.getConfigManager().deleteKitLink(kitName);
        return true;
    }

    /**
     * Get shop link for kit
     */
    public KitLinkData getKitLink(String kitName) {
        return kitLinks.get(kitName);
    }

    /**
     * Check if kit is linked
     */
    public boolean isKitLinked(String kitName) {
        return kitLinks.containsKey(kitName);
    }

    /**
     * Kit link data
     */
    public static class KitLinkData {
        private final String areaName;
        private final String shopName;
        
        public KitLinkData(String areaName, String shopName) {
            this.areaName = areaName;
            this.shopName = shopName;
        }
        
        public String getAreaName() { return areaName; }
        public String getShopName() { return shopName; }
    }
}