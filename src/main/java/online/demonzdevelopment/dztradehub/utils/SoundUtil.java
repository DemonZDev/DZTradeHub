package online.demonzdevelopment.dztradehub.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Utility class for playing sounds to enhance player experience
 */
public class SoundUtil {
    
    /**
     * Play success sound
     */
    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
    }
    
    /**
     * Play error/failure sound
     */
    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
    }
    
    /**
     * Play click sound
     */
    public static void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Play purchase sound
     */
    public static void playPurchase(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
    }
    
    /**
     * Play sell sound
     */
    public static void playSell(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
    }
    
    /**
     * Play coin sound (for casino)
     */
    public static void playCoin(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);
    }
    
    /**
     * Play win sound
     */
    public static void playWin(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }
    
    /**
     * Play lose sound
     */
    public static void playLose(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
    }
    
    /**
     * Play jackpot spin sound
     */
    public static void playJackpotSpin(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
    }
    
    /**
     * Play bounty claim sound
     */
    public static void playBountyClaim(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
    
    /**
     * Play auction bid sound
     */
    public static void playAuctionBid(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 1.5f);
    }
    
    /**
     * Play bank transaction sound
     */
    public static void playBankTransaction(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 1.2f);
    }
    
    /**
     * Play warning sound
     */
    public static void playWarning(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
    }
    
    /**
     * Play info notification sound
     */
    public static void playInfo(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.4f, 1.8f);
    }
    
    /**
     * Play queue join sound
     */
    public static void playQueueJoin(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }
    
    /**
     * Play queue progress sound
     */
    public static void playQueueProgress(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1.5f);
    }
}
