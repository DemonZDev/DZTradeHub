package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.gui.CasinoMainGUI;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CasinoCommand implements CommandExecutor {
    private final DZTradeHub plugin;
    private final CasinoMainGUI casinoGUI;

    public CasinoCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.casinoGUI = new CasinoMainGUI(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.casino")) {
            MessageUtil.sendError(player, "You don't have permission to use the casino!");
            return true;
        }

        // /cashino help
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        // Open casino GUI
        casinoGUI.open(player);
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l▒▒▒▒▒▒ Cashino Commands ▒▒▒▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e§lMain:");
        player.sendMessage("§e/cashino §7- Open casino main menu");
        player.sendMessage("§e/cashino help §7- Show this help");
        player.sendMessage("");
        player.sendMessage("§e§lCoin Flip:");
        player.sendMessage("§e/coinflip §7- Coin flip menu");
        player.sendMessage("§e/coinflip single <currency> <amount> <heads|tails> §7- Quick flip");
        player.sendMessage("§e/coinflip double <currency> <amount> <heads|tails> <player> §7- Challenge");
        player.sendMessage("§e/coinflip requests §7- View requests");
        player.sendMessage("§e/coinflip help §7- Coin flip help");
        player.sendMessage("");
        player.sendMessage("§e§lJackpot:");
        player.sendMessage("§e/jackpot §7- Jackpot menu");
        player.sendMessage("§e/jackpot <currency> <amount> <rows> §7- Quick spin (rows: 3-5)");
        player.sendMessage("§e/jackpot help §7- Jackpot help");
        player.sendMessage("");
    }
}
