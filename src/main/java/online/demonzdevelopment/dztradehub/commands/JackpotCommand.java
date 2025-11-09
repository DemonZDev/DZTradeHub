package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.gui.JackpotGUI;
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

public class JackpotCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final JackpotGUI jackpotGUI;

    public JackpotCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.jackpotGUI = new JackpotGUI(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.casino")) {
            MessageUtil.sendError(player, "You don't have permission to use jackpot!");
            return true;
        }

        // /jackpot - Opens jackpot GUI
        if (args.length == 0) {
            jackpotGUI.open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        // /jackpot help
        if (subCommand.equals("help")) {
            sendHelp(player);
            return true;
        }

        // /jackpot <money|mobcoin|gem> <amount> <row_number>
        if (args.length >= 3) {
            handleDirectJackpot(player, args);
            return true;
        }

        sendHelp(player);
        return true;
    }

    /**
     * /jackpot <currency> <amount> <row_number>
     */
    private void handleDirectJackpot(Player player, String[] args) {
        String currencyType = args[0].toUpperCase();
        if (!currencyType.equals("MONEY") && !currencyType.equals("MOBCOIN") && !currencyType.equals("GEM")) {
            MessageUtil.sendError(player, "Invalid currency type! Use: money, mobcoin, or gem");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                MessageUtil.sendError(player, "Amount must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid amount!");
            return;
        }

        int rows;
        try {
            rows = Integer.parseInt(args[2]);
            if (rows < 3 || rows > 5) {
                MessageUtil.sendError(player, "Row number must be 3, 4, or 5!");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid row number!");
            return;
        }

        // Perform jackpot spin with specified rows
        plugin.getCasinoManager().performJackpotSpin(player, currencyType, amount, rows);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Jackpot Commands ===");
        player.sendMessage("");
        player.sendMessage("§e/jackpot §7- Open jackpot GUI");
        player.sendMessage("§e/jackpot <money|mobcoin|gem> <amount> <row_number> §7- Quick spin");
        player.sendMessage("  §7Row numbers: 3, 4, or 5");
        player.sendMessage("  §7Example: /jackpot money 1000 3");
        player.sendMessage("§e/jackpot help §7- Show this help message");
        player.sendMessage("");
        player.sendMessage("§6§lJackpot Multipliers:");
        player.sendMessage("§e3 Rows:");
        player.sendMessage("  §72 match: §a0.8x §7| 3 match: §a2x");
        player.sendMessage("§e4 Rows:");
        player.sendMessage("  §72 match: §a0.5x §7| 3 match: §a1x §7| 4 match: §a2x");
        player.sendMessage("§e5 Rows:");
        player.sendMessage("  §72 match: §a0.4x §7| 3 match: §a0.8x §7| 4 match: §a1.5x §7| 5 match: §a3x");
        player.sendMessage("");
        player.sendMessage("§6§lSpecial Symbols:");
        player.sendMessage("  §e7️⃣ 7️⃣ 7️⃣ §7- §a10x-20x (depends on rows)");
        player.sendMessage("  §e💎 💎 💎 §7- §a7x-15x");
        player.sendMessage("  §e⭐ ⭐ ⭐ §7- §a5x-10x");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("money", "mobcoin", "gem", "help"));
        } else if (args.length == 3) {
            completions.addAll(Arrays.asList("3", "4", "5"));
        }
        
        return completions;
    }
}
