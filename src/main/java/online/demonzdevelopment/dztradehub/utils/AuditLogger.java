package online.demonzdevelopment.dztradehub.utils;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Audit logging system for administrative actions.
 * Provides accountability and compliance tracking for all admin operations.
 */
public class AuditLogger {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log an administrative action.
     * 
     * @param plugin The plugin instance
     * @param sender The command sender (admin)
     * @param action The action type (e.g., "BANK_DELETE", "SHOP_CREATE")
     * @param target The target of the action
     * @param details Additional details about the action
     */
    public static void logAdminAction(DZTradeHub plugin, CommandSender sender, 
                                     String action, String target, String details) {
        String actorName = sender.getName();
        UUID actorUUID = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
        long timestamp = System.currentTimeMillis();
        
        // Log to console
        String consoleMessage = String.format(
            "[AUDIT] %s | Actor: %s (%s) | Action: %s | Target: %s | Details: %s",
            dateFormat.format(new Date(timestamp)), 
            actorName, 
            actorUUID != null ? actorUUID.toString() : "CONSOLE", 
            action, 
            target, 
            details
        );
        plugin.getLogger().info(consoleMessage);
        
        // Store in database asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            storeAuditLog(plugin, actorUUID, actorName, action, target, details, timestamp);
        });
    }
    
    /**
     * Store audit log entry in database.
     */
    private static void storeAuditLog(DZTradeHub plugin, UUID actorUUID, String actorName, 
                                     String action, String target, String details, long timestamp) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO admin_audit_log (log_id, actor_uuid, actor_name, " +
                        "action_type, target_type, target_id, details, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, actorUUID != null ? actorUUID.toString() : null);
            stmt.setString(3, actorName);
            stmt.setString(4, action);
            stmt.setString(5, determineTargetType(action));
            stmt.setString(6, target);
            stmt.setString(7, details);
            stmt.setLong(8, timestamp);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to store audit log: " + e.getMessage());
        }
    }
    
    /**
     * Determine target type from action.
     */
    private static String determineTargetType(String action) {
        if (action.startsWith("BANK_")) return "BANK";
        if (action.startsWith("SHOP_")) return "SHOP";
        if (action.startsWith("AREA_")) return "AREA";
        if (action.startsWith("AUCTION_")) return "AUCTION";
        if (action.startsWith("CONFIG_")) return "CONFIG";
        return "OTHER";
    }
    
    /**
     * Create audit log table if it doesn't exist.
     */
    public static void initializeAuditTable(DZTradeHub plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "CREATE TABLE IF NOT EXISTS admin_audit_log (" +
                            "log_id VARCHAR(36) PRIMARY KEY, " +
                            "actor_uuid VARCHAR(36), " +
                            "actor_name VARCHAR(100), " +
                            "action_type VARCHAR(50), " +
                            "target_type VARCHAR(50), " +
                            "target_id VARCHAR(100), " +
                            "details TEXT, " +
                            "timestamp BIGINT" +
                            ")";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate();
                
                // Create indices
                String indexSql1 = "CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON admin_audit_log(timestamp)";
                String indexSql2 = "CREATE INDEX IF NOT EXISTS idx_audit_actor ON admin_audit_log(actor_uuid)";
                
                conn.prepareStatement(indexSql1).executeUpdate();
                conn.prepareStatement(indexSql2).executeUpdate();
                
                plugin.getLogger().info("Audit log table initialized");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize audit log table: " + e.getMessage());
            }
        });
    }
}
