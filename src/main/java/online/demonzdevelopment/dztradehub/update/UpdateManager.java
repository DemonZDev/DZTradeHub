package online.demonzdevelopment.dztradehub.update;

import online.demonzdevelopment.dztradehub.DZTradeHub;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpdateManager {
    
    private static final String GITHUB_API_URL = "https://api.github.com/repos/DemonZDev/DZTradeHub/releases";
    private static final String REPO_OWNER = "DemonZDev";
    private static final String REPO_NAME = "DZTradeHub";
    
    private final DZTradeHub plugin;
    private final Logger logger;
    private String latestVersion;
    private boolean autoUpdateEnabled;
    private List<ReleaseInfo> allReleases;
    
    public UpdateManager(DZTradeHub plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.autoUpdateEnabled = plugin.getConfig().getBoolean("update.auto-update", false);
        this.allReleases = new ArrayList<>();
        
        // Start periodic version check if enabled
        if (plugin.getConfig().getBoolean("update.check-on-start", true)) {
            checkForUpdates().thenAccept(hasUpdate -> {
                if (hasUpdate) {
                    logger.info("§e⚠ New version available: v" + latestVersion);
                    logger.info("§eCurrent version: v" + getCurrentVersion());
                    
                    if (autoUpdateEnabled) {
                        logger.info("§aAuto-update is enabled. Downloading update...");
                        downloadAndInstallUpdate(latestVersion, null);
                    }
                }
            });
        }
        
        startPeriodicCheck();
    }
    
    /**
     * Start periodic update checking
     */
    private void startPeriodicCheck() {
        int intervalMinutes = plugin.getConfig().getInt("update.check-interval-minutes", 60);
        if (intervalMinutes <= 0 || !plugin.getConfig().getBoolean("update.runtime-check", true)) {
            return;
        }
        
        long intervalTicks = intervalMinutes * 60 * 20L;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdates().thenAccept(hasUpdate -> {
                    if (hasUpdate && autoUpdateEnabled) {
                        logger.info("§e⚡ New version detected during runtime check!");
                        logger.info("§aAuto-downloading version v" + latestVersion);
                        downloadAndInstallUpdate(latestVersion, null);
                    }
                });
            }
        }.runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);
    }
    
    /**
     * Check for updates and fetch all releases
     */
    public CompletableFuture<Boolean> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                allReleases = fetchAllReleases();
                
                if (allReleases.isEmpty()) {
                    logger.warning("No releases found on GitHub");
                    return false;
                }
                
                // Get latest non-prerelease version
                ReleaseInfo latest = allReleases.stream()
                    .filter(r -> !r.isPrerelease)
                    .findFirst()
                    .orElse(null);
                
                if (latest != null) {
                    latestVersion = latest.version;
                    String currentVersion = getCurrentVersion();
                    return !currentVersion.equals(latestVersion) && isNewerVersion(latestVersion, currentVersion);
                }
            } catch (Exception e) {
                logger.warning("Failed to check for updates: " + e.getMessage());
            }
            return false;
        });
    }
    
    /**
     * Fetch all releases from GitHub
     */
    private List<ReleaseInfo> fetchAllReleases() throws IOException {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        if (conn.getResponseCode() != 200) {
            throw new IOException("GitHub API returned: " + conn.getResponseCode());
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return parseReleases(response.toString());
    }
    
    /**
     * Parse JSON releases
     */
    private List<ReleaseInfo> parseReleases(String json) {
        List<ReleaseInfo> releases = new ArrayList<>();
        
        // Simple JSON parsing (avoiding external dependencies)
        String[] releasesArray = json.substring(1, json.length() - 1).split("\\},\\{");
        
        for (String releaseJson : releasesArray) {
            try {
                ReleaseInfo info = new ReleaseInfo();
                
                // Extract tag_name
                int tagIndex = releaseJson.indexOf("\"tag_name\":");
                if (tagIndex != -1) {
                    int start = releaseJson.indexOf("\"", tagIndex + 11) + 1;
                    int end = releaseJson.indexOf("\"", start);
                    info.version = releaseJson.substring(start, end).replace("v", "");
                }
                
                // Extract download URL
                int assetsIndex = releaseJson.indexOf("\"browser_download_url\":");
                if (assetsIndex != -1) {
                    int start = releaseJson.indexOf("\"", assetsIndex + 23) + 1;
                    int end = releaseJson.indexOf("\"", start);
                    info.downloadUrl = releaseJson.substring(start, end);
                }
                
                // Check if prerelease
                info.isPrerelease = releaseJson.contains("\"prerelease\":true");
                
                if (info.version != null && info.downloadUrl != null) {
                    releases.add(info);
                }
            } catch (Exception e) {
                // Skip malformed release
            }
        }
        
        // Sort by version (newest first)
        releases.sort((a, b) -> compareVersions(b.version, a.version));
        
        return releases;
    }
    
    /**
     * Compare versions (semantic versioning)
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        
        return 0;
    }
    
    /**
     * Check if version1 is newer than version2
     */
    private boolean isNewerVersion(String v1, String v2) {
        return compareVersions(v1, v2) > 0;
    }
    
    /**
     * Get current version
     */
    public String getCurrentVersion() {
        return plugin.getDescription().getVersion();
    }
    
    /**
     * Get latest version
     */
    public String getLatestVersion() {
        return latestVersion != null ? latestVersion : getCurrentVersion();
    }
    
    /**
     * Update to latest version
     */
    public void updateToLatest(Player sender) {
        sender.sendMessage("§e⏳ Checking for latest version...");
        
        checkForUpdates().thenAccept(hasUpdate -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (hasUpdate) {
                    sender.sendMessage("§a✓ New version found: §ev" + latestVersion);
                    sender.sendMessage("§7  Current version: §ev" + getCurrentVersion());
                    sender.sendMessage("");
                    sender.sendMessage("§e⚡ Downloading and installing update...");
                    
                    downloadAndInstallUpdate(latestVersion, sender);
                } else {
                    sender.sendMessage("§a✓ You are running the latest version! §7(v" + getCurrentVersion() + ")");
                }
            });
        });
    }
    
    /**
     * Update to previous version
     */
    public void updateToPrevious(Player sender) {
        sender.sendMessage("§e⏳ Finding previous version...");
        
        CompletableFuture.runAsync(() -> {
            try {
                if (allReleases.isEmpty()) {
                    allReleases = fetchAllReleases();
                }
                
                String currentVersion = getCurrentVersion();
                ReleaseInfo previous = null;
                
                // Find the version before current
                for (int i = 0; i < allReleases.size(); i++) {
                    if (allReleases.get(i).version.equals(currentVersion)) {
                        if (i + 1 < allReleases.size()) {
                            previous = allReleases.get(i + 1);
                            break;
                        }
                    }
                }
                
                ReleaseInfo finalPrevious = previous;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (finalPrevious != null) {
                        sender.sendMessage("§a✓ Previous version found: §ev" + finalPrevious.version);
                        sender.sendMessage("§7  Current version: §ev" + currentVersion);
                        sender.sendMessage("");
                        sender.sendMessage("§e⚡ Downloading previous version...");
                        
                        downloadAndInstallUpdate(finalPrevious.version, sender);
                    } else {
                        sender.sendMessage("§c✗ No previous version found!");
                        sender.sendMessage("§7  You may already be on the oldest version.");
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§c✗ Failed to fetch versions: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Update to next version
     */
    public void updateToNext(Player sender) {
        sender.sendMessage("§e⏳ Finding next version...");
        
        CompletableFuture.runAsync(() -> {
            try {
                if (allReleases.isEmpty()) {
                    allReleases = fetchAllReleases();
                }
                
                String currentVersion = getCurrentVersion();
                ReleaseInfo next = null;
                
                // Find the version after current
                for (int i = 0; i < allReleases.size(); i++) {
                    if (allReleases.get(i).version.equals(currentVersion)) {
                        if (i - 1 >= 0) {
                            next = allReleases.get(i - 1);
                            break;
                        }
                    }
                }
                
                ReleaseInfo finalNext = next;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (finalNext != null) {
                        sender.sendMessage("§a✓ Next version found: §ev" + finalNext.version);
                        sender.sendMessage("§7  Current version: §ev" + currentVersion);
                        sender.sendMessage("");
                        sender.sendMessage("§e⚡ Downloading next version...");
                        
                        downloadAndInstallUpdate(finalNext.version, sender);
                    } else {
                        sender.sendMessage("§c✗ No next version found!");
                        sender.sendMessage("§7  You may already be on the latest version.");
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    sender.sendMessage("§c✗ Failed to fetch versions: " + e.getMessage());
                });
            }
        });
    }
    
    /**
     * Download and install update
     */
    private void downloadAndInstallUpdate(String version, Player sender) {
        CompletableFuture.runAsync(() -> {
            try {
                // Find release info for this version
                ReleaseInfo release = allReleases.stream()
                    .filter(r -> r.version.equals(version))
                    .findFirst()
                    .orElse(null);
                
                if (release == null || release.downloadUrl == null) {
                    throw new IOException("Download URL not found for version " + version);
                }
                
                // Download the JAR file
                URL url = new URL(release.downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "application/octet-stream");
                
                if (sender != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        sender.sendMessage("§7  Downloading from GitHub...")
                    );
                }
                
                File updateFolder = new File(plugin.getDataFolder().getParentFile(), "update");
                if (!updateFolder.exists()) {
                    updateFolder.mkdirs();
                }
                
                File downloadedFile = new File(updateFolder, "DZTradeHub.jar");
                
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(downloadedFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytes = 0;
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }
                    
                    if (sender != null) {
                        long finalTotalBytes = totalBytes;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            sender.sendMessage("§a✓ Downloaded " + (finalTotalBytes / 1024) + " KB successfully!");
                            sender.sendMessage("§e⚡ Installing update...");
                        });
                    }
                    
                    logger.info("Downloaded v" + version + " (" + totalBytes + " bytes)");
                }
                
                // The file is now in the update folder
                // Bukkit/Spigot/Paper will automatically move it on restart
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (sender != null) {
                        sender.sendMessage("");
                        sender.sendMessage("§a§l✓ UPDATE READY!");
                        sender.sendMessage("§7  Version §ev" + version + " §7has been downloaded");
                        sender.sendMessage("§7  Restart the server to complete installation");
                        sender.sendMessage("");
                        sender.sendMessage("§e  Use: §b/restart §eor §b/stop");
                    }
                    
                    logger.info("§a✓ Update ready! Restart server to install v" + version);
                    
                    // Notify all online admins
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("dztradehub.admin") && !p.equals(sender)) {
                            p.sendMessage("§6[TradeHub] §eUpdate v" + version + " is ready! Restart to install.");
                        }
                    }
                });
                
            } catch (Exception e) {
                logger.severe("Failed to download update: " + e.getMessage());
                e.printStackTrace();
                
                if (sender != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage("§c✗ Download failed: " + e.getMessage());
                        sender.sendMessage("§7  Manual download: §bhttps://github.com/" + REPO_OWNER + "/" + REPO_NAME + "/releases");
                    });
                }
            }
        });
    }
    
    /**
     * Toggle auto-update
     */
    public void toggleAutoUpdate(Player sender) {
        autoUpdateEnabled = !autoUpdateEnabled;
        plugin.getConfig().set("update.auto-update", autoUpdateEnabled);
        plugin.saveConfig();
        
        if (autoUpdateEnabled) {
            sender.sendMessage("§a✓ Auto-update enabled!");
            sender.sendMessage("§7  Checks every " + plugin.getConfig().getInt("update.check-interval-minutes", 60) + " minutes");
            sender.sendMessage("§7  New versions will be downloaded automatically");
        } else {
            sender.sendMessage("§c✗ Auto-update disabled!");
            sender.sendMessage("§7  You will need to update manually");
        }
    }
    
    /**
     * Disable auto-update
     */
    public void disableAutoUpdate(Player sender) {
        if (!autoUpdateEnabled) {
            sender.sendMessage("§7Auto-update is already disabled");
            return;
        }
        
        autoUpdateEnabled = false;
        plugin.getConfig().set("update.auto-update", false);
        plugin.saveConfig();
        
        sender.sendMessage("§c✗ Auto-update has been disabled");
        sender.sendMessage("§7  Use §e/dztradehub update auto §7to re-enable");
    }
    
    /**
     * Get auto-update status
     */
    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }
    
    /**
     * Release information holder
     */
    private static class ReleaseInfo {
        String version;
        String downloadUrl;
        boolean isPrerelease;
    }
}
