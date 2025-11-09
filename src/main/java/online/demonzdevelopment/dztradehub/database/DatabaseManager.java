package online.demonzdevelopment.dztradehub.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.*;
import online.demonzdevelopment.dztradehub.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final DZTradeHub plugin;
    private HikariDataSource dataSource;
    private final String databaseType;

    public DatabaseManager(DZTradeHub plugin) {
        this.plugin = plugin;
        ConfigManager configManager = plugin.getConfigManager();
        this.databaseType = configManager.getDatabaseType();
        
        setupDatabase();
        createTables();
    }

    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        
        if (databaseType.equalsIgnoreCase("MYSQL")) {
            ConfigManager cm = plugin.getConfigManager();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                cm.getMySQLHost(), cm.getMySQLPort(), cm.getMySQLDatabase()));
            config.setUsername(cm.getMySQLUsername());
            config.setPassword(cm.getMySQLPassword());
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            // SQLite
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            config.setJdbcUrl("jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
        }
        
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setPoolName("DZTradeHub-Pool");
        
        dataSource = new HikariDataSource(config);
    }

    private void createTables() {
        try (Connection conn = getConnection()) {
            // Shops table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS shops (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "area_name VARCHAR(100) NOT NULL, " +
                "shop_name VARCHAR(100) NOT NULL, " +
                "display_name VARCHAR(200), " +
                "shop_type VARCHAR(20), " +
                "data TEXT, " +
                "UNIQUE(area_name, shop_name))" 
            );

            // Shop items table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS shop_items (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "shop_id VARCHAR(36), " +
                "item_data BLOB, " +
                "buy_price REAL, " +
                "sell_price REAL, " +
                "current_stock INTEGER, " +
                "max_stock INTEGER, " +
                "refill_interval VARCHAR(20), " +
                "refill_amount INTEGER, " +
                "min_price REAL, " +
                "max_price REAL, " +
                "dynamic_pricing INTEGER, " +
                "transaction_type VARCHAR(20), " +
                "FOREIGN KEY(shop_id) REFERENCES shops(id) ON DELETE CASCADE)" 
            );

            // Transactions table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid VARCHAR(36), " +
                "shop_id VARCHAR(36), " +
                "item_type VARCHAR(50), " +
                "quantity INTEGER, " +
                "price_per_item REAL, " +
                "total_price REAL, " +
                "transaction_type VARCHAR(10), " +
                "currency_type VARCHAR(20), " +
                "timestamp BIGINT)" 
            );

            // Auctions table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS auctions (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "seller_uuid VARCHAR(36), " +
                "item_data BLOB, " +
                "actual_price REAL, " +
                "max_drop_price REAL, " +
                "drop_per_unit REAL, " +
                "drop_interval_hours INTEGER, " +
                "max_queue INTEGER, " +
                "price_increase_percent REAL, " +
                "created_time BIGINT, " +
                "last_drop_time BIGINT, " +
                "frozen INTEGER, " +
                "queue_data TEXT)" 
            );

            // Kit cooldowns table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS kit_cooldowns (" +
                "player_uuid VARCHAR(36), " +
                "kit_name VARCHAR(100), " +
                "last_claim BIGINT, " +
                "PRIMARY KEY(player_uuid, kit_name))" 
            );

            // Player carts table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS player_carts (" +
                "player_uuid VARCHAR(36), " +
                "shop_id VARCHAR(36), " +
                "cart_data TEXT, " +
                "last_updated BIGINT, " +
                "PRIMARY KEY(player_uuid, shop_id))" 
            );

            // Bounties table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bounties (" +
                "bounty_id VARCHAR(36) PRIMARY KEY, " +
                "target_player VARCHAR(36) NOT NULL, " +
                "creator_player VARCHAR(36) NOT NULL, " +
                "reward_items BLOB, " +
                "money_reward REAL, " +
                "mobcoin_reward REAL, " +
                "gem_reward REAL, " +
                "created_time BIGINT, " +
                "active INTEGER)" 
            );

            // Bank tables
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS banks (" +
                "bank_id VARCHAR(36) PRIMARY KEY, " +
                "bank_name VARCHAR(100) UNIQUE NOT NULL, " +
                "display_name VARCHAR(200), " +
                "created_time BIGINT)"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bank_accounts (" +
                "account_id VARCHAR(36) PRIMARY KEY, " +
                "bank_id VARCHAR(36) NOT NULL, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(100), " +
                "account_type VARCHAR(20), " +
                "password_hash VARCHAR(64), " +
                "account_level INTEGER, " +
                "money_balance REAL DEFAULT 0, " +
                "mobcoin_balance REAL DEFAULT 0, " +
                "gem_balance REAL DEFAULT 0, " +
                "created_time BIGINT, " +
                "last_access_time BIGINT, " +
                "total_transactions INTEGER DEFAULT 0, " +
                "total_interest_earned BIGINT DEFAULT 0, " +
                "active INTEGER DEFAULT 1, " +
                "locked INTEGER DEFAULT 0, " +
                "FOREIGN KEY(bank_id) REFERENCES banks(bank_id) ON DELETE CASCADE)"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bank_loans (" +
                "loan_id VARCHAR(36) PRIMARY KEY, " +
                "account_id VARCHAR(36) NOT NULL, " +
                "bank_id VARCHAR(36) NOT NULL, " +
                "player_uuid VARCHAR(36) NOT NULL, " +
                "currency VARCHAR(20), " +
                "principal_amount REAL, " +
                "remaining_amount REAL, " +
                "interest_rate REAL, " +
                "current_interest_rate REAL, " +
                "issued_time BIGINT, " +
                "due_time BIGINT, " +
                "payment_period_minutes INTEGER, " +
                "missed_payments INTEGER DEFAULT 0, " +
                "active INTEGER DEFAULT 1, " +
                "defaulted INTEGER DEFAULT 0, " +
                "FOREIGN KEY(account_id) REFERENCES bank_accounts(account_id) ON DELETE CASCADE)"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bank_transactions (" +
                "transaction_id VARCHAR(36) PRIMARY KEY, " +
                "account_id VARCHAR(36), " +
                "bank_id VARCHAR(36), " +
                "player_uuid VARCHAR(36), " +
                "transaction_type VARCHAR(50), " +
                "currency VARCHAR(20), " +
                "amount REAL, " +
                "balance_before REAL, " +
                "balance_after REAL, " +
                "timestamp BIGINT, " +
                "notes TEXT, " +
                "target_account_id VARCHAR(36), " +
                "target_player_uuid VARCHAR(36), " +
                "target_bank_id VARCHAR(36))"
            );

            plugin.getLogger().info("Database tables created successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Async operations
    public CompletableFuture<Void> saveShopAsync(Shop shop, String areaName) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "INSERT OR REPLACE INTO shops (id, area_name, shop_name, display_name, shop_type) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, shop.getId().toString());
                stmt.setString(2, areaName);
                stmt.setString(3, shop.getName());
                stmt.setString(4, shop.getDisplayName());
                stmt.setString(5, shop.getShopType().name());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save shop: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> saveShopItemAsync(ShopItem item, UUID shopId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "INSERT OR REPLACE INTO shop_items (id, shop_id, item_data, buy_price, sell_price, " +
                    "current_stock, max_stock, refill_interval, refill_amount, min_price, max_price, " +
                    "dynamic_pricing, transaction_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, item.getId().toString());
                stmt.setString(2, shopId.toString());
                stmt.setBytes(3, serializeItemStack(item.getItemStack()));
                stmt.setDouble(4, item.getBuyPrice());
                stmt.setDouble(5, item.getSellPrice());
                stmt.setInt(6, item.getCurrentStock());
                stmt.setInt(7, item.getMaxStock());
                stmt.setString(8, item.getRefillInterval());
                stmt.setInt(9, item.getRefillAmount());
                stmt.setDouble(10, item.getMinPrice());
                stmt.setDouble(11, item.getMaxPrice());
                stmt.setInt(12, item.isDynamicPricingEnabled() ? 1 : 0);
                stmt.setString(13, item.getTransactionType().name());
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save shop item: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Long> getKitCooldownAsync(UUID playerUUID, String kitName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "SELECT last_claim FROM kit_cooldowns WHERE player_uuid = ? AND kit_name = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, kitName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("last_claim");
                }
                return 0L;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get kit cooldown: " + e.getMessage());
                return 0L;
            }
        });
    }

    public CompletableFuture<Void> setKitCooldownAsync(UUID playerUUID, String kitName, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "INSERT OR REPLACE INTO kit_cooldowns (player_uuid, kit_name, last_claim) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, kitName);
                stmt.setLong(3, timestamp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set kit cooldown: " + e.getMessage());
            }
        });
    }

    // Serialize/Deserialize ItemStack
    private byte[] serializeItemStack(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return outputStream.toByteArray();
    }

    private ItemStack deserializeItemStack(byte[] data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }

    // Bounty operations
    public CompletableFuture<Void> saveBountyAsync(online.demonzdevelopment.dztradehub.data.Bounty bounty) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "INSERT OR REPLACE INTO bounties (bounty_id, target_player, creator_player, " +
                    "reward_items, money_reward, mobcoin_reward, gem_reward, created_time, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, bounty.getBountyId().toString());
                stmt.setString(2, bounty.getTargetPlayer().toString());
                stmt.setString(3, bounty.getCreatorPlayer().toString());
                
                // Serialize items
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                BukkitObjectOutputStream oos = new BukkitObjectOutputStream(bos);
                oos.writeObject(bounty.getRewardItems());
                oos.close();
                stmt.setBytes(4, bos.toByteArray());
                
                stmt.setDouble(5, bounty.getMoneyReward());
                stmt.setDouble(6, bounty.getMobcoinReward());
                stmt.setDouble(7, bounty.getGemReward());
                stmt.setLong(8, bounty.getCreatedTime());
                stmt.setInt(9, bounty.isActive() ? 1 : 0);
                stmt.executeUpdate();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save bounty: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> removeBountyAsync(UUID bountyId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                String sql = "DELETE FROM bounties WHERE bounty_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, bountyId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove bounty: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Map<UUID, List<online.demonzdevelopment.dztradehub.data.Bounty>>> loadBountiesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, List<online.demonzdevelopment.dztradehub.data.Bounty>> bounties = new HashMap<>();
            try (Connection conn = getConnection()) {
                String sql = "SELECT * FROM bounties WHERE active = 1";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    UUID targetPlayer = UUID.fromString(rs.getString("target_player"));
                    UUID creatorPlayer = UUID.fromString(rs.getString("creator_player"));
                    
                    online.demonzdevelopment.dztradehub.data.Bounty bounty = 
                        new online.demonzdevelopment.dztradehub.data.Bounty(targetPlayer, creatorPlayer);
                    
                    // Deserialize items
                    byte[] itemData = rs.getBytes("reward_items");
                    if (itemData != null) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(itemData);
                        BukkitObjectInputStream ois = new BukkitObjectInputStream(bis);
                        @SuppressWarnings("unchecked")
                        List<ItemStack> items = (List<ItemStack>) ois.readObject();
                        ois.close();
                        bounty.setRewardItems(items);
                    }
                    
                    bounty.setMoneyReward(rs.getDouble("money_reward"));
                    bounty.setMobcoinReward(rs.getDouble("mobcoin_reward"));
                    bounty.setGemReward(rs.getDouble("gem_reward"));
                    bounty.setActive(rs.getInt("active") == 1);
                    
                    bounties.computeIfAbsent(targetPlayer, k -> new ArrayList<>()).add(bounty);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load bounties: " + e.getMessage());
                e.printStackTrace();
            }
            return bounties;
        });
    }

    /**
     * Start periodic database health checks.
     * IMPROVEMENT-007: Monitor database connection health and auto-reconnect.
     */
    public void startHealthCheck() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                Connection conn = getConnection();
                if (conn == null || conn.isClosed()) {
                    plugin.getLogger().warning("[DB HEALTH] Database connection lost! Attempting reconnect...");
                    reconnect();
                } else {
                    // Test connection with a simple query
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeQuery("SELECT 1");
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("[DB HEALTH] Database health check failed: " + e.getMessage());
                plugin.getLogger().severe("[DB HEALTH] Attempting to reconnect...");
                reconnect();
            }
        }, 6000L, 6000L); // Every 5 minutes (6000 ticks)
    }
    
    /**
     * Attempt to reconnect to the database.
     * IMPROVEMENT-007: Reconnection logic for health check.
     */
    private void reconnect() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            setupDatabase();
            plugin.getLogger().info("[DB HEALTH] §aDatabase reconnected successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("[DB HEALTH] §cFailed to reconnect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed");
        }
    }
}