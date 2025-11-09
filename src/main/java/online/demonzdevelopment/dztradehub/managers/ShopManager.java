package online.demonzdevelopment.dztradehub.managers;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Area;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {
    private final DZTradeHub plugin;
    private final Map<String, Area> areas;
    private final Map<String, Map<String, Shop>> shops; // areaName -> shopName -> Shop

    public ShopManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.areas = new ConcurrentHashMap<>();
        this.shops = new ConcurrentHashMap<>();
    }

    public void registerArea(Area area) {
        areas.put(area.getName(), area);
        shops.putIfAbsent(area.getName(), new ConcurrentHashMap<>());
        plugin.getLogger().info("Registered area: " + area.getName());
    }

    public void registerShop(String areaName, Shop shop) {
        shops.computeIfAbsent(areaName, k -> new ConcurrentHashMap<>())
              .put(shop.getName(), shop);
        
        Area area = areas.get(areaName);
        if (area != null) {
            shop.setArea(area); // Set area reference
            area.addShop(shop);
        }
        
        plugin.getLogger().info("Registered shop: " + shop.getName() + " in area: " + areaName);
    }

    public Area getArea(String areaName) {
        return areas.get(areaName);
    }

    public Shop getShop(String areaName, String shopName) {
        Map<String, Shop> areaShops = shops.get(areaName);
        return areaShops != null ? areaShops.get(shopName) : null;
    }

    public List<Area> getAllAreas() {
        return new ArrayList<>(areas.values());
    }

    public List<Shop> getShopsInArea(String areaName) {
        Map<String, Shop> areaShops = shops.get(areaName);
        return areaShops != null ? new ArrayList<>(areaShops.values()) : new ArrayList<>();
    }

    public boolean areaExists(String areaName) {
        return areas.containsKey(areaName);
    }

    public boolean shopExists(String areaName, String shopName) {
        return getShop(areaName, shopName) != null;
    }

    public void addItemToShop(String areaName, String shopName, ShopItem item) {
        Shop shop = getShop(areaName, shopName);
        if (shop != null) {
            shop.addItem(item);
            plugin.getDatabaseManager().saveShopItemAsync(item, shop.getId());
        }
    }

    public void removeItemFromShop(String areaName, String shopName, ShopItem item) {
        Shop shop = getShop(areaName, shopName);
        if (shop != null) {
            shop.removeItem(item);
        }
    }

    public void deleteArea(String areaName) {
        shops.remove(areaName);
        areas.remove(areaName);
        plugin.getLogger().info("Deleted area: " + areaName);
    }

    public void deleteShop(String areaName, String shopName) {
        Map<String, Shop> areaShops = shops.get(areaName);
        if (areaShops != null) {
            Shop removed = areaShops.remove(shopName);
            if (removed != null) {
                Area area = areas.get(areaName);
                if (area != null) {
                    area.removeShop(removed);
                }
                plugin.getLogger().info("Deleted shop: " + shopName + " from area: " + areaName);
            }
        }
    }

    public void reloadAllShops() {
        areas.clear();
        shops.clear();
        // Load from file storage
        // This will be called when reload command is used
        plugin.getLogger().info("Reloaded all shops");
    }
}