package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DynamicAreaCommand implements CommandExecutor {
    private final DZTradeHub plugin;
    private final String areaName;

    public DynamicAreaCommand(DZTradeHub plugin, String areaName) {
        this.plugin = plugin;
        this.areaName = areaName;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Area area = plugin.getShopManager().getArea(areaName);
        if (area == null) {
            player.sendMessage("§cArea not found!");
            return true;
        }

        // Open area GUI
        plugin.getAreaGUI().openArea(player, area);
        return true;
    }

    public String getAreaName() {
        return areaName;
    }
}
