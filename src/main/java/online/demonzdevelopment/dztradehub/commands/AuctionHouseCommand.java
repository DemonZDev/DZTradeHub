package online.demonzdevelopment.dztradehub.commands;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
import online.demonzdevelopment.dztradehub.data.RankData;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import online.demonzdevelopment.dztradehub.utils.TimeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AuctionHouseCommand implements CommandExecutor {
    private final DZTradeHub plugin;

    public AuctionHouseCommand(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!player.hasPermission("dztradehub.auction")) {
            MessageUtil.sendError(player, "You don't have permission to use the auction house!");
            return true;
        }

        if (args.length == 0) {
            // Open auction browser
            plugin.getAuctionBrowserGUI().openBrowser(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(player);
            case "add-hand-item" -> handleAddHandItem(player, args);
            case "list" -> handleList(player);
            case "manage" -> handleManage(player, args);
            case "remove" -> handleRemove(player, args);
            case "rename-price" -> handleRenamePrice(player, args);
            case "cancel-item" -> handleCancelItem(player, args);
            case "freeze-item" -> handleFreezeItem(player, args);
            case "un-freeze-item" -> handleUnFreezeItem(player, args);
            case "manage-items" -> handleManageItems(player);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleAdd(Player player) {
        // Open auction creation GUI
        plugin.getAuctionBrowserGUI().openAuctionAddGUI(player);
    }

    private void handleAddHandItem(Player player, String[] args) {
        if (args.length < 9) {
            MessageUtil.sendError(player, "Usage: /ah add-hand-item <currency> <actual> <maxdrop> <drop> <time> <unit> <queue> <increase>");
            MessageUtil.sendInfo(player, "Currency: MONEY, MOBCOIN, GEM");
            return;
        }

        // Check listing limit
        RankData rank = plugin.getPermissionManager().getPlayerRank(player);
        int currentListings = plugin.getAuctionManager().getPlayerListingCount(player.getUniqueId());
        
        if (currentListings >= rank.auctionSettings().maxListings()) {
            MessageUtil.sendError(player, "Listing limit reached! (" + currentListings + "/" + rank.auctionSettings().maxListings() + ")");
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType().isAir()) {
            MessageUtil.sendError(player, "You must hold an item in your hand!");
            return;
        }

        try {
            String currencyType = args[1].toUpperCase();
            double actualPrice = Double.parseDouble(args[2]);
            double maxDropPrice = Double.parseDouble(args[3]);
            double dropPerUnit = Double.parseDouble(args[4]);
            int timeValue = Integer.parseInt(args[5]);
            String timeUnit = args[6];
            int maxQueue = Integer.parseInt(args[7]);
            double priceIncrease = Double.parseDouble(args[8]);

            // Validate currency
            if (!currencyType.equals("MONEY") && !currencyType.equals("MOBCOIN") && !currencyType.equals("GEM")) {
                MessageUtil.sendError(player, "Invalid currency! Use: MONEY, MOBCOIN, or GEM");
                return;
            }

            long timeInSeconds = timeValue * TimeUtil.parseTimeUnit(timeUnit);
            int dropIntervalHours = (int) (timeInSeconds / 3600);

            // Charge listing fee
            double listingFee = rank.auctionSettings().listingFee();
            if (!plugin.getEconomyAPI().hasBalance(
                    player.getUniqueId(),
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
                    listingFee)) {
                MessageUtil.sendError(player, "You need $" + listingFee + " listing fee!");
                return;
            }

            plugin.getEconomyAPI().removeCurrency(
                player.getUniqueId(),
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY,
                listingFee
            );

            // Remove item from player
            player.getInventory().setItemInMainHand(null);

            // Create auction
            Auction auction = new Auction(
                player.getUniqueId(),
                handItem,
                currencyType,
                actualPrice,
                maxDropPrice,
                dropPerUnit,
                dropIntervalHours,
                maxQueue,
                priceIncrease
            );

            plugin.getAuctionManager().createAuction(auction);
            MessageUtil.sendSuccess(player, "Auction created successfully!");
            MessageUtil.sendInfo(player, "Item #" + auction.getItemNumber() + " listed for " + formatCurrency(actualPrice, currencyType));

        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid number format!");
        }
    }

    private void handleList(Player player) {
        java.util.List<Auction> playerAuctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId());
        
        if (playerAuctions.isEmpty()) {
            MessageUtil.sendInfo(player, "You have no active auctions.");
            return;
        }

        player.sendMessage("§6§l▒▒▒ Your Auctions ▒▒▒");
        player.sendMessage("");
        
        for (Auction auction : playerAuctions) {
            String itemName = auction.getItemStack().getType().name();
            int amount = auction.getItemStack().getAmount();
            double currentPrice = auction.getCurrentPrice();
            String currency = auction.getCurrencyType();
            String status = auction.isFrozen() ? "§c[FROZEN]" : "§a[ACTIVE]";
            
            player.sendMessage(String.format("§e#%d §7- §f%dx %s §7- %s %s",
                auction.getItemNumber(),
                amount,
                itemName,
                formatCurrency(currentPrice, currency),
                status));
        }
        
        player.sendMessage("");
        player.sendMessage("§7Use /ah remove <number> to cancel an auction");
    }

    private void handleManage(Player player, String[] args) {
        // /ah manage - Opens GUI showing all user's auctions
        if (args.length == 1) {
            plugin.getAuctionBrowserGUI().openManageAllAuctionsGUI(player);
            return;
        }

        // /ah manage <item_number> - Opens management GUI for specific auction
        try {
            int itemNumber = Integer.parseInt(args[1]);
            Auction auction = plugin.getAuctionManager().getAuctionByNumber(player.getUniqueId(), itemNumber);
            
            if (auction == null) {
                MessageUtil.sendError(player, "You don't have an auction with item #" + itemNumber);
                return;
            }

            // Open management GUI
            plugin.getAuctionBrowserGUI().openAuctionManageGUI(player, auction);

        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid item number!");
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /ah remove <item_number>");
            return;
        }

        try {
            int itemNumber = Integer.parseInt(args[1]);
            Auction auction = plugin.getAuctionManager().getAuctionByNumber(player.getUniqueId(), itemNumber);
            
            if (auction == null) {
                MessageUtil.sendError(player, "You don't have an auction with item #" + itemNumber);
                return;
            }

            plugin.getAuctionManager().cancelAuction(auction.getId());
            player.getInventory().addItem(auction.getItemStack());
            MessageUtil.sendSuccess(player, "Auction #" + itemNumber + " cancelled and item returned.");

        } catch (NumberFormatException e) {
            MessageUtil.sendError(player, "Invalid item number!");
        }
    }

    private String formatCurrency(double amount, String currencyType) {
        String symbol = switch (currencyType.toUpperCase()) {
            case "MOBCOIN" -> "MC";
            case "GEM" -> "G";
            default -> "$";
        };
        return symbol + String.format("%.2f", amount);
    }

    private void handleRenamePrice(Player player, String[] args) {
        MessageUtil.sendInfo(player, "Rename price - Not yet implemented");
    }

    private void handleCancelItem(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /ah cancel-item <id>");
            return;
        }

        try {
            UUID auctionId = UUID.fromString(args[1]);
            Auction auction = plugin.getAuctionManager().getAuction(auctionId);
            
            if (auction == null) {
                MessageUtil.sendError(player, "Auction not found!");
                return;
            }

            if (!auction.getSellerUUID().equals(player.getUniqueId()) && 
                !player.hasPermission("dztradehub.admin")) {
                MessageUtil.sendError(player, "You can only cancel your own auctions!");
                return;
            }

            plugin.getAuctionManager().cancelAuction(auctionId);
            player.getInventory().addItem(auction.getItemStack());
            MessageUtil.sendSuccess(player, "Auction cancelled and item returned.");

        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid auction ID!");
        }
    }

    private void handleFreezeItem(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /ah freeze-item <id>");
            return;
        }

        try {
            UUID auctionId = UUID.fromString(args[1]);
            Auction auction = plugin.getAuctionManager().getAuction(auctionId);
            
            if (auction == null) {
                MessageUtil.sendError(player, "Auction not found!");
                return;
            }

            if (!auction.getSellerUUID().equals(player.getUniqueId()) && 
                !player.hasPermission("dztradehub.admin")) {
                MessageUtil.sendError(player, "You can only freeze your own auctions!");
                return;
            }

            auction.setFrozen(true);
            MessageUtil.sendSuccess(player, "Auction frozen.");

        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid auction ID!");
        }
    }

    private void handleUnFreezeItem(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendError(player, "Usage: /ah un-freeze-item <id>");
            return;
        }

        try {
            UUID auctionId = UUID.fromString(args[1]);
            Auction auction = plugin.getAuctionManager().getAuction(auctionId);
            
            if (auction == null) {
                MessageUtil.sendError(player, "Auction not found!");
                return;
            }

            if (!auction.getSellerUUID().equals(player.getUniqueId()) && 
                !player.hasPermission("dztradehub.admin")) {
                MessageUtil.sendError(player, "You can only unfreeze your own auctions!");
                return;
            }

            auction.setFrozen(false);
            MessageUtil.sendSuccess(player, "Auction resumed.");

        } catch (IllegalArgumentException e) {
            MessageUtil.sendError(player, "Invalid auction ID!");
        }
    }

    private void handleManageItems(Player player) {
        MessageUtil.sendInfo(player, "Manage items GUI - Not yet implemented");
    }

    private void sendHelp(Player player) {
        boolean isAdmin = player.hasPermission("dztradehub.admin");
        
        player.sendMessage("§6§l▒▒▒ Auction House Commands ▒▒▒");
        player.sendMessage("");
        player.sendMessage("§e/ah §7- Open auction browser");
        player.sendMessage("§e/ah add §7- Open auction creation GUI");
        player.sendMessage("§e/ah list §7- List your auctions");
        player.sendMessage("§e/ah remove <number> §7- Cancel auction by number");
        player.sendMessage("§e/ah add-hand-item <currency> <actual> <maxdrop> <drop> <time> <unit> <queue> <increase>");
        player.sendMessage("  §7- List item from hand");
        player.sendMessage("  §7  Currency: MONEY, MOBCOIN, GEM");
        player.sendMessage("  §7  Example: /ah add-hand-item MONEY 1000 500 50 2 h 5 20");
        
        if (isAdmin) {
            player.sendMessage("");
            player.sendMessage("§c§lAdmin Commands:");
            player.sendMessage("§e/ah cancel-item <id> §7- Cancel any auction by ID");
            player.sendMessage("§e/ah freeze-item <id> §7- Freeze auction");
            player.sendMessage("§e/ah un-freeze-item <id> §7- Resume auction");
            player.sendMessage("§e/ah manage-items §7- Manage all auctions");
        }
        
        player.sendMessage("");
    }
}