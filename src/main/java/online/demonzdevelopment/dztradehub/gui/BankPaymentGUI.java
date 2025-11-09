package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Shop;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.data.bank.BankAccount;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BankPaymentGUI {
    private final DZTradeHub plugin;

    public static class BankPaymentHolder implements InventoryHolder {
        private final Shop shop;
        private final List<ItemStack> items;
        private final double totalPrice;
        private String ownerType; // "own" or "other"

        public BankPaymentHolder(Shop shop, List<ItemStack> items, double totalPrice) {
            this.shop = shop;
            this.items = items;
            this.totalPrice = totalPrice;
        }

        public Shop getShop() { return shop; }
        public List<ItemStack> getItems() { return items; }
        public double getTotalPrice() { return totalPrice; }
        public String getOwnerType() { return ownerType; }
        public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankPaymentGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Open owner selection (your bank or other player's bank)
     */
    public void openOwnerSelection(Player player, Shop shop, List<ItemStack> items, double totalPrice) {
        Inventory inv = Bukkit.createInventory(
            new BankPaymentHolder(shop, items, totalPrice),
            27,
            Component.text("§d§lBank Payment - Choose Owner")
        );

        // Your bank (slot 11)
        ItemStack yourBank = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta yourMeta = yourBank.getItemMeta();
        yourMeta.displayName(Component.text("§a§lYour Bank Account"));
        List<Component> yourLore = new ArrayList<>();
        yourLore.add(Component.text(""));
        yourLore.add(Component.text("§7Pay from your own"));
        yourLore.add(Component.text("§7bank account"));
        yourLore.add(Component.text(""));
        
        // Count player's bank accounts
        int accountCount = plugin.getBankAccountManager().getPlayerAccountCount(player.getUniqueId());
        yourLore.add(Component.text("§7Your accounts: §f" + accountCount));
        
        if (accountCount > 0) {
            yourLore.add(Component.text(""));
            yourLore.add(Component.text("§eClick to select account"));
        } else {
            yourLore.add(Component.text(""));
            yourLore.add(Component.text("§cNo bank accounts"));
        }
        yourMeta.lore(yourLore);
        yourBank.setItemMeta(yourMeta);
        inv.setItem(11, yourBank);

        // Other player's bank (slot 15)
        ItemStack otherBank = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta otherMeta = otherBank.getItemMeta();
        otherMeta.displayName(Component.text("§b§lOther Player's Account"));
        List<Component> otherLore = new ArrayList<>();
        otherLore.add(Component.text(""));
        otherLore.add(Component.text("§7Pay from another player's"));
        otherLore.add(Component.text("§7bank account"));
        otherLore.add(Component.text(""));
        otherLore.add(Component.text("§7Requires their password"));
        otherLore.add(Component.text(""));
        otherLore.add(Component.text("§eClick to enter player name"));
        otherMeta.lore(otherLore);
        otherBank.setItemMeta(otherMeta);
        inv.setItem(15, otherBank);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("§cCancel"));
        cancel.setItemMeta(cancelMeta);
        inv.setItem(26, cancel);

        player.openInventory(inv);
    }

    /**
     * Open bank account selection for a player
     */
    public void openAccountSelection(Player player, Player accountOwner, Shop shop, 
                                     List<ItemStack> items, double totalPrice) {
        Inventory inv = Bukkit.createInventory(
            new BankPaymentHolder(shop, items, totalPrice),
            27,
            Component.text("§d§lSelect Bank Account")
        );

        // Get all banks where player has accounts
        List<Bank> banksWithAccounts = new ArrayList<>();
        for (Bank bank : plugin.getBankManager().getAllBanks()) {
            BankAccount account = plugin.getBankAccountManager().getPlayerAccountAtBank(
                accountOwner.getUniqueId(), bank.getBankId()
            );
            if (account != null) {
                banksWithAccounts.add(bank);
            }
        }

        if (banksWithAccounts.isEmpty()) {
            player.closeInventory();
            player.sendMessage("§c" + accountOwner.getName() + " has no bank accounts!");
            return;
        }

        // Display bank accounts
        int slot = 10;
        for (Bank bank : banksWithAccounts) {
            if (slot > 16) break;

            BankAccount account = plugin.getBankAccountManager().getPlayerAccountAtBank(
                accountOwner.getUniqueId(), bank.getBankId()
            );

            Material material = Material.GOLD_BLOCK;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§e§l" + bank.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§7Owner: §f" + accountOwner.getName()));
            lore.add(Component.text("§7Type: §f" + account.getAccountType().getDisplayName()));
            lore.add(Component.text(""));
            
            double balance = account.getBalance(shop.getCurrencyType());
            lore.add(Component.text("§7Balance: §f" + String.format("%.2f", balance)));
            lore.add(Component.text("§7Required: §e" + String.format("%.2f", totalPrice)));
            
            if (balance >= totalPrice) {
                lore.add(Component.text(""));
                lore.add(Component.text("§a✓ Sufficient balance"));
                lore.add(Component.text(""));
                lore.add(Component.text("§eClick to proceed"));
            } else {
                lore.add(Component.text(""));
                lore.add(Component.text("§c✗ Insufficient balance"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("§cCancel"));
        cancel.setItemMeta(cancelMeta);
        inv.setItem(26, cancel);

        player.openInventory(inv);
    }
}