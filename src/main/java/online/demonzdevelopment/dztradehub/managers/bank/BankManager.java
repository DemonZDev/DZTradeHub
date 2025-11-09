package online.demonzdevelopment.dztradehub.managers.bank;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.Bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankManager {
    private final DZTradeHub plugin;
    private final BankConfigManager configManager;
    private final Map<UUID, Bank> banksById;
    private final Map<String, Bank> banksByName;
    
    public BankManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.configManager = new BankConfigManager(plugin);
        this.banksById = new ConcurrentHashMap<>();
        this.banksByName = new ConcurrentHashMap<>();
        
        loadBanksFromDatabase();
        
        // Create default banks if needed
        if (shouldCreateDefaultBanks()) {
            createDefaultBanks();
        }
    }
    
    /**
     * Load all banks from database
     */
    private void loadBanksFromDatabase() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM banks";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                UUID bankId = UUID.fromString(rs.getString("bank_id"));
                String bankName = rs.getString("bank_name");
                
                // Load bank configuration from YAML
                Bank bank = configManager.loadBankFromConfig(bankName);
                if (bank != null) {
                    bank.setBankId(bankId);
                    bank.setDisplayName(rs.getString("display_name"));
                    bank.setCreatedTime(rs.getLong("created_time"));
                    
                    registerBank(bank);
                }
            }
            
            plugin.getLogger().info("Loaded " + banksById.size() + " banks from database");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load banks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if default banks should be created
     */
    private boolean shouldCreateDefaultBanks() {
        return banksByName.isEmpty() && configManager.shouldAutoCreateDefaultBanks();
    }
    
    /**
     * Create default banks from configuration
     */
    private void createDefaultBanks() {
        List<String> defaultBanks = configManager.getDefaultBanks();
        plugin.getLogger().info("Creating " + defaultBanks.size() + " default banks...");
        
        for (String bankName : defaultBanks) {
            if (!bankExists(bankName)) {
                Bank bank = configManager.loadBankFromConfig(bankName);
                if (bank != null) {
                    createBank(bank);
                    plugin.getLogger().info("Created default bank: " + bankName);
                }
            }
        }
    }
    
    /**
     * Create a new bank
     */
    public boolean createBank(Bank bank) {
        if (bankExists(bank.getBankName())) {
            return false;
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO banks (bank_id, bank_name, display_name, created_time) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bank.getBankId().toString());
            stmt.setString(2, bank.getBankName());
            stmt.setString(3, bank.getDisplayName());
            stmt.setLong(4, bank.getCreatedTime());
            stmt.executeUpdate();
            
            registerBank(bank);
            
            // Register dynamic command for this bank
            plugin.registerBankCommand(bank);
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create bank: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a bank
     */
    public boolean deleteBank(String bankName) {
        Bank bank = getBankByName(bankName);
        if (bank == null) {
            return false;
        }
        
        // Check if bank has any accounts
        // TODO: Add check for existing accounts
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM banks WHERE bank_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, bank.getBankId().toString());
            stmt.executeUpdate();
            
            unregisterBank(bank);
            
            // Unregister dynamic command
            plugin.unregisterBankCommand(bankName);
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete bank: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Rename a bank
     */
    public boolean renameBank(String oldName, String newName) {
        Bank bank = getBankByName(oldName);
        if (bank == null || bankExists(newName)) {
            return false;
        }
        
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE banks SET bank_name = ? WHERE bank_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newName);
            stmt.setString(2, bank.getBankId().toString());
            stmt.executeUpdate();
            
            // Update in memory
            banksByName.remove(oldName);
            bank.setBankName(newName);
            banksByName.put(newName, bank);
            
            // Update commands
            plugin.unregisterBankCommand(oldName);
            plugin.registerBankCommand(bank);
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to rename bank: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Register a bank in memory
     */
    private void registerBank(Bank bank) {
        banksById.put(bank.getBankId(), bank);
        banksByName.put(bank.getBankName().toLowerCase(), bank);
    }
    
    /**
     * Unregister a bank from memory
     */
    private void unregisterBank(Bank bank) {
        banksById.remove(bank.getBankId());
        banksByName.remove(bank.getBankName().toLowerCase());
    }
    
    /**
     * Check if a bank exists
     */
    public boolean bankExists(String bankName) {
        return banksByName.containsKey(bankName.toLowerCase());
    }
    
    /**
     * Get bank by ID
     */
    public Bank getBankById(UUID bankId) {
        return banksById.get(bankId);
    }
    
    /**
     * Get bank by name
     */
    public Bank getBankByName(String bankName) {
        return banksByName.get(bankName.toLowerCase());
    }
    
    /**
     * Get all banks
     */
    public Collection<Bank> getAllBanks() {
        return banksById.values();
    }
    
    /**
     * Get all bank names
     */
    public Set<String> getAllBankNames() {
        return new HashSet<>(banksByName.keySet());
    }
    
    /**
     * Get config manager
     */
    public BankConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Reload all banks
     */
    public void reloadBanks() {
        banksById.clear();
        banksByName.clear();
        loadBanksFromDatabase();
    }
}
