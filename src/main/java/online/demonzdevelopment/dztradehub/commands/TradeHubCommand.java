package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TradeHubCommand implements CommandExecutor {
    private final DZTradeHub plugin;

    public TradeHubCommand(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.admin")) {
            MessageUtil.sendError(player, "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // Handle /tradehub command (member access)
        if (!player.hasPermission("dztradehub.admin")) {
            if (args.length == 0) {
                // Open area browser GUI
                plugin.getAreaGUI().openAreaBrowser(player);
                return true;
            } else if (args[0].equalsIgnoreCase("help")) {
                sendMemberHelp(player);
                return true;
            } else {
                MessageUtil.sendError(player, "You don't have permission to use admin commands!");
                return true;
            }
        }

        switch (args[0].toLowerCase()) {
            case "create-area" -> handleCreateArea(player, args);
            case "delete-area" -> handleDeleteArea(player, args);
            case "rename-area" -> handleRenameArea(player, args);
            case "create-shop" -> handleCreateShop(player, args);
            case "delete-shop" -> handleDeleteShop(player, args);
            case "rename-shop" -> handleRenameShop(player, args);
            case "config" -> handleConfig(player, args);
            case "gui" -> handleGUI(player, args);
            case "add-item" -> handleAddItem(player, args);
            case "remove-item" -> handleRemoveItem(player, args);
            case "link-shop" -> handleLinkShop(player, args);
            case "unlink-shop" -> handleUnlinkShop(player, args);
            case "areas", "list" -> handleListCommand(player, args);
            case "shops" -> handleShopsList(player, args);
            case "items" -> handleItemsList(player, args);
            case "migrate" -> handleMigrate(player, args);
            case "reload" -> handleReload(player);
            case "credits" -> handleCredits(player);
            case "update" -> handleUpdate(player, args);
            case "disable" -> handleDisable(player, args);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleCreateArea(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /dzth create-area <area_name>");
            return;
        }

        String areaName = args[1];
        
        if (plugin.getShopManager().areaExists(areaName)) {
            MessageUtil.sendError(player, "Area '" + areaName + "' already exists!");
            return;
        }

        Location loc = player.getLocation();
        Area area = new Area(areaName, "§e§l" + areaName, Area.AreaType.BAZAR, loc);
        plugin.getShopManager().registerArea(area);
        plugin.getFileStorageManager().saveArea(area);
        
        // Register dynamic command
        plugin.registerAreaCommand(area);
        
        MessageUtil.sendSuccess(player, "Created area: " + areaName);
        MessageUtil.sendInfo(player, "Use /" + areaName.toLowerCase() + " to open it");
    }

    private void handleDeleteArea(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /dzth delete-area <area_name>");
            return;
        }

        String areaName = args[1];
        
        if (!plugin.getShopManager().areaExists(areaName)) {
            MessageUtil.sendError(player, "Area '" + areaName + "' does not exist!");
            return;
        }

        plugin.getShopManager().deleteArea(areaName);
        plugin.getFileStorageManager().deleteArea(areaName);
        plugin.unregisterAreaCommand(areaName);
        
        MessageUtil.sendSuccess(player, "Deleted area: " + areaName);
    }

    private void handleCreateShop(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth create-shop <area_name> <shop_name>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        
        if (!plugin.getShopManager().areaExists(areaName)) {
            MessageUtil.sendError(player, "Area '" + areaName + "' does not exist!");
            return;
        }
        
        if (plugin.getShopManager().shopExists(areaName, shopName)) {
            MessageUtil.sendError(player, "Shop '" + shopName + "' already exists in " + areaName + "!");
            return;
        }

        Shop shop = new Shop(shopName, "§b§l" + shopName, Shop.ShopType.BUY_SELL);
        plugin.getShopManager().registerShop(areaName, shop);
        plugin.getFileStorageManager().saveShop(areaName, shop);
        
        MessageUtil.sendSuccess(player, "Created shop: " + shopName + " in " + areaName);
        MessageUtil.sendInfo(player, "Use /dzth config to configure it");
    }

    private void handleDeleteShop(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth delete-shop <area_name> <shop_name>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        plugin.getShopManager().deleteShop(areaName, shopName);
        plugin.getFileStorageManager().deleteShop(areaName, shopName);
        
        MessageUtil.sendSuccess(player, "Deleted shop: " + shopName);
    }

    private void handleConfig(Player player, String[] args) {
        if (args.length < 8) {
            player.sendMessage("§cUsage: /dzth config <area> <shop> <reception|checkout> <true|false> <number> <time_kick> <afk_kick>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        String systemType = args[3].toLowerCase();
        boolean enabled = Boolean.parseBoolean(args[4]);
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        try {
            if (systemType.equals("reception")) {
                int number = Integer.parseInt(args[5]);
                int timeKick = Integer.parseInt(args[6]);
                int afkKick = Integer.parseInt(args[7]);
                
                shop.setReceptionEnabled(enabled);
                shop.setReceptionNumber(number);
                shop.setReceptionTimeKick(timeKick);
                shop.setReceptionAfkKick(afkKick);
                shop.setQueueType(enabled ? Shop.QueueType.RECEPTION : Shop.QueueType.NONE);
                
                // Disable checkout if reception is enabled
                if (enabled) {
                    shop.setCheckoutEnabled(false);
                }
                
                MessageUtil.sendSuccess(player, "Reception configured!");
                player.sendMessage("§7- Enabled: " + enabled);
                player.sendMessage("§7- Slots: " + number);
                player.sendMessage("§7- Time limit: " + timeKick + "s");
                player.sendMessage("§7- AFK kick: " + afkKick + "s");
                
            } else if (systemType.equals("checkout")) {
                int number = Integer.parseInt(args[5]);
                int timeKick = Integer.parseInt(args[6]);
                
                shop.setCheckoutEnabled(enabled);
                shop.setCheckoutNumber(number);
                shop.setCheckoutTimeKick(timeKick);
                shop.setQueueType(enabled ? Shop.QueueType.CASH_COUNTER : Shop.QueueType.NONE);
                
                // Disable reception if checkout is enabled
                if (enabled) {
                    shop.setReceptionEnabled(false);
                }
                
                MessageUtil.sendSuccess(player, "Checkout configured!");
                player.sendMessage("§7- Enabled: " + enabled);
                player.sendMessage("§7- Counters: " + number);
                player.sendMessage("§7- Time per item: " + timeKick + "s");
            } else {
                MessageUtil.sendError(player, "Invalid system type! Use 'reception' or 'checkout'");
                return;
            }
            
            plugin.getFileStorageManager().saveShop(areaName, shop);
            
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid number format!");
        }
    }

    private void handleGUI(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth gui <area_name> <shop_name>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        // Open shop configuration GUI
        plugin.getShopGUI().openShopConfig(player, shop);
    }

    private void handleAddItem(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth add-item <area> <shop> [item] [currency] [min_price] [max_price] [type]");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        // If no additional args, open GUI
        if (args.length == 3) {
            plugin.getShopGUI().openAddItemGUI(player, shop, areaName);
            return;
        }

        // Command-based item adding
        if (args.length < 8) {
            MessageUtil.sendError(player, "Usage: /dzth add-item <area> <shop> <item> <currency> <min_price> <max_price> <buy|sell|both>");
            return;
        }

        try {
            Material material = Material.valueOf(args[3].toUpperCase());
            ShopItem.Currency currency = ShopItem.Currency.valueOf(args[4].toUpperCase());
            double minPrice = Double.parseDouble(args[5]);
            double maxPrice = Double.parseDouble(args[6]);
            String transactionStr = args[7].toLowerCase();
            
            ShopItem.TransactionType transactionType = switch (transactionStr) {
                case "buy" -> ShopItem.TransactionType.BUY_ONLY;
                case "sell" -> ShopItem.TransactionType.SELL_ONLY;
                case "both" -> ShopItem.TransactionType.BOTH;
                default -> throw new IllegalArgumentException("Invalid transaction type");
            };
            
            ItemStack itemStack = new ItemStack(material, 1);
            ShopItem shopItem = new ShopItem(itemStack, maxPrice, minPrice);
            shopItem.setCurrency(currency);
            shopItem.setMinPrice(minPrice);
            shopItem.setMaxPrice(maxPrice);
            shopItem.setTransactionType(transactionType);
            
            plugin.getShopManager().addItemToShop(areaName, shopName, shopItem);
            plugin.getFileStorageManager().saveShopItems(areaName, shop);
            
            MessageUtil.sendSuccess(player, "Added " + material.name() + " to " + shopName);
            player.sendMessage("§7- Currency: " + currency.name());
            player.sendMessage("§7- Price range: " + minPrice + " - " + maxPrice);
            player.sendMessage("§7- Type: " + transactionType.name());
            
        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid price format!");
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid input: " + e.getMessage());
        }
    }

    private void handleLinkShop(Player player, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendError(player, "Usage: /dzth link-shop <area> <sell_shop> <buy_shop>");
            return;
        }

        String areaName = args[1];
        String sellShopName = args[2];
        String buyShopName = args[3];
        
        Shop sellShop = plugin.getShopManager().getShop(areaName, sellShopName);
        Shop buyShop = plugin.getShopManager().getShop(areaName, buyShopName);
        
        if (sellShop == null || buyShop == null) {
            MessageUtil.sendError(player, "One or both shops not found!");
            return;
        }

        sellShop.setLinkedShopName(buyShopName);
        plugin.getFileStorageManager().saveShop(areaName, sellShop);
        
        MessageUtil.sendSuccess(player, "Linked " + sellShopName + " → " + buyShopName);
        MessageUtil.sendInfo(player, "Sold items will transfer to " + buyShopName);
    }

    private void handleReload(Player player) {
        plugin.getConfigManager().reloadConfigs();
        plugin.getShopManager().reloadAllShops();
        MessageUtil.sendSuccess(player, "Configuration reloaded successfully!");
    }

    private void handleCredits(Player player) {
        player.sendMessage("");
        player.sendMessage("§6DZTradeHub is created by DemonZ Development");
        player.sendMessage("§eDemonZ Development Ecosystem");
        player.sendMessage("§e- demonzdevelopment.online");
        player.sendMessage("§e- hyzerox.me");
        player.sendMessage("");
        player.sendMessage("§cCaution: This msg cant be changed by config");
        player.sendMessage("");
    }

    private void handleUpdate(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "<yellow>Usage: /dzth update <previous|next|latest|auto>");
            player.sendMessage("§7  previous - Download previous version");
            player.sendMessage("§7  next - Download next version");
            player.sendMessage("§7  latest - Download latest version");
            player.sendMessage("§7  auto - Toggle auto-update");
            return;
        }

        String updateType = args[1].toLowerCase();
        switch (updateType) {
            case "latest" -> plugin.getUpdateManager().updateToLatest(player);
            case "previous" -> plugin.getUpdateManager().updateToPrevious(player);
            case "next" -> plugin.getUpdateManager().updateToNext(player);
            case "auto" -> plugin.getUpdateManager().toggleAutoUpdate(player);
            default -> MessageUtil.send(player, "<red>Invalid update type! Use: previous, next, latest, or auto");
        }
    }
    
    private void handleDisable(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /dzth disable <auto-update>");
            return;
        }
        
        if (args[1].equalsIgnoreCase("auto-update")) {
            plugin.getUpdateManager().disableAutoUpdate(player);
        } else {
            MessageUtil.sendError(player, "Unknown option! Use: auto-update");
        }
    }

    private void handleRenameArea(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth rename-area <old_name> <new_name>");
            return;
        }

        String oldName = args[1];
        String newName = args[2];
        
        Area area = plugin.getShopManager().getArea(oldName);
        if (area == null) {
            MessageUtil.sendError(player, "Area '" + oldName + "' does not exist!");
            return;
        }

        if (plugin.getShopManager().areaExists(newName)) {
            MessageUtil.sendError(player, "Area '" + newName + "' already exists!");
            return;
        }

        // Delete old area files
        plugin.getFileStorageManager().deleteArea(oldName);
        plugin.getShopManager().deleteArea(oldName);
        
        // Create with new name
        Area newArea = new Area(newName, area.getDisplayName(), area.getType(), area.getLocation());
        newArea.setDescription(area.getDescription());
        plugin.getShopManager().registerArea(newArea);
        plugin.getFileStorageManager().saveArea(newArea);
        
        // Re-register all shops with new area name
        for (Shop shop : area.getShops()) {
            plugin.getShopManager().registerShop(newName, shop);
            plugin.getFileStorageManager().saveShop(newName, shop);
            plugin.getFileStorageManager().saveShopItems(newName, shop);
        }

        MessageUtil.sendSuccess(player, "Renamed area: " + oldName + " → " + newName);
    }

    private void handleRenameShop(Player player, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendError(player, "Usage: /dzth rename-shop <area> <old_name> <new_name>");
            return;
        }

        String areaName = args[1];
        String oldName = args[2];
        String newName = args[3];
        
        Shop oldShop = plugin.getShopManager().getShop(areaName, oldName);
        if (oldShop == null) {
            MessageUtil.sendError(player, "Shop '" + oldName + "' not found in area '" + areaName + "'!");
            return;
        }

        if (plugin.getShopManager().shopExists(areaName, newName)) {
            MessageUtil.sendError(player, "Shop '" + newName + "' already exists in area '" + areaName + "'!");
            return;
        }

        // Delete old shop
        plugin.getFileStorageManager().deleteShop(areaName, oldName);
        plugin.getShopManager().deleteShop(areaName, oldName);
        
        // Create new shop with same settings
        Shop newShop = new Shop(newName, oldShop.getDisplayName(), oldShop.getShopType());
        newShop.setQueueType(oldShop.getQueueType());
        newShop.setReceptionEnabled(oldShop.isReceptionEnabled());
        newShop.setReceptionNumber(oldShop.getReceptionNumber());
        newShop.setReceptionTimeKick(oldShop.getReceptionTimeKick());
        newShop.setReceptionAfkKick(oldShop.getReceptionAfkKick());
        newShop.setCheckoutEnabled(oldShop.isCheckoutEnabled());
        newShop.setCheckoutNumber(oldShop.getCheckoutNumber());
        newShop.setCheckoutTimeKick(oldShop.getCheckoutTimeKick());
        newShop.setLinkedShopName(oldShop.getLinkedShopName());
        
        // Copy items
        for (ShopItem item : oldShop.getItems()) {
            newShop.addItem(item);
        }
        
        plugin.getShopManager().registerShop(areaName, newShop);
        plugin.getFileStorageManager().saveShop(areaName, newShop);
        plugin.getFileStorageManager().saveShopItems(areaName, newShop);

        MessageUtil.sendSuccess(player, "Renamed shop: " + oldName + " → " + newName);
    }

    private void handleUnlinkShop(Player player, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendError(player, "Usage: /dzth unlink-shop <area> <shop>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        shop.setLinkedShopName(null);
        plugin.getFileStorageManager().saveShop(areaName, shop);
        
        MessageUtil.sendSuccess(player, "Unlinked shop: " + shopName);
        MessageUtil.sendInfo(player, "Sold items will no longer transfer");
    }

    private void handleRemoveItem(Player player, String[] args) {
        if (args.length < 4) {
            MessageUtil.sendError(player, "Usage: /dzth remove-item <area> <shop> <item_material>");
            return;
        }

        String areaName = args[1];
        String shopName = args[2];
        String materialName = args[3];
        
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ShopItem toRemove = null;
            
            for (ShopItem item : shop.getItems()) {
                if (item.getItemStack().getType() == material) {
                    toRemove = item;
                    break;
                }
            }
            
            if (toRemove != null) {
                shop.removeItem(toRemove);
                plugin.getFileStorageManager().saveShopItems(areaName, shop);
                MessageUtil.sendSuccess(player, "Removed " + material.name() + " from " + shopName);
            } else {
                MessageUtil.sendError(player, "Item not found in shop!");
            }
            
        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid material: " + materialName);
        }
    }

    private void handleMigrate(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /dzth migrate <flatfile|mysql|sqlite>");
            return;
        }

        String targetType = args[1].toUpperCase();
        
        if (!targetType.equals("FLATFILE") && !targetType.equals("MYSQL") && !targetType.equals("SQLITE")) {
            MessageUtil.sendError(player, "Invalid storage type! Use: flatfile, mysql, or sqlite");
            return;
        }

        player.sendMessage("§e⏳ Starting migration to " + targetType + "...");
        
        // Perform migration in async
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final int[] counts = {0, 0}; // [areasCount, shopsCount]
                
                // Save all areas and shops to target storage
                for (Area area : plugin.getShopManager().getAllAreas()) {
                    plugin.getFileStorageManager().saveArea(area);
                    counts[0]++;
                    
                    for (Shop shop : plugin.getShopManager().getShopsInArea(area.getName())) {
                        plugin.getFileStorageManager().saveShop(area.getName(), shop);
                        plugin.getFileStorageManager().saveShopItems(area.getName(), shop);
                        counts[1]++;
                    }
                }
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendSuccess(player, "Migration complete!");
                    player.sendMessage("§7- Areas: " + counts[0]);
                    player.sendMessage("§7- Shops: " + counts[1]);
                    player.sendMessage("§7Please update config.yml and restart server");
                });
                
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendError(player, "Migration failed: " + e.getMessage());
                });
                plugin.getLogger().severe("Migration error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleListCommand(Player player, String[] args) {
        if (args.length == 1) {
            // /dzth list - Show everything
            showCompleteList(player);
        } else if (args[1].equalsIgnoreCase("list")) {
            // /dzth areas list
            handleAreasList(player);
        }
    }

    private void handleAreasList(Player player) {
        java.util.List<Area> areas = plugin.getShopManager().getAllAreas();
        
        player.sendMessage("§6§l▒▒▒ All Areas (" + areas.size() + ") ▒▒▒");
        player.sendMessage("");
        
        int index = 1;
        for (Area area : areas) {
            player.sendMessage(String.format("§e%d. §f%s §7(%s) §7- §e%d shops",
                index++,
                area.getDisplayName(),
                area.getType().name(),
                area.getShops().size()));
        }
        player.sendMessage("");
    }

    private void handleShopsList(Player player, String[] args) {
        if (args.length >= 3 && args[1].equalsIgnoreCase("list")) {
            // /dzth shops list - Show all shops in all areas
            showAllShops(player);
        } else if (args.length >= 2) {
            // /dzth <area> shops list
            String areaName = args[0];
            showShopsInArea(player, areaName);
        } else {
            MessageUtil.sendError(player, "Usage: /dzth shops list OR /dzth <area> shops list");
        }
    }

    private void showAllShops(Player player) {
        java.util.List<Area> areas = plugin.getShopManager().getAllAreas();
        
        player.sendMessage("§6§l▒▒▒ All Shops ▒▒▒");
        player.sendMessage("");
        
        for (Area area : areas) {
            player.sendMessage("§6" + area.getDisplayName() + ":");
            java.util.List<Shop> shops = plugin.getShopManager().getShopsInArea(area.getName());
            
            int shopIndex = 1;
            for (Shop shop : shops) {
                player.sendMessage(String.format("  §e%d. §f%s §7(%s) §7- §e%d items",
                    shopIndex++,
                    shop.getDisplayName(),
                    shop.getShopType().name(),
                    shop.getItems().size()));
            }
        }
        player.sendMessage("");
    }

    private void showShopsInArea(Player player, String areaName) {
        Area area = plugin.getShopManager().getArea(areaName);
        if (area == null) {
            MessageUtil.sendError(player, "Area not found: " + areaName);
            return;
        }
        
        java.util.List<Shop> shops = plugin.getShopManager().getShopsInArea(areaName);
        
        player.sendMessage("§6§l▒▒▒ " + area.getDisplayName() + " Shops (" + shops.size() + ") ▒▒▒");
        player.sendMessage("");
        
        int index = 1;
        for (Shop shop : shops) {
            player.sendMessage(String.format("§e%d. §f%s §7(%s) §7- §e%d items",
                index++,
                shop.getDisplayName(),
                shop.getShopType().name(),
                shop.getItems().size()));
        }
        player.sendMessage("");
    }

    private void handleItemsList(Player player, String[] args) {
        if (args.length >= 4) {
            // /dzth <area> <shop> items list
            String areaName = args[0];
            String shopName = args[1];
            showItemsInShop(player, areaName, shopName);
        } else if (args.length >= 3 && args[1].equalsIgnoreCase("items")) {
            // /dzth <area> items list
            String areaName = args[0];
            showItemsInArea(player, areaName);
        } else if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
            // /dzth items list - Show all items
            showAllItems(player);
        } else {
            MessageUtil.sendError(player, "Usage: /dzth items list OR /dzth <area> items list OR /dzth <area> <shop> items list");
        }
    }

    private void showAllItems(Player player) {
        java.util.List<Area> areas = plugin.getShopManager().getAllAreas();
        
        player.sendMessage("§6§l▒▒▒ All Items ▒▒▒");
        player.sendMessage("");
        
        int totalItems = 0;
        for (Area area : areas) {
            java.util.List<Shop> shops = plugin.getShopManager().getShopsInArea(area.getName());
            for (Shop shop : shops) {
                totalItems += shop.getItems().size();
            }
        }
        
        player.sendMessage("§7Total items across all shops: §e" + totalItems);
        player.sendMessage("§7Use /dzth <area> items list for details");
        player.sendMessage("");
    }

    private void showItemsInArea(Player player, String areaName) {
        Area area = plugin.getShopManager().getArea(areaName);
        if (area == null) {
            MessageUtil.sendError(player, "Area not found: " + areaName);
            return;
        }
        
        player.sendMessage("§6§l▒▒▒ Items in " + area.getDisplayName() + " ▒▒▒");
        player.sendMessage("");
        
        java.util.List<Shop> shops = plugin.getShopManager().getShopsInArea(areaName);
        for (Shop shop : shops) {
            player.sendMessage("§6" + shop.getDisplayName() + ":");
            int itemIndex = 1;
            for (ShopItem item : shop.getItems()) {
                String itemName = item.getItemStack().getType().name();
                player.sendMessage(String.format("  §e%d. §f%s §7- Buy: §a%.2f §7Sell: §c%.2f",
                    itemIndex++,
                    itemName,
                    item.getBuyPrice(),
                    item.getSellPrice()));
            }
        }
        player.sendMessage("");
    }

    private void showItemsInShop(Player player, String areaName, String shopName) {
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found!");
            return;
        }
        
        player.sendMessage("§6§l▒▒▒ " + shop.getDisplayName() + " Items (" + shop.getItems().size() + ") ▒▒▒");
        player.sendMessage("");
        
        int index = 1;
        for (ShopItem item : shop.getItems()) {
            String itemName = item.getItemStack().getType().name();
            String currency = item.getCurrency().name();
            player.sendMessage(String.format("§e%d. §f%s §7(%s)",
                index++,
                itemName,
                currency));
            player.sendMessage(String.format("   §aBuy: %.2f §7| §cSell: %.2f §7| §eStock: %d/%d",
                item.getBuyPrice(),
                item.getSellPrice(),
                item.getCurrentStock(),
                item.getMaxStock()));
        }
        player.sendMessage("");
    }

    private void showCompleteList(Player player) {
        java.util.List<Area> areas = plugin.getShopManager().getAllAreas();
        
        player.sendMessage("§6§l▒▒▒▒▒▒ DZTradeHub Complete Overview ▒▒▒▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e§lAreas: §f" + areas.size());
        player.sendMessage("");
        
        int totalShops = 0;
        int totalItems = 0;
        
        for (Area area : areas) {
            java.util.List<Shop> shops = plugin.getShopManager().getShopsInArea(area.getName());
            totalShops += shops.size();
            
            player.sendMessage("§6▒ " + area.getDisplayName() + " §7(" + shops.size() + " shops)");
            
            for (Shop shop : shops) {
                int itemCount = shop.getItems().size();
                totalItems += itemCount;
                player.sendMessage(String.format("  §e├─ %s §7(%d items, %s)",
                    shop.getDisplayName(),
                    itemCount,
                    shop.getQueueType().name()));
            }
            player.sendMessage("");
        }
        
        player.sendMessage("§e§lTotal Shops: §f" + totalShops);
        player.sendMessage("§e§lTotal Items: §f" + totalItems);
        player.sendMessage("");
    }

    private void sendMemberHelp(Player player) {
        player.sendMessage("§6§l▒▒▒ TradeHub Commands ▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e/tradehub §7- Browse all marketplace areas");
        player.sendMessage("§e/<area_name> §7- Open specific area");
        player.sendMessage("  §7Examples: /supermarket, /bazar, /pawnshop");
        player.sendMessage("");
        player.sendMessage("§e/ah §7- Open auction house");
        player.sendMessage("§e/ah add §7- Create new auction");
        player.sendMessage("§e/ah list §7- List your auctions");
        player.sendMessage("§e/ah help §7- Show auction help");
        player.sendMessage("");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l▒▒▒ DZTradeHub Admin Commands ▒▒▒");
        player.sendMessage("");
        player.sendMessage("§c§lArea Management:");
        player.sendMessage("§e/dzth create-area <name> §7- Create marketplace area");
        player.sendMessage("§e/dzth delete-area <name> §7- Delete area");
        player.sendMessage("§e/dzth rename-area <old> <new> §7- Rename area");
        player.sendMessage("");
        player.sendMessage("§c§lShop Management:");
        player.sendMessage("§e/dzth create-shop <area> <shop> §7- Create shop");
        player.sendMessage("§e/dzth delete-shop <area> <shop> §7- Delete shop");
        player.sendMessage("§e/dzth rename-shop <area> <old> <new> §7- Rename shop");
        player.sendMessage("§e/dzth config <area> <shop> ... §7- Configure shop");
        player.sendMessage("§e/dzth link-shop <area> <sell> <buy> §7- Link shops");
        player.sendMessage("§e/dzth unlink-shop <area> <shop> §7- Unlink shop");
        player.sendMessage("");
        player.sendMessage("§c§lItem Management:");
        player.sendMessage("§e/dzth add-item <area> <shop> §7- Add items");
        player.sendMessage("§e/dzth remove-item <area> <shop> <item> §7- Remove item");
        player.sendMessage("");
        player.sendMessage("§c§lList Commands:");
        player.sendMessage("§e/dzth list §7- Show complete overview");
        player.sendMessage("§e/dzth areas list §7- List all areas");
        player.sendMessage("§e/dzth shops list §7- List all shops");
        player.sendMessage("§e/dzth <area> shops list §7- List shops in area");
        player.sendMessage("§e/dzth items list §7- List all items");
        player.sendMessage("§e/dzth <area> items list §7- List items in area");
        player.sendMessage("§e/dzth <area> <shop> items list §7- List items in shop");
        player.sendMessage("");
        player.sendMessage("§c§lSystem:");
        player.sendMessage("§e/dzth migrate <type> §7- Migrate database");
        player.sendMessage("§e/dzth reload §7- Reload config");
        player.sendMessage("§e/dzth credits §7- Show credits");
        player.sendMessage("");
        player.sendMessage("§c§lUpdate System:");
        player.sendMessage("§e/dzth update latest §7- Update to latest version");
        player.sendMessage("§e/dzth update previous §7- Downgrade to previous version");
        player.sendMessage("§e/dzth update next §7- Update to next version");
        player.sendMessage("§e/dzth update auto §7- Toggle auto-update");
        player.sendMessage("§e/dzth disable auto-update §7- Disable auto-update");
        player.sendMessage("");
    }
}
