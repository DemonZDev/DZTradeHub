package online.demonzdevelopment.dztradehub.data;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bounty {
    private final UUID bountyId;
    private final UUID targetPlayer;
    private final UUID creatorPlayer;
    private final List<ItemStack> rewardItems;
    private double moneyReward;
    private double mobcoinReward;
    private double gemReward;
    private final long createdTime;
    private boolean active;
    private int bountyNumber; // For /bounty delete <number> and /bounty manage <number>

    public Bounty(UUID targetPlayer, UUID creatorPlayer) {
        this.bountyId = UUID.randomUUID();
        this.targetPlayer = targetPlayer;
        this.creatorPlayer = creatorPlayer;
        this.rewardItems = new ArrayList<>();
        this.moneyReward = 0.0;
        this.mobcoinReward = 0.0;
        this.gemReward = 0.0;
        this.createdTime = System.currentTimeMillis();
        this.active = true;
        this.bountyNumber = 0;
    }

    // Getters and Setters
    public UUID getBountyId() { return bountyId; }
    public UUID getTargetPlayer() { return targetPlayer; }
    public UUID getCreatorPlayer() { return creatorPlayer; }
    public int getBountyNumber() { return bountyNumber; }
    public void setBountyNumber(int bountyNumber) { this.bountyNumber = bountyNumber; }
    public List<ItemStack> getRewardItems() { return new ArrayList<>(rewardItems); }
    public void addRewardItem(ItemStack item) { 
        if (item != null && !item.getType().isAir()) {
            rewardItems.add(item.clone());
        }
    }
    public void setRewardItems(List<ItemStack> items) { 
        this.rewardItems.clear();
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                this.rewardItems.add(item.clone());
            }
        }
    }
    public double getMoneyReward() { return moneyReward; }
    public void setMoneyReward(double moneyReward) { this.moneyReward = moneyReward; }
    public double getMobcoinReward() { return mobcoinReward; }
    public void setMobcoinReward(double mobcoinReward) { this.mobcoinReward = mobcoinReward; }
    public double getGemReward() { return gemReward; }
    public void setGemReward(double gemReward) { this.gemReward = gemReward; }
    public long getCreatedTime() { return createdTime; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public double getTotalCurrencyReward() {
        return moneyReward + mobcoinReward + gemReward;
    }
    
    public boolean hasRewards() {
        return !rewardItems.isEmpty() || getTotalCurrencyReward() > 0;
    }
}
