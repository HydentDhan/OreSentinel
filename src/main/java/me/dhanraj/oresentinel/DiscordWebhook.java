package me.dhanraj.oresentinel;

import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class DiscordWebhook {

    public static void sendAlert(Plugin plugin, String url, String playerName, String oreName, int amount, long elapsedMs, int limitMs, int avgY, int avgLight) {
        if (url == null || url.isEmpty() || url.equals("YOUR_WEBHOOK_URL_HERE")) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Dynamic color based on how fast they mined.
                // Under 3 seconds = Dark Purple (Bot). Under 15 seconds = Orange (Highly Sus). Default = Red.
                int color = 16711680; // Default Red
                if (elapsedMs <= 3000) {
                    color = 7419530; // Dark Purple
                } else if (elapsedMs <= 15000) {
                    color = 16742144; // Orange
                }

                String jsonPayload = """
                        {
                          "embeds": [
                            {
                              "title": "🚨 Suspicious Mining Activity Detected",
                              "description": "Player **%s** mined **%d** of **%s**.\\n\\n**Time Taken:** %d ms / %d ms\\n**Average Y-Level:** %d\\n**Average Light Level:** %d\\n\\nPlease investigate.",
                              "color": %d
                            }
                          ]
                        }
                        """.formatted(playerName, amount, oreName, elapsedMs, limitMs, avgY, avgLight, color);

                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                client.send(request, HttpResponse.BodyHandlers.ofString());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to transfer payload to Discord: " + e.getMessage());
            }
        });
    }

    public static void sendChangelog(Plugin plugin, String url, String currentVersion) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String prevVer = plugin.getConfig().getString("changelog.previous-version.version", "Unknown");
                List<String> prevChanges = plugin.getConfig().getStringList("changelog.previous-version.changes");
                List<String> currChanges = plugin.getConfig().getStringList("changelog.current-version.changes");

                StringBuilder prevStr = new StringBuilder();
                for (String s : prevChanges) prevStr.append("\\n• ").append(s);

                StringBuilder currStr = new StringBuilder();
                for (String s : currChanges) currStr.append("\\n• ").append(s);

                String jsonPayload = """
                        {
                          "embeds": [
                            {
                              "title": "🚀 OreSentinel %s Released!",
                              "color": 3066993,
                              "fields": [
                                {
                                  "name": "🌟 New in %s",
                                  "value": "%s"
                                },
                                {
                                  "name": "📜 Previous (%s)",
                                  "value": "%s"
                                }
                              ]
                            }
                          ]
                        }
                        """.formatted(currentVersion, currentVersion, currStr.toString(), prevVer, prevStr.toString());

                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                client.send(request, HttpResponse.BodyHandlers.ofString());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to send changelog to Discord: " + e.getMessage());
            }
        });
    }
}