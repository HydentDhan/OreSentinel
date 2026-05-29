package me.dhanraj.oresentinel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OreListener implements Listener {

    private final OreSentinel plugin;
    private final Map<UUID, MiningData> playerMiningData;

    public OreListener(OreSentinel plugin) {
        this.plugin = plugin;
        this.playerMiningData = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        markAsPlayerPlaced(event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Exemption check for Admins and Builders
        if (player.hasPermission("oresentinel.exempt")) {
            return;
        }

        Block block = event.getBlock();

        if (isPlayerPlaced(block)) {
            removePlayerPlacedStatus(block);
            return;
        }

        String legacyName = block.getType().name();
        String namespacedKey = block.getType().getKey().toString();

        ConfigurationSection monitoredOres = plugin.getConfig().getConfigurationSection("monitored-ores");
        if (monitoredOres == null) return;

        String matchedKey = null;
        if (monitoredOres.contains(namespacedKey)) {
            matchedKey = namespacedKey;
        } else if (monitoredOres.contains(legacyName)) {
            matchedKey = legacyName;
        }

        if (matchedKey != null) {
            int yLevel = block.getY();
            int lightLevel = block.getLightLevel();

            plugin.getDatabaseManager().logMining(player.getUniqueId(), player.getName(), matchedKey, yLevel);

            UUID playerId = player.getUniqueId();
            int timeLimitMs = plugin.getConfig().getInt("check-interval-milliseconds");
            int maxAllowed = monitoredOres.getInt(matchedKey);
            int cooldownSeconds = plugin.getConfig().getInt("alert-cooldown-seconds");

            playerMiningData.putIfAbsent(playerId, new MiningData());
            MiningData data = playerMiningData.get(playerId);

            if (data.isCooldownActive()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - data.getStartTime() > timeLimitMs) {
                data.reset();
            }

            data.addOre(matchedKey, yLevel, lightLevel);
            int currentCount = data.getOreCount(matchedKey);

            if (currentCount >= maxAllowed) {
                data.setAlertCooldown(cooldownSeconds);

                int avgY = data.getAverageY(matchedKey);
                int avgLight = data.getAverageLight(matchedKey);
                long elapsedMs = System.currentTimeMillis() - data.getStartTime();

                String webhookUrl = plugin.getConfig().getString("webhook-url");
                DiscordWebhook.sendAlert(plugin, webhookUrl, player.getName(), matchedKey, currentCount, elapsedMs, timeLimitMs, avgY, avgLight);

                if (plugin.getConfig().getBoolean("enable-staff-chat-alerts")) {
                    String alertMessage = ChatColor.RED + "[OreSentinel] " + ChatColor.YELLOW + player.getName() +
                            ChatColor.WHITE + " broke " + ChatColor.LIGHT_PURPLE + currentCount + "x " + matchedKey +
                            ChatColor.WHITE + " in " + ChatColor.GREEN + elapsedMs + "ms" +
                            ChatColor.GRAY + " (Avg Y: " + avgY + " | Lgt: " + avgLight + ")" + ChatColor.WHITE + "!";

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("oresentinel.alerts")) {
                            onlinePlayer.sendMessage(alertMessage);
                        }
                    }
                }

                data.reset();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            removePlayerPlacedStatus(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            removePlayerPlacedStatus(block);
        }
    }

    private void markAsPlayerPlaced(Block block) {
        NamespacedKey key = new NamespacedKey(plugin, "p_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        block.getChunk().getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }

    private boolean isPlayerPlaced(Block block) {
        NamespacedKey key = new NamespacedKey(plugin, "p_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        return block.getChunk().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    private void removePlayerPlacedStatus(Block block) {
        NamespacedKey key = new NamespacedKey(plugin, "p_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        block.getChunk().getPersistentDataContainer().remove(key);
    }
}