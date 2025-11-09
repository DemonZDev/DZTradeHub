package online.demonzdevelopment.dztradehub.utils;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.BankAccount;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Transaction export utility for bank accounts.
 * Allows players to export their transaction history to CSV format.
 * IMPROVEMENT-004: Transaction Export System
 */
public class TransactionExport {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Export all transactions for a bank account to CSV.
     * 
     * @param plugin The plugin instance
     * @param player The player requesting the export
     * @param account The bank account to export transactions from
     * @return The exported file, or null if export failed
     */
    public static File exportTransactions(DZTradeHub plugin, Player player, BankAccount account) {
        return exportTransactions(plugin, player, account, 0, System.currentTimeMillis());
    }
    
    /**
     * Export transactions for a bank account within a date range.
     * 
     * @param plugin The plugin instance
     * @param player The player requesting the export
     * @param account The bank account to export transactions from
     * @param startTime Start timestamp (inclusive)
     * @param endTime End timestamp (inclusive)
     * @return The exported file, or null if export failed
     */
    public static File exportTransactions(DZTradeHub plugin, Player player, BankAccount account, 
                                         long startTime, long endTime) {
        String timestamp = fileFormat.format(new Date());
        String filename = "transactions_" + account.getAccountId().toString().substring(0, 8) + "_" + timestamp + ".csv";
        
        File exportsDir = new File(plugin.getDataFolder(), "exports");
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }
        
        File exportFile = new File(exportsDir, filename);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {
            // Write CSV header
            writer.println("Transaction ID,Date,Type,Currency,Amount,Balance Before,Balance After");
            
            // Query transactions
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "SELECT * FROM bank_transactions WHERE account_id = ? " +
                            "AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp ASC";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, account.getAccountId().toString());
                stmt.setLong(2, startTime);
                stmt.setLong(3, endTime);
                
                ResultSet rs = stmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    UUID transactionId = UUID.fromString(rs.getString("transaction_id"));
                    long txTimestamp = rs.getLong("timestamp");
                    String type = rs.getString("transaction_type");
                    String currency = rs.getString("currency");
                    double amount = rs.getDouble("amount");
                    double balanceBefore = rs.getDouble("balance_before");
                    double balanceAfter = rs.getDouble("balance_after");
                    
                    // Write CSV row
                    writer.printf("%s,%s,%s,%s,%.2f,%.2f,%.2f%n",
                        transactionId.toString().substring(0, 8),
                        dateFormat.format(new Date(txTimestamp)),
                        type,
                        currency,
                        amount,
                        balanceBefore,
                        balanceAfter
                    );
                    
                    count++;
                }
                
                plugin.getLogger().info("Exported " + count + " transactions for account " + 
                                       account.getAccountId() + " to " + filename);
                
                if (player != null) {
                    player.sendMessage("§a✓ Exported " + count + " transactions to CSV");
                    player.sendMessage("§7File: " + filename);
                }
                
                return exportFile;
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to query transactions for export: " + e.getMessage());
                if (player != null) {
                    player.sendMessage("§c✗ Failed to export transactions!");
                }
                return null;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create export file: " + e.getMessage());
            if (player != null) {
                player.sendMessage("§c✗ Failed to create export file!");
            }
            return null;
        }
    }
    
    /**
     * Clean up old export files (older than 7 days).
     */
    public static void cleanupOldExports(DZTradeHub plugin) {
        File exportsDir = new File(plugin.getDataFolder(), "exports");
        if (!exportsDir.exists()) {
            return;
        }
        
        File[] exports = exportsDir.listFiles((dir, name) -> name.startsWith("transactions_") && name.endsWith(".csv"));
        if (exports == null) {
            return;
        }
        
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        int deleted = 0;
        for (File export : exports) {
            if (export.lastModified() < sevenDaysAgo) {
                if (export.delete()) {
                    deleted++;
                }
            }
        }
        
        if (deleted > 0) {
            plugin.getLogger().info("Cleaned up " + deleted + " old transaction export files");
        }
    }
}
