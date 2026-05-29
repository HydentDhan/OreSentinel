package me.dhanraj.oresentinel;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class OreSentinelCommand implements CommandExecutor {
    private final OreSentinel plugin;

    public OreSentinelCommand(OreSentinel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /oresentinel reload OR /oresentinel history <player>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "[OreSentinel] Configuration reloaded successfully!");
            return true;
        }

        if (args[0].equalsIgnoreCase("history")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /oresentinel history <player>");
                return true;
            }

            String targetPlayer = args[1];
            sender.sendMessage(ChatColor.YELLOW + "Fetching recent mining history for " + targetPlayer + "...");

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                List<String> history = plugin.getDatabaseManager().getRecentHistory(targetPlayer, 10);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (history.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "No tracked mining history found for this player.");
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "--- Last 10 Ores Mined by " + targetPlayer + " ---");
                    for (String log : history) {
                        sender.sendMessage(ChatColor.WHITE + log);
                    }
                });
            });
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown argument. Use 'reload' or 'history'.");
        return true;
    }
}