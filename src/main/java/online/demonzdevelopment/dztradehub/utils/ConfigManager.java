package online.demonzdevelopment.dztradehub.utils;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.RankData;
import online.demonzdevelopment.dztradehub.data.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {
    private final DZTradeHub plugin;
    private FileConfiguration config;
    private FileConfiguration ranksConfig;
    private FileConfiguration kitsConfig;
    private FileConfiguration auctionConfig;

    public ConfigManager(DZTradeHub plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        // Save default config if doesn't exist
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load ranks.yml
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);

        // Load kits.yml
        File kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        // Load auction.yml
        File auctionFile = new File(plugin.getDataFolder(), "auction.yml");
        if (!auctionFile.exists()) {
            plugin.saveResource("auction.yml", false);
        }
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        loadConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Map<String, RankData> loadRanks() {
        Map<String, RankData> ranks = new HashMap<>();
        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("ranks");
        
        if (ranksSection == null) {
            // Create default rank
            Map<String, List<String>> defaultAccess = new HashMap<>();
            defaultAccess.put("Bazar", List.of("MeatShop", "VegetableShop"));
            RankData.AuctionSettings defaultAuction = new RankData.AuctionSettings(true, 3, 10.0);
            ranks.put("default", new RankData(
                "dztradehub.rank.default",
                "§7Member",
                true, true, 27, 0,
                defaultAccess,
                defaultAuction
            ));
            return ranks;
        }

        for (String rankName : ranksSection.getKeys(false)) {
            ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankName);
            if (rankSection == null) continue;

            String permission = rankSection.getString("permission", "dztradehub.rank." + rankName);
            String displayName = rankSection.getString("display-name", "§7" + rankName);
            boolean buyItems = rankSection.getBoolean("buy-items", true);
            boolean sellItems = rankSection.getBoolean("sell-items", true);
            int maxCartSize = rankSection.getInt("max-cart-size", 27);
            int queuePriority = rankSection.getInt("queue-priority", 0);

            // Load accessible areas
            Map<String, List<String>> accessibleAreas = new HashMap<>();
            ConfigurationSection areasSection = rankSection.getConfigurationSection("areas");
            if (areasSection != null) {
                for (String areaName : areasSection.getKeys(false)) {
                    List<String> shops = areasSection.getStringList(areaName);
                    accessibleAreas.put(areaName, shops);
                }
            }

            // Load auction settings
            ConfigurationSection auctionSection = rankSection.getConfigurationSection("auction");
            RankData.AuctionSettings auctionSettings = new RankData.AuctionSettings(
                auctionSection != null && auctionSection.getBoolean("can-list", true),
                auctionSection != null ? auctionSection.getInt("max-listings", 3) : 3,
                auctionSection != null ? auctionSection.getDouble("listing-fee", 10.0) : 10.0
            );

            ranks.put(rankName, new RankData(
                permission, displayName, buyItems, sellItems,
                maxCartSize, queuePriority, accessibleAreas, auctionSettings
            ));
        }

        return ranks;
    }

    public Map<String, Kit> loadKits() {
        Map<String, Kit> kits = new HashMap<>();
        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        
        if (kitsSection == null) {
            return kits;
        }

        for (String kitName : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
            if (kitSection == null) continue;

            String displayName = kitSection.getString("display-name", kitName);
            Material icon = Material.valueOf(kitSection.getString("icon", "CHEST"));
            double price = kitSection.getDouble("price", 0.0);
            long cooldown = kitSection.getLong("cooldown", 86400);
            String permission = kitSection.getString("permission", "dztradehub.kit." + kitName.toLowerCase());

            Kit kit = new Kit(kitName, displayName, icon, price, cooldown, permission);
            
            // Load description
            List<String> description = kitSection.getStringList("description");
            kit.setDescription(description);

            // Load commands
            List<String> commands = kitSection.getStringList("commands");
            kit.setCommands(commands);

            kits.put(kitName, kit);
        }

        return kits;
    }

    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "dztradehub");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }

    // Auction configuration getters
    public FileConfiguration getAuctionConfig() {
        return auctionConfig;
    }

    public boolean isAuctionEnabled() {
        return auctionConfig.getBoolean("auction.enabled", true);
    }

    public String getAuctionStorageType() {
        return auctionConfig.getString("auction.storage.type", "FLATFILE");
    }

    public int getAuctionSaveInterval() {
        return auctionConfig.getInt("auction.storage.save-interval", 300);
    }

    public boolean isAuctionCleanupEnabled() {
        return auctionConfig.getBoolean("auction.cleanup.enabled", true);
    }

    public int getAuctionRetentionDays() {
        return auctionConfig.getInt("auction.cleanup.retention-days", 30);
    }

    public int getTransactionRetentionDays() {
        return auctionConfig.getInt("auction.cleanup.transaction-retention-days", 30);
    }

    public int getCleanupCheckInterval() {
        return auctionConfig.getInt("auction.cleanup.check-interval-hours", 6);
    }

    public int getMaxAuctionDurationDays() {
        return auctionConfig.getInt("auction.limits.max-duration-days", 7);
    }

    public double getMinStartingPrice() {
        return auctionConfig.getDouble("auction.limits.min-starting-price", 1.0);
    }

    public double getMaxStartingPrice() {
        return auctionConfig.getDouble("auction.limits.max-starting-price", 1000000.0);
    }

    public boolean isMultiCurrencyEnabled() {
        return auctionConfig.getBoolean("auction.currency.multi-currency-enabled", true);
    }

    public List<String> getAllowedCurrencies() {
        return auctionConfig.getStringList("auction.currency.allowed-currencies");
    }

    // Kit management methods
    public void saveKit(Kit kit) {
        String path = "kits." + kit.getName();
        kitsConfig.set(path + ".display-name", kit.getDisplayName());
        kitsConfig.set(path + ".icon", kit.getIconMaterial().name());
        kitsConfig.set(path + ".price", kit.getPrice());
        kitsConfig.set(path + ".cooldown", kit.getCooldown());
        kitsConfig.set(path + ".permission", kit.getPermission());
        kitsConfig.set(path + ".description", kit.getDescription());
        kitsConfig.set(path + ".commands", kit.getCommands());
        
        try {
            kitsConfig.save(new java.io.File(plugin.getDataFolder(), "kits.yml"));
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to save kit: " + e.getMessage());
        }
    }

    public void deleteKit(String kitName) {
        kitsConfig.set("kits." + kitName, null);
        try {
            kitsConfig.save(new java.io.File(plugin.getDataFolder(), "kits.yml"));
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to delete kit: " + e.getMessage());
        }
    }

    public void saveKitLink(String kitName, String areaName, String shopName) {
        kitsConfig.set("links." + kitName + ".area", areaName);
        kitsConfig.set("links." + kitName + ".shop", shopName);
        try {
            kitsConfig.save(new java.io.File(plugin.getDataFolder(), "kits.yml"));
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to save kit link: " + e.getMessage());
        }
    }

    public void deleteKitLink(String kitName) {
        kitsConfig.set("links." + kitName, null);
        try {
            kitsConfig.save(new java.io.File(plugin.getDataFolder(), "kits.yml"));
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Failed to delete kit link: " + e.getMessage());
        }
    }
}