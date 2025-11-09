package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.RankData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    private final DZTradeHub plugin;
    private final Map<String, RankData> ranks;

    public PermissionManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.ranks = new HashMap<>();
        loadRanks();
    }

    public void loadRanks() {
        ranks.clear();
        ranks.putAll(plugin.getConfigManager().loadRanks());
        plugin.getLogger().info("Loaded " + ranks.size() + " ranks");
    }

    public RankData getPlayerRank(Player player) {
        // Check from highest priority to lowest
        RankData highestRank = ranks.get("default");
        int highestPriority = -1;

        for (RankData rank : ranks.values()) {
            if (player.hasPermission(rank.permission()) && rank.queuePriority() > highestPriority) {
                highestRank = rank;
                highestPriority = rank.queuePriority();
            }
        }

        return highestRank != null ? highestRank : getDefaultRank();
    }

    public RankData getDefaultRank() {
        return ranks.getOrDefault("default", 
            new RankData(
                "dztradehub.rank.default",
                "§7Member",
                true, true, 27, 0,
                Map.of("Bazar", java.util.List.of("ALL")),
                new RankData.AuctionSettings(true, 3, 10.0)
            )
        );
    }

    public boolean canAccessArea(Player player, String areaName) {
        RankData rank = getPlayerRank(player);
        return rank.hasAccessToArea(areaName);
    }

    public boolean canAccessShop(Player player, String areaName, String shopName) {
        RankData rank = getPlayerRank(player);
        return rank.hasAccessToShop(areaName, shopName);
    }

    public boolean canBuyItems(Player player) {
        RankData rank = getPlayerRank(player);
        return rank.buyItems();
    }

    public boolean canSellItems(Player player) {
        RankData rank = getPlayerRank(player);
        return rank.sellItems();
    }

    public int getMaxCartSize(Player player) {
        RankData rank = getPlayerRank(player);
        return rank.maxCartSize();
    }

    public int getQueuePriority(Player player) {
        RankData rank = getPlayerRank(player);
        return rank.queuePriority();
    }
}