package online.demonzdevelopment.dztradehub.utils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Enhanced security logging system for sensitive events.
 * Provides dedicated [SECURITY] channel for authentication, authorization, and suspicious activities.
 */
public class SecurityLogger {
    
    private static File securityLogFile;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Initialize security logger with plugin data folder.
     */
    public static void initialize(Plugin plugin) {
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        
        String filename = "security-" + fileFormat.format(new Date()) + ".log";
        securityLogFile = new File(logsDir, filename);
    }
    
    /**
     * Log a security event.
     */
    public static void logSecurityEvent(Plugin plugin, String event, String details) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] [SECURITY] %s - %s", timestamp, event, details);
        
        // Log to console
        plugin.getLogger().warning(logMessage);
        
        // Log to file
        writeToFile(logMessage);
    }
    
    /**
     * Log account lockout event.
     */
    public static void logAccountLockout(Plugin plugin, String accountId, String reason) {
        logSecurityEvent(plugin, "ACCOUNT_LOCKOUT", 
            String.format("Account %s locked - Reason: %s", accountId, reason));
    }
    
    /**
     * Log failed login attempt.
     */
    public static void logFailedLogin(Plugin plugin, String accountId, String playerName, int attemptNumber) {
        logSecurityEvent(plugin, "FAILED_LOGIN", 
            String.format("Player %s failed login for account %s - Attempt %d", 
                playerName, accountId, attemptNumber));
    }
    
    /**
     * Log successful login.
     */
    public static void logSuccessfulLogin(Plugin plugin, String accountId, String playerName) {
        logSecurityEvent(plugin, "SUCCESSFUL_LOGIN", 
            String.format("Player %s successfully logged into account %s", playerName, accountId));
    }
    
    /**
     * Log suspicious activity.
     */
    public static void logSuspiciousActivity(Plugin plugin, String activity, String details) {
        logSecurityEvent(plugin, "SUSPICIOUS_ACTIVITY", 
            String.format("%s - Details: %s", activity, details));
    }
    
    /**
     * Log permission violation.
     */
    public static void logPermissionViolation(Plugin plugin, String playerName, String permission, String action) {
        logSecurityEvent(plugin, "PERMISSION_VIOLATION", 
            String.format("Player %s attempted %s without permission %s", 
                playerName, action, permission));
    }
    
    /**
     * Write log message to file.
     */
    private static void writeToFile(String message) {
        if (securityLogFile == null) {
            return;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(securityLogFile, true))) {
            writer.println(message);
        } catch (IOException e) {
            // Silently fail to avoid logging loops
        }
    }
}
