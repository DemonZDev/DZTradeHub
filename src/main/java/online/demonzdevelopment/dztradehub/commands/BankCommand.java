package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.managers.bank.BankManager;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BankCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private BankManager bankManager;
    
    public BankCommand(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    public void setBankManager(BankManager bankManager) {
        this.bankManager = bankManager;
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
        
        // /bank - Open bank list GUI
        if (args.length == 0) {
            openBankListGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                sendHelp(player);
                break;
                
            case "list":
                listBanks(player);
                break;
                
            case "create":
                if (!player.hasPermission("dztradehub.bank.admin")) {
                    MessageUtil.sendError(player, "You don't have permission to create banks!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /bank create <bank_name>");
                    return true;
                }
                createBank(player, args[1]);
                break;
                
            case "delete":
                if (!player.hasPermission("dztradehub.bank.admin")) {
                    MessageUtil.sendError(player, "You don't have permission to delete banks!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /bank delete <bank_name>");
                    return true;
                }
                deleteBank(player, args[1]);
                break;
                
            case "rename":
                if (!player.hasPermission("dztradehub.bank.admin")) {
                    MessageUtil.sendError(player, "You don't have permission to rename banks!");
                    return true;
                }
                if (args.length < 3) {
                    MessageUtil.sendError(player, "Usage: /bank rename <old_name> <new_name>");
                    return true;
                }
                renameBank(player, args[1], args[2]);
                break;
                
            case "config":
                if (!player.hasPermission("dztradehub.bank.admin")) {
                    MessageUtil.sendError(player, "You don't have permission to configure banks!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /bank config <bank_name>");
                    return true;
                }
                openBankConfigGUI(player, args[1]);
                break;
                
            default:
                MessageUtil.sendError(player, "Unknown subcommand! Use /bank help");
                break;
        }
        
        return true;
    }
    
    private void openBankListGUI(Player player) {
        online.demonzdevelopment.dztradehub.gui.bank.BankListGUI gui = 
            new online.demonzdevelopment.dztradehub.gui.bank.BankListGUI(plugin);
        gui.open(player);
    }
    
    private void listBanks(Player player) {
        player.sendMessage("§6§l════════ Available Banks ════════");
        player.sendMessage("");
        
        List<Bank> banks = new ArrayList<>(bankManager.getAllBanks());
        if (banks.isEmpty()) {
            player.sendMessage("§7No banks available");
        } else {
            for (Bank bank : banks) {
                player.sendMessage("§e▸ " + bank.getDisplayName());
                player.sendMessage("  §7Command: §f/" + bank.getBankName());
                player.sendMessage("  §7Currencies: §f" + 
                    bank.getEnabledCurrencies().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
                player.sendMessage("");
            }
        }
        
        player.sendMessage("§7Use §f/<bank_name> §7to access a bank");
    }
    
    private void createBank(Player player, String bankName) {
        if (bankManager.bankExists(bankName)) {
            MessageUtil.sendError(player, "A bank with this name already exists!");
            return;
        }
        
        // Load config and create bank
        Bank bank = bankManager.getConfigManager().loadBankFromConfig(bankName);
        if (bank == null) {
            MessageUtil.sendError(player, "Could not load configuration for bank: " + bankName);
            MessageUtil.sendInfo(player, "Make sure a config file exists at banks/" + bankName + ".yml");
            return;
        }
        
        if (bankManager.createBank(bank)) {
            MessageUtil.sendSuccess(player, "Bank created successfully: " + bank.getDisplayName());
            MessageUtil.sendInfo(player, "Players can now access it with: /" + bankName);
        } else {
            MessageUtil.sendError(player, "Failed to create bank!");
        }
    }
    
    private void deleteBank(Player player, String bankName) {
        if (!bankManager.bankExists(bankName)) {
            MessageUtil.sendError(player, "Bank not found: " + bankName);
            return;
        }
        
        // TODO: Add confirmation GUI
        if (bankManager.deleteBank(bankName)) {
            MessageUtil.sendSuccess(player, "Bank deleted: " + bankName);
        } else {
            MessageUtil.sendError(player, "Failed to delete bank! It may have active accounts.");
        }
    }
    
    private void renameBank(Player player, String oldName, String newName) {
        if (!bankManager.bankExists(oldName)) {
            MessageUtil.sendError(player, "Bank not found: " + oldName);
            return;
        }
        
        if (bankManager.bankExists(newName)) {
            MessageUtil.sendError(player, "A bank with name '" + newName + "' already exists!");
            return;
        }
        
        if (bankManager.renameBank(oldName, newName)) {
            MessageUtil.sendSuccess(player, "Bank renamed: " + oldName + " → " + newName);
        } else {
            MessageUtil.sendError(player, "Failed to rename bank!");
        }
    }
    
    private void openBankConfigGUI(Player player, String bankName) {
        Bank bank = bankManager.getBankByName(bankName);
        if (bank == null) {
            MessageUtil.sendError(player, "Bank not found: " + bankName);
            return;
        }
        
        online.demonzdevelopment.dztradehub.gui.bank.BankConfigGUI gui = 
            new online.demonzdevelopment.dztradehub.gui.bank.BankConfigGUI(plugin);
        gui.open(player, bank);
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l═══════ Bank Commands ═══════");
        player.sendMessage("");
        player.sendMessage("§e§lPlayer Commands:");
        player.sendMessage("§e/bank §7- Open bank list");
        player.sendMessage("§e/bank list §7- List all banks");
        player.sendMessage("§e/<bank_name> §7- Access specific bank");
        player.sendMessage("");
        
        if (player.hasPermission("dztradehub.bank.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§c/bank create <name> §7- Create new bank");
            player.sendMessage("§c/bank delete <name> §7- Delete bank");
            player.sendMessage("§c/bank rename <old> <new> §7- Rename bank");
            player.sendMessage("§c/bank config <name> §7- Configure bank");
            player.sendMessage("");
        }
        
        player.sendMessage("§7Banks protect your money from death!");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "list");
            
            if (sender.hasPermission("dztradehub.bank.admin")) {
                subCommands = Arrays.asList("help", "list", "create", "delete", "rename", "config");
            }
            
            return subCommands.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && sender.hasPermission("dztradehub.bank.admin")) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("delete") || subCmd.equals("config") || subCmd.equals("rename")) {
                return new ArrayList<>(bankManager.getAllBankNames()).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}
