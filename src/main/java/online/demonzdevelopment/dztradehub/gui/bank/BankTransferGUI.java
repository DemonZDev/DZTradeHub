package online.demonzdevelopment.dztradehub.gui.bank;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
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

public class BankTransferGUI {
    private final DZTradeHub plugin;

    public static class TransferHolder implements InventoryHolder {
        private final Bank bank;
        private final BankAccount account;
        private String targetType; // "account" or "bank"
        private String ownerType; // "own" or "other"

        public TransferHolder(Bank bank, BankAccount account) {
            this.bank = bank;
            this.account = account;
        }

        public Bank getBank() { return bank; }
        public BankAccount getAccount() { return account; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public String getOwnerType() { return ownerType; }
        public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankTransferGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    /**
     * Open main transfer type selection
     */
    public void open(Player player, Bank bank, BankAccount account) {
        Inventory inv = Bukkit.createInventory(
            new TransferHolder(bank, account),
            27,
            Component.text("§d§lTransfer - Choose Type")
        );

        // Account transfer option
        if (bank.isAccountTransferEnabled()) {
            ItemStack accountTransfer = new ItemStack(Material.CHEST);
            ItemMeta meta = accountTransfer.getItemMeta();
            meta.displayName(Component.text("§e§lTransfer to Account"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§7Transfer to another account"));
            lore.add(Component.text("§7in this bank (" + bank.getDisplayName() + ")"));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to proceed"));
            meta.lore(lore);
            accountTransfer.setItemMeta(meta);
            inv.setItem(11, accountTransfer);
        }

        // Bank transfer option
        if (bank.isBankTransferEnabled()) {
            ItemStack bankTransfer = new ItemStack(Material.ENDER_CHEST);
            ItemMeta meta = bankTransfer.getItemMeta();
            meta.displayName(Component.text("§d§lTransfer to Other Bank"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§7Transfer to an account"));
            lore.add(Component.text("§7in a different bank"));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to proceed"));
            meta.lore(lore);
            bankTransfer.setItemMeta(meta);
            inv.setItem(15, bankTransfer);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(26, close);

        player.openInventory(inv);
    }

    /**
     * Open owner type selection (own or other player)
     */
    public void openOwnerSelection(Player player, Bank bank, BankAccount account, String targetType) {
        Inventory inv = Bukkit.createInventory(
            new TransferHolder(bank, account),
            27,
            Component.text("§d§lTransfer - Choose Owner")
        );

        // Own account
        ItemStack ownAccount = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta ownMeta = ownAccount.getItemMeta();
        ownMeta.displayName(Component.text("§a§lYour Account"));
        List<Component> ownLore = new ArrayList<>();
        ownLore.add(Component.text(""));
        ownLore.add(Component.text("§7Transfer to your own account"));
        ownLore.add(Component.text(""));
        ownLore.add(Component.text("§eClick to see your accounts"));
        ownMeta.lore(ownLore);
        ownAccount.setItemMeta(ownMeta);
        inv.setItem(11, ownAccount);

        // Other player's account
        ItemStack otherAccount = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta otherMeta = otherAccount.getItemMeta();
        otherMeta.displayName(Component.text("§b§lOther Player's Account"));
        List<Component> otherLore = new ArrayList<>();
        otherLore.add(Component.text(""));
        otherLore.add(Component.text("§7Transfer to another player's account"));
        otherLore.add(Component.text(""));
        otherLore.add(Component.text("§eClick to enter player name"));
        otherMeta.lore(otherLore);
        otherAccount.setItemMeta(otherMeta);
        inv.setItem(15, otherAccount);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(26, close);

        // Store targetType in holder
        TransferHolder holder = (TransferHolder) inv.getHolder();
        if (holder != null) {
            holder.setTargetType(targetType);
        }

        player.openInventory(inv);
    }
}