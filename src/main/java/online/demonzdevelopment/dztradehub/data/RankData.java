package online.demonzdevelopment.dztradehub.data;

import java.util.*;

public record RankData(
    String permission,
    String displayName,
    boolean buyItems,
    boolean sellItems,
    int maxCartSize,
    int queuePriority,
    Map<String, List<String>> accessibleAreas,
    AuctionSettings auctionSettings
) {
    public record AuctionSettings(
        boolean canList,
        int maxListings,
        double listingFee
    ) {}

    public boolean hasAccessToArea(String areaName) {
        return accessibleAreas.containsKey("ALL") || accessibleAreas.containsKey(areaName);
    }

    public boolean hasAccessToShop(String areaName, String shopName) {
        if (accessibleAreas.containsKey("ALL")) {
            return true;
        }
        
        List<String> shops = accessibleAreas.get(areaName);
        if (shops == null) {
            return false;
        }
        
        return shops.contains("ALL") || shops.contains(shopName);
    }
}