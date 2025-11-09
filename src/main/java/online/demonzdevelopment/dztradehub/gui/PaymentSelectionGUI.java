package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PaymentSelectionGUI {
    private final DZTradeHub plugin;

    public static class PaymentSelectionHolder implements InventoryHolder {
        private final Shop shop;
        private final List<ItemStack> items;
        private final double totalPrice;

        public PaymentSelectionHolder(Shop shop, List<ItemStack> items, double totalPrice) {
            this.shop = shop;
            this.items = items;
            this.totalPrice = totalPrice;
        }

        public Shop getShop() { return shop; }
        public List<ItemStack> getItems() { return items; }
        public double getTotalPrice() { return totalPrice; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public PaymentSelectionGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Shop shop, List<ItemStack> items, double totalPrice) {
        Inventory inv = Bukkit.createInventory(
            new PaymentSelectionHolder(shop, items, totalPrice),
            27,
            Component.text("§6§lSelect Payment Method")
        );

        // Payment summary (slot 4)
        ItemStack summary = new ItemStack(Material.PAPER);
        ItemMeta summaryMeta = summary.getItemMeta();
        summaryMeta.displayName(Component.text("§e§lPurchase Summary"));
        List<Component> summaryLore = new ArrayList<>();
        summaryLore.add(Component.text(""));
        summaryLore.add(Component.text("§7Items: §f" + items.size()));
        summaryLore.add(Component.text("§7Total: §a" + 
            String.format("%.2f", totalPrice) + " " + shop.getCurrencyType().name()));
        summaryLore.add(Component.text(""));
        summaryMeta.lore(summaryLore);
        summary.setItemMeta(summaryMeta);
        inv.setItem(4, summary);

        // Pay by pocket (slot 11)
        ItemStack pocket = new ItemStack(Material.GOLD_INGOT);
        ItemMeta pocketMeta = pocket.getItemMeta();
        pocketMeta.displayName(Component.text("§6§lPay from Pocket"));
        List<Component> pocketLore = new ArrayList<>();
        pocketLore.add(Component.text(""));
        pocketLore.add(Component.text("§7Pay directly from your"));
        pocketLore.add(Component.text("§7current balance"));
        pocketLore.add(Component.text(""));
        double pocketBalance = plugin.getEconomyAPI().getBalance(
            player.getUniqueId(), shop.getCurrencyType()
        );
        pocketLore.add(Component.text("§7Available: §f" + String.format("%.2f", pocketBalance)));
        pocketLore.add(Component.text("§7Required: §e" + String.format("%.2f", totalPrice)));
        
        if (pocketBalance >= totalPrice) {
            pocketLore.add(Component.text(""));
            pocketLore.add(Component.text("§a✓ Sufficient balance"));
            pocketLore.add(Component.text(""));
            pocketLore.add(Component.text("§eClick to pay"));
        } else {
            pocketLore.add(Component.text(""));
            pocketLore.add(Component.text("§c✗ Insufficient balance"));
        }
        pocketMeta.lore(pocketLore);
        pocket.setItemMeta(pocketMeta);
        inv.setItem(11, pocket);

        // Pay by bank (slot 15)
        ItemStack bank = new ItemStack(Material.ENDER_CHEST);
        ItemMeta bankMeta = bank.getItemMeta();
        bankMeta.displayName(Component.text("§d§lPay from Bank"));
        List<Component> bankLore = new ArrayList<>();
        bankLore.add(Component.text(""));
        bankLore.add(Component.text("§7Pay from a bank account"));
        bankLore.add(Component.text(""));
        bankLore.add(Component.text("§7Choose:"));
        bankLore.add(Component.text("§7▸ Your bank account"));
        bankLore.add(Component.text("§7▸ Another player's account"));
        bankLore.add(Component.text(""));
        bankLore.add(Component.text("§eClick to proceed"));
        bankMeta.lore(bankLore);
        bank.setItemMeta(bankMeta);
        inv.setItem(15, bank);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("§cCancel"));
        cancel.setItemMeta(cancelMeta);
        inv.setItem(22, cancel);

        player.openInventory(inv);
    }
}