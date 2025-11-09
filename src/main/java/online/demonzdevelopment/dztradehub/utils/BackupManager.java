package online.demonzdevelopment.dztradehub.utils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Automated backup system for database files.
 * Creates timestamped backups before critical operations.
 */
public class BackupManager {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final int MAX_BACKUPS = 10; // Keep last 10 backups
    
    /**
     * Create a backup of the database before a critical operation.
     * 
     * @param plugin The plugin instance
     * @param operation Description of the operation (e.g., "migration", "deletion")
     * @return true if backup successful
     */
    public static boolean backupBeforeCriticalOp(Plugin plugin, String operation) {
        String timestamp = dateFormat.format(new Date());
        String backupName = "backup_" + operation.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".db";
        
        File dataFolder = plugin.getDataFolder();
        File sourceDb = new File(dataFolder, "database.db");
        
        // Check if source database exists
        if (!sourceDb.exists()) {
            plugin.getLogger().warning("Database file not found, skipping backup");
            return false;
        }
        
        // Create backups directory
        File backupDir = new File(dataFolder, "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        File backupFile = new File(backupDir, backupName);
        
        try {
            // Copy database file
            Files.copy(sourceDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("§a✓ Backup created: " + backupName);
            
            // Clean up old backups
            cleanupOldBackups(plugin, backupDir);
            
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a manual backup.
     */
    public static boolean createManualBackup(Plugin plugin) {
        return backupBeforeCriticalOp(plugin, "manual");
    }
    
    /**
     * Clean up old backups, keeping only the most recent ones.
     */
    private static void cleanupOldBackups(Plugin plugin, File backupDir) {
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
        
        if (backups == null || backups.length <= MAX_BACKUPS) {
            return;
        }
        
        // Sort by last modified time (oldest first)
        java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        
        // Delete oldest backups
        int toDelete = backups.length - MAX_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                plugin.getLogger().info("Deleted old backup: " + backups[i].getName());
            }
        }
    }
    
    /**
     * Restore from a backup file.
     * WARNING: This will overwrite the current database!
     */
    public static boolean restoreFromBackup(Plugin plugin, String backupFileName) {
        File dataFolder = plugin.getDataFolder();
        File backupDir = new File(dataFolder, "backups");
        File backupFile = new File(backupDir, backupFileName);
        File targetDb = new File(dataFolder, "database.db");
        
        if (!backupFile.exists()) {
            plugin.getLogger().severe("Backup file not found: " + backupFileName);
            return false;
        }
        
        try {
            // Create a backup of current database before restoring
            if (targetDb.exists()) {
                File safetyBackup = new File(backupDir, "pre-restore_" + dateFormat.format(new Date()) + ".db");
                Files.copy(targetDb.toPath(), safetyBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Restore from backup
            Files.copy(backupFile.toPath(), targetDb.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("§a✓ Database restored from: " + backupFileName);
            
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to restore from backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * List all available backups.
     */
    public static String[] listBackups(Plugin plugin) {
        File backupDir = new File(plugin.getDataFolder(), "backups");
        
        if (!backupDir.exists()) {
            return new String[0];
        }
        
        File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
        
        if (backups == null || backups.length == 0) {
            return new String[0];
        }
        
        // Sort by last modified time (newest first)
        java.util.Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        
        String[] names = new String[backups.length];
        for (int i = 0; i < backups.length; i++) {
            names[i] = backups[i].getName();
        }
        
        return names;
    }
}
