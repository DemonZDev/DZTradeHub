package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Bounty;
import online.demonzdevelopment.dztradehub.gui.BountyGUI;
import online.demonzdevelopment.dztradehub.gui.BountyManageGUI;
import online.demonzdevelopment.dztradehub.managers.BountyManager;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
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

public class BountyCommandNew implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final BountyManager bountyManager;
    private final BountyGUI bountyGUI;
    private final BountyManageGUI bountyManageGUI;

    public BountyCommandNew(DZTradeHub plugin) {
        this.plugin = plugin;
        this.bountyManager = plugin.getBountyManager();
        this.bountyGUI = new BountyGUI(plugin);
        this.bountyManageGUI = new BountyManageGUI(plugin);
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

        // /bounty - Opens main bounty GUI showing all bounties
        if (args.length == 0) {
            bountyGUI.openBountyBrowser(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player, args);
            case "manage" -> handleManage(player, args);
            case "list" -> handleList(player);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }

        return true;
    }

    /**
     * /bounty create <player> - Opens GUI to create bounty on a player
     */
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /bounty create <player>");
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
            bountyGUI.openForTarget(player, offlineTarget.getUniqueId(), offlineTarget.getName());
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "You cannot place a bounty on yourself!");
            return;
        }

        // Open bounty creation GUI
        bountyGUI.openForTarget(player, target.getUniqueId(), target.getName());
    }

    /**
     * /bounty delete <bounty_number> - Deletes your bounty by number
     */
    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /bounty delete <bounty_number>");
            return;
        }

        try {
            int bountyNumber = Integer.parseInt(args[1]);
            Bounty bounty = bountyManager.getBountyByNumber(player.getUniqueId(), bountyNumber);
            
            if (bounty == null) {
                MessageUtil.sendError(player, "You don't have a bounty with number #" + bountyNumber);
                return;
            }

            // Refund currency to player
            if (bounty.getMoneyReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY, 
                    bounty.getMoneyReward());
            }
            if (bounty.getMobcoinReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN, 
                    bounty.getMobcoinReward());
            }
            if (bounty.getGemReward() > 0) {
                plugin.getEconomyAPI().addCurrency(player.getUniqueId(), 
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM, 
                    bounty.getGemReward());
            }
            
            // Return items to player
            for (org.bukkit.inventory.ItemStack item : bounty.getRewardItems()) {
                player.getInventory().addItem(item);
            }

            // Remove bounty
            bountyManager.removeBounty(bounty.getBountyId()).thenAccept(success -> {
                if (success) {
                    MessageUtil.sendSuccess(player, "Deleted bounty #" + bountyNumber + " and refunded rewards");
                } else {
                    MessageUtil.sendError(player, "Failed to delete bounty");
                }
            });

        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid bounty number!");
        }
    }

    /**
     * /bounty manage <bounty_number> - Opens GUI to manage specific bounty
     * /bounty manage - Opens GUI showing all your bounties for management
     */
    private void handleManage(Player player, String[] args) {
        if (args.length < 2) {
            // Open GUI showing all player's bounties
            bountyManageGUI.openBountyListGUI(player);
            return;
        }

        try {
            int bountyNumber = Integer.parseInt(args[1]);
            Bounty bounty = bountyManager.getBountyByNumber(player.getUniqueId(), bountyNumber);
            
            if (bounty == null) {
                MessageUtil.sendError(player, "You don't have a bounty with number #" + bountyNumber);
                return;
            }

            // Open management GUI for specific bounty
            bountyManageGUI.openManageGUI(player, bounty);

        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid bounty number!");
        }
    }

    /**
     * /bounty list - Lists all bounties
     * Shows YOUR bounties first with full details and numbers
     * Then shows OTHER bounties without revealing creator, amounts, or numbers
     */
    private void handleList(Player player) {
        List<Bounty> yourBounties = bountyManager.getBountiesCreatedBy(player.getUniqueId());
        var allBounties = bountyManager.getAllBounties();
        
        if (yourBounties.isEmpty() && allBounties.isEmpty()) {
            MessageUtil.sendInfo(player, "There are no active bounties.");
            return;
        }

        player.sendMessage("§6§l=== Bounty List ===");
        player.sendMessage("");
        
        // Show player's own bounties first
        if (!yourBounties.isEmpty()) {
            player.sendMessage("§e§lYour Bounties:");
            for (Bounty bounty : yourBounties) {
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(bounty.getTargetPlayer());
                
                player.sendMessage(String.format("§e#%d §7- Target: §c%s", 
                    bounty.getBountyNumber(), 
                    target.getName()));
                
                // Show currency rewards
                if (bounty.getMoneyReward() > 0) {
                    player.sendMessage("  §7Money: §a$" + formatAmount(bounty.getMoneyReward()));
                }
                if (bounty.getMobcoinReward() > 0) {
                    player.sendMessage("  §7Mobcoin: §a" + formatAmount(bounty.getMobcoinReward()) + " MC");
                }
                if (bounty.getGemReward() > 0) {
                    player.sendMessage("  §7Gem: §a" + formatAmount(bounty.getGemReward()) + " G");
                }
                
                // Show item count
                if (!bounty.getRewardItems().isEmpty()) {
                    player.sendMessage("  §7Items: §a" + bounty.getRewardItems().size());
                }
            }
            player.sendMessage("");
        }
        
        // Show other players' bounties (without details)
        player.sendMessage("§e§lOther Bounties:");
        int otherCount = 0;
        for (var entry : allBounties.entrySet()) {
            for (Bounty bounty : entry.getValue()) {
                // Skip player's own bounties
                if (bounty.getCreatorPlayer().equals(player.getUniqueId())) {
                    continue;
                }
                
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
                
                // Only show target name, not rewards or creator
                player.sendMessage(String.format("§7- Target: §c%s §7(Bounty active)", target.getName()));
                otherCount++;
            }
        }
        
        if (otherCount == 0) {
            player.sendMessage("§7No other bounties active");
        }
        
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Bounty Commands ===");
        player.sendMessage("");
        player.sendMessage("§e/bounty §7- View all bounties");
        player.sendMessage("§e/bounty create <player> §7- Place bounty on a player (opens GUI)");
        player.sendMessage("§e/bounty delete <bounty_number> §7- Delete your bounty by number");
        player.sendMessage("§e/bounty manage <bounty_number> §7- Manage specific bounty");
        player.sendMessage("§e/bounty manage §7- Manage all your bounties (opens GUI)");
        player.sendMessage("§e/bounty list §7- List all active bounties");
        player.sendMessage("§e/bounty help §7- Show this help message");
        player.sendMessage("");
        player.sendMessage("§7Kill a player with a bounty to claim the rewards!");
    }

    private String formatAmount(double amount) {
        if (amount >= 1000000) {
            return String.format("%.2fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.2fK", amount / 1000);
        } else {
            return String.format("%.2f", amount);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "manage", "list", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }
        
        return completions;
    }
}
