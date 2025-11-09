package online.demonzdevelopment.dztradehub.utils;

public class TimeUtil {
    
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        } else {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            return days + "d " + hours + "h";
        }
    }

    public static long parseTimeUnit(String unit) {
        return switch (unit.toLowerCase()) {
            case "second", "seconds", "s" -> 1L;
            case "minute", "minutes", "m" -> 60L;
            case "hour", "hours", "h" -> 3600L;
            case "day", "days", "d" -> 86400L;
            case "week", "weeks", "w" -> 604800L;
            default -> 1L;
        };
    }

    public static long getMillisFromNow(long seconds) {
        return System.currentTimeMillis() + (seconds * 1000);
    }

    public static long getRemainingSeconds(long targetTimeMillis) {
        long remaining = targetTimeMillis - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}