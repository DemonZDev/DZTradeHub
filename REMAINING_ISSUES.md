# DZTradeHub - Remaining Issues & Future Improvements

**Status:** Post-Critical/High Security Fixes
**Date:** 2025-11-08
**Priority Order:** MEDIUM → LOW → IMPROVEMENTS

---

## 📊 Summary

| Priority | Count | Status |
|----------|-------|--------|
| CRITICAL | 0 | ✅ ALL FIXED |
| HIGH | 0 | ✅ ALL FIXED |
| MEDIUM | 4 | ⏳ To be addressed |
| LOW | 2 | ⏳ To be addressed |
| IMPROVEMENTS | 8 | 💡 Recommended |

---

## 🟠 MEDIUM PRIORITY ISSUES

### MEDIUM-001: Session Hijacking via Queue System
**Severity:** MEDIUM  
**CVSS Score:** 5.3

**Files Affected:**
- `QueueManager.java`
- `BankQueueManager.java`

**Issue:**
No session validation in queue system. If player disconnects and reconnects, queue position could be hijacked on offline/cracked servers.

**Current Behavior:**
```java
// Player A enters queue with UUID
QueueEntry entry = new QueueEntry(player, queueNumber);
queue.add(entry);

// Player disconnects
// Player B connects with same UUID (offline server)
// Player B accesses Player A's queue slot
```

**Attack Scenario:**
1. Player A enters bank reception queue
2. Player A disconnects
3. Attacker connects with same UUID (if offline server)
4. Attacker gains queue position and bank access

**Recommended Fix:**
```java
// Add session token system
public class QueueEntry {
    private UUID sessionToken; // Add this field
    
    public QueueEntry(Player player, int queueNumber) {
        this.playerId = player.getUniqueId();
        this.queueNumber = queueNumber;
        this.sessionToken = UUID.randomUUID(); // Generate unique token
        this.joinTime = System.currentTimeMillis();
    }
    
    public boolean isValidSession(Player player) {
        // Verify player is still online and session matches
        return player.isOnline() && sessionToken.equals(getStoredToken(player));
    }
}

// In QueueManager, add session cleanup on disconnect
@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    // Clear all queue entries for this player
    clearPlayerQueues(playerId);
}
```

**Priority Justification:**
- Only affects offline/cracked servers
- Online mode servers with Mojang auth are protected
- Still a security concern for private servers

**Estimated Effort:** 2-3 hours

---

### MEDIUM-002: Passwords Logged in Debug Mode
**Severity:** MEDIUM  
**CVSS Score:** 4.8

**Files Affected:**
- All files with logging
- Potentially `BankAccountManager.java`
- Potentially `BankPasswordListener.java`

**Issue:**
Need to verify passwords are never logged, even in debug mode.

**Required Check:**
```bash
# Search for potential password logging
grep -r "password" src/ | grep -i "log\|print\|debug"
```

**Recommended Fix:**
1. Audit all logging statements
2. Replace any password logs with sanitized versions:
```java
// BAD
plugin.getLogger().info("User login with password: " + password);

// GOOD
plugin.getLogger().info("User login attempt for account: " + account.getAccountId());
```

3. Add a code review guideline:
```java
/**
 * SECURITY: Never log sensitive data
 * - Passwords (plaintext or hashed)
 * - Session tokens
 * - Bank balances in error messages shown to other players
 */
```

**Priority Justification:**
- Could expose passwords in server logs
- Logs may be accessible to multiple admins
- Log files often stored insecurely

**Estimated Effort:** 1-2 hours (audit + fixes)

---

### MEDIUM-003: Missing Permission Re-checks in GUIs
**Severity:** MEDIUM  
**CVSS Score:** 4.3

**Files Affected:**
- `BankGUIListener.java`
- `ShopGUIListener.java`
- `AuctionGUIListener.java`
- All GUI listener classes

**Issue:**
GUIs may not re-check permissions on every action. Player could keep GUI open after permission removal.

**Attack Scenario:**
```
1. Player opens /bank with dztradehub.bank permission
2. Admin runs: /lp user Player permission unset dztradehub.bank
3. Player still has GUI open
4. Player can still interact with bank GUI
5. Unauthorized access until inventory closed
```

**Recommended Fix:**
```java
// Add to all GUI click handlers
@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
        return;
    }
    
    // Re-check permission on EVERY click
    if (!player.hasPermission("dztradehub.bank")) {
        player.closeInventory();
        player.sendMessage("§c✗ You no longer have permission to access this!");
        event.setCancelled(true);
        return;
    }
    
    // Process click...
}
```

**Files to Update:**
1. `BankGUIListener.java` - Check `dztradehub.bank`
2. `ShopGUIListener.java` - Check `dztradehub.use`
3. `AuctionGUIListener.java` - Check `dztradehub.auction`
4. `CasinoGUIListener.java` - Check `dztradehub.casino`
5. `BountyListener.java` - Check `dztradehub.bounty`
6. `SellGUIListener.java` - Check `dztradehub.use`
7. `AreaGUIListener.java` - Check `dztradehub.use`

**Priority Justification:**
- Window of vulnerability is small (until GUI closed)
- Only affects servers with dynamic permission changes
- Still important for proper security

**Estimated Effort:** 2-3 hours (7 files to update)

---

### MEDIUM-004: Async Startup Loading Blocks Main Thread
**Severity:** MEDIUM (Performance)  
**CVSS Score:** N/A (Performance, not security)

**File Affected:**
- `BankAccountManager.java` lines 34-52

**Issue:**
Initial data load happens synchronously on main thread during plugin enable, blocking server startup.

**Current Code:**
```java
public BankAccountManager(DZTradeHub plugin, BankManager bankManager) {
    this.plugin = plugin;
    this.bankManager = bankManager;
    // ...
    
    loadAccountsFromDatabase(); // BLOCKS MAIN THREAD
}
```

**Impact:**
- Server startup delays with many bank accounts
- "Server took too long to start" warnings
- Not a security issue but affects user experience

**Recommended Fix:**
```java
public BankAccountManager(DZTradeHub plugin, BankManager bankManager) {
    this.plugin = plugin;
    this.bankManager = bankManager;
    this.accountsById = new ConcurrentHashMap<>();
    this.accountsByPlayer = new ConcurrentHashMap<>();
    
    // Load asynchronously
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
        loadAccountsFromDatabase();
        plugin.getLogger().info("§aBank accounts loaded asynchronously");
    });
}

// Add a method to check if loading is complete
public boolean isLoaded() {
    return !accountsById.isEmpty() || hasCheckedDatabase;
}
```

**Priority Justification:**
- Performance issue, not security
- Only affects startup time
- Can wait for future optimization

**Estimated Effort:** 1 hour

---

## 🟡 LOW PRIORITY ISSUES

### LOW-001: Information Disclosure in Error Messages
**Severity:** LOW  
**CVSS Score:** 2.7

**Files Affected:**
- Multiple files with error handling
- All `catch` blocks that log exceptions

**Issue:**
Error messages may reveal internal information (database structure, file paths, stack traces) to players.

**Current Pattern:**
```java
catch (SQLException e) {
    plugin.getLogger().severe("Failed to save shop: " + e.getMessage());
    e.printStackTrace(); // Stack trace in console
}
```

**Potential Information Leaks:**
- Database table names
- Column names
- File system paths
- Java class structure

**Recommended Fix:**
```java
// Create a secure error handler utility
public class SecureErrorHandler {
    
    /**
     * Log error with full details server-side,
     * show generic message to player
     */
    public static void handleError(Plugin plugin, Player player, 
                                   String userMessage, Exception e) {
        // Full details in server log
        plugin.getLogger().severe("Error occurred: " + e.getClass().getName());
        plugin.getLogger().severe("Details: " + e.getMessage());
        e.printStackTrace();
        
        // Generic message to player
        if (player != null) {
            player.sendMessage("§c✗ " + userMessage);
            player.sendMessage("§7An error occurred. Please contact an administrator.");
        }
    }
}

// Usage
try {
    // Database operation
} catch (SQLException e) {
    SecureErrorHandler.handleError(plugin, player, 
        "Failed to save your data", e);
}
```

**Priority Justification:**
- Information disclosure only
- Doesn't allow exploitation
- Server logs already semi-public
- Low risk overall

**Estimated Effort:** 3-4 hours (many error handlers to update)

---

### LOW-002: No Audit Logging for Admin Actions
**Severity:** LOW  
**CVSS Score:** 2.3

**Files Affected:**
- All admin command handlers
- `TradeHubCommand.java`
- `BankCommand.java`

**Issue:**
Admin actions (bank creation, shop deletion, etc.) are not logged with actor information.

**Current Behavior:**
```java
// Admin deletes a bank
plugin.getLogger().info("Bank deleted: " + bankName);
// WHO deleted it? WHEN? WHY?
```

**Impact:**
- No accountability for admin actions
- Cannot trace who deleted a shop/bank
- Difficult to investigate abuse
- No audit trail for compliance

**Recommended Fix:**

1. Create audit logging system:
```java
public class AuditLogger {
    
    public static void logAdminAction(Plugin plugin, CommandSender sender, 
                                     String action, String target, String details) {
        String actor = sender.getName();
        UUID actorUUID = (sender instanceof Player) ? 
            ((Player) sender).getUniqueId() : null;
        long timestamp = System.currentTimeMillis();
        
        // Log to console
        plugin.getLogger().info(String.format(
            "[AUDIT] %s | Actor: %s (%s) | Action: %s | Target: %s | Details: %s",
            new java.util.Date(timestamp), actor, actorUUID, action, target, details
        ));
        
        // Store in database (optional)
        storeAuditLog(actorUUID, actor, action, target, details, timestamp);
    }
}
```

2. Add database table:
```sql
CREATE TABLE admin_audit_log (
    log_id VARCHAR(36) PRIMARY KEY,
    actor_uuid VARCHAR(36),
    actor_name VARCHAR(100),
    action_type VARCHAR(50),
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    details TEXT,
    timestamp BIGINT,
    INDEX idx_timestamp (timestamp),
    INDEX idx_actor (actor_uuid)
);
```

3. Use in commands:
```java
// In BankCommand.java
if (args[0].equalsIgnoreCase("delete")) {
    String bankName = args[1];
    
    // Perform deletion
    boolean success = bankManager.deleteBank(bankName);
    
    if (success) {
        // Audit log
        AuditLogger.logAdminAction(plugin, sender, 
            "BANK_DELETE", bankName, 
            "Bank deleted via command");
        
        sender.sendMessage("§aBank deleted: " + bankName);
    }
}
```

**Actions to Log:**
- Bank create/delete/rename/config
- Shop create/delete/rename
- Area create/delete/rename
- Item add/remove
- Permission changes (if done via plugin)
- Config reloads
- Database migrations

**Priority Justification:**
- Important for large servers
- Not critical for small servers
- More of a feature than security issue

**Estimated Effort:** 4-5 hours

---

## 💡 ADDITIONAL IMPROVEMENTS (Nice to Have)

### IMPROVEMENT-001: Enhanced Logging System
**Priority:** Optional

**Recommendation:**
- Implement log levels (DEBUG, INFO, WARN, ERROR)
- Add log rotation
- Separate security events into dedicated log file

**Implementation:**
```java
// Create SecurityLogger.java
public class SecurityLogger {
    private static final Logger secLog = Logger.getLogger("DZTradeHub-Security");
    
    public static void logSecurityEvent(String event, String details) {
        secLog.warning(String.format("[SECURITY] %s - %s", event, details));
    }
}

// Usage
SecurityLogger.logSecurityEvent("ACCOUNT_LOCKOUT", 
    "Account " + accountId + " locked after 3 failed attempts");
```

---

### IMPROVEMENT-002: Password Strength Meter
**Priority:** Optional

**Recommendation:**
Add password strength validation and feedback to players.

**Implementation:**
```java
public enum PasswordStrength {
    WEAK, MODERATE, STRONG, VERY_STRONG
}

public static PasswordStrength checkPasswordStrength(String password) {
    int score = 0;
    
    if (password.length() >= 8) score++;
    if (password.length() >= 12) score++;
    if (password.matches(".*[a-z].*")) score++;
    if (password.matches(".*[A-Z].*")) score++;
    if (password.matches(".*[0-9].*")) score++;
    if (password.matches(".*[!@#$%^&*].*")) score++;
    
    if (score <= 2) return WEAK;
    if (score <= 4) return MODERATE;
    if (score <= 5) return STRONG;
    return VERY_STRONG;
}
```

---

### IMPROVEMENT-003: Two-Factor Authentication (2FA)
**Priority:** Optional (Advanced Feature)

**Recommendation:**
Add optional 2FA for bank accounts.

**Implementation Ideas:**
- TOTP (Time-based One-Time Password) support
- Backup codes generation
- 2FA recovery system
- Optional, not mandatory

---

### IMPROVEMENT-004: Bank Transaction Export
**Priority:** Optional

**Recommendation:**
Allow players to export their transaction history.

**Features:**
- Export as CSV
- Filter by date range
- Filter by transaction type
- Include balance snapshots

---

### IMPROVEMENT-005: Automated Backup System
**Priority:** Recommended

**Recommendation:**
Add automatic database backups before critical operations.

**Implementation:**
```java
public class BackupManager {
    
    public static void backupBeforeCriticalOp(Plugin plugin, String operation) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
            .format(new Date());
        String backupName = "backup_" + operation + "_" + timestamp + ".db";
        
        // Copy database file
        File sourceDb = new File(plugin.getDataFolder(), "database.db");
        File backupDb = new File(plugin.getDataFolder(), "backups/" + backupName);
        
        Files.copy(sourceDb.toPath(), backupDb.toPath());
        plugin.getLogger().info("Backup created: " + backupName);
    }
}
```

---

### IMPROVEMENT-006: Rate Limiting for Commands
**Priority:** Optional

**Recommendation:**
Add rate limiting to prevent command spam.

**Implementation:**
```java
public class CommandRateLimiter {
    private final Map<UUID, Long> lastCommandTime = new ConcurrentHashMap<>();
    private final long cooldownMs = 1000; // 1 second
    
    public boolean canExecute(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastCommandTime.get(uuid);
        
        if (last != null && (now - last) < cooldownMs) {
            return false;
        }
        
        lastCommandTime.put(uuid, now);
        return true;
    }
}
```

---

### IMPROVEMENT-007: Database Connection Health Checks
**Priority:** Recommended

**Recommendation:**
Add periodic health checks for database connections.

**Implementation:**
```java
// In DatabaseManager
public void startHealthCheck() {
    plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                plugin.getLogger().warning("Database connection lost! Attempting reconnect...");
                reconnect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database health check failed: " + e.getMessage());
        }
    }, 6000L, 6000L); // Every 5 minutes
}
```

---

### IMPROVEMENT-008: Metrics & Monitoring
**Priority:** Optional

**Recommendation:**
Add metrics collection for monitoring plugin health.

**Metrics to Track:**
- Active bank accounts
- Transactions per minute
- Failed login attempts
- Queue wait times
- Database query times
- Error rates

**Implementation:**
Use bStats or similar metrics collection library.

---

## 📋 Implementation Priority Roadmap

### Phase 1 (Next Update)
1. ✅ CRITICAL issues - **DONE**
2. ✅ HIGH issues - **DONE**
3. 🟠 MEDIUM-003 - GUI permission re-checks (most impactful)
4. 🟠 MEDIUM-002 - Password logging audit (quick win)

### Phase 2 (Future Update)
5. 🟠 MEDIUM-001 - Queue session tokens
6. 🟠 MEDIUM-004 - Async loading optimization
7. 🟡 LOW-002 - Audit logging (good for compliance)

### Phase 3 (Quality of Life)
8. 🟡 LOW-001 - Error message sanitization
9. 💡 IMPROVEMENT-005 - Automated backups
10. 💡 IMPROVEMENT-007 - Database health checks

### Phase 4 (Advanced Features)
11. 💡 IMPROVEMENT-002 - Password strength meter
12. 💡 IMPROVEMENT-006 - Command rate limiting
13. 💡 IMPROVEMENT-001 - Enhanced logging
14. 💡 IMPROVEMENT-003 - 2FA (if requested)

---

## 🔧 Development Guidelines

### When Implementing Fixes:

1. **Always test locally first**
2. **Create backups before modifying database schema**
3. **Add unit tests for security-critical code**
4. **Document all changes in changelog**
5. **Review code for similar issues in other files**

### Code Review Checklist:

- [ ] No passwords logged
- [ ] All inputs sanitized
- [ ] Permissions checked on every action
- [ ] Errors handled gracefully
- [ ] Thread-safety considered
- [ ] Database transactions used where needed
- [ ] Backward compatibility maintained

---

## 📞 Support & Questions

If you need help implementing any of these fixes:

1. Check the security reports for detailed examples
2. Review the existing fixes in `PasswordUtil.java` and `BankAccountManager.java`
3. Follow OWASP security best practices
4. Test thoroughly on a dev server before production

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-08  
**Plugin Version:** 1.0.0 (Post-Critical-Fixes)
