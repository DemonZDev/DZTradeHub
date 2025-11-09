package online.demonzdevelopment.dztradehub.data;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Area {
    private final UUID id;
    private final String name;
    private String displayName;
    private AreaType type;
    private Location location;
    private final List<Shop> shops;
    private List<String> description;

    public enum AreaType {
        BAZAR, SUPERMARKET, JUNKYARD, PAWNAREA, BLACKMARKET, KITS, AUCTIONHOUSE
    }

    public Area(String name, String displayName, AreaType type, Location location) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.location = location;
        this.shops = new ArrayList<>();
        this.description = new ArrayList<>();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public AreaType getType() { return type; }
    public void setType(AreaType type) { this.type = type; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public List<Shop> getShops() { return new ArrayList<>(shops); }
    public void addShop(Shop shop) { shops.add(shop); }
    public void removeShop(Shop shop) { shops.remove(shop); }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public void setDescription(List<String> description) { this.description = new ArrayList<>(description); }
}