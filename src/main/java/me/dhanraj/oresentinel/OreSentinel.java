package me.dhanraj.oresentinel;

import org.bukkit.plugin.java.JavaPlugin;

public class OreSentinel extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize SQLite Database
        this.databaseManager = new DatabaseManager(this);

        // Register Command
        if (getCommand("oresentinel") != null) {
            getCommand("oresentinel").setExecutor(new OreSentinelCommand(this));
        }

        // Register Event Listener
        getServer().getPluginManager().registerEvents(new OreListener(this), this);

        // Check for version update and send Discord Changelog
        String currentVersion = getDescription().getVersion();
        String lastVersion = getConfig().getString("last-announced-version", "0.0.0");

        if (!currentVersion.equals(lastVersion)) {
            String webhookUrl = getConfig().getString("webhook-url");
            if (webhookUrl != null && !webhookUrl.isEmpty() && !webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
                DiscordWebhook.sendChangelog(this, webhookUrl, currentVersion);
                getConfig().set("last-announced-version", currentVersion);
                saveConfig();
            }
        }

        getLogger().info("OreSentinel v" + currentVersion + " Release online. All security protocols active.");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
        getLogger().info("OreSentinel Framework powered down cleanly.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}