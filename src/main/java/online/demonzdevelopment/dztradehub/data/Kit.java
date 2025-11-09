package online.demonzdevelopment.dztradehub.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    private final String name;
    private String displayName;
    private List<String> description;
    private Material iconMaterial;
    private double price;
    private long cooldown; // in seconds
    private String permission;
    private List<ItemStack> items;
    private List<String> commands;

    public Kit(String name, String displayName, Material iconMaterial, double price, long cooldown, String permission) {
        this.name = name;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.price = price;
        this.cooldown = cooldown;
        this.permission = permission;
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.description = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public void setDescription(List<String> description) { this.description = new ArrayList<>(description); }
    public Material getIconMaterial() { return iconMaterial; }
    public void setIconMaterial(Material iconMaterial) { this.iconMaterial = iconMaterial; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public long getCooldown() { return cooldown; }
    public void setCooldown(long cooldown) { this.cooldown = cooldown; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public List<ItemStack> getItems() { return new ArrayList<>(items); }
    public void addItem(ItemStack item) { items.add(item); }
    public void setItems(List<ItemStack> items) { this.items = new ArrayList<>(items); }
    public List<String> getCommands() { return new ArrayList<>(commands); }
    public void addCommand(String command) { commands.add(command); }
    public void setCommands(List<String> commands) { this.commands = new ArrayList<>(commands); }
}