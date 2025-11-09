package online.demonzdevelopment.dztradehub;

import online.demonzdevelopment.dztradehub.api.DZTradeHubAPI;
import online.demonzdevelopment.dztradehub.commands.AuctionHouseCommand;
import online.demonzdevelopment.dztradehub.commands.TradeHubCommand;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.database.DatabaseManager;
import online.demonzdevelopment.dztradehub.gui.AuctionBrowserGUI;
import online.demonzdevelopment.dztradehub.gui.ShopGUI;
import online.demonzdevelopment.dztradehub.listeners.AuctionGUIListener;
import online.demonzdevelopment.dztradehub.listeners.ShopGUIListener;
import online.demonzdevelopment.dztradehub.managers.*;
import online.demonzdevelopment.dztradehub.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class DZTradeHub extends JavaPlugin {
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ShopManager shopManager;
    private PermissionManager permissionManager;
    private KitManager kitManager;
    private AuctionManager auctionManager;
    private QueueManager queueManager;
    private CasinoManager casinoManager;
    private BountyManager bountyManager;
    private online.demonzdevelopment.dztradehub.update.UpdateManager updateManager;
    private online.demonzdevelopment.dztradehub.storage.FileStorageManager fileStorageManager;
    private StockManager stockManager;
    private DynamicPricingManager dynamicPricingManager;
    
    // Bank Managers
    private online.demonzdevelopment.dztradehub.managers.bank.BankManager bankManager;
    private online.demonzdevelopment.dztradehub.managers.bank.BankAccountManager bankAccountManager;
    private online.demonzdevelopment.dztradehub.managers.bank.BankLoanManager bankLoanManager;
    private online.demonzdevelopment.dztradehub.managers.bank.BankInterestManager bankInterestManager;
    private online.demonzdevelopment.dztradehub.managers.bank.BankQueueManager bankQueueManager;
    
    // GUIs
    private ShopGUI shopGUI;
    private online.demonzdevelopment.dztradehub.gui.AreaGUI areaGUI;
    private AuctionBrowserGUI auctionBrowserGUI;
    private online.demonzdevelopment.dztradehub.gui.BountyGUI bountyGUI;
    
    // API
    private DZTradeHubAPI api;
    private online.demonzdevelopment.dzeconomy.api.DZEconomyAPI economyAPI;
    
    @Override
    public void onEnable() {
        getLogger().info("§6========================================");
        getLogger().info("§e  DZTradeHub v1.0.0");
        getLogger().info("§e  Developer: DemonZDev");
        getLogger().info("§6========================================");
        
        // Check for DZEconomy
        if (!setupEconomy()) {
            getLogger().severe("DZEconomy not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize utility systems (IMPROVEMENTS)
        online.demonzdevelopment.dztradehub.utils.SecurityLogger.initialize(this);
        online.demonzdevelopment.dztradehub.utils.AuditLogger.initializeAuditTable(this);
        
        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        
        // Start database health checks (IMPROVEMENT-007)
        databaseManager.startHealthCheck();
        fileStorageManager = new online.demonzdevelopment.dztradehub.storage.FileStorageManager(this);
        shopManager = new ShopManager(this);
        permissionManager = new PermissionManager(this);
        kitManager = new KitManager(this);
        auctionManager = new AuctionManager(this);
        queueManager = new QueueManager(this);
        casinoManager = new CasinoManager(this);
        bountyManager = new BountyManager(this);
        updateManager = new online.demonzdevelopment.dztradehub.update.UpdateManager(this);
        stockManager = new StockManager(this);
        dynamicPricingManager = new DynamicPricingManager(this);
        
        // Initialize bank managers
        bankManager = new online.demonzdevelopment.dztradehub.managers.bank.BankManager(this);
        bankAccountManager = new online.demonzdevelopment.dztradehub.managers.bank.BankAccountManager(this, bankManager);
        bankLoanManager = new online.demonzdevelopment.dztradehub.managers.bank.BankLoanManager(this, bankManager, bankAccountManager);
        bankInterestManager = new online.demonzdevelopment.dztradehub.managers.bank.BankInterestManager(this, bankManager, bankAccountManager);
        bankQueueManager = new online.demonzdevelopment.dztradehub.managers.bank.BankQueueManager(this, bankManager);
        
        // Initialize GUIs
        shopGUI = new ShopGUI(this);
        areaGUI = new online.demonzdevelopment.dztradehub.gui.AreaGUI(this);
        auctionBrowserGUI = new AuctionBrowserGUI(this);
        bountyGUI = new online.demonzdevelopment.dztradehub.gui.BountyGUI(this);
        
        // Initialize API
        api = new DZTradeHubAPI(this);
        
        // Register commands
        TradeHubCommand tradeHubCommand = new TradeHubCommand(this);
        getCommand("dzth").setExecutor(tradeHubCommand);
        getCommand("tradehub").setExecutor(tradeHubCommand);
        getCommand("ah").setExecutor(new AuctionHouseCommand(this));
        getCommand("casino").setExecutor(new online.demonzdevelopment.dztradehub.commands.CasinoCommand(this));
        getCommand("coinflip").setExecutor(new online.demonzdevelopment.dztradehub.commands.CoinFlipCommand(this));
        getCommand("jackpot").setExecutor(new online.demonzdevelopment.dztradehub.commands.JackpotCommand(this));
        getCommand("bounty").setExecutor(new online.demonzdevelopment.dztradehub.commands.BountyCommand(this));
        
        // Register bank command
        online.demonzdevelopment.dztradehub.commands.BankCommand bankCommand = new online.demonzdevelopment.dztradehub.commands.BankCommand(this);
        bankCommand.setBankManager(bankManager);
        getCommand("bank").setExecutor(bankCommand);
        getCommand("bank").setTabCompleter(bankCommand);
        
        // Register kits command
        online.demonzdevelopment.dztradehub.commands.KitsCommand kitsCommand = new online.demonzdevelopment.dztradehub.commands.KitsCommand(this);
        getCommand("kits").setExecutor(kitsCommand);
        getCommand("kits").setTabCompleter(kitsCommand);
        
        // Register kit command (for Kit area access)
        online.demonzdevelopment.dztradehub.commands.KitCommand kitCommand = new online.demonzdevelopment.dztradehub.commands.KitCommand(this);
        getCommand("kit").setExecutor(kitCommand);
        
        // Register sell commands
        online.demonzdevelopment.dztradehub.commands.SellCommand sellCommand = new online.demonzdevelopment.dztradehub.commands.SellCommand(this);
        getCommand("sell").setExecutor(sellCommand);
        getCommand("sell").setTabCompleter(sellCommand);
        getCommand("sellall").setExecutor(sellCommand);
        getCommand("sellall").setTabCompleter(sellCommand);
        getCommand("sellhand").setExecutor(sellCommand);
        getCommand("sellhand").setTabCompleter(sellCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ShopGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.AreaGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AuctionGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.CasinoGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.BountyListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.BankGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.SellGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new online.demonzdevelopment.dztradehub.listeners.BankPasswordListener(this), this);
        
        // Load existing areas and shops from file storage
        loadAreasFromStorage();
        
        // Setup default areas and shops if first run
        if (shopManager.getAllAreas().isEmpty()) {
            setupDefaultAreas();
        }
        
        // Load auctions from storage
        auctionManager.loadAuctions();
        
        // Register dynamic area commands
        registerAreaCommands();
        
        // Register dynamic bank commands
        registerBankCommands();
        
        // Start scheduled tasks
        startScheduledTasks();
        
        getLogger().info("§aDZTradeHub enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        // Save all auctions
        if (auctionManager != null) {
            auctionManager.saveAllAuctions();
        }
        
        // Close database connections
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("§cDZTradeHub disabled!");
    }
    
    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("DZEconomy") == null) {
            return false;
        }
        
        try {
            economyAPI = Bukkit.getServicesManager()
                .getRegistration(online.demonzdevelopment.dzeconomy.api.DZEconomyAPI.class)
                .getProvider();
            getLogger().info("§aSuccessfully hooked into DZEconomy!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to hook into DZEconomy: " + e.getMessage());
            return false;
        }
    }
    
    private void loadAreasFromStorage() {
        for (String areaName : fileStorageManager.getAllAreaNames()) {
            Area area = fileStorageManager.loadArea(areaName);
            if (area != null) {
                shopManager.registerArea(area);
                
                // Load shops in this area
                for (String shopName : fileStorageManager.getAllShopNames(areaName)) {
                    Shop shop = fileStorageManager.loadShop(areaName, shopName);
                    if (shop != null) {
                        shopManager.registerShop(areaName, shop);
                        
                        // Load items for this shop
                        shop.getItems().clear();
                        shop.getItems().addAll(fileStorageManager.loadShopItems(areaName, shopName));
                    }
                }
            }
        }
        getLogger().info("§aLoaded " + shopManager.getAllAreas().size() + " areas from storage");
    }
    
    private void setupDefaultAreas() {
        getLogger().info("§eCreating default areas and shops...");
        
        Location defaultLoc = new Location(getServer().getWorlds().get(0), 0, 64, 0);
        
        // Setup default areas with their shops
        DefaultShopsSetup.createSuperMarket(this, defaultLoc);
        DefaultShopsSetup.createBazar(this, defaultLoc);
        DefaultShopsSetup.createPawnShop(this, defaultLoc);
        DefaultShopsSetup.createJunkyard(this, defaultLoc);
        DefaultShopsSetup.createBlackMarket(this, defaultLoc);
        DefaultShopsSetup.createKitsArea(this, defaultLoc);
        
        getLogger().info("§aDefault areas created successfully!");
    }
    
    private void startScheduledTasks() {
        // Update auction prices every hour
        getServer().getScheduler().runTaskTimer(this, () -> {
            auctionManager.updatePrices();
        }, 0L, 72000L); // 72000 ticks = 1 hour
        
        // Check expired auctions every 6 hours
        getServer().getScheduler().runTaskTimer(this, () -> {
            auctionManager.checkExpiredAuctions();
        }, 0L, 432000L); // 432000 ticks = 6 hours
        
        // Cleanup old auctions based on config
        int cleanupHours = configManager.getCleanupCheckInterval();
        getServer().getScheduler().runTaskTimer(this, () -> {
            auctionManager.cleanupOldAuctions();
        }, 72000L, cleanupHours * 72000L); // Run based on config
        
        // Auto-save auctions periodically
        int saveInterval = configManager.getAuctionSaveInterval();
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            auctionManager.saveAllAuctions();
        }, saveInterval * 20L, saveInterval * 20L); // Based on config (seconds)
        
        // Update dynamic pricing every 30 minutes
        getServer().getScheduler().runTaskTimer(this, () -> {
            dynamicPricingManager.updateAllPrices();
        }, 0L, 36000L); // 36000 ticks = 30 minutes
        
        // Process stock restocking every hour
        getServer().getScheduler().runTaskTimer(this, () -> {
            stockManager.processRestocking();
        }, 0L, 72000L); // 72000 ticks = 1 hour
        
        getLogger().info("§aScheduled tasks started!");
        getLogger().info("§aAuction cleanup: every " + cleanupHours + " hours");
        getLogger().info("§aAuction auto-save: every " + saveInterval + " seconds");
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public ShopManager getShopManager() { return shopManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public KitManager getKitManager() { return kitManager; }
    public AuctionManager getAuctionManager() { return auctionManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public CasinoManager getCasinoManager() { return casinoManager; }
    public BountyManager getBountyManager() { return bountyManager; }
    public online.demonzdevelopment.dztradehub.update.UpdateManager getUpdateManager() { return updateManager; }
    public online.demonzdevelopment.dztradehub.storage.FileStorageManager getFileStorageManager() { return fileStorageManager; }
    public StockManager getStockManager() { return stockManager; }
    public DynamicPricingManager getDynamicPricingManager() { return dynamicPricingManager; }
    
    // Getters for bank managers
    public online.demonzdevelopment.dztradehub.managers.bank.BankManager getBankManager() { return bankManager; }
    public online.demonzdevelopment.dztradehub.managers.bank.BankAccountManager getBankAccountManager() { return bankAccountManager; }
    public online.demonzdevelopment.dztradehub.managers.bank.BankLoanManager getBankLoanManager() { return bankLoanManager; }
    public online.demonzdevelopment.dztradehub.managers.bank.BankInterestManager getBankInterestManager() { return bankInterestManager; }
    public online.demonzdevelopment.dztradehub.managers.bank.BankQueueManager getBankQueueManager() { return bankQueueManager; }
    
    // Getters for GUIs
    public ShopGUI getShopGUI() { return shopGUI; }
    public online.demonzdevelopment.dztradehub.gui.AreaGUI getAreaGUI() { return areaGUI; }
    public AuctionBrowserGUI getAuctionBrowserGUI() { return auctionBrowserGUI; }
    public online.demonzdevelopment.dztradehub.gui.BountyGUI getBountyGUI() { return bountyGUI; }
    
    // Getters for APIs
    public DZTradeHubAPI getAPI() { return api; }
    public online.demonzdevelopment.dzeconomy.api.DZEconomyAPI getEconomyAPI() { return economyAPI; }
    
    /**
     * Register dynamic commands for each area (/<area_name>)
     */
    private void registerAreaCommands() {
        for (Area area : shopManager.getAllAreas()) {
            registerAreaCommand(area);
        }
        getLogger().info("§aRegistered " + shopManager.getAllAreas().size() + " area commands");
    }
    
    /**
     * Register a single area command
     */
    public void registerAreaCommand(Area area) {
        try {
            String commandName = area.getName().toLowerCase();
            online.demonzdevelopment.dztradehub.commands.DynamicAreaCommand executor = 
                new online.demonzdevelopment.dztradehub.commands.DynamicAreaCommand(this, area.getName());
            
            // Try to register the command using Bukkit's command map
            org.bukkit.command.CommandMap commandMap = getServer().getCommandMap();
            org.bukkit.command.PluginCommand command = getCommand(commandName);
            
            if (command != null) {
                command.setExecutor(executor);
            } else {
                // Create a new command since it doesn't exist in plugin.yml
                org.bukkit.command.Command newCommand = new org.bukkit.command.Command(commandName) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }
                };
                newCommand.setDescription("Open " + area.getDisplayName());
                newCommand.setUsage("/" + commandName);
                commandMap.register("dztradehub", newCommand);
            }
            
            getLogger().fine("Registered command: /" + commandName);
        } catch (Exception e) {
            getLogger().warning("Failed to register command for area: " + area.getName());
            e.printStackTrace();
        }
    }
    
    /**
     * Unregister area command
     */
    public void unregisterAreaCommand(String areaName) {
        try {
            String commandName = areaName.toLowerCase();
            org.bukkit.command.CommandMap commandMap = getServer().getCommandMap();
            
            // Try to unregister via reflection (Bukkit doesn't provide direct unregister)
            java.lang.reflect.Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, org.bukkit.command.Command> knownCommands = 
                (java.util.Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
            knownCommands.remove(commandName);
            knownCommands.remove("dztradehub:" + commandName);
            
            getLogger().fine("Unregistered command: /" + commandName);
        } catch (Exception e) {
            getLogger().warning("Failed to unregister command for area: " + areaName);
        }
    }
    
    /**
     * Register dynamic commands for each bank (/<bank_name>)
     */
    private void registerBankCommands() {
        for (online.demonzdevelopment.dztradehub.data.bank.Bank bank : bankManager.getAllBanks()) {
            registerBankCommand(bank);
        }
        getLogger().info("§aRegistered " + bankManager.getAllBanks().size() + " bank commands");
    }
    
    /**
     * Register a single bank command
     */
    public void registerBankCommand(online.demonzdevelopment.dztradehub.data.bank.Bank bank) {
        try {
            String commandName = bank.getBankName().toLowerCase();
            online.demonzdevelopment.dztradehub.commands.DynamicBankCommand executor = 
                new online.demonzdevelopment.dztradehub.commands.DynamicBankCommand(this, bank.getBankName());
            executor.setManagers(bankManager, bankAccountManager, bankQueueManager);
            
            // Try to register the command using Bukkit's command map
            org.bukkit.command.CommandMap commandMap = getServer().getCommandMap();
            org.bukkit.command.PluginCommand command = getCommand(commandName);
            
            if (command != null) {
                command.setExecutor(executor);
            } else {
                // Create a new command since it doesn't exist in plugin.yml
                org.bukkit.command.Command newCommand = new org.bukkit.command.Command(commandName) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }
                };
                newCommand.setDescription("Access " + bank.getDisplayName());
                newCommand.setUsage("/" + commandName);
                commandMap.register("dztradehub", newCommand);
            }
            
            getLogger().fine("Registered bank command: /" + commandName);
        } catch (Exception e) {
            getLogger().warning("Failed to register command for bank: " + bank.getBankName());
            e.printStackTrace();
        }
    }
    
    /**
     * Unregister bank command
     */
    public void unregisterBankCommand(String bankName) {
        unregisterAreaCommand(bankName); // Uses same method as area commands
    }
}
