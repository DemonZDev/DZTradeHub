package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.CheckoutEntry;
import online.demonzdevelopment.dztradehub.data.QueueEntry;
import online.demonzdevelopment.dztradehub.data.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Queue manager with session hijacking protection.
 * SECURITY: Session tokens prevent queue takeover on disconnect/reconnect.
 */
public class QueueManager implements Listener {
    private final DZTradeHub plugin;
    
    // Reception queues: shopId -> queue of players
    private final Map<UUID, LinkedList<QueueEntry>> receptionQueues;
    // Active reception slots: shopId -> list of active players
    private final Map<UUID, List<QueueEntry>> activeReceptionSlots;
    
    // Checkout queues: shopId -> queue of checkout entries
    private final Map<UUID, LinkedList<CheckoutEntry>> checkoutQueues;
    // Active checkout slots: shopId -> list of active checkouts
    private final Map<UUID, List<CheckoutEntry>> activeCheckoutSlots;

    public QueueManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.receptionQueues = new ConcurrentHashMap<>();
        this.activeReceptionSlots = new ConcurrentHashMap<>();
        this.checkoutQueues = new ConcurrentHashMap<>();
        this.activeCheckoutSlots = new ConcurrentHashMap<>();
        
        // Register as event listener for PlayerQuitEvent
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        startQueueProcessor();
    }
    
    /**
     * Handle player disconnect - clear all queue entries.
     * SECURITY: Prevents session hijacking when player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        clearPlayerQueues(playerId);
    }
    
    /**
     * Clear all queue entries for a player across all shops.
     * SECURITY: Called on disconnect to prevent session reuse.
     */
    private void clearPlayerQueues(UUID playerId) {
        // Clear from reception queues
        for (Map.Entry<UUID, LinkedList<QueueEntry>> entry : receptionQueues.entrySet()) {
            entry.getValue().removeIf(qe -> qe.getPlayerId().equals(playerId));
        }
        
        // Clear from active reception slots
        for (Map.Entry<UUID, List<QueueEntry>> entry : activeReceptionSlots.entrySet()) {
            boolean removed = entry.getValue().removeIf(qe -> qe.getPlayerId().equals(playerId));
            if (removed) {
                // Process queue to fill empty slot
                UUID shopId = entry.getKey();
                Shop shop = getShopById(shopId);
                if (shop != null) {
                    processReceptionQueue(shop);
                }
            }
        }
        
        // Clear from checkout queues
        for (Map.Entry<UUID, LinkedList<CheckoutEntry>> entry : checkoutQueues.entrySet()) {
            entry.getValue().removeIf(ce -> ce.getPlayerId().equals(playerId));
        }
        
        // Clear from active checkout slots
        for (Map.Entry<UUID, List<CheckoutEntry>> entry : activeCheckoutSlots.entrySet()) {
            entry.getValue().removeIf(ce -> ce.getPlayerId().equals(playerId));
        }
    }

    // ==================== RECEPTION QUEUE METHODS ====================
    
    public boolean joinReceptionQueue(Player player, Shop shop) {
        UUID shopId = shop.getId();
        
        if (!shop.isReceptionEnabled()) {
            return false;
        }
        
        // Check if already in queue or active
        if (isInReceptionQueue(player, shop) || isInActiveReception(player, shop)) {
            player.sendMessage("§cYou are already in this shop's reception!");
            return false;
        }
        
        LinkedList<QueueEntry> queue = receptionQueues.computeIfAbsent(shopId, k -> new LinkedList<>());
        List<QueueEntry> activeSlots = activeReceptionSlots.computeIfAbsent(shopId, k -> new ArrayList<>());
        
        int queueNumber = queue.size() + activeSlots.size() + 1;
        QueueEntry entry = new QueueEntry(player, queueNumber);
        
        // Check if there's an available reception slot
        if (activeSlots.size() < shop.getReceptionNumber()) {
            // Move directly to active slot
            activeSlots.add(entry);
            entry.setActive(true);
            player.sendMessage("§a✓ You entered the shop! (Reception #" + queueNumber + ")");
            player.sendMessage("§7You have " + (shop.getReceptionTimeKick() / 60) + " minutes to shop.");
            
            // Open shop GUI
            plugin.getShopGUI().openShop(player, shop);
            return true;
        } else {
            // Add to queue
            queue.add(entry);
            player.sendMessage("§e⏳ Added to reception queue at position #" + queueNumber);
            player.sendMessage("§7There are " + queue.size() + " players ahead of you.");
            return true;
        }
    }
    
    public void leaveReceptionQueue(Player player, Shop shop) {
        UUID shopId = shop.getId();
        UUID playerId = player.getUniqueId();
        
        // Remove from queue
        LinkedList<QueueEntry> queue = receptionQueues.get(shopId);
        if (queue != null) {
            queue.removeIf(entry -> entry.getPlayerId().equals(playerId));
        }
        
        // Remove from active slots
        List<QueueEntry> activeSlots = activeReceptionSlots.get(shopId);
        if (activeSlots != null) {
            activeSlots.removeIf(entry -> entry.getPlayerId().equals(playerId));
            processReceptionQueue(shop);
        }
    }
    
    public boolean isInReceptionQueue(Player player, Shop shop) {
        LinkedList<QueueEntry> queue = receptionQueues.get(shop.getId());
        return queue != null && queue.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
    }
    
    public boolean isInActiveReception(Player player, Shop shop) {
        List<QueueEntry> activeSlots = activeReceptionSlots.get(shop.getId());
        return activeSlots != null && activeSlots.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
    }
    
    public void updateReceptionActivity(Player player, Shop shop) {
        List<QueueEntry> activeSlots = activeReceptionSlots.get(shop.getId());
        if (activeSlots != null) {
            activeSlots.stream()
                .filter(e -> e.getPlayerId().equals(player.getUniqueId()))
                .forEach(QueueEntry::updateActivity);
        }
    }
    
    private void processReceptionQueue(Shop shop) {
        UUID shopId = shop.getId();
        LinkedList<QueueEntry> queue = receptionQueues.get(shopId);
        List<QueueEntry> activeSlots = activeReceptionSlots.get(shopId);
        
        if (queue == null || activeSlots == null) return;
        
        // Move players from queue to active slots if space available
        while (activeSlots.size() < shop.getReceptionNumber() && !queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            Player player = plugin.getServer().getPlayer(entry.getPlayerId());
            
            // SECURITY: Validate session before granting access
            if (player != null && player.isOnline() && entry.isValidSession(player)) {
                entry.setActive(true);
                activeSlots.add(entry);
                player.sendMessage("§a✓ It's your turn! Opening shop...");
                plugin.getShopGUI().openShop(player, shop);
            }
        }
    }
    
    public int getReceptionQueuePosition(Player player, Shop shop) {
        LinkedList<QueueEntry> queue = receptionQueues.get(shop.getId());
        if (queue == null) return -1;
        
        int pos = 1;
        for (QueueEntry entry : queue) {
            if (entry.getPlayerId().equals(player.getUniqueId())) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    // ==================== CHECKOUT QUEUE METHODS ====================
    
    public boolean joinCheckoutQueue(Player player, Shop shop, List<ItemStack> items) {
        UUID shopId = shop.getId();
        
        if (!shop.isCheckoutEnabled()) {
            return false;
        }
        
        // Check if already in checkout
        if (isInCheckoutQueue(player, shop)) {
            player.sendMessage("§cYou are already in checkout!");
            return false;
        }
        
        LinkedList<CheckoutEntry> queue = checkoutQueues.computeIfAbsent(shopId, k -> new LinkedList<>());
        List<CheckoutEntry> activeSlots = activeCheckoutSlots.computeIfAbsent(shopId, k -> new ArrayList<>());
        
        int queueNumber = queue.size() + activeSlots.size() + 1;
        CheckoutEntry entry = new CheckoutEntry(player, items, queueNumber);
        
        // Check if there's an available checkout slot
        if (activeSlots.size() < shop.getCheckoutNumber()) {
            // Move directly to checkout
            activeSlots.add(entry);
            entry.setProcessing(true);
            int totalTime = calculateCheckoutTime(shop, entry);
            player.sendMessage("§a✓ Checkout #" + queueNumber + " - Processing...");
            player.sendMessage("§7Estimated time: " + totalTime + " seconds");
            
            // Schedule checkout completion
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                completeCheckout(player, shop, entry);
            }, totalTime * 20L);
            return true;
        } else {
            // Add to checkout queue
            queue.add(entry);
            player.sendMessage("§e⏳ Added to checkout queue at position #" + queueNumber);
            player.sendMessage("§7" + queue.size() + " people ahead of you.");
            return true;
        }
    }
    
    private void completeCheckout(Player player, Shop shop, CheckoutEntry entry) {
        UUID shopId = shop.getId();
        List<CheckoutEntry> activeSlots = activeCheckoutSlots.get(shopId);
        
        if (activeSlots != null) {
            activeSlots.remove(entry);
            player.sendMessage("§a✓ Checkout complete! Enjoy your items.");
            processCheckoutQueue(shop);
        }
    }
    
    public boolean isInCheckoutQueue(Player player, Shop shop) {
        LinkedList<CheckoutEntry> queue = checkoutQueues.get(shop.getId());
        List<CheckoutEntry> activeSlots = activeCheckoutSlots.get(shop.getId());
        
        boolean inQueue = queue != null && queue.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
        boolean inActive = activeSlots != null && activeSlots.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
        
        return inQueue || inActive;
    }
    
    private void processCheckoutQueue(Shop shop) {
        UUID shopId = shop.getId();
        LinkedList<CheckoutEntry> queue = checkoutQueues.get(shopId);
        List<CheckoutEntry> activeSlots = activeCheckoutSlots.get(shopId);
        
        if (queue == null || activeSlots == null) return;
        
        // Move checkouts from queue to active slots if space available
        while (activeSlots.size() < shop.getCheckoutNumber() && !queue.isEmpty()) {
            CheckoutEntry entry = queue.poll();
            Player player = plugin.getServer().getPlayer(entry.getPlayerId());
            
            // SECURITY: Validate player is online before processing
            if (player != null && player.isOnline()) {
                entry.setProcessing(true);
                activeSlots.add(entry);
                
                int totalTime = calculateCheckoutTime(shop, entry);
                player.sendMessage("§a✓ Your turn at checkout!");
                player.sendMessage("§7Processing your " + entry.getTotalItems() + " items...");
                
                // Schedule checkout completion
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    completeCheckout(player, shop, entry);
                }, totalTime * 20L);
            }
        }
    }
    
    private int calculateCheckoutTime(Shop shop, CheckoutEntry entry) {
        return shop.getCheckoutTimeKick() * entry.getTotalItems();
    }

    // ==================== QUEUE PROCESSOR ====================
    
    private void startQueueProcessor() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            checkReceptionTimeouts();
            checkReceptionAFK();
        }, 20L, 20L); // Run every second
    }
    
    private void checkReceptionTimeouts() {
        long now = System.currentTimeMillis();
        
        for (Map.Entry<UUID, List<QueueEntry>> entry : activeReceptionSlots.entrySet()) {
            UUID shopId = entry.getKey();
            List<QueueEntry> activeSlots = entry.getValue();
            Shop shop = getShopById(shopId);
            
            if (shop == null || !shop.isReceptionEnabled()) continue;
            
            List<QueueEntry> toRemove = new ArrayList<>();
            
            for (QueueEntry queueEntry : activeSlots) {
                long sessionTime = queueEntry.getSessionTime() / 1000; // Convert to seconds
                
                if (sessionTime >= shop.getReceptionTimeKick()) {
                    Player player = plugin.getServer().getPlayer(queueEntry.getPlayerId());
                    if (player != null) {
                        player.sendMessage("§c✗ Your shopping time has expired!");
                        player.closeInventory();
                    }
                    toRemove.add(queueEntry);
                }
            }
            
            toRemove.forEach(activeSlots::remove);
            if (!toRemove.isEmpty()) {
                processReceptionQueue(shop);
            }
        }
    }
    
    private void checkReceptionAFK() {
        long now = System.currentTimeMillis();
        
        for (Map.Entry<UUID, List<QueueEntry>> entry : activeReceptionSlots.entrySet()) {
            UUID shopId = entry.getKey();
            List<QueueEntry> activeSlots = entry.getValue();
            Shop shop = getShopById(shopId);
            
            if (shop == null || !shop.isReceptionEnabled()) continue;
            
            List<QueueEntry> toRemove = new ArrayList<>();
            
            for (QueueEntry queueEntry : activeSlots) {
                long afkTime = queueEntry.getAfkTime() / 1000; // Convert to seconds
                
                if (afkTime >= shop.getReceptionAfkKick()) {
                    Player player = plugin.getServer().getPlayer(queueEntry.getPlayerId());
                    if (player != null) {
                        player.sendMessage("§c✗ Kicked from shop due to inactivity!");
                        player.closeInventory();
                    }
                    toRemove.add(queueEntry);
                }
            }
            
            toRemove.forEach(activeSlots::remove);
            if (!toRemove.isEmpty()) {
                processReceptionQueue(shop);
            }
        }
    }
    
    private Shop getShopById(UUID shopId) {
        for (String areaName : plugin.getShopManager().getAllAreas().stream()
                .map(area -> area.getName()).collect(Collectors.toList())) {
            for (Shop shop : plugin.getShopManager().getShopsInArea(areaName)) {
                if (shop.getId().equals(shopId)) {
                    return shop;
                }
            }
        }
        return null;
    }
    
    // ==================== UTILITY METHODS ====================
    
    public void clearQueues(Shop shop) {
        UUID shopId = shop.getId();
        receptionQueues.remove(shopId);
        activeReceptionSlots.remove(shopId);
        checkoutQueues.remove(shopId);
        activeCheckoutSlots.remove(shopId);
    }
    
    public Map<String, Object> getQueueStats(Shop shop) {
        Map<String, Object> stats = new HashMap<>();
        UUID shopId = shop.getId();
        
        LinkedList<QueueEntry> recQueue = receptionQueues.get(shopId);
        List<QueueEntry> recActive = activeReceptionSlots.get(shopId);
        LinkedList<CheckoutEntry> checkQueue = checkoutQueues.get(shopId);
        List<CheckoutEntry> checkActive = activeCheckoutSlots.get(shopId);
        
        stats.put("receptionQueue", recQueue != null ? recQueue.size() : 0);
        stats.put("receptionActive", recActive != null ? recActive.size() : 0);
        stats.put("checkoutQueue", checkQueue != null ? checkQueue.size() : 0);
        stats.put("checkoutActive", checkActive != null ? checkActive.size() : 0);
        
        return stats;
    }
}
