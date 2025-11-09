package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command for accessing the Kit Area (not to be confused with Kits)
 * /kit command opens the Kit area which contains starter kit shops
 */
public class KitCommand implements CommandExecutor {
    private final DZTradeHub plugin;
    
    public KitCommand(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        // /kit opens the Kit area (not kits manager)
        Area kitArea = plugin.getShopManager().getArea("Kits");
        
        if (kitArea == null) {
            MessageUtil.sendError(player, "Kit area not found!");
            return true;
        }
        
        // Open Kit area GUI
        plugin.getAreaGUI().openArea(player, kitArea);
        return true;
    }
}
