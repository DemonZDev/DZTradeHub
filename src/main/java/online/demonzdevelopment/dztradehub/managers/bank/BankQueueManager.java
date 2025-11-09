package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.QueueEntry;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bank queue manager with session hijacking protection.
 * SECURITY: Session tokens prevent queue takeover on disconnect/reconnect.
 */
public class BankQueueManager implements Listener {
    private final DZTradeHub plugin;
    private final BankManager bankManager;
    
    // Bank reception queues: bankId -> queue of players
    private final Map<UUID, LinkedList<QueueEntry>> bankQueues;
    // Active bank slots: bankId -> list of active players
    private final Map<UUID, List<QueueEntry>> activeBankSlots;
    
    public BankQueueManager(DZTradeHub plugin, BankManager bankManager) {
        this.plugin = plugin;
        this.bankManager = bankManager;
        this.bankQueues = new ConcurrentHashMap<>();
        this.activeBankSlots = new ConcurrentHashMap<>();
        
        // Register as event listener for PlayerQuitEvent
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        startQueueProcessor();
    }
    
    /**
     * Handle player disconnect - clear all bank queue entries.
     * SECURITY: Prevents session hijacking when player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        clearPlayerBankQueues(playerId);
    }
    
    /**
     * Clear all bank queue entries for a player across all banks.
     * SECURITY: Called on disconnect to prevent session reuse.
     */
    private void clearPlayerBankQueues(UUID playerId) {
        // Clear from bank queues
        for (Map.Entry<UUID, LinkedList<QueueEntry>> entry : bankQueues.entrySet()) {
            entry.getValue().removeIf(qe -> qe.getPlayerId().equals(playerId));
        }
        
        // Clear from active bank slots
        for (Map.Entry<UUID, List<QueueEntry>> entry : activeBankSlots.entrySet()) {
            boolean removed = entry.getValue().removeIf(qe -> qe.getPlayerId().equals(playerId));
            if (removed) {
                // Process queue to fill empty slot
                UUID bankId = entry.getKey();
                Bank bank = bankManager.getBankById(bankId);
                if (bank != null) {
                    processBankQueue(bank);
                }
            }
        }
    }
    
    /**
     * Join bank reception queue
     */
    public boolean joinBankQueue(Player player, Bank bank) {
        UUID bankId = bank.getBankId();
        
        // Check if already in queue or active
        if (isInQueue(player, bank) || isInActiveSlot(player, bank)) {
            player.sendMessage("§cYou are already in this bank's queue!");
            return false;
        }
        
        LinkedList<QueueEntry> queue = bankQueues.computeIfAbsent(bankId, k -> new LinkedList<>());
        List<QueueEntry> activeSlots = activeBankSlots.computeIfAbsent(bankId, k -> new ArrayList<>());
        
        int queueNumber = queue.size() + activeSlots.size() + 1;
        QueueEntry entry = new QueueEntry(player, queueNumber);
        
        // Check if there's an available slot
        if (activeSlots.size() < bank.getReceptionSlots()) {
            // Move directly to active slot
            activeSlots.add(entry);
            entry.setActive(true);
            player.sendMessage("§a✓ Welcome to " + bank.getDisplayName() + "!");
            player.sendMessage("§7You have " + (bank.getReceptionTimeSeconds() / 60) + " minutes to complete your banking.");
            return true;
        } else {
            // Add to queue
            queue.add(entry);
            player.sendMessage("§e⏳ Added to " + bank.getDisplayName() + " queue");
            player.sendMessage("§7Position: #" + queueNumber);
            player.sendMessage("§7Players ahead: " + queue.size());
            return true;
        }
    }
    
    /**
     * Leave bank queue
     */
    public void leaveBankQueue(Player player, Bank bank) {
        UUID bankId = bank.getBankId();
        UUID playerId = player.getUniqueId();
        
        // Remove from queue
        LinkedList<QueueEntry> queue = bankQueues.get(bankId);
        if (queue != null) {
            queue.removeIf(entry -> entry.getPlayerId().equals(playerId));
        }
        
        // Remove from active slots
        List<QueueEntry> activeSlots = activeBankSlots.get(bankId);
        if (activeSlots != null) {
            boolean removed = activeSlots.removeIf(entry -> entry.getPlayerId().equals(playerId));
            if (removed) {
                processBankQueue(bank);
            }
        }
    }
    
    /**
     * Check if player is in queue
     */
    public boolean isInQueue(Player player, Bank bank) {
        LinkedList<QueueEntry> queue = bankQueues.get(bank.getBankId());
        return queue != null && queue.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
    }
    
    /**
     * Check if player is in active slot
     */
    public boolean isInActiveSlot(Player player, Bank bank) {
        List<QueueEntry> activeSlots = activeBankSlots.get(bank.getBankId());
        return activeSlots != null && activeSlots.stream().anyMatch(e -> e.getPlayerId().equals(player.getUniqueId()));
    }
    
    /**
     * Update player activity
     */
    public void updateActivity(Player player, Bank bank) {
        List<QueueEntry> activeSlots = activeBankSlots.get(bank.getBankId());
        if (activeSlots != null) {
            activeSlots.stream()
                .filter(e -> e.getPlayerId().equals(player.getUniqueId()))
                .forEach(QueueEntry::updateActivity);
        }
    }
    
    /**
     * Get queue position
     */
    public int getQueuePosition(Player player, Bank bank) {
        LinkedList<QueueEntry> queue = bankQueues.get(bank.getBankId());
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
    
    /**
     * Process bank queue - move next player to active slot
     */
    private void processBankQueue(Bank bank) {
        UUID bankId = bank.getBankId();
        LinkedList<QueueEntry> queue = bankQueues.get(bankId);
        List<QueueEntry> activeSlots = activeBankSlots.get(bankId);
        
        if (queue == null || activeSlots == null) return;
        
        // Move players from queue to active slots if space available
        while (activeSlots.size() < bank.getReceptionSlots() && !queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            Player player = plugin.getServer().getPlayer(entry.getPlayerId());
            
            // SECURITY: Validate session before granting access
            if (player != null && player.isOnline() && entry.isValidSession(player)) {
                entry.setActive(true);
                activeSlots.add(entry);
                player.sendMessage("§a✓ It's your turn! Welcome to " + bank.getDisplayName());
            }
        }
    }
    
    /**
     * Start queue processor task
     */
    private void startQueueProcessor() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            checkBankTimeouts();
        }, 20L, 20L); // Run every second
    }
    
    /**
     * Check for session timeouts
     */
    private void checkBankTimeouts() {
        for (Map.Entry<UUID, List<QueueEntry>> entry : activeBankSlots.entrySet()) {
            UUID bankId = entry.getKey();
            List<QueueEntry> activeSlots = entry.getValue();
            Bank bank = bankManager.getBankById(bankId);
            
            if (bank == null) continue;
            
            List<QueueEntry> toRemove = new ArrayList<>();
            
            for (QueueEntry queueEntry : activeSlots) {
                long sessionTime = queueEntry.getSessionTime() / 1000; // Convert to seconds
                
                if (sessionTime >= bank.getReceptionTimeSeconds()) {
                    Player player = plugin.getServer().getPlayer(queueEntry.getPlayerId());
                    if (player != null) {
                        player.sendMessage("§c✗ Your banking session has expired!");
                        player.closeInventory();
                    }
                    toRemove.add(queueEntry);
                }
            }
            
            toRemove.forEach(activeSlots::remove);
            if (!toRemove.isEmpty()) {
                processBankQueue(bank);
            }
        }
    }
    
    /**
     * Clear all queues for a bank
     */
    public void clearBankQueues(Bank bank) {
        UUID bankId = bank.getBankId();
        bankQueues.remove(bankId);
        activeBankSlots.remove(bankId);
    }
    
    /**
     * Get queue statistics
     */
    public Map<String, Object> getQueueStats(Bank bank) {
        Map<String, Object> stats = new HashMap<>();
        UUID bankId = bank.getBankId();
        
        LinkedList<QueueEntry> queue = bankQueues.get(bankId);
        List<QueueEntry> active = activeBankSlots.get(bankId);
        
        stats.put("queueSize", queue != null ? queue.size() : 0);
        stats.put("activeSlots", active != null ? active.size() : 0);
        stats.put("maxSlots", bank.getReceptionSlots());
        
        return stats;
    }
}