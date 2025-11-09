package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager {
    private final DZTradeHub plugin;
    private final Map<UUID, Auction> auctions;
    private final Map<UUID, List<UUID>> playerListings; // playerUUID -> list of auction IDs
    private final Map<UUID, Integer> playerItemNumbers; // playerUUID -> next item number

    public AuctionManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.auctions = new ConcurrentHashMap<>();
        this.playerListings = new ConcurrentHashMap<>();
        this.playerItemNumbers = new ConcurrentHashMap<>();
    }

    public void createAuction(Auction auction) {
        // Assign item number
        UUID playerUUID = auction.getSellerUUID();
        int itemNumber = playerItemNumbers.getOrDefault(playerUUID, 1);
        auction.setItemNumber(itemNumber);
        playerItemNumbers.put(playerUUID, itemNumber + 1);
        
        auctions.put(auction.getId(), auction);
        playerListings.computeIfAbsent(auction.getSellerUUID(), k -> new ArrayList<>())
                      .add(auction.getId());
        
        // Save to storage
        plugin.getFileStorageManager().saveAuction(auction);
        
        plugin.getLogger().info("Created auction #" + itemNumber + ": " + auction.getId());
    }

    public Auction getAuction(UUID auctionId) {
        return auctions.get(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    public List<Auction> getPlayerAuctions(UUID playerUUID) {
        List<UUID> ids = playerListings.get(playerUUID);
        if (ids == null) return new ArrayList<>();
        
        List<Auction> result = new ArrayList<>();
        for (UUID id : ids) {
            Auction auction = auctions.get(id);
            if (auction != null) {
                result.add(auction);
            }
        }
        return result;
    }

    public int getPlayerListingCount(UUID playerUUID) {
        return playerListings.getOrDefault(playerUUID, new ArrayList<>()).size();
    }

    public void cancelAuction(UUID auctionId) {
        Auction auction = auctions.remove(auctionId);
        if (auction != null) {
            // Remove from player listings
            List<UUID> listings = playerListings.get(auction.getSellerUUID());
            if (listings != null) {
                listings.remove(auctionId);
            }
            
            // Refund all queued buyers
            auction.getQueue().forEach(entry -> {
                Player player = plugin.getServer().getPlayer(entry.playerUUID());
                if (player != null) {
                    // Get currency type from auction
                    online.demonzdevelopment.dzeconomy.currency.CurrencyType currencyType = 
                        getCurrencyTypeFromString(auction.getCurrencyType());
                    
                    plugin.getEconomyAPI().addCurrency(
                        entry.playerUUID(),
                        currencyType,
                        entry.paidPrice()
                    );
                    player.sendMessage("§eAuction cancelled. Refunded: " + formatCurrency(entry.paidPrice(), auction.getCurrencyType()));
                }
            });
            
            // Delete from storage
            plugin.getFileStorageManager().deleteAuction(auctionId);
            
            plugin.getLogger().info("Cancelled auction: " + auctionId);
        }
    }
    
    public Auction getAuctionByNumber(UUID playerUUID, int itemNumber) {
        List<Auction> playerAuctions = getPlayerAuctions(playerUUID);
        for (Auction auction : playerAuctions) {
            if (auction.getItemNumber() == itemNumber) {
                return auction;
            }
        }
        return null;
    }
    
    private online.demonzdevelopment.dzeconomy.currency.CurrencyType getCurrencyTypeFromString(String currency) {
        return switch (currency.toUpperCase()) {
            case "MOBCOIN" -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN;
            case "GEM" -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM;
            default -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY;
        };
    }
    
    private String formatCurrency(double amount, String currencyType) {
        String symbol = switch (currencyType.toUpperCase()) {
            case "MOBCOIN" -> "MC";
            case "GEM" -> "G";
            default -> "$";
        };
        return symbol + String.format("%.2f", amount);
    }

    public void updatePrices() {
        auctions.values().forEach(auction -> {
            if (!auction.isFrozen() && auction.getDropPerUnit() > 0) {
                double newPrice = auction.getCurrentPrice();
                if (newPrice != auction.getActualPrice()) {
                    auction.updatePriceAfterDrop();
                    plugin.getLogger().info("Updated auction " + auction.getId() + " price to: $" + newPrice);
                }
            }
        });
    }

    public void purchaseAuction(Player buyer, Auction auction) {
        double currentPrice = auction.getCurrentPrice();
        
        // Check if it's a bidding queue auction
        if (auction.getMaxQueue() > 0) {
            // Add to queue
            auction.addToQueue(buyer.getUniqueId(), currentPrice);
            
            // Check if queue is full
            if (auction.isQueueFull()) {
                // Increase price
                auction.increasePriceAfterQueueFill();
                buyer.sendMessage("§eQueue full! Price increased to: $" + auction.getActualPrice());
            }
        } else {
            // Direct purchase (price reduction auction)
            // Remove auction
            auctions.remove(auction.getId());
            
            // Remove from player listings
            List<UUID> listings = playerListings.get(auction.getSellerUUID());
            if (listings != null) {
                listings.remove(auction.getId());
            }
            
            // Give item to buyer
            buyer.getInventory().addItem(auction.getItemStack());
            
            // Pay seller
            Player seller = plugin.getServer().getPlayer(auction.getSellerUUID());
            if (seller != null) {
                seller.sendMessage("§aYour auction sold for: $" + currentPrice);
            }
            
            buyer.sendMessage("§aPurchased item for: $" + currentPrice);
        }
    }

    public void checkExpiredAuctions() {
        long now = System.currentTimeMillis();
        int maxDays = plugin.getConfigManager().getMaxAuctionDurationDays();
        long maxDuration = maxDays * 24L * 60 * 60 * 1000;
        List<UUID> toRemove = new ArrayList<>();
        
        auctions.values().forEach(auction -> {
            // Check if auction has been running for too long
            long elapsed = now - auction.getCreatedTime();
            if (elapsed > maxDuration) {
                // Return item to seller
                Player seller = plugin.getServer().getPlayer(auction.getSellerUUID());
                if (seller != null) {
                    seller.getInventory().addItem(auction.getItemStack());
                    seller.sendMessage("§eYour auction expired. Item returned.");
                }
                toRemove.add(auction.getId());
            }
        });
        
        toRemove.forEach(this::cancelAuction);
    }
    
    /**
     * Cleanup old auctions based on retention policy
     */
    public void cleanupOldAuctions() {
        if (!plugin.getConfigManager().isAuctionCleanupEnabled()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        int retentionDays = plugin.getConfigManager().getAuctionRetentionDays();
        long retentionMillis = retentionDays * 24L * 60 * 60 * 1000;
        
        int cleaned = plugin.getFileStorageManager().cleanupOldAuctions(retentionMillis);
        plugin.getLogger().info("Cleaned up " + cleaned + " old auctions (older than " + retentionDays + " days)");
    }
    
    /**
     * Load all auctions from storage
     */
    public void loadAuctions() {
        List<Auction> loadedAuctions = plugin.getFileStorageManager().loadAllAuctions();
        for (Auction auction : loadedAuctions) {
            auctions.put(auction.getId(), auction);
            playerListings.computeIfAbsent(auction.getSellerUUID(), k -> new ArrayList<>())
                          .add(auction.getId());
            
            // Update item number counter
            int itemNumber = auction.getItemNumber();
            UUID playerUUID = auction.getSellerUUID();
            int currentMax = playerItemNumbers.getOrDefault(playerUUID, 0);
            if (itemNumber >= currentMax) {
                playerItemNumbers.put(playerUUID, itemNumber + 1);
            }
        }
        plugin.getLogger().info("Loaded " + loadedAuctions.size() + " auctions from storage");
    }
    
    /**
     * Save all auctions to storage
     */
    public void saveAllAuctions() {
        for (Auction auction : auctions.values()) {
            plugin.getFileStorageManager().saveAuction(auction);
        }
        plugin.getLogger().info("Saved " + auctions.size() + " auctions to storage");
    }
}