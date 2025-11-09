package online.demonzdevelopment.dztradehub.gui.bank;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankDepositGUI {
    private final DZTradeHub plugin;

    public static class DepositHolder implements InventoryHolder {
        private final Bank bank;
        private final BankAccount account;
        private final Map<CurrencyType, Double> amounts = new HashMap<>();

        public DepositHolder(Bank bank, BankAccount account) {
            this.bank = bank;
            this.account = account;
        }

        public Bank getBank() { return bank; }
        public BankAccount getAccount() { return account; }
        public Map<CurrencyType, Double> getAmounts() { return amounts; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankDepositGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Bank bank, BankAccount account) {
        Inventory inv = Bukkit.createInventory(
            new DepositHolder(bank, account),
            27,
            Component.text("§a§lDeposit - " + bank.getDisplayName())
        );

        // Currency selection
        int slot = 10;
        for (CurrencyType currency : CurrencyType.values()) {
            if (bank.isCurrencyEnabled(currency)) {
                ItemStack item = createCurrencyOption(player, account, currency, bank);
                inv.setItem(slot++, item);
            }
        }

        // Confirm button
        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("§a§lConfirm Deposit"));
        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.text(""));
        confirmLore.add(Component.text("§7Click currencies above to set amounts"));
        confirmLore.add(Component.text("§7Then click here to deposit"));
        confirmMeta.lore(confirmLore);
        confirm.setItemMeta(confirmMeta);
        inv.setItem(22, confirm);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("§7Back"));
        back.setItemMeta(backMeta);
        inv.setItem(18, back);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cCancel"));
        close.setItemMeta(closeMeta);
        inv.setItem(26, close);

        player.openInventory(inv);
    }

    private ItemStack createCurrencyOption(Player player, BankAccount account, 
                                          CurrencyType currency, Bank bank) {
        Material material = switch (currency) {
            case MONEY -> Material.GOLD_INGOT;
            case MOBCOIN -> Material.EMERALD;
            case GEM -> Material.DIAMOND;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l" + currency.name()));

        List<Component> lore = new ArrayList<>();
        double pocket = plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency);
        double bankBalance = account.getBalance(currency);
        double maxStorage = bank.getMaxStorage(currency);
        
        lore.add(Component.text(""));
        lore.add(Component.text("§7Pocket: §f" + String.format("%.2f", pocket)));
        lore.add(Component.text("§7Bank: §f" + String.format("%.2f", bankBalance)));
        lore.add(Component.text("§7Max Storage: §e" + String.format("%.0f", maxStorage)));
        lore.add(Component.text(""));
        
        double tax = calculateDepositTax(bank, account);
        lore.add(Component.text("§7Tax: §e" + String.format("%.2f", tax) + "%"));
        lore.add(Component.text(""));
        lore.add(Component.text("§eLeft-click: Enter amount in chat"));
        lore.add(Component.text("§eRight-click: Deposit all"));
        lore.add(Component.text("§eShift+Right-click: Deposit half"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private double calculateDepositTax(Bank bank, BankAccount account) {
        double baseTax = bank.getGlobalDepositTax();
        double multiplier = account.getAccountType().getDepositTaxMultiplier();
        
        var levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            double reduction = levelConfig.getDepositTaxReduction();
            baseTax = baseTax * (1 - reduction / 100.0);
        }
        
        return baseTax * multiplier;
    }
}