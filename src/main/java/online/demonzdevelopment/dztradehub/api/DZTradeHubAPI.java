package online.demonzdevelopment.dztradehub.api;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;

import java.util.List;

/**
 * Public API for other plugins to interact with DZTradeHub
 */
public class DZTradeHubAPI {
    private final DZTradeHub plugin;

    public DZTradeHubAPI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Get all registered areas
     */
    public List<Area> getAreas() {
        return plugin.getShopManager().getAllAreas();
    }

    /**
     * Get an area by name
     */
    public Area getArea(String areaName) {
        return plugin.getShopManager().getArea(areaName);
    }

    /**
     * Get a shop by area and shop name
     */
    public Shop getShop(String areaName, String shopName) {
        return plugin.getShopManager().getShop(areaName, shopName);
    }

    /**
     * Get all shops in an area
     */
    public List<Shop> getShopsInArea(String areaName) {
        return plugin.getShopManager().getShopsInArea(areaName);
    }

    /**
     * Add an item to a shop
     */
    public void addItemToShop(String areaName, String shopName, ShopItem item) {
        plugin.getShopManager().addItemToShop(areaName, shopName, item);
    }

    /**
     * Check if an area exists
     */
    public boolean areaExists(String areaName) {
        return plugin.getShopManager().areaExists(areaName);
    }

    /**
     * Check if a shop exists
     */
    public boolean shopExists(String areaName, String shopName) {
        return plugin.getShopManager().shopExists(areaName, shopName);
    }
}