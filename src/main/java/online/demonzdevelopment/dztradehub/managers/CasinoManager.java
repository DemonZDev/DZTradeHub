package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.CoinFlipRequest;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CasinoManager {
    private final DZTradeHub plugin;
    private final Map<UUID, List<CoinFlipRequest>> pendingRequests; // toPlayer -> requests
    private final Map<UUID, CoinFlipRequest> lastRequests; // toPlayer -> last request for quick accept/deny
    private final Random random;

    public CasinoManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.lastRequests = new ConcurrentHashMap<>();
        this.random = new Random();
        
        // Start task to remove expired requests
        startExpiryTask();
    }

    /**
     * Perform single player coin flip
     */
    public void performSinglePlayerCoinFlip(Player player, String currencyType, double amount, CoinFlipRequest.CoinSide playerChoice) {
        // Check if player has enough balance
        if (!hasBalance(player, currencyType, amount)) {
            MessageUtil.sendError(player, "You don't have enough " + currencyType.toLowerCase() + "!");
            return;
        }

        // Deduct the amount
        deductBalance(player, currencyType, amount);

        // Flip the coin
        CoinFlipRequest.CoinSide result = random.nextBoolean() ? CoinFlipRequest.CoinSide.HEAD : CoinFlipRequest.CoinSide.TAIL;

        // Animate the flip
        animateCoinFlip(player, result, won -> {
            if (won) {
                // Player wins - return 2x
                double winAmount = amount * 2;
                addBalance(player, currencyType, winAmount);
                MessageUtil.sendSuccess(player, "§a§lYOU WON!");
                MessageUtil.sendInfo(player, "§eYou won §a" + formatAmount(winAmount) + " " + currencyType.toLowerCase() + "§e!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                // Player loses
                MessageUtil.sendError(player, "§c§lYOU LOST!");
                MessageUtil.sendInfo(player, "§eYou lost §c" + formatAmount(amount) + " " + currencyType.toLowerCase() + "§e!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }, playerChoice, result);
    }

    /**
     * Send double player coin flip request
     */
    public void sendCoinFlipRequest(Player fromPlayer, Player toPlayer, String currencyType, double amount, CoinFlipRequest.CoinSide side) {
        // Check if sender has enough balance
        if (!hasBalance(fromPlayer, currencyType, amount)) {
            MessageUtil.sendError(fromPlayer, "You don't have enough " + currencyType.toLowerCase() + "!");
            return;
        }

        // Get expiry time from config
        int expiryMinutes = plugin.getConfig().getInt("casino.coinflip.request-expiry-minutes", 2);
        
        // Create request
        CoinFlipRequest request = new CoinFlipRequest(fromPlayer.getUniqueId(), toPlayer.getUniqueId(), 
                                                      currencyType, amount, side, expiryMinutes);
        
        // Add to pending requests
        pendingRequests.computeIfAbsent(toPlayer.getUniqueId(), k -> new ArrayList<>()).add(request);
        lastRequests.put(toPlayer.getUniqueId(), request);

        // Notify players
        MessageUtil.sendSuccess(fromPlayer, "Coin flip request sent to " + toPlayer.getName());
        MessageUtil.sendInfo(fromPlayer, "Amount: " + formatAmount(amount) + " " + currencyType.toLowerCase() + " | Your side: " + side);
        
        MessageUtil.sendInfo(toPlayer, "§e§l⚠ COIN FLIP REQUEST");
        MessageUtil.sendInfo(toPlayer, "§e" + fromPlayer.getName() + " challenges you to a coin flip!");
        MessageUtil.sendInfo(toPlayer, "§eAmount: §a" + formatAmount(amount) + " " + currencyType.toLowerCase());
        MessageUtil.sendInfo(toPlayer, "§eTheir side: §6" + side);
        MessageUtil.sendInfo(toPlayer, "§eYour side: §6" + (side == CoinFlipRequest.CoinSide.HEAD ? CoinFlipRequest.CoinSide.TAIL : CoinFlipRequest.CoinSide.HEAD));
        MessageUtil.sendInfo(toPlayer, "§eUse §a/coinflip accept §eor §c/coinflip deny");
        toPlayer.playSound(toPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    /**
     * Accept coin flip request
     */
    public void acceptCoinFlipRequest(Player acceptingPlayer, CoinFlipRequest request) {
        Player challenger = Bukkit.getPlayer(request.getFromPlayer());
        
        // Check if request is still valid
        if (request.isExpired()) {
            MessageUtil.sendError(acceptingPlayer, "This request has expired!");
            removeCoinFlipRequest(acceptingPlayer.getUniqueId(), request);
            return;
        }

        // Check if challenger is still online
        if (challenger == null || !challenger.isOnline()) {
            MessageUtil.sendError(acceptingPlayer, "The challenger is no longer online!");
            removeCoinFlipRequest(acceptingPlayer.getUniqueId(), request);
            return;
        }

        // Check if both players have enough balance
        if (!hasBalance(challenger, request.getCurrencyType(), request.getAmount())) {
            MessageUtil.sendError(acceptingPlayer, "The challenger doesn't have enough balance!");
            MessageUtil.sendError(challenger, "You don't have enough balance for the coin flip!");
            removeCoinFlipRequest(acceptingPlayer.getUniqueId(), request);
            return;
        }

        if (!hasBalance(acceptingPlayer, request.getCurrencyType(), request.getAmount())) {
            MessageUtil.sendError(acceptingPlayer, "You don't have enough " + request.getCurrencyType().toLowerCase() + "!");
            return;
        }

        // Remove request
        removeCoinFlipRequest(acceptingPlayer.getUniqueId(), request);

        // Deduct amounts from both players
        deductBalance(challenger, request.getCurrencyType(), request.getAmount());
        deductBalance(acceptingPlayer, request.getCurrencyType(), request.getAmount());

        // Determine winner (flip coin)
        CoinFlipRequest.CoinSide result = random.nextBoolean() ? CoinFlipRequest.CoinSide.HEAD : CoinFlipRequest.CoinSide.TAIL;
        Player winner = result == request.getSide() ? challenger : acceptingPlayer;
        Player loser = winner == challenger ? acceptingPlayer : challenger;

        // Calculate win amount
        double winAmount = request.getAmount() * 2;

        // Notify both players with animation
        animateDoublePlayerFlip(challenger, acceptingPlayer, result, request.getSide(), () -> {
            // Award winner
            addBalance(winner, request.getCurrencyType(), winAmount);

            // Notify both
            MessageUtil.sendSuccess(winner, "§a§l✔ YOU WON THE COIN FLIP!");
            MessageUtil.sendInfo(winner, "§eYou won §a" + formatAmount(winAmount) + " " + request.getCurrencyType().toLowerCase() + "§e!");
            winner.playSound(winner.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            MessageUtil.sendError(loser, "§c§l✘ YOU LOST THE COIN FLIP!");
            MessageUtil.sendInfo(loser, "§eYou lost §c" + formatAmount(request.getAmount()) + " " + request.getCurrencyType().toLowerCase() + "§e!");
            loser.playSound(loser.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        });
    }

    /**
     * Deny coin flip request
     */
    public void denyCoinFlipRequest(Player denier, CoinFlipRequest request) {
        Player challenger = Bukkit.getPlayer(request.getFromPlayer());
        
        removeCoinFlipRequest(denier.getUniqueId(), request);
        
        MessageUtil.sendInfo(denier, "You denied the coin flip request.");
        
        if (challenger != null && challenger.isOnline()) {
            MessageUtil.sendError(challenger, denier.getName() + " denied your coin flip request.");
        }
    }

    /**
     * Get pending requests for a player
     */
    public List<CoinFlipRequest> getPendingRequests(UUID playerUUID) {
        List<CoinFlipRequest> requests = pendingRequests.getOrDefault(playerUUID, new ArrayList<>());
        // Remove expired requests
        requests.removeIf(CoinFlipRequest::isExpired);
        return new ArrayList<>(requests);
    }

    /**
     * Get last request for quick accept/deny
     */
    public CoinFlipRequest getLastRequest(UUID playerUUID) {
        CoinFlipRequest request = lastRequests.get(playerUUID);
        if (request != null && request.isExpired()) {
            lastRequests.remove(playerUUID);
            return null;
        }
        return request;
    }
    
    /**
     * Get request by number (1-indexed)
     */
    public CoinFlipRequest getRequestByNumber(UUID playerUUID, int number) {
        List<CoinFlipRequest> requests = getPendingRequests(playerUUID);
        if (number < 1 || number > requests.size()) {
            return null;
        }
        return requests.get(number - 1); // Convert to 0-indexed
    }

    /**
     * Remove coin flip request
     */
    private void removeCoinFlipRequest(UUID playerUUID, CoinFlipRequest request) {
        List<CoinFlipRequest> requests = pendingRequests.get(playerUUID);
        if (requests != null) {
            requests.remove(request);
            if (requests.isEmpty()) {
                pendingRequests.remove(playerUUID);
            }
        }
        
        CoinFlipRequest lastReq = lastRequests.get(playerUUID);
        if (lastReq != null && lastReq.getRequestId().equals(request.getRequestId())) {
            lastRequests.remove(playerUUID);
        }
    }

    /**
     * Perform jackpot spin with 3 rows (default)
     */
    public void performJackpotSpin(Player player, String currencyType, double betAmount) {
        performJackpotSpin(player, currencyType, betAmount, 3);
    }
    
    /**
     * Perform jackpot spin with specified number of rows (3, 4, or 5)
     */
    public void performJackpotSpin(Player player, String currencyType, double betAmount, int rows) {
        // Validate rows
        if (rows < 3 || rows > 5) {
            MessageUtil.sendError(player, "Row number must be 3, 4, or 5!");
            return;
        }
        
        // Check if player has enough balance
        if (!hasBalance(player, currencyType, betAmount)) {
            MessageUtil.sendError(player, "You don't have enough " + currencyType.toLowerCase() + "!");
            return;
        }

        // Deduct bet amount
        deductBalance(player, currencyType, betAmount);

        // Generate random results based on rows
        String[] symbols = {"🍒", "🍋", "🍊", "🍇", "💎", "⭐", "7️⃣"};
        String[] results = new String[rows];
        for (int i = 0; i < rows; i++) {
            results[i] = symbols[random.nextInt(symbols.length)];
        }

        // Check win condition
        double multiplier = calculateJackpotMultiplier(results, rows);
        
        // Animate jackpot
        animateJackpotSpin(player, results, () -> {
            if (multiplier > 0) {
                double winAmount = betAmount * multiplier;
                addBalance(player, currencyType, winAmount);
                MessageUtil.sendSuccess(player, "§6§l✦ JACKPOT WIN! ✦");
                MessageUtil.sendInfo(player, "§eRows: §a" + rows);
                MessageUtil.sendInfo(player, "§eMultiplier: §a" + multiplier + "x");
                MessageUtil.sendInfo(player, "§eYou won §a" + formatAmount(winAmount) + " " + currencyType.toLowerCase() + "§e!");
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } else {
                MessageUtil.sendError(player, "§cNo win this time!");
                MessageUtil.sendInfo(player, "§eTry again!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
            }
        });
    }

    /**
     * Calculate jackpot multiplier based on results and number of rows
     * Implements spec-defined multipliers for 3/4/5 row jackpots
     */
    private double calculateJackpotMultiplier(String[] results, int rows) {
        int matchCount = countMatches(results);
        
        // Check for special symbols
        String symbol = getMostCommonSymbol(results);
        boolean isSpecial = symbol.equals("7️⃣") || symbol.equals("💎") || symbol.equals("⭐");
        
        // Calculate multiplier based on rows and matches
        if (rows == 3) {
            // 3 Row: 2 match = 0.8x, 3 match = 2x
            if (matchCount == 3) {
                if (isSpecial) {
                    return switch (symbol) {
                        case "7️⃣" -> 10.0;
                        case "💎" -> 7.0;
                        case "⭐" -> 5.0;
                        default -> 2.0;
                    };
                }
                return 2.0;
            } else if (matchCount == 2) {
                return 0.8;
            }
        } else if (rows == 4) {
            // 4 Row: 2 match = 0.5x, 3 match = 1x, 4 match = 2x
            if (matchCount == 4) {
                if (isSpecial) {
                    return switch (symbol) {
                        case "7️⃣" -> 15.0;
                        case "💎" -> 10.0;
                        case "⭐" -> 7.0;
                        default -> 2.0;
                    };
                }
                return 2.0;
            } else if (matchCount == 3) {
                return 1.0;
            } else if (matchCount == 2) {
                return 0.5;
            }
        } else if (rows == 5) {
            // 5 Row: 2 match = 0.4x, 3 match = 0.8x, 4 match = 1.5x, 5 match = 3x
            if (matchCount == 5) {
                if (isSpecial) {
                    return switch (symbol) {
                        case "7️⃣" -> 20.0;
                        case "💎" -> 15.0;
                        case "⭐" -> 10.0;
                        default -> 3.0;
                    };
                }
                return 3.0;
            } else if (matchCount == 4) {
                return 1.5;
            } else if (matchCount == 3) {
                return 0.8;
            } else if (matchCount == 2) {
                return 0.4;
            }
        }
        
        return 0; // No win
    }
    
    /**
     * Count how many symbols match in the results
     */
    private int countMatches(String[] results) {
        Map<String, Integer> counts = new HashMap<>();
        for (String symbol : results) {
            counts.put(symbol, counts.getOrDefault(symbol, 0) + 1);
        }
        return counts.values().stream().max(Integer::compareTo).orElse(0);
    }
    
    /**
     * Get the most common symbol in results
     */
    private String getMostCommonSymbol(String[] results) {
        Map<String, Integer> counts = new HashMap<>();
        for (String symbol : results) {
            counts.put(symbol, counts.getOrDefault(symbol, 0) + 1);
        }
        return counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
    }

    /**
     * Animate coin flip (single player)
     */
    private void animateCoinFlip(Player player, CoinFlipRequest.CoinSide result, java.util.function.Consumer<Boolean> callback, 
                                  CoinFlipRequest.CoinSide playerChoice, CoinFlipRequest.CoinSide actualResult) {
        MessageUtil.sendInfo(player, "§e§lFlipping coin...");
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.sendInfo(player, "§6⬤ Flipping... ⬤");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        }, 10L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.sendInfo(player, "§6⬤⬤ Flipping... ⬤⬤");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
        }, 20L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String resultStr = actualResult == CoinFlipRequest.CoinSide.HEAD ? "§e§lHEAD" : "§e§lTAIL";
            MessageUtil.sendInfo(player, "§6§lResult: " + resultStr);
            boolean won = playerChoice == actualResult;
            callback.accept(won);
        }, 30L);
    }

    /**
     * Animate double player coin flip
     */
    private void animateDoublePlayerFlip(Player player1, Player player2, CoinFlipRequest.CoinSide result, 
                                         CoinFlipRequest.CoinSide player1Side, Runnable callback) {
        for (Player p : Arrays.asList(player1, player2)) {
            MessageUtil.sendInfo(p, "§e§lFlipping coin...");
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : Arrays.asList(player1, player2)) {
                MessageUtil.sendInfo(p, "§6⬤ Flipping... ⬤");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            }
        }, 10L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : Arrays.asList(player1, player2)) {
                MessageUtil.sendInfo(p, "§6⬤⬤ Flipping... ⬤⬤");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            }
        }, 20L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String resultStr = result == CoinFlipRequest.CoinSide.HEAD ? "§e§lHEAD" : "§e§lTAIL";
            for (Player p : Arrays.asList(player1, player2)) {
                MessageUtil.sendInfo(p, "§6§lResult: " + resultStr);
            }
            callback.run();
        }, 30L);
    }

    /**
     * Animate jackpot spin
     */
    private void animateJackpotSpin(Player player, String[] results, Runnable callback) {
        MessageUtil.sendInfo(player, "§6§l✦ SPINNING JACKPOT... ✦");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.5f);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.sendInfo(player, "§e[" + results[0] + "] [?] [?]");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.7f);
        }, 10L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.sendInfo(player, "§e[" + results[0] + "] [" + results[1] + "] [?]");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.9f);
        }, 20L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtil.sendInfo(player, "§6§l[" + results[0] + "] [" + results[1] + "] [" + results[2] + "]");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
            callback.run();
        }, 30L);
    }

    /**
     * Start task to remove expired requests
     */
    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            pendingRequests.values().forEach(requests -> requests.removeIf(CoinFlipRequest::isExpired));
            pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
            lastRequests.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 1200L, 1200L); // Every minute
    }

    // Helper methods for economy integration
    private boolean hasBalance(Player player, String currencyType, double amount) {
        online.demonzdevelopment.dzeconomy.currency.CurrencyType currency = 
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.fromString(currencyType);
        if (currency == null) return false;
        return plugin.getEconomyAPI().hasBalance(player.getUniqueId(), currency, amount);
    }

    private void deductBalance(Player player, String currencyType, double amount) {
        online.demonzdevelopment.dzeconomy.currency.CurrencyType currency = 
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.fromString(currencyType);
        if (currency != null) {
            plugin.getEconomyAPI().removeCurrency(player.getUniqueId(), currency, amount);
        }
    }

    private void addBalance(Player player, String currencyType, double amount) {
        online.demonzdevelopment.dzeconomy.currency.CurrencyType currency = 
            online.demonzdevelopment.dzeconomy.currency.CurrencyType.fromString(currencyType);
        if (currency != null) {
            plugin.getEconomyAPI().addCurrency(player.getUniqueId(), currency, amount);
        }
    }

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
