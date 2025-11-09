package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.CoinFlipRequest;
import online.demonzdevelopment.dztradehub.gui.CoinFlipGUI;
import online.demonzdevelopment.dztradehub.gui.CoinFlipRequestsGUI;
import online.demonzdevelopment.dztradehub.managers.CasinoManager;
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

public class CoinFlipCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final CasinoManager casinoManager;
    private final CoinFlipGUI coinFlipGUI;
    private final CoinFlipRequestsGUI requestsGUI;

    public CoinFlipCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.casinoManager = plugin.getCasinoManager();
        this.coinFlipGUI = new CoinFlipGUI(plugin);
        this.requestsGUI = new CoinFlipRequestsGUI(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.casino")) {
            MessageUtil.sendError(player, "You don't have permission to use coin flip!");
            return true;
        }

        if (args.length == 0) {
            // Open GUI for mode selection
            coinFlipGUI.open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "single" -> handleSingleMode(player, args);
            case "double" -> handleDoubleMode(player, args);
            case "accept" -> handleAccept(player, args);
            case "deny" -> handleDeny(player, args);
            case "requests" -> handleRequests(player, args);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleSingleMode(Player player, String[] args) {
        // /coinflip single - Opens single coin flip GUI
        if (args.length == 1) {
            coinFlipGUI.openSingleMode(player);
            return;
        }
        
        // /coinflip single <money|mobcoin|gem> <amount> <heads|tails>
        if (args.length < 4) {
            MessageUtil.sendError(player, "Usage: /coinflip single <money|mobcoin|gem> <amount> <heads|tails>");
            return;
        }

        String currencyType = args[1].toUpperCase();
        if (!currencyType.equals("MONEY") && !currencyType.equals("MOBCOIN") && !currencyType.equals("GEM")) {
            MessageUtil.sendError(player, "Invalid currency type! Use: money, mobcoin, or gem");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                MessageUtil.sendError(player, "Amount must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid amount!");
            return;
        }

        String sideStr = args[3].toUpperCase();
        CoinFlipRequest.CoinSide side;
        try {
            side = CoinFlipRequest.CoinSide.valueOf(sideStr);
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid side! Use: head or tail");
            return;
        }

        // Perform single player coin flip
        casinoManager.performSinglePlayerCoinFlip(player, currencyType, amount, side);
    }

    private void handleDoubleMode(Player player, String[] args) {
        // /coinflip double - Opens double coin flip GUI
        if (args.length == 1) {
            coinFlipGUI.openDoubleMode(player);
            return;
        }
        
        // /coinflip double <money|mobcoin|gem> <amount> <heads|tails> <player_name>
        if (args.length < 5) {
            MessageUtil.sendError(player, "Usage: /coinflip double <money|mobcoin|gem> <amount> <heads|tails> <player_name>");
            return;
        }

        String currencyType = args[1].toUpperCase();
        if (!currencyType.equals("MONEY") && !currencyType.equals("MOBCOIN") && !currencyType.equals("GEM")) {
            MessageUtil.sendError(player, "Invalid currency type! Use: money, mobcoin, or gem");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                MessageUtil.sendError(player, "Amount must be positive!");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid amount!");
            return;
        }

        String sideStr = args[3].toUpperCase();
        CoinFlipRequest.CoinSide side;
        try {
            side = CoinFlipRequest.CoinSide.valueOf(sideStr);
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid side! Use: heads or tails");
            return;
        }

        String targetName = args[4];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            MessageUtil.sendError(player, "Player not found or not online!");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            MessageUtil.sendError(player, "You cannot challenge yourself!");
            return;
        }

        // Send request
        casinoManager.sendCoinFlipRequest(player, target, currencyType, amount, side);
    }

    private void handleAccept(Player player, String[] args) {
        // /coinflip accept <number> - Accept specific request
        if (args.length >= 2) {
            try {
                int requestNumber = Integer.parseInt(args[1]);
                CoinFlipRequest request = casinoManager.getRequestByNumber(player.getUniqueId(), requestNumber);
                
                if (request == null) {
                    MessageUtil.sendError(player, "You don't have a coin flip request #" + requestNumber);
                    return;
                }
                
                casinoManager.acceptCoinFlipRequest(player, request);
                return;
            } catch (NumberFormatException e) {
                MessageUtil.sendError(player, "Invalid request number!");
                return;
            }
        }
        
        // /coinflip accept - Accept latest request
        CoinFlipRequest request = casinoManager.getLastRequest(player.getUniqueId());
        if (request == null) {
            MessageUtil.sendError(player, "You have no pending coin flip requests!");
            return;
        }

        casinoManager.acceptCoinFlipRequest(player, request);
    }

    private void handleDeny(Player player, String[] args) {
        // /coinflip deny <number> - Deny specific request
        if (args.length >= 2) {
            try {
                int requestNumber = Integer.parseInt(args[1]);
                CoinFlipRequest request = casinoManager.getRequestByNumber(player.getUniqueId(), requestNumber);
                
                if (request == null) {
                    MessageUtil.sendError(player, "You don't have a coin flip request #" + requestNumber);
                    return;
                }
                
                casinoManager.denyCoinFlipRequest(player, request);
                return;
            } catch (NumberFormatException e) {
                MessageUtil.sendError(player, "Invalid request number!");
                return;
            }
        }
        
        // /coinflip deny - Deny latest request
        CoinFlipRequest request = casinoManager.getLastRequest(player.getUniqueId());
        if (request == null) {
            MessageUtil.sendError(player, "You have no pending coin flip requests!");
            return;
        }

        casinoManager.denyCoinFlipRequest(player, request);
    }

    private void handleRequests(Player player, String[] args) {
        // /coinflip requests list - Show in chat with numbers
        if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            List<CoinFlipRequest> requests = casinoManager.getPendingRequests(player.getUniqueId());
            
            if (requests.isEmpty()) {
                MessageUtil.sendInfo(player, "You have no pending coin flip requests.");
                return;
            }
            
            player.sendMessage("§6§l▒▒▒ Coin Flip Requests ▒▒▒");
            player.sendMessage("");
            
            for (int i = 0; i < requests.size(); i++) {
                CoinFlipRequest req = requests.get(i);
                Player challenger = Bukkit.getPlayer(req.getFromPlayer());
                String challengerName = challenger != null ? challenger.getName() : "Unknown";
                
                player.sendMessage(String.format("§e#%d §f%s §7- §a%.0f %s §7| Their side: §6%s",
                    i + 1,
                    challengerName,
                    req.getAmount(),
                    req.getCurrencyType(),
                    req.getSide()));
            }
            
            player.sendMessage("");
            player.sendMessage("§7Use §e/coinflip accept <number> §7or §e/coinflip deny <number>");
            return;
        }
        
        // /coinflip requests - Opens GUI
        requestsGUI.open(player);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l▒▒▒ Coin Flip Commands ▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e§lSingle Player:");
        player.sendMessage("§e/coinflip single §7- Open single coin flip GUI");
        player.sendMessage("§e/coinflip single <money|mobcoin|gem> <amount> <heads|tails> §7- Quick flip");
        player.sendMessage("  §7Example: /coinflip single money 1000 heads");
        player.sendMessage("");
        player.sendMessage("§e§lDouble Player (Challenge):");
        player.sendMessage("§e/coinflip double §7- Open double coin flip GUI");
        player.sendMessage("§e/coinflip double <money|mobcoin|gem> <amount> <heads|tails> <player> §7- Challenge");
        player.sendMessage("  §7Example: /coinflip double money 1000 heads Steve");
        player.sendMessage("");
        player.sendMessage("§e§lRequests:");
        player.sendMessage("§e/coinflip requests §7- View requests (GUI)");
        player.sendMessage("§e/coinflip requests list §7- List requests in chat");
        player.sendMessage("§e/coinflip accept §7- Accept latest request");
        player.sendMessage("§e/coinflip deny §7- Deny latest request");
        player.sendMessage("§e/coinflip accept <number> §7- Accept specific request");
        player.sendMessage("§e/coinflip deny <number> §7- Deny specific request");
        player.sendMessage("");
        player.sendMessage("§e/coinflip help §7- Show this help");
        player.sendMessage("");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("single", "double", "accept", "deny", "requests", "help"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("single") || args[0].equalsIgnoreCase("double")) {
                completions.addAll(Arrays.asList("money", "mobcoin", "gem"));
            } else if (args[0].equalsIgnoreCase("requests")) {
                completions.add("list");
            } else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {
                // Add request numbers
                if (sender instanceof Player player) {
                    List<CoinFlipRequest> requests = casinoManager.getPendingRequests(player.getUniqueId());
                    for (int i = 0; i < requests.size(); i++) {
                        completions.add(String.valueOf(i + 1));
                    }
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("single")) {
            completions.addAll(Arrays.asList("heads", "tails"));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("double")) {
            completions.addAll(Arrays.asList("heads", "tails"));
        } else if (args.length == 5 && args[0].equalsIgnoreCase("double")) {
            // List online players
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }
        
        return completions;
    }
}
