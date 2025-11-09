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
import java.util.List;

public class BankAccountGUI {
    private final DZTradeHub plugin;

    public static class BankAccountHolder implements InventoryHolder {
        private final Bank bank;
        private final BankAccount account;

        public BankAccountHolder(Bank bank, BankAccount account) {
            this.bank = bank;
            this.account = account;
        }

        public Bank getBank() { return bank; }
        public BankAccount getAccount() { return account; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankAccountGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Bank bank, BankAccount account) {
        Inventory inv = Bukkit.createInventory(
            new BankAccountHolder(bank, account),
            54,
            Component.text(bank.getDisplayName() + " §7- §fAccount")
        );

        // Account info (slot 4)
        ItemStack accountInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta accountMeta = accountInfo.getItemMeta();
        accountMeta.displayName(Component.text("§e§lAccount Information"));
        List<Component> accountLore = new ArrayList<>();
        accountLore.add(Component.text(""));
        accountLore.add(Component.text("§7Owner: §f" + account.getPlayerName()));
        accountLore.add(Component.text("§7Type: §f" + account.getAccountType().getDisplayName()));
        accountLore.add(Component.text("§7Level: §f" + account.getAccountLevel()));
        accountLore.add(Component.text("§7Transactions: §f" + account.getTotalTransactions()));
        accountMeta.lore(accountLore);
        accountInfo.setItemMeta(accountMeta);
        inv.setItem(4, accountInfo);

        // Currency balances (slots 19-25)
        int slot = 19;
        for (CurrencyType currency : CurrencyType.values()) {
            if (bank.isCurrencyEnabled(currency)) {
                ItemStack balanceItem = createBalanceDisplay(player, account, currency);
                inv.setItem(slot++, balanceItem);
            }
        }

        // Deposit button (slot 29)
        ItemStack deposit = new ItemStack(Material.LIME_DYE);
        ItemMeta depositMeta = deposit.getItemMeta();
        depositMeta.displayName(Component.text("§a§lDeposit"));
        List<Component> depositLore = new ArrayList<>();
        depositLore.add(Component.text(""));
        depositLore.add(Component.text("§7Put money into the bank"));
        depositLore.add(Component.text("§7from your pocket"));
        depositLore.add(Component.text(""));
        depositLore.add(Component.text("§7Tax: §e" + 
            String.format("%.2f", calculateDepositTax(bank, account)) + "%"));
        depositLore.add(Component.text(""));
        depositLore.add(Component.text("§eClick to deposit"));
        depositMeta.lore(depositLore);
        deposit.setItemMeta(depositMeta);
        inv.setItem(29, deposit);

        // Withdraw button (slot 30)
        ItemStack withdraw = new ItemStack(Material.ORANGE_DYE);
        ItemMeta withdrawMeta = withdraw.getItemMeta();
        withdrawMeta.displayName(Component.text("§6§lWithdraw"));
        List<Component> withdrawLore = new ArrayList<>();
        withdrawLore.add(Component.text(""));
        withdrawLore.add(Component.text("§7Take money from the bank"));
        withdrawLore.add(Component.text("§7to your pocket"));
        withdrawLore.add(Component.text(""));
        withdrawLore.add(Component.text("§7Tax: §e" + 
            String.format("%.2f", calculateWithdrawTax(bank, account)) + "%"));
        withdrawLore.add(Component.text(""));
        withdrawLore.add(Component.text("§eClick to withdraw"));
        withdrawMeta.lore(withdrawLore);
        withdraw.setItemMeta(withdrawMeta);
        inv.setItem(30, withdraw);

        // Transfer button (slot 31)
        if (bank.isAccountTransferEnabled() || bank.isBankTransferEnabled()) {
            ItemStack transfer = new ItemStack(Material.ENDER_PEARL);
            ItemMeta transferMeta = transfer.getItemMeta();
            transferMeta.displayName(Component.text("§d§lTransfer"));
            List<Component> transferLore = new ArrayList<>();
            transferLore.add(Component.text(""));
            transferLore.add(Component.text("§7Transfer currency to"));
            if (bank.isAccountTransferEnabled()) {
                transferLore.add(Component.text("§7▸ Other accounts in this bank"));
            }
            if (bank.isBankTransferEnabled()) {
                transferLore.add(Component.text("§7▸ Accounts in other banks"));
            }
            transferLore.add(Component.text(""));
            transferLore.add(Component.text("§eClick to transfer"));
            transferMeta.lore(transferLore);
            transfer.setItemMeta(transferMeta);
            inv.setItem(31, transfer);
        }

        // Currency conversion (slot 32)
        if (bank.isCurrencyConversionEnabled()) {
            ItemStack convert = new ItemStack(Material.BREWING_STAND);
            ItemMeta convertMeta = convert.getItemMeta();
            convertMeta.displayName(Component.text("§b§lConvert Currency"));
            List<Component> convertLore = new ArrayList<>();
            convertLore.add(Component.text(""));
            convertLore.add(Component.text("§7Convert between currencies"));
            convertLore.add(Component.text(""));
            convertLore.add(Component.text("§eClick to convert"));
            convertMeta.lore(convertLore);
            convert.setItemMeta(convertMeta);
            inv.setItem(32, convert);
        }

        // Loans (slot 33)
        if (bank.isLoansEnabled()) {
            ItemStack loans = new ItemStack(Material.PAPER);
            ItemMeta loansMeta = loans.getItemMeta();
            loansMeta.displayName(Component.text("§e§lLoans"));
            List<Component> loansLore = new ArrayList<>();
            loansLore.add(Component.text(""));
            loansLore.add(Component.text("§7Take a loan from the bank"));
            loansLore.add(Component.text("§7Range: §f" + 
                String.format("%.0f", bank.getMinLoanAmount()) + " - " +
                String.format("%.0f", bank.getMaxLoanAmount())));
            loansLore.add(Component.text("§7Interest: §e" + 
                String.format("%.1f", bank.getLoanInterestRate()) + "%"));
            loansLore.add(Component.text(""));
            loansLore.add(Component.text("§eClick to manage loans"));
            loansMeta.lore(loansLore);
            loans.setItemMeta(loansMeta);
            inv.setItem(33, loans);
        }

        // Account settings (slot 47)
        ItemStack settings = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settings.getItemMeta();
        settingsMeta.displayName(Component.text("§7§lAccount Settings"));
        List<Component> settingsLore = new ArrayList<>();
        settingsLore.add(Component.text(""));
        settingsLore.add(Component.text("§7▸ Change password"));
        settingsLore.add(Component.text("§7▸ Change account type"));
        settingsLore.add(Component.text("§7▸ Upgrade account level"));
        settingsLore.add(Component.text("§7▸ Delete account"));
        settingsLore.add(Component.text(""));
        settingsLore.add(Component.text("§eClick to manage settings"));
        settingsMeta.lore(settingsLore);
        settings.setItemMeta(settingsMeta);
        inv.setItem(47, settings);

        // Transaction history (slot 48)
        ItemStack history = new ItemStack(Material.BOOK);
        ItemMeta historyMeta = history.getItemMeta();
        historyMeta.displayName(Component.text("§e§lTransaction History"));
        List<Component> historyLore = new ArrayList<>();
        historyLore.add(Component.text(""));
        historyLore.add(Component.text("§7View recent transactions"));
        historyLore.add(Component.text(""));
        historyLore.add(Component.text("§eClick to view"));
        historyMeta.lore(historyLore);
        history.setItemMeta(historyMeta);
        inv.setItem(48, history);

        // Close button (slot 49)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(49, close);

        player.openInventory(inv);
    }

    private ItemStack createBalanceDisplay(Player player, BankAccount account, CurrencyType currency) {
        Material material = switch (currency) {
            case MONEY -> Material.GOLD_INGOT;
            case MOBCOIN -> Material.EMERALD;
            case GEM -> Material.DIAMOND;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l" + currency.name()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§7Bank Balance: §f" + 
            String.format("%.2f", account.getBalance(currency))));
        lore.add(Component.text("§7Pocket: §f" + 
            String.format("%.2f", plugin.getEconomyAPI().getBalance(player.getUniqueId(), currency))));
        lore.add(Component.text(""));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private double calculateDepositTax(Bank bank, BankAccount account) {
        double baseTax = bank.getGlobalDepositTax();
        double multiplier = account.getAccountType().getDepositTaxMultiplier();
        
        // Apply level reduction
        var levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            double reduction = levelConfig.getDepositTaxReduction();
            baseTax = baseTax * (1 - reduction / 100.0);
        }
        
        return baseTax * multiplier;
    }

    private double calculateWithdrawTax(Bank bank, BankAccount account) {
        double baseTax = bank.getGlobalWithdrawalTax();
        double multiplier = account.getAccountType().getWithdrawalTaxMultiplier();
        
        // Apply level reduction
        var levelConfig = bank.getLevelConfig(account.getAccountLevel());
        if (levelConfig != null) {
            double reduction = levelConfig.getWithdrawalTaxReduction();
            baseTax = baseTax * (1 - reduction / 100.0);
        }
        
        return baseTax * multiplier;
    }
}