package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import online.demonzdevelopment.dztradehub.gui.SellGUI;
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

import java.util.*;
import java.util.stream.Collectors;

public class SellCommand implements CommandExecutor, TabCompleter {
    private final DZTradeHub plugin;
    private final SellGUI sellGUI;

    public SellCommand(DZTradeHub plugin) {
        this.plugin = plugin;
        this.sellGUI = new SellGUI(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "sell" -> handleSell(player, args);
            case "sellall" -> handleSellAll(player, args);
            case "sellhand" -> handleSellHand(player, args);
        }

        return true;
    }

    /**
     * Handle /sell command
     */
    private void handleSell(Player player, String[] args) {
        if (args.length == 0) {
            // Open area selection GUI
            sellGUI.openAreaSelection(player);
            return;
        }

        String areaName = args[0];
        Area area = plugin.getShopManager().getArea(areaName);

        if (area == null) {
            MessageUtil.sendError(player, "Area not found: " + areaName);
            return;
        }

        if (args.length == 1) {
            // Open shop selection for area
            sellGUI.openShopSelection(player, area);
            return;
        }

        String shopName = args[1];
        Shop shop = plugin.getShopManager().getShop(areaName, shopName);

        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found: " + shopName);
            return;
        }

        // Open sell GUI for specific shop
        sellGUI.openSellInterface(player, area, shop);
    }

    /**
     * Handle /sellall command - sell all inventory items
     */
    private void handleSellAll(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /sellall <area> <shop>");
            MessageUtil.sendInfo(player, "Or use /sell to select from GUI");
            return;
        }

        String areaName = args[0];
        String shopName = args[1];

        Area area = plugin.getShopManager().getArea(areaName);
        if (area == null) {
            MessageUtil.sendError(player, "Area not found: " + areaName);
            return;
        }

        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found: " + shopName);
            return;
        }

        // Sell all items in inventory
        List<ItemStack> itemsToSell = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                itemsToSell.add(item.clone());
            }
        }

        if (itemsToSell.isEmpty()) {
            MessageUtil.sendError(player, "No items to sell!");
            return;
        }

        // Execute sell
        executeSell(player, shop, itemsToSell, true);
    }

    /**
     * Handle /sellhand command - sell item in hand
     */
    private void handleSellHand(Player player, String[] args) {
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            MessageUtil.sendError(player, "You're not holding anything!");
            return;
        }

        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /sellhand <area> <shop>");
            MessageUtil.sendInfo(player, "Or use /sell to select from GUI");
            return;
        }

        String areaName = args[0];
        String shopName = args[1];

        Area area = plugin.getShopManager().getArea(areaName);
        if (area == null) {
            MessageUtil.sendError(player, "Area not found: " + areaName);
            return;
        }

        Shop shop = plugin.getShopManager().getShop(areaName, shopName);
        if (shop == null) {
            MessageUtil.sendError(player, "Shop not found: " + shopName);
            return;
        }

        // Sell hand item
        List<ItemStack> itemsToSell = new ArrayList<>();
        itemsToSell.add(handItem.clone());

        executeSell(player, shop, itemsToSell, false);
    }

    /**
     * Execute the sell transaction
     */
    public void executeSell(Player player, Shop shop, List<ItemStack> itemsToSell, boolean clearInventory) {
        Map<ShopItem, Integer> sellableItems = new HashMap<>();
        List<ItemStack> unsellableItems = new ArrayList<>();
        double totalEarnings = 0.0;

        // Process each item
        for (ItemStack item : itemsToSell) {
            if (item == null || item.getType() == Material.AIR) continue;

            // Find matching shop item
            ShopItem shopItem = findShopItem(shop, item);

            if (shopItem != null && shopItem.canSell()) {
                int amount = item.getAmount();
                sellableItems.put(shopItem, sellableItems.getOrDefault(shopItem, 0) + amount);
                totalEarnings += shopItem.getSellPrice() * amount;
            } else {
                unsellableItems.add(item);
            }
        }

        if (sellableItems.isEmpty()) {
            MessageUtil.sendError(player, "No items can be sold to this shop!");
            return;
        }

        // Clear inventory if sellall
        if (clearInventory) {
            player.getInventory().clear();
        } else {
            // Remove sold items from inventory
            for (var entry : sellableItems.entrySet()) {
                ShopItem shopItem = entry.getKey();
                int amount = entry.getValue();
                removeItemsFromInventory(player, shopItem.getItemStack().getType(), amount);
            }
        }

        // Return unsellable items
        for (ItemStack unsellable : unsellableItems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(unsellable);
            // Drop items that don't fit
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }
        }

        // Give money to player
        CurrencyType currency = shop.getCurrencyType();
        plugin.getEconomyAPI().addCurrency(player.getUniqueId(), currency, totalEarnings);

        // Send success message
        player.sendMessage("§a§l✓ Items Sold!");
        player.sendMessage("§7Sold §e" + sellableItems.size() + "§7 different item types");
        player.sendMessage("§7Earned: §a" + String.format("%.2f", totalEarnings) + " " + currency.name());

        if (!unsellableItems.isEmpty()) {
            player.sendMessage("§7Returned §c" + unsellableItems.size() + "§7 unsellable items");
        }

        // Update shop stock if tracked
        for (var entry : sellableItems.entrySet()) {
            ShopItem shopItem = entry.getKey();
            int amount = entry.getValue();
            
            // Increase shop stock when players sell
            if (shopItem.getCurrentStock() < shopItem.getMaxStock()) {
                int newStock = Math.min(shopItem.getCurrentStock() + amount, shopItem.getMaxStock());
                shopItem.setCurrentStock(newStock);
            }
        }

        // Save shop data
        plugin.getFileStorageManager().saveShop(shop.getArea().getName(), shop);
    }

    /**
     * Find a shop item that matches the given item
     */
    private ShopItem findShopItem(Shop shop, ItemStack item) {
        for (ShopItem shopItem : shop.getItems()) {
            if (shopItem.getItemStack().getType() == item.getType()) {
                // Basic match by material type
                // Can be enhanced to check NBT data, enchantments, etc.
                return shopItem;
            }
        }
        return null;
    }

    /**
     * Remove items from player inventory
     */
    private void removeItemsFromInventory(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    contents[i] = null;
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }

        player.getInventory().setContents(contents);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // Suggest area names
            return plugin.getShopManager().getAllAreas().stream()
                .map(Area::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // Suggest shop names in area
            Area area = plugin.getShopManager().getArea(args[0]);
            if (area != null) {
                return plugin.getShopManager().getShopsInArea(args[0]).stream()
                    .map(Shop::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
