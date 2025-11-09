package online.demonzdevelopment.dztradehub.utils;

/**
 * Input sanitization utility to prevent log injection, XSS-like attacks,
 * and data corruption from malicious user input.
 * 
 * All user-provided strings (names, descriptions, etc.) should be sanitized
 * before storage or display.
 */
public class InputSanitizer {
    
    // Maximum lengths for various input types
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    
    /**
     * Sanitize a player name or entity name.
     * Removes control characters except newlines and tabs.
     * Limits length to prevent overflow.
     * 
     * @param input The raw input string
     * @return Sanitized string, or empty string if input is null
     */
    public static String sanitizeName(String input) {
        return sanitizeInput(input, MAX_NAME_LENGTH);
    }
    
    /**
     * Sanitize a description or longer text field.
     * 
     * @param input The raw input string
     * @return Sanitized string, or empty string if input is null
     */
    public static String sanitizeDescription(String input) {
        return sanitizeInput(input, MAX_DESCRIPTION_LENGTH);
    }
    
    /**
     * Sanitize a chat message or user-facing text.
     * 
     * @param input The raw input string
     * @return Sanitized string, or empty string if input is null
     */
    public static String sanitizeMessage(String input) {
        return sanitizeInput(input, MAX_MESSAGE_LENGTH);
    }
    
    /**
     * General-purpose input sanitizer.
     * 
     * @param input The raw input string
     * @param maxLength Maximum allowed length
     * @return Sanitized string
     */
    public static String sanitizeInput(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        
        // Remove all control characters except \r, \n, \t
        String sanitized = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        
        // Remove potentially dangerous characters for logging
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F]", "");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        // Limit length
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }
    
    /**
     * Sanitize input for database storage (strict mode).
     * Removes more characters to ensure database compatibility.
     * 
     * @param input The raw input string
     * @param maxLength Maximum allowed length
     * @return Sanitized string safe for database storage
     */
    public static String sanitizeForDatabase(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        
        // Remove all control characters
        String sanitized = input.replaceAll("\\p{Cntrl}", "");
        
        // Remove potentially problematic characters
        sanitized = sanitized.replaceAll("[<>\"'\\\\]", "");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        // Limit length
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }
    
    /**
     * Validate that a string contains only alphanumeric characters, hyphens, and underscores.
     * Used for shop names, area names, etc.
     * 
     * @param input The input string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidIdentifier(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * Sanitize an identifier (shop name, area name, etc.).
     * Only allows alphanumeric, hyphens, and underscores.
     * 
     * @param input The raw input string
     * @return Sanitized identifier
     */
    public static String sanitizeIdentifier(String input) {
        if (input == null) {
            return "";
        }
        
        // Keep only allowed characters
        String sanitized = input.replaceAll("[^a-zA-Z0-9_-]", "");
        
        // Limit length
        if (sanitized.length() > MAX_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_NAME_LENGTH);
        }
        
        return sanitized;
    }
    
    /**
     * Escape special characters for logging to prevent log injection.
     * 
     * @param input The raw input string
     * @return String safe for logging
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "";
        }
        
        // Replace newlines and carriage returns with spaces
        String sanitized = input.replaceAll("[\r\n]", " ");
        
        // Remove other control characters
        sanitized = sanitized.replaceAll("\\p{Cntrl}", "");
        
        // Limit length for logs
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200) + "...";
        }
        
        return sanitized;
    }
}
