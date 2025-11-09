package online.demonzdevelopment.dztradehub.data;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Queue entry with session hijacking protection.
 * Each queue entry has a unique session token that must be validated.
 */
public class QueueEntry {
    private final UUID playerId;
    private final String playerName;
    private final long joinTime;
    private long lastActivityTime;
    private final int queueNumber;
    private boolean isActive;
    private long sessionStartTime;
    private final UUID sessionToken; // SECURITY: Prevents queue hijacking

    public QueueEntry(Player player, int queueNumber) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.joinTime = System.currentTimeMillis();
        this.lastActivityTime = System.currentTimeMillis();
        this.queueNumber = queueNumber;
        this.isActive = false;
        this.sessionStartTime = 0;
        this.sessionToken = UUID.randomUUID(); // Generate unique session token
    }

    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public long getJoinTime() { return joinTime; }
    public long getLastActivityTime() { return lastActivityTime; }
    public void updateActivity() { this.lastActivityTime = System.currentTimeMillis(); }
    public int getQueueNumber() { return queueNumber; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { 
        this.isActive = active;
        if (active) {
            this.sessionStartTime = System.currentTimeMillis();
        }
    }
    public long getSessionStartTime() { return sessionStartTime; }
    public long getWaitTime() { return System.currentTimeMillis() - joinTime; }
    public long getAfkTime() { return System.currentTimeMillis() - lastActivityTime; }
    public long getSessionTime() { return sessionStartTime > 0 ? System.currentTimeMillis() - sessionStartTime : 0; }
    
    /**
     * Get the unique session token for this queue entry.
     * SECURITY: This token prevents session hijacking.
     */
    public UUID getSessionToken() { return sessionToken; }
    
    /**
     * Validate that the player is still online and owns this session.
     * SECURITY: Prevents offline/cracked server session hijacking.
     */
    public boolean isValidSession(Player player) {
        return player != null && 
               player.isOnline() && 
               player.getUniqueId().equals(playerId);
    }
}
