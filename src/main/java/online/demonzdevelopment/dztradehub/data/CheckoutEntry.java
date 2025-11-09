package online.demonzdevelopment.dztradehub.data;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutEntry {
    private final UUID playerId;
    private final String playerName;
    private final List<ItemStack> items;
    private final long joinTime;
    private final int queueNumber;
    private boolean isProcessing;
    private long processingStartTime;

    public CheckoutEntry(Player player, List<ItemStack> items, int queueNumber) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.items = new ArrayList<>(items);
        this.joinTime = System.currentTimeMillis();
        this.queueNumber = queueNumber;
        this.isProcessing = false;
        this.processingStartTime = 0;
    }

    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public List<ItemStack> getItems() { return new ArrayList<>(items); }
    public int getTotalItems() {
        int total = 0;
        for (ItemStack item : items) {
            total += item.getAmount();
        }
        return total;
    }
    public long getJoinTime() { return joinTime; }
    public int getQueueNumber() { return queueNumber; }
    public boolean isProcessing() { return isProcessing; }
    public void setProcessing(boolean processing) {
        this.isProcessing = processing;
        if (processing) {
            this.processingStartTime = System.currentTimeMillis();
        }
    }
    public long getProcessingStartTime() { return processingStartTime; }
    public long getWaitTime() { return System.currentTimeMillis() - joinTime; }
    public long getProcessingTime() { return processingStartTime > 0 ? System.currentTimeMillis() - processingStartTime : 0; }
}
