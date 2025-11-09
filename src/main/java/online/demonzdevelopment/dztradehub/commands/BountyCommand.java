package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Bounty;
import online.demonzdevelopment.dztradehub.gui.BountyGUI;
import online.demonzdevelopment.dztradehub.managers.BountyManager;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BountyCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final BountyManager bountyManager;
    private final BountyGUI bountyGUI;

    public BountyCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.bountyManager = plugin.getBountyManager();
        this.bountyGUI = plugin.getBountyGUI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.bounty")) {
            MessageUtil.sendError(player, "You don't have permission to use bounty commands!");
            return true;
        }

        // /bounty - Opens main bounty GUI
        if (args.length == 0) {
            bountyGUI.openMainGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "list" -> handleList(player);
            case "manage" -> handleManage(player, args);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /bounty create <player_name>");
            return;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            // Try to get offline player UUID
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            
            if (!offlineTarget.hasPlayedBefore()) {
                MessageUtil.sendError(player, "Player not found!");
                return;
            }
            
            // Open bounty creation GUI with offline player
            bountyGUI.openCreateGUI(player, offlineTarget.getUniqueId(), offlineTarget.getName());
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "You cannot place a bounty on yourself!");
            return;
        }

        // Open bounty creation GUI
        bountyGUI.openCreateGUI(player, target.getUniqueId(), target.getName());
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /bounty delete <bounty_number>");
            return;
        }

        try {
            int bountyNumber = Integer.parseInt(args[1]);
            
            // Get bounty by number
            Bounty bounty = bountyManager.getBountyByNumber(player.getUniqueId(), bountyNumber);
            
            if (bounty == null) {
                MessageUtil.sendError(player, "You don't have a bounty with number #" + bountyNumber);
                return;
            }

            // Refund currencies
            if (bounty.getMoneyReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY, bounty.getMoneyReward());
            }
            if (bounty.getMobcoinReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN, bounty.getMobcoinReward());
            }
            if (bounty.getGemReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM, bounty.getGemReward());
            }
            
            // Return items
            for (ItemStack item : bounty.getRewardItems()) {
                player.getInventory().addItem(item);
            }

            // Remove bounty
            bountyManager.removeBounty(bounty.getBountyId()).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendSuccess(player, "Deleted bounty #" + bountyNumber);
                    MessageUtil.sendInfo(player, "Rewards have been refunded to you");
                } else {
                    MessageUtil.sendError(player, "Failed to delete bounty!");
                }
            });
            
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid bounty number!");
        }
    }
    
    private void handleManage(Player player, String[] args) {
        // /bounty manage - Opens GUI with all player's bounties
        if (args.length == 1) {
            bountyGUI.openManageAllGUI(player);
            return;
        }
        
        // /bounty manage <bounty_number> - Opens management GUI for specific bounty
        try {
            int bountyNumber = Integer.parseInt(args[1]);
            
            Bounty bounty = bountyManager.getBountyByNumber(player.getUniqueId(), bountyNumber);
            
            if (bounty == null) {
                MessageUtil.sendError(player, "You don't have a bounty with number #" + bountyNumber);
                return;
            }
            
            // Open management GUI for this bounty
            bountyGUI.openManageSingleGUI(player, bounty);
            
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid bounty number!");
        }
    }

    private void handleList(Player player) {
        var allBounties = bountyManager.getAllBounties();
        
        if (allBounties.isEmpty()) {
            MessageUtil.sendInfo(player, "There are no active bounties.");
            return;
        }

        player.sendMessage("§6§l▒▒▒ Active Bounties ▒▒▒");
        player.sendMessage("");
        
        // Show player's own bounties first
        List<Bounty> playerBounties = bountyManager.getBountiesCreatedBy(player.getUniqueId());
        
        if (!playerBounties.isEmpty()) {
            player.sendMessage("§e§lYour Bounties:");
            for (Bounty bounty : playerBounties) {
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
                
                player.sendMessage(String.format("§e#%d §f%s §7- §a%.0f$ §7| §b%.0f MC §7| §d%.0f G §7| §e%d items",
                    bounty.getBountyNumber(),
                    target.getName(),
                    bounty.getMoneyReward(),
                    bounty.getMobcoinReward(),
                    bounty.getGemReward(),
                    bounty.getRewardItems().size()));
            }
            player.sendMessage("");
        }
        
        // Show other players' bounties (without detailed rewards)
        player.sendMessage("§e§lOther Players' Bounties:");
        allBounties.forEach((targetUUID, bounties) -> {
            // Skip if it's just player's own bounties
            boolean hasOtherBounties = bounties.stream()
                .anyMatch(b -> !b.getCreatorPlayer().equals(player.getUniqueId()));
            
            if (hasOtherBounties) {
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
                
                // Count only other players' bounties
                long count = bounties.stream()
                    .filter(b -> !b.getCreatorPlayer().equals(player.getUniqueId()))
                    .count();
                
                player.sendMessage(String.format("§e%s §7- §c%d bounties §7(rewards hidden)",
                    target.getName(), count));
            }
        });
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l▒▒▒ Bounty Commands ▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e/bounty §7- Open bounty GUI");
        player.sendMessage("§e/bounty create <player_name> §7- Create bounty on player (opens GUI)");
        player.sendMessage("§e/bounty delete <bounty_number> §7- Delete your bounty");
        player.sendMessage("§e/bounty list §7- List all active bounties");
        player.sendMessage("§e/bounty manage <bounty_number> §7- Manage specific bounty");
        player.sendMessage("§e/bounty manage §7- Manage all your bounties (GUI)");
        player.sendMessage("§e/bounty help §7- Show this help");
        player.sendMessage("");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "list", "manage", "help"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                // Tab complete online player names
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("manage")) {
                // Tab complete bounty numbers (show player's bounty numbers)
                if (sender instanceof Player player) {
                    List<Bounty> playerBounties = bountyManager.getBountiesCreatedBy(player.getUniqueId());
                    playerBounties.forEach(b -> completions.add(String.valueOf(b.getBountyNumber())));
                }
            }
        }
        
        return completions;
    }
}
