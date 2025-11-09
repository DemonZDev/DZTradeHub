package online.demonzdevelopment.dztradehub.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MessageUtil {
    private static final String PREFIX = "<gold>[TradeHub]</gold> ";
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    
    // Unicode symbols for better visuals
    public static final String SUCCESS = "✓";
    public static final String ERROR = "✗";
    public static final String INFO = "ℹ";
    public static final String WARNING = "⚠";
    public static final String ARROW_RIGHT = "→";
    public static final String ARROW_LEFT = "←";
    public static final String BULLET = "•";
    public static final String DIAMOND = "◆";
    public static final String STAR = "★";
    public static final String COIN = "⛀";
    public static final String GEM = "◈";

    public static void send(Player player, String message) {
        Component component = miniMessage.deserialize(PREFIX + message);
        player.sendMessage(component);
    }

    public static void sendError(Player player, String message) {
        send(player, "<red>" + ERROR + " " + message + "</red>");
        SoundUtil.playError(player);
    }

    public static void sendSuccess(Player player, String message) {
        send(player, "<green>" + SUCCESS + " " + message + "</green>");
        SoundUtil.playSuccess(player);
    }

    public static void sendInfo(Player player, String message) {
        send(player, "<yellow>" + INFO + " " + message + "</yellow>");
        SoundUtil.playInfo(player);
    }
    
    public static void sendWarning(Player player, String message) {
        send(player, "<gold>" + WARNING + " " + message + "</gold>");
        SoundUtil.playWarning(player);
    }

    public static Component parseComponent(String text) {
        return miniMessage.deserialize(text);
    }

    public static String stripFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    /**
     * Format currency amount with proper symbols
     */
    public static String formatCurrency(double amount, String currencyType) {
        String symbol = switch (currencyType.toUpperCase()) {
            case "MONEY" -> "$";
            case "MOBCOIN" -> "MC ";
            case "GEM" -> GEM + " ";
            default -> "";
        };
        
        return symbol + formatNumber(amount);
    }
    
    /**
     * Format large numbers (1000 -> 1K, 1000000 -> 1M)
     */
    public static String formatNumber(double number) {
        if (number >= 1_000_000_000) {
            return String.format("%.2fB", number / 1_000_000_000);
        } else if (number >= 1_000_000) {
            return String.format("%.2fM", number / 1_000_000);
        } else if (number >= 1_000) {
            return String.format("%.2fK", number / 1_000);
        } else {
            return DECIMAL_FORMAT.format(number);
        }
    }
    
    /**
     * Format time in seconds to readable format
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    /**
     * Create a progress bar
     */
    public static String createProgressBar(double current, double max, int bars) {
        double percentage = (current / max) * 100;
        int filled = (int) ((current / max) * bars);
        
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append(" §e").append(String.format("%.1f%%", percentage));
        
        return bar.toString();
    }
    
    /**
     * Create a centered message
     */
    public static String center(String message) {
        int maxWidth = 80;
        int spaces = (maxWidth - stripFormatting(message).length()) / 2;
        return " ".repeat(Math.max(0, spaces)) + message;
    }
    
    /**
     * Create a separator line
     */
    public static String separator(String pattern, int length) {
        return pattern.repeat(length);
    }
    
    /**
     * Send title and subtitle to player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
            miniMessage.serialize(parseComponent(title)),
            miniMessage.serialize(parseComponent(subtitle)),
            fadeIn, stay, fadeOut
        );
    }
    
    /**
     * Send action bar message
     */
    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(parseComponent(message));
    }
    
    /**
     * Format a list with bullets
     */
    public static String bulletList(String item) {
        return "§7  " + BULLET + " §f" + item;
    }
}
