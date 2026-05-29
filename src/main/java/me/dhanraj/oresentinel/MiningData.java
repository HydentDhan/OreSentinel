package me.dhanraj.oresentinel;

import java.util.HashMap;
import java.util.Map;

public class MiningData {
    private long startTime;
    private long alertCooldownExpires;
    private final Map<String, Integer> oreCounts;
    private final Map<String, Integer> oreYSum;
    private final Map<String, Integer> oreLightSum;

    public MiningData() {
        this.startTime = System.currentTimeMillis();
        this.alertCooldownExpires = 0;
        this.oreCounts = new HashMap<>();
        this.oreYSum = new HashMap<>();
        this.oreLightSum = new HashMap<>();
    }

    public void addOre(String oreIdentifier, int yLevel, int lightLevel) {
        oreCounts.put(oreIdentifier, oreCounts.getOrDefault(oreIdentifier, 0) + 1);
        oreYSum.put(oreIdentifier, oreYSum.getOrDefault(oreIdentifier, 0) + yLevel);
        oreLightSum.put(oreIdentifier, oreLightSum.getOrDefault(oreIdentifier, 0) + lightLevel);
    }

    public int getOreCount(String oreIdentifier) {
        return oreCounts.getOrDefault(oreIdentifier, 0);
    }

    public int getAverageY(String oreIdentifier) {
        int count = getOreCount(oreIdentifier);
        if (count == 0) return 0;
        return oreYSum.getOrDefault(oreIdentifier, 0) / count;
    }

    public int getAverageLight(String oreIdentifier) {
        int count = getOreCount(oreIdentifier);
        if (count == 0) return 0;
        return oreLightSum.getOrDefault(oreIdentifier, 0) / count;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isCooldownActive() {
        return System.currentTimeMillis() < alertCooldownExpires;
    }

    public void setAlertCooldown(int cooldownSeconds) {
        this.alertCooldownExpires = System.currentTimeMillis() + (cooldownSeconds * 1000L);
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.oreCounts.clear();
        this.oreYSum.clear();
        this.oreLightSum.clear();
    }
}