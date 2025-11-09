package online.demonzdevelopment.dztradehub.listeners;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.data.bank.BankAccount;
import online.demonzdevelopment.dztradehub.data.bank.BankTransactionType;
import online.demonzdevelopment.dztradehub.gui.bank.*;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankGUIListener implements Listener {
    private final DZTradeHub plugin;
    
    // Chat input tracking
    private final Map<UUID, ChatInputState> chatInputs = new ConcurrentHashMap<>();
    
    // GUI instances
    private final BankListGUI bankListGUI;
    private final BankAccountGUI bankAccountGUI;
    private final BankDepositGUI bankDepositGUI;
    private final BankWithdrawGUI bankWithdrawGUI;
    private final BankTransferGUI bankTransferGUI;
    private final BankConfigGUI bankConfigGUI;

    public BankGUIListener(DZTradeHub plugin) {
        this.plugin = plugin;
        this.bankListGUI = new BankListGUI(plugin);
        this.bankAccountGUI = new BankAccountGUI(plugin);
        this.bankDepositGUI = new BankDepositGUI(plugin);
        this.bankWithdrawGUI = new BankWithdrawGUI(plugin);
        this.bankTransferGUI = new BankTransferGUI(plugin);
        this.bankConfigGUI = new BankConfigGUI(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // SECURITY: Re-check permission on every GUI click (MEDIUM-003 fix)
        if (!player.hasPermission("dztradehub.bank")) {
            player.closeInventory();
            player.sendMessage("§c✗ You no longer have permission to access this!");
            event.setCancelled(true);
            return;
        }
        
        Inventory inv = event.getInventory();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Handle different GUI types
        if (inv.getHolder() instanceof BankListGUI.BankListHolder holder) {
            event.setCancelled(true);
            handleBankListClick(player, holder, clicked, event.getSlot());
        } else if (inv.getHolder() instanceof BankAccountGUI.BankAccountHolder holder) {
            event.setCancelled(true);
            handleBankAccountClick(player, holder, clicked, event.getSlot());
        } else if (inv.getHolder() instanceof BankDepositGUI.DepositHolder holder) {
            event.setCancelled(true);
            handleDepositClick(player, holder, clicked, event.getSlot(), event.isRightClick(), event.isShiftClick());
        } else if (inv.getHolder() instanceof BankWithdrawGUI.WithdrawHolder holder) {
            event.setCancelled(true);
            handleWithdrawClick(player, holder, clicked, event.getSlot(), event.isRightClick(), event.isShiftClick());
        } else if (inv.getHolder() instanceof BankTransferGUI.TransferHolder holder) {
            event.setCancelled(true);
            handleTransferClick(player, holder, clicked, event.getSlot());
        } else if (inv.getHolder() instanceof BankConfigGUI.BankConfigHolder holder) {
            event.setCancelled(true);
            handleConfigClick(player, holder, clicked, event.getSlot());
        }
    }

    private void handleBankListClick(Player player, BankListGUI.BankListHolder holder, ItemStack clicked, int slot) {
        // Navigation
        if (slot == 45) { // Previous page
            bankListGUI.open(player, holder.getPage() - 1);
        } else if (slot == 53) { // Next page
            bankListGUI.open(player, holder.getPage() + 1);
        } else if (slot == 50) { // Close
            player.closeInventory();
        } else if (slot >= 9 && slot <= 44) { // Bank selection
            List<Bank> banks = new ArrayList<>(plugin.getBankManager().getAllBanks());
            int index = holder.getPage() * 36 + (slot - 9);
            if (index < banks.size()) {
                Bank bank = banks.get(index);
                openBankAccessOrCreate(player, bank);
            }
        }
    }

    private void openBankAccessOrCreate(Player player, Bank bank) {
        BankAccount account = plugin.getBankAccountManager().getPlayerAccountAtBank(
            player.getUniqueId(), bank.getBankId()
        );

        if (account == null) {
            // Show account creation info
            player.closeInventory();
            player.sendMessage(Component.text("§6§l═══ " + bank.getDisplayName() + " ═══"));
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("§eYou don't have an account at this bank."));
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("§7To create an account:"));
            player.sendMessage(Component.text("§f/" + bank.getBankName() + " create <type> <password>"));
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("§7Types: §fSAVINGS, INTEREST, BUSINESS"));
        } else {
            // Join queue and access
            if (plugin.getBankQueueManager().joinBankQueue(player, bank)) {
                if (plugin.getBankQueueManager().isInActiveSlot(player, bank)) {
                    // Prompt for password
                    promptForPassword(player, bank, account, "access");
                } else {
                    MessageUtil.sendInfo(player, "Joined queue for " + bank.getDisplayName());
                }
            }
        }
    }

    private void handleBankAccountClick(Player player, BankAccountGUI.BankAccountHolder holder, 
                                       ItemStack clicked, int slot) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();

        switch (slot) {
            case 29 -> bankDepositGUI.open(player, bank, account); // Deposit
            case 30 -> bankWithdrawGUI.open(player, bank, account); // Withdraw
            case 31 -> bankTransferGUI.open(player, bank, account); // Transfer
            case 32 -> {
                if (bank.isCurrencyConversionEnabled()) {
                    // TODO: Open currency conversion GUI
                    MessageUtil.sendInfo(player, "Currency conversion GUI (Coming soon)");
                }
            }
            case 33 -> {
                if (bank.isLoansEnabled()) {
                    // TODO: Open loans GUI
                    MessageUtil.sendInfo(player, "Loans GUI (Coming soon)");
                }
            }
            case 47 -> {
                // TODO: Open account settings GUI
                MessageUtil.sendInfo(player, "Account settings GUI (Coming soon)");
            }
            case 48 -> {
                // TODO: Open transaction history
                MessageUtil.sendInfo(player, "Transaction history (Coming soon)");
            }
            case 49 -> player.closeInventory(); // Close
        }
    }

    private void handleDepositClick(Player player, BankDepositGUI.DepositHolder holder, 
                                   ItemStack clicked, int slot, boolean rightClick, boolean shiftClick) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();

        if (slot == 18) { // Back
            bankAccountGUI.open(player, bank, account);
        } else if (slot == 26) { // Cancel
            player.closeInventory();
        } else if (slot == 22) { // Confirm deposit
            executeDeposit(player, holder);
        } else if (slot >= 10 && slot <= 12) { // Currency selection
            CurrencyType[] currencies = CurrencyType.values();
            int currencyIndex = slot - 10;
            
            List<CurrencyType> enabledCurrencies = new ArrayList<>(bank.getEnabledCurrencies());
            if (currencyIndex < enabledCurrencies.size()) {
                CurrencyType currency = enabledCurrencies.get(currencyIndex);
                
                if (rightClick && shiftClick) {
                    // Deposit half
                    double pocket = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
                    holder.getAmounts().put(currency, pocket / 2.0);
                    bankDepositGUI.open(player, bank, account);
                } else if (rightClick) {
                    // Deposit all
                    double pocket = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
                    holder.getAmounts().put(currency, pocket);
                    bankDepositGUI.open(player, bank, account);
                } else {
                    // Prompt for amount
                    player.closeInventory();
                    promptForAmount(player, bank, account, currency, "deposit");
                }
            }
        }
    }

    private void handleWithdrawClick(Player player, BankWithdrawGUI.WithdrawHolder holder, 
                                    ItemStack clicked, int slot, boolean rightClick, boolean shiftClick) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();

        if (slot == 18) { // Back
            bankAccountGUI.open(player, bank, account);
        } else if (slot == 26) { // Cancel
            player.closeInventory();
        } else if (slot == 22) { // Confirm withdraw
            executeWithdraw(player, holder);
        } else if (slot >= 10 && slot <= 12) { // Currency selection
            List<CurrencyType> enabledCurrencies = new ArrayList<>(bank.getEnabledCurrencies());
            int currencyIndex = slot - 10;
            
            if (currencyIndex < enabledCurrencies.size()) {
                CurrencyType currency = enabledCurrencies.get(currencyIndex);
                
                if (rightClick && shiftClick) {
                    // Withdraw half
                    double bankBalance = account.getBalance(currency);
                    holder.getAmounts().put(currency, bankBalance / 2.0);
                    bankWithdrawGUI.open(player, bank, account);
                } else if (rightClick) {
                    // Withdraw all
                    double bankBalance = account.getBalance(currency);
                    holder.getAmounts().put(currency, bankBalance);
                    bankWithdrawGUI.open(player, bank, account);
                } else {
                    // Prompt for amount
                    player.closeInventory();
                    promptForAmount(player, bank, account, currency, "withdraw");
                }
            }
        }
    }

    private void handleTransferClick(Player player, BankTransferGUI.TransferHolder holder, 
                                    ItemStack clicked, int slot) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();

        if (slot == 18) { // Back
            if (holder.getTargetType() == null) {
                bankAccountGUI.open(player, bank, account);
            } else {
                bankTransferGUI.open(player, bank, account);
            }
        } else if (slot == 26) { // Close
            player.closeInventory();
        } else if (slot == 11) { // First option
            if (holder.getTargetType() == null) {
                // Account transfer selected
                bankTransferGUI.openOwnerSelection(player, bank, account, "account");
            } else {
                // Own account selected
                // TODO: Show own accounts
                MessageUtil.sendInfo(player, "Show own accounts (Coming soon)");
            }
        } else if (slot == 15) { // Second option
            if (holder.getTargetType() == null) {
                // Bank transfer selected
                bankTransferGUI.openOwnerSelection(player, bank, account, "bank");
            } else {
                // Other player selected
                player.closeInventory();
                MessageUtil.sendInfo(player, "Enter player name in chat:");
                // TODO: Setup chat listener
            }
        }
    }

    private void handleConfigClick(Player player, BankConfigGUI.BankConfigHolder holder, 
                                  ItemStack clicked, int slot) {
        Bank bank = holder.getBank();

        switch (slot) {
            case 11 -> { // Currency conversion toggle
                if (bank.getEnabledCurrencies().size() >= 2) {
                    bank.setCurrencyConversionEnabled(!bank.isCurrencyConversionEnabled());
                    bankConfigGUI.open(player, bank);
                }
            }
            case 12 -> { // Account transfer toggle
                bank.setAccountTransferEnabled(!bank.isAccountTransferEnabled());
                bankConfigGUI.open(player, bank);
            }
            case 13 -> { // Bank transfer toggle
                bank.setBankTransferEnabled(!bank.isBankTransferEnabled());
                bankConfigGUI.open(player, bank);
            }
            case 15 -> { // Loans toggle
                bank.setLoansEnabled(!bank.isLoansEnabled());
                bankConfigGUI.open(player, bank);
            }
            case 48 -> { // Save
                plugin.getBankManager().getConfigManager().saveBankToConfig(bank);
                MessageUtil.sendSuccess(player, "Bank configuration saved!");
                player.closeInventory();
            }
            case 49 -> player.closeInventory(); // Close
        }
    }

    private void executeDeposit(Player player, BankDepositGUI.DepositHolder holder) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();
        Map<CurrencyType, Double> amounts = holder.getAmounts();

        if (amounts.isEmpty()) {
            MessageUtil.sendError(player, "No amounts specified!");
            return;
        }

        for (var entry : amounts.entrySet()) {
            CurrencyType currency = entry.getKey();
            double amount = entry.getValue();

            if (amount <= 0) continue;

            // Check pocket balance
            double pocket = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
            if (pocket < amount) {
                MessageUtil.sendError(player, "Insufficient " + currency.name() + " in pocket!");
                continue;
            }

            // Calculate tax
            double tax = calculateDepositTax(bank, account, amount);
            double afterTax = amount - tax;

            // Execute transaction
            plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), currency, amount);
            account.addBalance(currency, afterTax);
            account.incrementTransactions();

            // Save transaction
            plugin.getBankAccountManager().saveAccount(account);
            plugin.getBankAccountManager().recordTransaction(
                account, currency, amount, afterTax, BankTransactionType.DEPOSIT
            );

            MessageUtil.sendSuccess(player, "Deposited " + String.format("%.2f", amount) + " " + 
                currency.name() + " (Tax: " + String.format("%.2f", tax) + ")");
        }

        player.closeInventory();
        bankAccountGUI.open(player, bank, account);
    }

    private void executeWithdraw(Player player, BankWithdrawGUI.WithdrawHolder holder) {
        Bank bank = holder.getBank();
        BankAccount account = holder.getAccount();
        Map<CurrencyType, Double> amounts = holder.getAmounts();

        if (amounts.isEmpty()) {
            MessageUtil.sendError(player, "No amounts specified!");
            return;
        }

        for (var entry : amounts.entrySet()) {
            CurrencyType currency = entry.getKey();
            double amount = entry.getValue();

            if (amount <= 0) continue;

            // Check bank balance
            if (!account.hasBalance(currency, amount)) {
                MessageUtil.sendError(player, "Insufficient " + currency.name() + " in bank!");
                continue;
            }

            // Calculate tax
            double tax = calculateWithdrawTax(bank, account, amount);
            double afterTax = amount - tax;

            // Execute transaction
            account.deductBalance(currency, amount);
            plugin.getEconomyAPI().addCurrency(player.getUniqueId(), currency, afterTax);
            account.incrementTransactions();

            // Save transaction
            plugin.getBankAccountManager().saveAccount(account);
            plugin.getBankAccountManager().recordTransaction(
                account, currency, amount, afterTax, BankTransactionType.WITHDRAWAL
            );

            MessageUtil.sendSuccess(player, "Withdrew " + String.format("%.2f", amount) + " " + 
                currency.name() + " (Tax: " + String.format("%.2f", tax) + ")");
        }

        player.closeInventory();
        bankAccountGUI.open(player, bank, account);
    }

    private double calculateDepositTax(Bank bank, BankAccount account, double amount) {
        double baseTax = bank.getGlobalDepositTax();
        double multiplier = account.getAccountType().getDepositTaxMultiplier();
        
        var levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            double reduction = levelConfig.getDepositTaxReduction();
            baseTax = baseTax * (1 - reduction / 100.0);
        }
        
        return amount * (baseTax * multiplier / 100.0);
    }

    private double calculateWithdrawTax(Bank bank, BankAccount account, double amount) {
        double baseTax = bank.getGlobalWithdrawalTax();
        double multiplier = account.getAccountType().getWithdrawalTaxMultiplier();
        
        var levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            double reduction = levelConfig.getWithdrawalTaxReduction();
            baseTax = baseTax * (1 - reduction / 100.0);
        }
        
        return amount * (baseTax * multiplier / 100.0);
    }

    private void promptForPassword(Player player, Bank bank, BankAccount account, String action) {
        player.closeInventory();
        player.sendMessage(Component.text("§e§lEnter your password:"));
        player.sendMessage(Component.text("§7(or type 'cancel' to exit)"));
        
        chatInputs.put(player.getUniqueId(), new ChatInputState(
            "password", bank, account, null, action
        ));
    }

    private void promptForAmount(Player player, Bank bank, BankAccount account, 
                                CurrencyType currency, String action) {
        player.sendMessage(Component.text("§e§lEnter amount to " + action + ":"));
        player.sendMessage(Component.text("§7(or type 'cancel' to exit)"));
        
        chatInputs.put(player.getUniqueId(), new ChatInputState(
            "amount", bank, account, currency, action
        ));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputState state = chatInputs.get(player.getUniqueId());
        
        if (state == null) return;
        
        event.setCancelled(true);
        String input = event.getMessage().trim();
        
        if (input.equalsIgnoreCase("cancel")) {
            chatInputs.remove(player.getUniqueId());
            player.sendMessage(Component.text("§cCancelled"));
            return;
        }

        if (state.type.equals("password")) {
            handlePasswordInput(player, state, input);
        } else if (state.type.equals("amount")) {
            handleAmountInput(player, state, input);
        }
    }

    private void handlePasswordInput(Player player, ChatInputState state, String password) {
        if (plugin.getBankAccountManager().verifyPassword(state.account, password)) {
            chatInputs.remove(player.getUniqueId());
            player.sendMessage(Component.text("§aPassword verified!"));
            
            // Open account GUI
            plugin.getServer().getScheduler().runTask(plugin, () -> 
                bankAccountGUI.open(player, state.bank, state.account)
            );
        } else {
            player.sendMessage(Component.text("§cIncorrect password! Try again or type 'cancel'"));
        }
    }

    private void handleAmountInput(Player player, ChatInputState state, String input) {
        try {
            double amount = Double.parseDouble(input);
            
            if (amount <= 0) {
                player.sendMessage(Component.text("§cAmount must be positive!"));
                return;
            }

            chatInputs.remove(player.getUniqueId());
            player.sendMessage(Component.text("§aAmount set: " + String.format("%.2f", amount)));
            
            // Reopen GUI with amount set
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (state.action.equals("deposit")) {
                    BankDepositGUI depositGUI = new BankDepositGUI(plugin);
                    depositGUI.open(player, state.bank, state.account);
                } else if (state.action.equals("withdraw")) {
                    BankWithdrawGUI withdrawGUI = new BankWithdrawGUI(plugin);
                    withdrawGUI.open(player, state.bank, state.account);
                }
            });
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("§cInvalid number! Try again or type 'cancel'"));
        }
    }

    // Helper class for chat input state
    private static class ChatInputState {
        private final String type; // "password", "amount", "player"
        private final Bank bank;
        private final BankAccount account;
        private final CurrencyType currency;
        private final String action;

        public ChatInputState(String type, Bank bank, BankAccount account, 
                            CurrencyType currency, String action) {
            this.type = type;
            this.bank = bank;
            this.account = account;
            this.currency = currency;
            this.action = action;
        }
    }
}
