package online.demonzdevelopment.dztradehub.gui.bank;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.AccountType;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BankConfigGUI {
    private final DZTradeHub plugin;

    public static class BankConfigHolder implements InventoryHolder {
        private final Bank bank;

        public BankConfigHolder(Bank bank) {
            this.bank = bank;
        }

        public Bank getBank() { return bank; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankConfigGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Bank bank) {
        Inventory inv = Bukkit.createInventory(
            new BankConfigHolder(bank),
            54,
            Component.text("§6§lBank Config - " + bank.getDisplayName())
        );

        // Bank info (slot 4)
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("§e§l" + bank.getDisplayName()));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text(""));
        infoLore.add(Component.text("§7Bank Name: §f" + bank.getBankName()));
        infoLore.add(Component.text("§7Max Level: §f" + bank.getMaxLevel()));
        infoLore.add(Component.text(""));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Enabled currencies (slots 10-12)
        ItemStack currencies = new ItemStack(Material.GOLD_INGOT);
        ItemMeta currMeta = currencies.getItemMeta();
        currMeta.displayName(Component.text("§e§lEnabled Currencies"));
        List<Component> currLore = new ArrayList<>();
        currLore.add(Component.text(""));
        for (var currency : bank.getEnabledCurrencies()) {
            currLore.add(Component.text("§a✓ §f" + currency.name()));
        }
        currLore.add(Component.text(""));
        currLore.add(Component.text("§7Edit in config file"));
        currMeta.lore(currLore);
        currencies.setItemMeta(currMeta);
        inv.setItem(10, currencies);

        // Currency conversion toggle
        ItemStack conversion = new ItemStack(
            bank.isCurrencyConversionEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL
        );
        ItemMeta convMeta = conversion.getItemMeta();
        convMeta.displayName(Component.text("§b§lCurrency Conversion"));
        List<Component> convLore = new ArrayList<>();
        convLore.add(Component.text(""));
        convLore.add(Component.text(bank.isCurrencyConversionEnabled() ? 
            "§aEnabled" : "§cDisabled"));
        convLore.add(Component.text(""));
        if (bank.getEnabledCurrencies().size() < 2) {
            convLore.add(Component.text("§cRequires 2+ currencies"));
        } else {
            convLore.add(Component.text("§eClick to toggle"));
        }
        convMeta.lore(convLore);
        conversion.setItemMeta(convMeta);
        inv.setItem(11, conversion);

        // Account transfer toggle
        ItemStack accountTransfer = new ItemStack(
            bank.isAccountTransferEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL
        );
        ItemMeta accMeta = accountTransfer.getItemMeta();
        accMeta.displayName(Component.text("§d§lAccount Transfer"));
        List<Component> accLore = new ArrayList<>();
        accLore.add(Component.text(""));
        accLore.add(Component.text(bank.isAccountTransferEnabled() ? 
            "§aEnabled" : "§cDisabled"));
        accLore.add(Component.text(""));
        accLore.add(Component.text("§7Allow transfers within"));
        accLore.add(Component.text("§7the same bank"));
        accLore.add(Component.text(""));
        accLore.add(Component.text("§eClick to toggle"));
        accMeta.lore(accLore);
        accountTransfer.setItemMeta(accMeta);
        inv.setItem(12, accountTransfer);

        // Bank transfer toggle
        ItemStack bankTransfer = new ItemStack(
            bank.isBankTransferEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL
        );
        ItemMeta bankMeta = bankTransfer.getItemMeta();
        bankMeta.displayName(Component.text("§5§lBank Transfer"));
        List<Component> bankLore = new ArrayList<>();
        bankLore.add(Component.text(""));
        bankLore.add(Component.text(bank.isBankTransferEnabled() ? 
            "§aEnabled" : "§cDisabled"));
        bankLore.add(Component.text(""));
        bankLore.add(Component.text("§7Allow transfers to"));
        bankLore.add(Component.text("§7other banks"));
        bankLore.add(Component.text(""));
        bankLore.add(Component.text("§eClick to toggle"));
        bankMeta.lore(bankLore);
        bankTransfer.setItemMeta(bankMeta);
        inv.setItem(13, bankTransfer);

        // Account types
        ItemStack accountTypes = new ItemStack(Material.PAPER);
        ItemMeta typesMeta = accountTypes.getItemMeta();
        typesMeta.displayName(Component.text("§e§lAccount Types"));
        List<Component> typesLore = new ArrayList<>();
        typesLore.add(Component.text(""));
        for (AccountType type : AccountType.values()) {
            if (bank.isAccountTypeAvailable(type)) {
                typesLore.add(Component.text("§a✓ §f" + type.getDisplayName()));
            } else {
                typesLore.add(Component.text("§7✗ §8" + type.getDisplayName()));
            }
        }
        typesLore.add(Component.text(""));
        typesLore.add(Component.text("§eClick to manage types"));
        typesMeta.lore(typesLore);
        accountTypes.setItemMeta(typesMeta);
        inv.setItem(14, accountTypes);

        // Loans settings
        ItemStack loans = new ItemStack(
            bank.isLoansEnabled() ? Material.LIME_DYE : Material.GRAY_DYE
        );
        ItemMeta loansMeta = loans.getItemMeta();
        loansMeta.displayName(Component.text("§6§lLoans"));
        List<Component> loansLore = new ArrayList<>();
        loansLore.add(Component.text(""));
        loansLore.add(Component.text(bank.isLoansEnabled() ? 
            "§aEnabled" : "§cDisabled"));
        if (bank.isLoansEnabled()) {
            loansLore.add(Component.text(""));
            loansLore.add(Component.text("§7Min: §f" + 
                String.format("%.0f", bank.getMinLoanAmount())));
            loansLore.add(Component.text("§7Max: §f" + 
                String.format("%.0f", bank.getMaxLoanAmount())));
            loansLore.add(Component.text("§7Interest: §e" + 
                String.format("%.1f", bank.getLoanInterestRate()) + "%"));
        }
        loansLore.add(Component.text(""));
        loansLore.add(Component.text("§eClick to toggle"));
        loansMeta.lore(loansLore);
        loans.setItemMeta(loansMeta);
        inv.setItem(15, loans);

        // Taxes
        ItemStack taxes = new ItemStack(Material.REDSTONE);
        ItemMeta taxMeta = taxes.getItemMeta();
        taxMeta.displayName(Component.text("§c§lGlobal Taxes"));
        List<Component> taxLore = new ArrayList<>();
        taxLore.add(Component.text(""));
        taxLore.add(Component.text("§7Deposit Tax: §e" + 
            String.format("%.2f", bank.getGlobalDepositTax()) + "%"));
        taxLore.add(Component.text("§7Withdrawal Tax: §e" + 
            String.format("%.2f", bank.getGlobalWithdrawalTax()) + "%"));
        taxLore.add(Component.text(""));
        taxLore.add(Component.text("§7Edit in config file"));
        taxMeta.lore(taxLore);
        taxes.setItemMeta(taxMeta);
        inv.setItem(16, taxes);

        // Max storage
        ItemStack storage = new ItemStack(Material.CHEST);
        ItemMeta storageMeta = storage.getItemMeta();
        storageMeta.displayName(Component.text("§e§lMax Storage"));
        List<Component> storageLore = new ArrayList<>();
        storageLore.add(Component.text(""));
        for (var entry : bank.getMaxStoragePerCurrency().entrySet()) {
            storageLore.add(Component.text("§7" + entry.getKey().name() + ": §f" + 
                String.format("%.0f", entry.getValue())));
        }
        storageLore.add(Component.text(""));
        storageLore.add(Component.text("§7Edit in config file"));
        storageMeta.lore(storageLore);
        storage.setItemMeta(storageMeta);
        inv.setItem(19, storage);

        // Save button
        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.displayName(Component.text("§a§lSave Configuration"));
        List<Component> saveLore = new ArrayList<>();
        saveLore.add(Component.text(""));
        saveLore.add(Component.text("§7Save changes to config file"));
        saveLore.add(Component.text(""));
        saveLore.add(Component.text("§eClick to save"));
        saveMeta.lore(saveLore);
        save.setItemMeta(saveMeta);
        inv.setItem(48, save);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        player.openInventory(inv);
    }
}