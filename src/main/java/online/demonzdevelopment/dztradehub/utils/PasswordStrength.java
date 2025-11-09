package online.demonzdevelopment.dztradehub.utils;

/**
 * Password strength evaluation utility.
 * Provides feedback to players on password quality.
 */
public class PasswordStrength {
    
    public enum Strength {
        WEAK("§c✗ Weak", "This password is easily guessable. Consider adding more characters and variety."),
        MODERATE("§e⚠ Moderate", "This password is acceptable but could be stronger."),
        STRONG("§a✓ Strong", "This is a strong password!"),
        VERY_STRONG("§a✓✓ Very Strong", "Excellent! This is a very secure password!");
        
        private final String display;
        private final String description;
        
        Strength(String display, String description) {
            this.display = display;
            this.description = description;
        }
        
        public String getDisplay() { return display; }
        public String getDescription() { return description; }
    }
    
    /**
     * Evaluate password strength.
     * 
     * @param password The password to evaluate
     * @return Strength rating
     */
    public static Strength evaluate(String password) {
        if (password == null || password.isEmpty()) {
            return Strength.WEAK;
        }
        
        int score = 0;
        
        // Length checks
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++;  // Lowercase
        if (password.matches(".*[A-Z].*")) score++;  // Uppercase
        if (password.matches(".*[0-9].*")) score++;  // Numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*")) score++;  // Special chars
        
        // Penalty for common patterns
        if (password.matches("^[0-9]+$")) score -= 2;  // Only numbers
        if (password.toLowerCase().matches("^[a-z]+$")) score -= 2;  // Only letters
        if (password.matches(".*(012|123|234|345|456|567|678|789|abc|def|ghi).*")) score--;  // Sequential
        if (password.matches(".*(000|111|222|333|444|555|666|777|888|999|aaa|bbb).*")) score--;  // Repetitive
        
        // Map score to strength
        if (score <= 2) return Strength.WEAK;
        if (score <= 4) return Strength.MODERATE;
        if (score <= 6) return Strength.STRONG;
        return Strength.VERY_STRONG;
    }
    
    /**
     * Get password strength score (0-10).
     */
    public static int getScore(String password) {
        Strength strength = evaluate(password);
        return switch (strength) {
            case WEAK -> 2;
            case MODERATE -> 5;
            case STRONG -> 8;
            case VERY_STRONG -> 10;
        };
    }
    
    /**
     * Check if password meets minimum strength requirement.
     */
    public static boolean meetsMinimumStrength(String password, Strength minStrength) {
        Strength actual = evaluate(password);
        return actual.ordinal() >= minStrength.ordinal();
    }
    
    /**
     * Get suggestions for improving password strength.
     */
    public static String[] getSuggestions(String password) {
        if (password == null || password.isEmpty()) {
            return new String[]{"Password cannot be empty"};
        }
        
        Strength strength = evaluate(password);
        if (strength == Strength.VERY_STRONG) {
            return new String[]{"Your password is excellent!"}
;
        }
        
        var suggestions = new java.util.ArrayList<String>();
        
        if (password.length() < 8) {
            suggestions.add("Use at least 8 characters");
        } else if (password.length() < 12) {
            suggestions.add("Consider using 12+ characters for better security");
        }
        
        if (!password.matches(".*[a-z].*")) {
            suggestions.add("Add lowercase letters");
        }
        if (!password.matches(".*[A-Z].*")) {
            suggestions.add("Add uppercase letters");
        }
        if (!password.matches(".*[0-9].*")) {
            suggestions.add("Add numbers");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*")) {
            suggestions.add("Add special characters (!@#$%^&*)");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Your password is good, but could be longer");
        }
        
        return suggestions.toArray(new String[0]);
    }
}
