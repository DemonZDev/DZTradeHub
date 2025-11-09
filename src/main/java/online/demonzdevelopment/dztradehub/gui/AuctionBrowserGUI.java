package online.demonzdevelopment.dztradehub.gui;

import net.kyori.adventure.text.Component;
import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.Auction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionBrowserGUI {
    private final DZTradeHub plugin;
    private final AuctionAddGUI auctionAddGUI;
    private final AuctionManageGUI auctionManageGUI;

    public static class AuctionHolder implements InventoryHolder {
        private int page;

        public AuctionHolder(int page) {
            this.page = page;
        }

        public int getPage() { return page; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public AuctionBrowserGUI(DZTradeHub plugin) {
        this.plugin = plugin;
        this.auctionAddGUI = new AuctionAddGUI(plugin);
        this.auctionManageGUI = new AuctionManageGUI(plugin);
    }

    public void openAuctionAddGUI(Player player) {
        auctionAddGUI.openAddGUI(player);
    }

    public void openAuctionManageGUI(Player player, Auction auction) {
        auctionManageGUI.openManageGUI(player, auction);
    }
    
    /**
     * Opens GUI showing all player's auctions for management
     */
    public void openManageAllAuctionsGUI(Player player) {
        List<Auction> playerAuctions = plugin.getAuctionManager().getPlayerAuctions(player.getUniqueId());
        
        if (playerAuctions.isEmpty()) {
            player.sendMessage("§cYou have no active auctions!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("§6§lManage Your Auctions"));
        
        int slot = 0;
        for (Auction auction : playerAuctions) {
            if (slot >= 45) break; // Leave room for navigation
            
            ItemStack display = auction.getItemStack().clone();
            ItemMeta meta = display.getItemMeta();
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Auction #" + auction.getItemNumber()));
            lore.add(Component.text(""));
            lore.add(Component.text("§7Current Price: §a$" + String.format("%.2f", auction.getCurrentPrice())));
            lore.add(Component.text("§7Currency: §e" + auction.getCurrencyType()));
            
            if (auction.isFrozen()) {
                lore.add(Component.text("§c§lFROZEN"));
            } else {
                lore.add(Component.text("§a§lACTIVE"));
            }
            
            lore.add(Component.text(""));
            lore.add(Component.text("§eClick to manage!"));
            
            meta.lore(lore);
            display.setItemMeta(meta);
            
            inv.setItem(slot++, display);
        }
        
        // Exit button
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.displayName(Component.text("§c§lClose"));
        exit.setItemMeta(exitMeta);
        inv.setItem(49, exit);
        
        player.openInventory(inv);
    }

    public AuctionAddGUI getAuctionAddGUI() {
        return auctionAddGUI;
    }

    public AuctionManageGUI getAuctionManageGUI() {
        return auctionManageGUI;
    }

    public void openBrowser(Player player) {
        openBrowser(player, 0);
    }

    public void openBrowser(Player player, int page) {
        Inventory inv = Bukkit.createInventory(
            new AuctionHolder(page),
            54,
            Component.text("§6Auction House")
        );

        // Get auctions for this page
        List<Auction> auctions = plugin.getAuctionManager().getAllAuctions();
        int startIndex = page * 36;
        int endIndex = Math.min(startIndex + 36, auctions.size());

        // Fill auction items (slots 9-44)
        for (int i = startIndex; i < endIndex; i++) {
            Auction auction = auctions.get(i);
            ItemStack display = createAuctionDisplay(auction);
            inv.setItem(9 + (i - startIndex), display);
        }

        // Previous page (slot 45)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("§ePrevious Page"));
            prev.setItemMeta(prevMeta);
            inv.setItem(45, prev);
        }

        // My Auctions (slot 49)
        ItemStack myAuctions = new ItemStack(Material.BOOK);
        ItemMeta myMeta = myAuctions.getItemMeta();
        myMeta.displayName(Component.text("§eMy Auctions"));
        myAuctions.setItemMeta(myMeta);
        inv.setItem(49, myAuctions);

        // Next page (slot 53)
        if (endIndex < auctions.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("§eNext Page"));
            next.setItemMeta(nextMeta);
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    private ItemStack createAuctionDisplay(Auction auction) {
        ItemStack display = auction.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();
        
        List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore.addAll(meta.lore());
        }
        
        lore.add(Component.text(""));
        
        double currentPrice = auction.getCurrentPrice();
        lore.add(Component.text("§7Current Price: §a$" + String.format("%.2f", currentPrice)));
        
        if (auction.getDropPerUnit() > 0) {
            lore.add(Component.text("§7Type: §ePri ce Reduction"));
            lore.add(Component.text("§7Drops: §e$" + auction.getDropPerUnit() + " per " + 
                auction.getDropIntervalHours() + "h"));
        }
        
        if (auction.getMaxQueue() > 0) {
            lore.add(Component.text("§7Type: §eBidding Queue"));
            lore.add(Component.text("§7Queue: §e" + auction.getQueueSize() + "/" + auction.getMaxQueue()));
        }
        
        if (auction.isFrozen()) {
            lore.add(Component.text("§c§lFROZEN"));
        }
        
        lore.add(Component.text(""));
        lore.add(Component.text("§eClick to purchase"));
        
        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }
}