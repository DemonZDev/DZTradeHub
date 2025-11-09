package online.demonzdevelopment.dztradehub.gui.bank;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
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
import java.util.stream.Collectors;

public class BankListGUI {
    private final DZTradeHub plugin;

    public static class BankListHolder implements InventoryHolder {
        private int page;

        public BankListHolder(int page) {
            this.page = page;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public BankListGUI(DZTradeHub plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(
            new BankListHolder(page),
            54,
            Component.text("§6§lAvailable Banks")
        );

        List<Bank> banks = new ArrayList<>(plugin.getBankManager().getAllBanks());
        int startIndex = page * 36;
        int endIndex = Math.min(startIndex + 36, banks.size());

        // Fill bank items (slots 9-44)
        for (int i = startIndex; i < endIndex; i++) {
            Bank bank = banks.get(i);
            ItemStack display = createBankDisplay(player, bank);
            inv.setItem(9 + (i - startIndex), display);
        }

        // Navigation
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("§ePrevious Page"));
            prev.setItemMeta(prevMeta);
            inv.setItem(45, prev);
        }

        if (endIndex < banks.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("§eNext Page"));
            next.setItemMeta(nextMeta);
            inv.setItem(53, next);
        }

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("§e§lBank Information"));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text("§7Total Banks: §f" + banks.size()));
        infoLore.add(Component.text(""));
        infoLore.add(Component.text("§7Click a bank to view details"));
        infoLore.add(Component.text("§7or create an account"));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(49, info);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("§cClose"));
        close.setItemMeta(closeMeta);
        inv.setItem(50, close);

        player.openInventory(inv);
    }

    private ItemStack createBankDisplay(Player player, Bank bank) {
        Material material = Material.GOLD_BLOCK;
        
        // Choose material based on enabled currencies
        if (bank.getEnabledCurrencies().size() == 1) {
            var currency = bank.getEnabledCurrencies().iterator().next();
            switch (currency) {
                case MONEY -> material = Material.GOLD_BLOCK;
                case MOBCOIN -> material = Material.EMERALD_BLOCK;
                case GEM -> material = Material.DIAMOND_BLOCK;
            }
        } else if (bank.getEnabledCurrencies().size() == 2) {
            material = Material.IRON_BLOCK;
        } else {
            material = Material.NETHERITE_BLOCK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(bank.getDisplayName()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("§e§lCurrencies:"));
        for (var currency : bank.getEnabledCurrencies()) {
            lore.add(Component.text("§7▸ §f" + currency.name()));
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§e§lFeatures:"));
        if (bank.isCurrencyConversionEnabled()) {
            lore.add(Component.text("§a✓ §7Currency Conversion"));
        }
        if (bank.isAccountTransferEnabled()) {
            lore.add(Component.text("§a✓ §7Account Transfer"));
        }
        if (bank.isBankTransferEnabled()) {
            lore.add(Component.text("§a✓ §7Bank Transfer"));
        }
        if (bank.isLoansEnabled()) {
            lore.add(Component.text("§a✓ §7Loans Available"));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§e§lAccount Types:"));
        for (var type : bank.getAvailableAccountTypes()) {
            lore.add(Component.text("§7▸ §f" + type.getDisplayName()));
        }

        // Check if player has account
        var account = plugin.getBankAccountManager().getPlayerAccountAtBank(
            player.getUniqueId(), bank.getBankId()
        );
        
        lore.add(Component.text(""));
        if (account != null) {
            lore.add(Component.text("§a✓ You have an account"));
            lore.add(Component.text("§7Account: §f" + account.getAccountType().getDisplayName()));
            lore.add(Component.text("§7Level: §f" + account.getAccountLevel()));
        } else {
            lore.add(Component.text("§7You don't have an account yet"));
        }

        lore.add(Component.text(""));
        lore.add(Component.text("§eClick to access this bank"));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}