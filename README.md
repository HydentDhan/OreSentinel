# 🚨 OreSentinel [v1.0.0]

A lightweight, enterprise-grade anti-xray and suspicious mining activity monitor for Minecraft 1.21.1 servers. Built native for hybrid platforms, it seamlessly tracks standard vanilla blocks alongside custom modded ores (e.g., Pixelmon, IndustrialCraft, or TechReborn) using high-resolution millisecond tracking, Y-level profiling, and light level verification.

---

## 🌟 Features

*   **Custom/Modded Ore Compatibility:** Tracks any block injected by external mods via native `NamespacedKey` registry lookups (e.g., `pixelmon:sapphire_ore`).
*   **High-Resolution Precision:** Tracks mining speeds using raw millisecond intervals to instantly catch fast-break cheats, macro bots (like Baritone), or vein-miner exploiters.
*   **Persistent Anti-Silk Touch Protection:** Manages manual player-placed blocks permanently across server restarts using a chunk-level `PersistentDataContainer`.
*   **Ghost-Key Cleanup:** Automatically listens for TNT, Creeper, or other environmental explosions to securely wipe dead tracking blocks, saving chunk file overhead.
*   **Y-Level & Light Verification:** Tracks both the height level and light level of every mined vein, instantly revealing players strip-mining in pitch darkness.
*   **Dual-Alert Delivery:** Synchronously broadcasts formatted color-coded alert payloads to a Discord Webhook channel and handles raw real-time staff alerts in the game chat.
*   **SQLite Database Ledger:** Automatically archives historical block-breaking transactions asynchronously without lowering your server’s TPS.

---

## 🛠️ Administrative Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/os reload` | Hot-swaps and reloads the `config.yml` on live servers. | `oresentinel.admin` | `op` |
| `/os history <player>` | Fetches the last 10 database logging records for a player. | `oresentinel.admin` | `op` |
| *Passive Node* | Grants staff access to see real-time in-game chat alerts. | `oresentinel.alerts` | `op` |
| *Passive Node* | Exempts high-ranking users/builders from triggering alerts. | `oresentinel.exempt` | `false` |

---

## ⚙️ Configuration File (`config.yml`)

```yaml
# OreSentinel Configuration File
last-announced-version: "1.0.0"
webhook-url: "YOUR_WEBHOOK_URL_HERE"

# Monitoring window criteria
check-interval-milliseconds: 60000
alert-cooldown-seconds: 30
enable-staff-chat-alerts: true

# List of tracked ores and maximum speed limits
monitored-ores:
  "minecraft:diamond_ore": 15
  "minecraft:deepslate_diamond_ore": 15
  "minecraft:ancient_debris": 10
  "minecraft:emerald_ore": 20
  "pixelmon:sapphire_ore": 12
  "pixelmon:ruby_ore": 12
  "pixelmon:crystal_ore": 10
