package online.demonzdevelopment.dztradehub.listeners;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import online.demonzdevelopment.dztradehub.data.bank.Bank;
import online.demonzdevelopment.dztradehub.data.bank.BankAccount;
import online.demonzdevelopment.dztradehub.gui.bank.BankAccountGUI;
import online.demonzdevelopment.dztradehub.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BankPasswordListener implements Listener {
    private final DZTradeHub plugin;
    private final Map<UUID, PasswordState> awaitingPassword;
    private final Map<UUID, Integer> failedAttempts;
    private static final int MAX_ATTEMPTS = 3;
    private static final long PASSWORD_TIMEOUT = 60000; // 60 seconds
    
    public BankPasswordListener(DZTradeHub plugin) {
        this.plugin = plugin;
        this.awaitingPassword = new ConcurrentHashMap<>();
        this.failedAttempts = new ConcurrentHashMap<>();
    }
    
    /**
     * Request password from player
     */
    public void requestPassword(Player player, Bank bank, BankAccount account, PasswordCallback callback) {
        awaitingPassword.put(player.getUniqueId(), new PasswordState(bank, account, callback));
        failedAttempts.put(player.getUniqueId(), 0);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        if (!awaitingPassword.containsKey(playerUUID)) {
            return;
        }
        
        event.setCancelled(true);
        
        PasswordState state = awaitingPassword.get(playerUUID);
        
        // Check timeout
        if (System.currentTimeMillis() - state.timestamp > PASSWORD_TIMEOUT) {
            awaitingPassword.remove(playerUUID);
            failedAttempts.remove(playerUUID);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MessageUtil.sendError(player, "Password entry timed out!");
                state.callback.onTimeout();
            });
            return;
        }
        
        String input = event.getMessage().trim();
        
        // Cancel command
        if (input.equalsIgnoreCase("cancel")) {
            awaitingPassword.remove(playerUUID);
            failedAttempts.remove(playerUUID);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MessageUtil.sendInfo(player, "Cancelled password entry.");
                state.callback.onCancel();
            });
            return;
        }
        
        // Verify password
        boolean verified = plugin.getBankAccountManager().verifyPassword(state.account, input);
        
        if (verified) {
            awaitingPassword.remove(playerUUID);
            failedAttempts.remove(playerUUID);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                MessageUtil.sendSuccess(player, "Access granted!");
                state.callback.onSuccess(state.account);
            });
        } else {
            int attempts = failedAttempts.get(playerUUID) + 1;
            failedAttempts.put(playerUUID, attempts);
            
            if (attempts >= MAX_ATTEMPTS) {
                awaitingPassword.remove(playerUUID);
                failedAttempts.remove(playerUUID);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendError(player, "Too many failed attempts! Access denied.");
                    state.callback.onFailure();
                });
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendError(player, "Incorrect password! Attempts remaining: " + (MAX_ATTEMPTS - attempts));
                    player.sendMessage("§7Type 'cancel' to exit");
                });
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        awaitingPassword.remove(playerUUID);
        failedAttempts.remove(playerUUID);
    }
    
    /**
     * Check if player is entering password
     */
    public boolean isAwaitingPassword(UUID playerUUID) {
        return awaitingPassword.containsKey(playerUUID);
    }
    
    /**
     * Cancel password request
     */
    public void cancelPasswordRequest(UUID playerUUID) {
        awaitingPassword.remove(playerUUID);
        failedAttempts.remove(playerUUID);
    }
    
    /**
     * Password state holder
     */
    private static class PasswordState {
        private final Bank bank;
        private final BankAccount account;
        private final PasswordCallback callback;
        private final long timestamp;
        
        public PasswordState(Bank bank, BankAccount account, PasswordCallback callback) {
            this.bank = bank;
            this.account = account;
            this.callback = callback;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Password callback interface
     */
    public interface PasswordCallback {
        void onSuccess(BankAccount account);
        void onFailure();
        void onCancel();
        void onTimeout();
    }
}
