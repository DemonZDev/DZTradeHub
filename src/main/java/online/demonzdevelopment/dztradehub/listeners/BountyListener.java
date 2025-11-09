package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyListener implements Listener {
    private final DZTradeHub plugin;
    
    public BountyListener(DZTradeHub plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) {
            return;
        }
        
        // Check if victim has bounty
        if (plugin.getBountyManager().hasBounty(victim.getUniqueId())) {
            plugin.getBountyManager().claimBounty(killer, victim);
        }
    }
}
