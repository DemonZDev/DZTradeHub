package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Bounty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BountyManager {
    private final DZTradeHub plugin;
    private final Map<UUID, List<Bounty>> activeBounties; // targetPlayer -> bounties
    private final Map<UUID, Integer> playerBountyNumbers; // creatorPlayer -> next bounty number

    public BountyManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.activeBounties = new ConcurrentHashMap<>();
        this.playerBountyNumbers = new ConcurrentHashMap<>();
        loadBounties();
    }

    /**
     * Add a bounty on a player
     */
    public CompletableFuture<Boolean> addBounty(Bounty bounty) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Assign bounty number
                UUID creatorUUID = bounty.getCreatorPlayer();
                int bountyNumber = playerBountyNumbers.getOrDefault(creatorUUID, 1);
                bounty.setBountyNumber(bountyNumber);
                playerBountyNumbers.put(creatorUUID, bountyNumber + 1);
                
                // Save to database
                plugin.getDatabaseManager().saveBountyAsync(bounty).join();
                
                // Add to cache
                activeBounties.computeIfAbsent(bounty.getTargetPlayer(), k -> new ArrayList<>()).add(bounty);
                
                // Notify online players
                Player target = Bukkit.getPlayer(bounty.getTargetPlayer());
                if (target != null && target.isOnline()) {
                    target.sendMessage("§c§l⚠ A bounty has been placed on your head!");
                }
                
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to add bounty: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get bounty by creator and bounty number
     */
    public Bounty getBountyByNumber(UUID creatorUUID, int bountyNumber) {
        for (List<Bounty> bounties : activeBounties.values()) {
            for (Bounty bounty : bounties) {
                if (bounty.getCreatorPlayer().equals(creatorUUID) && bounty.getBountyNumber() == bountyNumber) {
                    return bounty;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all bounties created by a player
     */
    public List<Bounty> getBountiesCreatedBy(UUID creatorUUID) {
        List<Bounty> result = new ArrayList<>();
        for (List<Bounty> bounties : activeBounties.values()) {
            for (Bounty bounty : bounties) {
                if (bounty.getCreatorPlayer().equals(creatorUUID)) {
                    result.add(bounty);
                }
            }
        }
        return result;
    }
    
    /**
     * Remove bounty by ID
     */
    public CompletableFuture<Boolean> removeBounty(UUID bountyId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Find and remove bounty
                for (Map.Entry<UUID, List<Bounty>> entry : activeBounties.entrySet()) {
                    List<Bounty> bounties = entry.getValue();
                    Bounty toRemove = null;
                    for (Bounty bounty : bounties) {
                        if (bounty.getBountyId().equals(bountyId)) {
                            toRemove = bounty;
                            break;
                        }
                    }
                    if (toRemove != null) {
                        bounties.remove(toRemove);
                        if (bounties.isEmpty()) {
                            activeBounties.remove(entry.getKey());
                        }
                        
                        // Remove from database
                        plugin.getDatabaseManager().removeBountyAsync(bountyId).join();
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to remove bounty: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Remove all bounties on a player
     */
    public CompletableFuture<Boolean> removeBounties(UUID targetPlayer, UUID removerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Bounty> bounties = activeBounties.get(targetPlayer);
                if (bounties == null || bounties.isEmpty()) {
                    return false;
                }
                
                // Filter bounties created by the remover
                List<Bounty> toRemove = bounties.stream()
                    .filter(b -> b.getCreatorPlayer().equals(removerUUID))
                    .collect(Collectors.toList());
                
                if (toRemove.isEmpty()) {
                    return false;
                }
                
                // Remove from database
                for (Bounty bounty : toRemove) {
                    plugin.getDatabaseManager().removeBountyAsync(bounty.getBountyId()).join();
                }
                
                // Remove from cache
                bounties.removeAll(toRemove);
                if (bounties.isEmpty()) {
                    activeBounties.remove(targetPlayer);
                }
                
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to remove bounties: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get all bounties on a player
     */
    public List<Bounty> getBounties(UUID targetPlayer) {
        List<Bounty> bounties = activeBounties.get(targetPlayer);
        return bounties != null ? new ArrayList<>(bounties) : new ArrayList<>();
    }

    /**
     * Get all active bounties
     */
    public Map<UUID, List<Bounty>> getAllBounties() {
        return new HashMap<>(activeBounties);
    }

    /**
     * Check if a player has bounties
     */
    public boolean hasBounty(UUID targetPlayer) {
        List<Bounty> bounties = activeBounties.get(targetPlayer);
        return bounties != null && !bounties.isEmpty();
    }

    /**
     * Claim bounty when a player kills the target
     */
    public void claimBounty(Player killer, Player victim) {
        List<Bounty> bounties = activeBounties.get(victim.getUniqueId());
        if (bounties == null || bounties.isEmpty()) {
            return;
        }

        // Calculate total rewards
        double totalMoney = 0;
        double totalMobcoin = 0;
        double totalGem = 0;
        List<ItemStack> allItems = new ArrayList<>();

        for (Bounty bounty : bounties) {
            totalMoney += bounty.getMoneyReward();
            totalMobcoin += bounty.getMobcoinReward();
            totalGem += bounty.getGemReward();
            allItems.addAll(bounty.getRewardItems());
        }

        // Award currency rewards
        if (totalMoney > 0) {
            plugin.getEconomyAPI().addCurrency(killer.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY, totalMoney);
        }
        if (totalMobcoin > 0) {
            plugin.getEconomyAPI().addCurrency(killer.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN, totalMobcoin);
        }
        if (totalGem > 0) {
            plugin.getEconomyAPI().addCurrency(killer.getUniqueId(), 
                online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM, totalGem);
        }

        // Award item rewards
        for (ItemStack item : allItems) {
            // Try to add to inventory, drop if full
            HashMap<Integer, ItemStack> leftover = killer.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    killer.getWorld().dropItemNaturally(killer.getLocation(), drop);
                }
            }
        }

        // Notify killer
        killer.sendMessage("§6§l✦ BOUNTY CLAIMED! ✦");
        if (totalMoney > 0) {
            killer.sendMessage("§e+ §a" + formatAmount(totalMoney) + " money");
        }
        if (totalMobcoin > 0) {
            killer.sendMessage("§e+ §a" + formatAmount(totalMobcoin) + " mobcoin");
        }
        if (totalGem > 0) {
            killer.sendMessage("§e+ §a" + formatAmount(totalGem) + " gem");
        }
        if (!allItems.isEmpty()) {
            killer.sendMessage("§e+ §a" + allItems.size() + " items");
        }

        // Notify victim
        victim.sendMessage("§c§lYour bounty has been claimed by " + killer.getName() + "!");

        // Remove bounties from database
        for (Bounty bounty : bounties) {
            plugin.getDatabaseManager().removeBountyAsync(bounty.getBountyId());
        }

        // Remove from cache
        activeBounties.remove(victim.getUniqueId());
    }

    /**
     * Load bounties from database
     */
    private void loadBounties() {
        CompletableFuture.runAsync(() -> {
            try {
                Map<UUID, List<Bounty>> loaded = plugin.getDatabaseManager().loadBountiesAsync().join();
                activeBounties.clear();
                activeBounties.putAll(loaded);
                plugin.getLogger().info("Loaded " + activeBounties.size() + " active bounties");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load bounties: " + e.getMessage());
            }
        });
    }

    /**
     * Format amount for display
     */
    private String formatAmount(double amount) {
        if (amount >= 1000000) {
            return String.format("%.2fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.2fK", amount / 1000);
        } else {
            return String.format("%.2f", amount);
        }
    }
}
