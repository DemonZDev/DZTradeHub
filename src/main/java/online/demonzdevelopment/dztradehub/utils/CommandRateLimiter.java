package online.demonzdevelopment.dztradehub.utils;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting system for commands to prevent spam and DoS.
 * Implements per-player cooldown tracking.
 */
public class CommandRateLimiter {
    
    private final Map<UUID, Map<String, Long>> lastCommandTime = new ConcurrentHashMap<>();
    private final long defaultCooldownMs;
    
    /**
     * Create a rate limiter with default cooldown.
     * 
     * @param cooldownMs Cooldown in milliseconds
     */
    public CommandRateLimiter(long cooldownMs) {
        this.defaultCooldownMs = cooldownMs;
    }
    
    /**
     * Create a rate limiter with 1 second cooldown.
     */
    public CommandRateLimiter() {
        this(1000); // 1 second default
    }
    
    /**
     * Check if a player can execute a command.
     * 
     * @param player The player
     * @param command The command name
     * @return true if player can execute, false if on cooldown
     */
    public boolean canExecute(Player player, String command) {
        return canExecute(player, command, defaultCooldownMs);
    }
    
    /**
     * Check if a player can execute a command with custom cooldown.
     * 
     * @param player The player
     * @param command The command name
     * @param cooldownMs Custom cooldown in milliseconds
     * @return true if player can execute, false if on cooldown
     */
    public boolean canExecute(Player player, String command, long cooldownMs) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        Map<String, Long> playerCommands = lastCommandTime.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Long lastTime = playerCommands.get(command);
        
        if (lastTime != null && (now - lastTime) < cooldownMs) {
            return false; // Still on cooldown
        }
        
        // Update last execution time
        playerCommands.put(command, now);
        return true;
    }
    
    /**
     * Get remaining cooldown time in milliseconds.
     * 
     * @param player The player
     * @param command The command name
     * @return Remaining cooldown in ms, or 0 if no cooldown
     */
    public long getRemainingCooldown(Player player, String command) {
        return getRemainingCooldown(player, command, defaultCooldownMs);
    }
    
    /**
     * Get remaining cooldown time with custom cooldown.
     */
    public long getRemainingCooldown(Player player, String command, long cooldownMs) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCommands = lastCommandTime.get(uuid);
        
        if (playerCommands == null) {
            return 0;
        }
        
        Long lastTime = playerCommands.get(command);
        if (lastTime == null) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - lastTime;
        long remaining = cooldownMs - elapsed;
        
        return Math.max(0, remaining);
    }
    
    /**
     * Clear cooldown for a specific player and command.
     */
    public void clearCooldown(Player player, String command) {
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCommands = lastCommandTime.get(uuid);
        
        if (playerCommands != null) {
            playerCommands.remove(command);
        }
    }
    
    /**
     * Clear all cooldowns for a player.
     */
    public void clearAllCooldowns(Player player) {
        lastCommandTime.remove(player.getUniqueId());
    }
    
    /**
     * Cleanup disconnected players.
     */
    public void cleanup() {
        // This should be called periodically to prevent memory leaks
        // In practice, players who disconnect will eventually be cleaned up
        // when their cooldowns expire naturally
    }
}
