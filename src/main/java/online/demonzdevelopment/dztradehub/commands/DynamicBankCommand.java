package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.AccountType;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.data.bank.BankAccount;
import online.demonzdevelopment.dztradehub.managers.bank.BankAccountManager;
import online.demonzdevelopment.dztradehub.managers.bank.BankManager;
import online.demonzdevelopment.dztradehub.managers.bank.BankQueueManager;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamicBankCommand implements CommandExecutor {
    private final DZTradeHub plugin;
    private final String bankName;
    private BankManager bankManager;
    private BankAccountManager accountManager;
    private BankQueueManager queueManager;
    
    // Password input tracking
    private final Map<UUID, PasswordInputState> passwordInputs;
    
    public DynamicBankCommand(DZTradeHub plugin, String bankName) {
        this.plugin = plugin;
        this.bankName = bankName;
        this.passwordInputs = new HashMap<>();
    }
    
    public void setManagers(BankManager bankManager, BankAccountManager accountManager, BankQueueManager queueManager) {
        this.bankManager = bankManager;
        this.accountManager = accountManager;
        this.queueManager = queueManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (bankManager == null) {
            MessageUtil.sendError(player, "Bank system is not initialized!");
            return true;
        }
        
        Bank bank = bankManager.getBankByName(bankName);
        if (bank == null) {
            MessageUtil.sendError(player, "Bank not found!");
            return true;
        }
        
        // /<bank_name> - Open bank (join queue or access account)
        if (args.length == 0) {
            handleBankAccess(player, bank);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                if (args.length < 3) {
                    MessageUtil.sendError(player, "Usage: /" + bankName + " create <account_type> <password>");
                    player.sendMessage("§7Account types: SAVINGS, INTEREST, BUSINESS");
                    return true;
                }
                createAccount(player, bank, args[1], args[2]);
                break;
                
            case "help":
                sendHelp(player, bank);
                break;
                
            default:
                MessageUtil.sendError(player, "Unknown command! Use /" + bankName + " help");
                break;
        }
        
        return true;
    }
    
    private void handleBankAccess(Player player, Bank bank) {
        // Check if player has an account
        BankAccount account = accountManager.getPlayerAccountAtBank(player.getUniqueId(), bank.getBankId());
        
        if (account == null) {
            // No account - show account creation options
            showAccountCreationInfo(player, bank);
        } else {
            // Has account - join queue or ask for password
            joinBankQueue(player, bank, account);
        }
    }
    
    private void showAccountCreationInfo(Player player, Bank bank) {
        player.sendMessage("§6§l═══ " + bank.getDisplayName() + " ═══");
        player.sendMessage("");
        player.sendMessage("§eYou don't have an account at this bank.");
        player.sendMessage("");
        player.sendMessage("§7Available Account Types:");
        
        for (AccountType type : bank.getAvailableAccountTypes()) {
            player.sendMessage("§e▸ " + type.getDisplayName());
            player.sendMessage("  §7" + type.getDescription());
        }
        
        player.sendMessage("");
        player.sendMessage("§7To create an account:");
        player.sendMessage("§f/" + bank.getBankName() + " create <type> <password>");
        player.sendMessage("");
        player.sendMessage("§7Example: §f/" + bank.getBankName() + " create SAVINGS mypass123");
    }
    
    private void createAccount(Player player, Bank bank, String typeStr, String password) {
        // Parse account type
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid account type: " + typeStr);
            player.sendMessage("§7Valid types: SAVINGS, INTEREST, BUSINESS");
            return;
        }
        
        // Check if type is available
        if (!bank.isAccountTypeAvailable(accountType)) {
            MessageUtil.sendError(player, "This account type is not available at this bank!");
            return;
        }
        
        // Check if player already has max accounts
        int currentAccounts = accountManager.getPlayerAccountCount(player.getUniqueId());
        int maxAccounts = bankManager.getConfigManager().getMaxAccountsPerPlayer();
        if (currentAccounts >= maxAccounts) {
            MessageUtil.sendError(player, "You already have the maximum number of accounts (" + maxAccounts + ")!");
            return;
        }
        
        // Check account creation costs
        boolean canAfford = true;
        StringBuilder costMsg = new StringBuilder("§eAccount creation cost:\n");
        
        for (var entry : bank.getAccountCreationCost().entrySet()) {
            double cost = entry.getValue();
            if (cost > 0) {
                double balance = plugin.getEconomyAPI().getBalance(player.getUniqueId(), entry.getKey());
                String status = balance >= cost ? "§a✓" : "§c✗";
                costMsg.append(status).append(" §7").append(entry.getKey().name()).append(": §f")
                       .append(String.format("%.2f", cost)).append("\n");
                if (balance < cost) {
                    canAfford = false;
                }
            }
        }
        
        if (!canAfford) {
            player.sendMessage(costMsg.toString());
            MessageUtil.sendError(player, "You cannot afford to create an account!");
            return;
        }
        
        // Deduct costs
        for (var entry : bank.getAccountCreationCost().entrySet()) {
            double cost = entry.getValue();
            if (cost > 0) {
                plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), entry.getKey(), cost);
            }
        }
        
        // Create account
        BankAccount account = accountManager.createAccount(
            player.getUniqueId(),
            player.getName(),
            bank,
            accountType,
            password
        );
        
        if (account != null) {
            MessageUtil.sendSuccess(player, "Account created successfully!");
            player.sendMessage("§eBank: §f" + bank.getDisplayName());
            player.sendMessage("§eAccount Type: §f" + accountType.getDisplayName());
            player.sendMessage("§eAccount Level: §f" + account.getAccountLevel());
            player.sendMessage("");
            player.sendMessage("§7Use §f/" + bank.getBankName() + " §7to access your account");
        } else {
            MessageUtil.sendError(player, "Failed to create account! Password may be invalid.");
            player.sendMessage("§7Password must be 4-32 characters");
            
            // Refund costs
            for (var entry : bank.getAccountCreationCost().entrySet()) {
                double cost = entry.getValue();
                if (cost > 0) {
                    plugin.getEconomyAPI().addCurrency(player.getUniqueId(), entry.getKey(), cost);
                }
            }
        }
    }
    
    private void joinBankQueue(Player player, Bank bank, BankAccount account) {
        // Check if account is locked
        if (account.isLocked()) {
            MessageUtil.sendError(player, "Your account is locked! Contact an administrator.");
            return;
        }
        
        // Join queue
        if (queueManager.joinBankQueue(player, bank)) {
            // If successfully joined active slot, prompt for password
            if (queueManager.isInActiveSlot(player, bank)) {
                promptForPassword(player, bank, account);
            }
        }
    }
    
    private void promptForPassword(Player player, Bank bank, BankAccount account) {
        player.sendMessage("§6§l═══ " + bank.getDisplayName() + " ═══");
        player.sendMessage("");
        player.sendMessage("§ePlease enter your password in chat:");
        player.sendMessage("§7(Type 'cancel' to exit)");
        
        // Store password input state
        passwordInputs.put(player.getUniqueId(), new PasswordInputState(bank, account));
        
        // TODO: Implement password listener
        // For now, just open the account GUI
        // openBankAccountGUI(player, bank, account);
    }
    
    private void sendHelp(Player player, Bank bank) {
        player.sendMessage("§6§l═══ " + bank.getDisplayName() + " Help ═══");
        player.sendMessage("");
        player.sendMessage("§e/" + bank.getBankName() + " §7- Access your account");
        player.sendMessage("§e/" + bank.getBankName() + " create <type> <password> §7- Create account");
        player.sendMessage("");
        player.sendMessage("§7Account Types: §fSAVINGS, INTEREST, BUSINESS");
        player.sendMessage("");
        player.sendMessage("§7Enabled Currencies:");
        for (var currency : bank.getEnabledCurrencies()) {
            player.sendMessage("§e▸ §f" + currency.name());
        }
    }
    
    // Helper class for password input state
    private static class PasswordInputState {
        private final Bank bank;
        private final BankAccount account;
        private final long timestamp;
        
        public PasswordInputState(Bank bank, BankAccount account) {
            this.bank = bank;
            this.account = account;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Bank getBank() { return bank; }
        public BankAccount getAccount() { return account; }
        public long getTimestamp() { return timestamp; }
    }
}
