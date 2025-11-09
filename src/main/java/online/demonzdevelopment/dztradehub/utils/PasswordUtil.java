package online.demonzdevelopment.dztradehub.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Secure password hashing utility using PBKDF2-HMAC-SHA256.
 * 
 * Security Features:
 * - PBKDF2-HMAC-SHA256 with 65,536 iterations (OWASP recommended)
 * - Random 16-byte salt per password
 * - Constant-time comparison to prevent timing attacks
 * - Backward compatible with legacy SHA-256 hashes
 * - Automatic migration from legacy to secure hashing
 * 
 * Hash Format: "salt:hash" (Base64 encoded)
 * 
 * ⚠️ SECURITY WARNING ⚠️
 * NEVER log passwords, hashes, or sensitive data:
 * - Do NOT log plaintext passwords
 * - Do NOT log password hashes (even PBKDF2)
 * - Do NOT log session tokens or authentication data
 * - Only log account IDs and operation results
 */
public class PasswordUtil {
    // Legacy salt for backward compatibility only
    private static final String LEGACY_SALT = "DZTradeHub_Bank_Salt_2024";
    
    // PBKDF2 configuration (OWASP recommendations)
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65536; // 2^16 iterations
    private static final int SALT_BYTES = 16; // 128 bits
    private static final int HASH_BYTES = 32; // 256 bits
    
    /**
     * Hash a password using PBKDF2-HMAC-SHA256 with random salt.
     * 
     * @param password The plaintext password to hash
     * @return Base64 encoded "salt:hash" string
     * @throws RuntimeException if hashing fails
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);
            
            // Hash password with PBKDF2
            byte[] hash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTES);
            
            // Encode as "salt:hash" in Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password with PBKDF2", e);
        }
    }
    
    /**
     * Verify a password against a hash.
     * Supports both new PBKDF2 format and legacy SHA-256 format.
     * 
     * @param password The plaintext password to verify
     * @param storedHash The stored hash (either PBKDF2 or legacy SHA-256)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        
        // Check if it's a new PBKDF2 hash (contains ":")
        if (storedHash.contains(":")) {
            return verifyPBKDF2(password, storedHash);
        } else {
            // Legacy SHA-256 hash
            return verifyLegacySHA256(password, storedHash);
        }
    }
    
    /**
     * Verify password against PBKDF2 hash using constant-time comparison.
     * 
     * @param password The plaintext password
     * @param storedHash The stored "salt:hash" string
     * @return true if password matches
     */
    private static boolean verifyPBKDF2(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the provided password with the same salt
            byte[] testHash = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, hash.length);
            
            // Constant-time comparison to prevent timing attacks
            return slowEquals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verify password against legacy SHA-256 hash (for backward compatibility).
     * 
     * @param password The plaintext password
     * @param storedHash The stored SHA-256 hex hash
     * @return true if password matches
     */
    private static boolean verifyLegacySHA256(String password, String storedHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + LEGACY_SALT;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(hash);
            
            // Constant-time comparison
            return slowEquals(computedHash.getBytes(StandardCharsets.UTF_8), 
                            storedHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
    
    /**
     * Check if a hash is in legacy SHA-256 format (no colon separator).
     * Used to trigger automatic migration.
     * 
     * @param hash The stored hash
     * @return true if it's a legacy hash
     */
    public static boolean isLegacyHash(String hash) {
        return hash != null && !hash.contains(":");
    }
    
    /**
     * PBKDF2 key derivation function.
     * 
     * @param password The password as char array
     * @param salt The salt bytes
     * @param iterations Number of iterations (65536 recommended)
     * @param bytes Number of bytes to derive
     * @return The derived key bytes
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
    
    /**
     * Constant-time byte array comparison.
     * Prevents timing attacks by always comparing all bytes.
     * 
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
    
    /**
     * Convert byte array to hex string (for legacy compatibility).
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Validate password strength.
     * 
     * @param password The password to validate
     * @return true if password meets requirements
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 4) {
            return false;
        }
        if (password.length() > 32) {
            return false;
        }
        // Must contain at least one letter or number
        return password.matches(".*[a-zA-Z0-9].*");
    }
    
    /**
     * Generate a cryptographically secure random password.
     * 
     * @param length The length of the password
     * @return A random password string
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}