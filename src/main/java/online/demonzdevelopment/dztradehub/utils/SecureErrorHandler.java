package online.demonzdevelopment.dztradehub.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Secure error handler that prevents information disclosure.
 * Logs full technical details server-side, shows generic messages to players.
 * 
 * SECURITY: This prevents internal implementation details from being exposed to players.
 */
public class SecureErrorHandler {
    
    /**
     * Handle an error with full server logging and generic player message.
     * 
     * @param plugin The plugin instance
     * @param player The player to notify (can be null)
     * @param userMessage Generic message to show to the player
     * @param e The exception that occurred
     */
    public static void handleError(Plugin plugin, Player player, String userMessage, Exception e) {
        // Full details in server log
        plugin.getLogger().severe("Error occurred: " + e.getClass().getName());
        plugin.getLogger().severe("Details: " + e.getMessage());
        e.printStackTrace();
        
        // Generic message to player
        if (player != null && player.isOnline()) {
            player.sendMessage("§c✗ " + userMessage);
            player.sendMessage("§7An error occurred. Please contact an administrator.");
        }
    }
    
    /**
     * Handle an error without player notification (server-side only).
     * 
     * @param plugin The plugin instance
     * @param message Context message about the error
     * @param e The exception that occurred
     */
    public static void handleError(Plugin plugin, String message, Exception e) {
        handleError(plugin, null, message, e);
    }
    
    /**
     * Log a warning with sanitized output.
     * 
     * @param plugin The plugin instance
     * @param message Warning message
     */
    public static void logWarning(Plugin plugin, String message) {
        plugin.getLogger().warning(message);
    }
    
    /**
     * Log an error without exception details (for validation errors).
     * 
     * @param plugin The plugin instance
     * @param player The player to notify
     * @param userMessage Message to show to player
     * @param serverMessage Detailed message for server log
     */
    public static void logValidationError(Plugin plugin, Player player, String userMessage, String serverMessage) {
        plugin.getLogger().warning("Validation error: " + serverMessage);
        
        if (player != null && player.isOnline()) {
            player.sendMessage("§c✗ " + userMessage);
        }
    }
}
