package online.demonzdevelopment.dztradehub.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Shop {
    private final UUID id;
    private final String name;
    private String displayName;
    private ShopType shopType;
    private QueueType queueType;
    private final List<ShopItem> items;
    private Area area; // Reference to the area this shop belongs to
    
    // Reception settings
    private boolean receptionEnabled;
    private int receptionNumber;
    private int receptionTimeKick;
    private int receptionAfkKick;
    
    // Checkout settings
    private boolean checkoutEnabled;
    private int checkoutNumber;
    private int checkoutTimeKick;
    
    // Stock settings
    private String linkedShopName;
    private String restockInterval;
    private int restockAmount;

    public enum ShopType {
        BUY_ONLY, SELL_ONLY, BUY_SELL
    }

    public enum QueueType {
        RECEPTION, CASH_COUNTER, NONE
    }

    public Shop(String name, String displayName, ShopType shopType) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.displayName = displayName;
        this.shopType = shopType;
        this.items = new ArrayList<>();
        this.queueType = QueueType.NONE;
        
        // Default reception settings
        this.receptionEnabled = false;
        this.receptionNumber = 1;
        this.receptionTimeKick = 300; // 5 minutes
        this.receptionAfkKick = 60; // 1 minute
        
        // Default checkout settings
        this.checkoutEnabled = false;
        this.checkoutNumber = 3;
        this.checkoutTimeKick = 5; // 5 seconds per item
        
        // Default stock settings
        this.linkedShopName = null;
        this.restockInterval = "DAILY";
        this.restockAmount = 64;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public ShopType getShopType() { return shopType; }
    public void setShopType(ShopType shopType) { this.shopType = shopType; }
    public QueueType getQueueType() { return queueType; }
    public void setQueueType(QueueType queueType) { this.queueType = queueType; }
    public List<ShopItem> getItems() { return new ArrayList<>(items); }
    public void addItem(ShopItem item) { items.add(item); }
    public void removeItem(ShopItem item) { items.remove(item); }
    
    // Reception getters/setters
    public boolean isReceptionEnabled() { return receptionEnabled; }
    public void setReceptionEnabled(boolean enabled) { this.receptionEnabled = enabled; }
    public int getReceptionNumber() { return receptionNumber; }
    public void setReceptionNumber(int number) { this.receptionNumber = number; }
    public int getReceptionTimeKick() { return receptionTimeKick; }
    public void setReceptionTimeKick(int time) { this.receptionTimeKick = time; }
    public int getReceptionAfkKick() { return receptionAfkKick; }
    public void setReceptionAfkKick(int time) { this.receptionAfkKick = time; }
    
    // Checkout getters/setters
    public boolean isCheckoutEnabled() { return checkoutEnabled; }
    public void setCheckoutEnabled(boolean enabled) { this.checkoutEnabled = enabled; }
    public int getCheckoutNumber() { return checkoutNumber; }
    public void setCheckoutNumber(int number) { this.checkoutNumber = number; }
    public int getCheckoutTimeKick() { return checkoutTimeKick; }
    public void setCheckoutTimeKick(int time) { this.checkoutTimeKick = time; }
    
    // Stock getters/setters
    public String getLinkedShopName() { return linkedShopName; }
    public void setLinkedShopName(String shopName) { this.linkedShopName = shopName; }
    public String getRestockInterval() { return restockInterval; }
    public void setRestockInterval(String interval) { this.restockInterval = interval; }
    public int getRestockAmount() { return restockAmount; }
    public void setRestockAmount(int amount) { this.restockAmount = amount; }
    
    // Area getter/setter
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    
    // Get the primary currency type used in this shop (from first item or MONEY as default)
    public online.demonzdevelopment.dzeconomy.currency.CurrencyType getCurrencyType() {
        if (!items.isEmpty()) {
            ShopItem firstItem = items.get(0);
            return convertToCurrencyType(firstItem.getCurrency());
        }
        return online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY;
    }
    
    // Convert ShopItem.Currency to CurrencyType
    private online.demonzdevelopment.dzeconomy.currency.CurrencyType convertToCurrencyType(ShopItem.Currency currency) {
        return switch (currency) {
            case MONEY -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.MONEY;
            case MOBCOIN -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.MOBCOIN;
            case GEM -> online.demonzdevelopment.dzeconomy.currency.CurrencyType.GEM;
        };
    }
}