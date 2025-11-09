package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Kit;
import online.demonzdevelopment.dztradehub.gui.KitsGUI;
import online.demonzdevelopment.dztradehub.managers.KitManager;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Material;
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
import java.util.stream.Collectors;

public class KitsCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final KitManager kitManager;
    private final KitsGUI kitsGUI;
    
    public KitsCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        this.kitsGUI = new KitsGUI(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        // /kits - Open kits GUI
        if (args.length == 0) {
            kitsGUI.open(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "claim":
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /kits claim <kit_name>");
                    return true;
                }
                claimKit(player, args[1]);
                break;
                
            case "create":
                if (!player.hasPermission("dztradehub.admin")) {
                    MessageUtil.sendError(player, "You don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /kits create <name>");
                    return true;
                }
                createKit(player, args);
                break;
                
            case "delete":
                if (!player.hasPermission("dztradehub.admin")) {
                    MessageUtil.sendError(player, "You don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /kits delete <name>");
                    return true;
                }
                deleteKit(player, args[1]);
                break;
                
            case "link":
                if (!player.hasPermission("dztradehub.admin")) {
                    MessageUtil.sendError(player, "You don't have permission!");
                    return true;
                }
                if (args.length < 4) {
                    MessageUtil.sendError(player, "Usage: /kits link <kit_name> <area> <shop>");
                    return true;
                }
                linkKit(player, args[1], args[2], args[3]);
                break;
                
            case "unlink":
                if (!player.hasPermission("dztradehub.admin")) {
                    MessageUtil.sendError(player, "You don't have permission!");
                    return true;
                }
                if (args.length < 2) {
                    MessageUtil.sendError(player, "Usage: /kits unlink <kit_name>");
                    return true;
                }
                unlinkKit(player, args[1]);
                break;
                
            case "list":
                listKits(player);
                break;
                
            case "help":
                sendHelp(player);
                break;
                
            default:
                MessageUtil.sendError(player, "Unknown subcommand! Use /kits help");
                break;
        }
        
        return true;
    }
    
    private void claimKit(Player player, String kitName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            MessageUtil.sendError(player, "Kit not found: " + kitName);
            return;
        }
        
        // Check cooldown
        if (!kitManager.canClaimKit(player, kit)) {
            long remaining = kitManager.getRemainingCooldown(player.getUniqueId(), kitName);
            MessageUtil.sendError(player, "Kit is on cooldown!");
            player.sendMessage("§7Time remaining: " + formatTime(remaining));
            return;
        }
        
        // Check permission
        if (kit.getPermission() != null && !kit.getPermission().isEmpty()) {
            if (!player.hasPermission(kit.getPermission())) {
                MessageUtil.sendError(player, "You don't have permission to claim this kit!");
                return;
            }
        }
        
        // Claim kit
        if (kitManager.claimKit(player, kit)) {
            MessageUtil.sendSuccess(player, "Claimed kit: " + kit.getDisplayName());
            player.sendMessage("§7Items added to your inventory!");
        } else {
            MessageUtil.sendError(player, "Failed to claim kit! Your inventory may be full.");
        }
    }
    
    private void createKit(Player player, String[] args) {
        String kitName = args[1];
        
        if (kitManager.getKit(kitName) != null) {
            MessageUtil.sendError(player, "A kit with this name already exists!");
            return;
        }
        
        // Parse arguments if provided: /kits create <name> <currency> <amount> <cooldown> <item:amount>...
        if (args.length >= 5) {
            // Command creation with items
            try {
                CurrencyType currency = CurrencyType.valueOf(args[2].toUpperCase());
                double price = Double.parseDouble(args[3]);
                long cooldown = Long.parseLong(args[4]);
                
                List<ItemStack> items = new ArrayList<>();
                for (int i = 5; i < args.length; i++) {
                    String[] parts = args[i].split(":");
                    Material material = Material.valueOf(parts[0].toUpperCase());
                    int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    items.add(new ItemStack(material, amount));
                }
                
                Kit kit = new Kit(kitName, kitName, Material.CHEST, price, cooldown, "");
                kit.setItems(items);
                
                if (kitManager.createKit(kit)) {
                    MessageUtil.sendSuccess(player, "Kit created: " + kitName);
                    player.sendMessage("§ePrice: §f" + price + " " + currency.name());
                    player.sendMessage("§eCooldown: §f" + cooldown + " seconds");
                    player.sendMessage("§eItems: §f" + items.size());
                } else {
                    MessageUtil.sendError(player, "Failed to create kit!");
                }
            } catch (Exception e) {
                MessageUtil.sendError(player, "Invalid arguments!");
                player.sendMessage("§7Usage: /kits create <name> <currency> <amount> <cooldown> <item:amount>...");
            }
        } else {
            // Open GUI creation
            MessageUtil.sendInfo(player, "Opening kit creation GUI...");
            // TODO: Open kit creation GUI
            MessageUtil.sendInfo(player, "GUI not yet implemented. Use command syntax instead.");
            player.sendMessage("§7/kits create <name> <currency> <amount> <cooldown> <item:amount>...");
        }
    }
    
    private void deleteKit(Player player, String kitName) {
        if (kitManager.getKit(kitName) == null) {
            MessageUtil.sendError(player, "Kit not found: " + kitName);
            return;
        }
        
        if (kitManager.deleteKit(kitName)) {
            MessageUtil.sendSuccess(player, "Kit deleted: " + kitName);
        } else {
            MessageUtil.sendError(player, "Failed to delete kit!");
        }
    }
    
    private void linkKit(Player player, String kitName, String areaName, String shopName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            MessageUtil.sendError(player, "Kit not found: " + kitName);
            return;
        }
        
        if (kitManager.linkKitToShop(kitName, areaName, shopName)) {
            MessageUtil.sendSuccess(player, "Kit linked successfully!");
            player.sendMessage("§eKit: §f" + kitName);
            player.sendMessage("§eShop: §f" + areaName + "/" + shopName);
        } else {
            MessageUtil.sendError(player, "Failed to link kit! Shop may not exist.");
        }
    }
    
    private void unlinkKit(Player player, String kitName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            MessageUtil.sendError(player, "Kit not found: " + kitName);
            return;
        }
        
        if (kitManager.unlinkKit(kitName)) {
            MessageUtil.sendSuccess(player, "Kit unlinked: " + kitName);
        } else {
            MessageUtil.sendError(player, "Failed to unlink kit!");
        }
    }
    
    private void listKits(Player player) {
        List<Kit> kits = kitManager.getAllKits();
        player.sendMessage("§6§l════════ Available Kits ════════");
        player.sendMessage("");
        
        if (kits.isEmpty()) {
            player.sendMessage("§7No kits available");
        } else {
            for (Kit kit : kits) {
                player.sendMessage("§e▸ " + kit.getDisplayName());
                player.sendMessage("  §7Name: §f" + kit.getName());
                player.sendMessage("  §7Price: §f" + kit.getPrice());
                player.sendMessage("  §7Cooldown: §f" + kit.getCooldown() + "s");
                player.sendMessage("");
            }
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l═══════ Kits Commands ═══════");
        player.sendMessage("");
        player.sendMessage("§e/kits §7- Browse available kits");
        player.sendMessage("§e/kits claim <name> §7- Claim a kit");
        player.sendMessage("§e/kits list §7- List all kits");
        player.sendMessage("");
        
        if (player.hasPermission("dztradehub.admin")) {
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§c/kits create <name> §7- Create kit (GUI)");
            player.sendMessage("§c/kits create <name> <curr> <price> <cd> <items> §7- Create kit");
            player.sendMessage("§c/kits delete <name> §7- Delete kit");
            player.sendMessage("§c/kits link <kit> <area> <shop> §7- Link to shop");
            player.sendMessage("§c/kits unlink <kit> §7- Unlink kit");
        }
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("claim", "list", "help"));
            if (sender.hasPermission("dztradehub.admin")) {
                completions.addAll(Arrays.asList("create", "delete", "link", "unlink"));
            }
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("claim") || subCmd.equals("delete") || subCmd.equals("link") || subCmd.equals("unlink")) {
                return kitManager.getAllKits().stream()
                    .map(Kit::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("link")) {
            return plugin.getShopManager().getAllAreas().stream()
                .map(area -> area.getName())
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 4 && args[0].equalsIgnoreCase("link")) {
            String areaName = args[2];
            return plugin.getShopManager().getShopsInArea(areaName).stream()
                .map(shop -> shop.getName())
                .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
