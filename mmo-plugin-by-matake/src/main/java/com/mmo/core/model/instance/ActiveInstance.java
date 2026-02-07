package com.mmo.core.model.instance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instance active en cours d'exécution
 * Thread-safe pour gérer les accès concurrents
 */
public class ActiveInstance {

    private final InstanceDef def;
    private final Set<UUID> UUIDs;
    private final Set<UUID> mobs;
    private final Map<String, Boolean> chestOpened;
    private final long createdAt;

    private UUID bossId;
    private boolean started;
    private boolean completed;
    private boolean lootPhase;

    private int startCountdownTaskId = -1;
    private int lootCountdownTaskId = -1;

    public ActiveInstance(InstanceDef def) {
        if (def == null) {
            throw new IllegalArgumentException("InstanceDef cannot be null");
        }

        this.def = def;
        // Utiliser ConcurrentHashMap pour thread-safety
        this.UUIDs = ConcurrentHashMap.newKeySet();
        this.mobs = ConcurrentHashMap.newKeySet();
        this.chestOpened = new ConcurrentHashMap<>();
        this.createdAt = System.currentTimeMillis();

        // Initialiser tous les coffres comme non ouverts
        for (ChestDef chest : def.getChests()) {
            chestOpened.put(chest.getId(), false);
        }
    }

    // ==================== Getters ====================

    public InstanceDef getDef() { return def; }
    public Set<UUID> getUUIDs() { return UUIDs; }
    public Set<UUID> getMobs() { return mobs; }
    public UUID getBossId() { return bossId; }
    public boolean isStarted() { return started; }
    public boolean isCompleted() { return completed; }
    public boolean isLootPhase() { return lootPhase; }
    public int getStartCountdownTaskId() { return startCountdownTaskId; }
    public int getLootCountdownTaskId() { return lootCountdownTaskId; }
    public long getCreatedAt() { return createdAt; }

    // ==================== Setters ====================

    public void setBossId(UUID bossId) { this.bossId = bossId; }
    public void setStarted(boolean started) { this.started = started; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setLootPhase(boolean lootPhase) { this.lootPhase = lootPhase; }
    public void setStartCountdownTaskId(int id) { this.startCountdownTaskId = id; }
    public void setLootCountdownTaskId(int id) { this.lootCountdownTaskId = id; }

    // ==================== Chest Management ====================

    public boolean isChestOpened(String chestId) {
        return chestOpened.getOrDefault(chestId, false);
    }

    public void setChestOpened(String chestId) {
        chestOpened.put(chestId, true);
    }

    public int getChestsOpenedCount() {
        return (int) chestOpened.values().stream().filter(opened -> opened).count();
    }

    // ==================== Utility Methods ====================

    public boolean isEmpty() {
        return UUIDs.isEmpty();
    }

    public int getUUIDCount() {
        return UUIDs.size();
    }

    public boolean isBossAlive() {
        return bossId != null;
    }

    public boolean allMobsDead() {
        return mobs.isEmpty() && bossId == null;
    }

    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - createdAt) / 1000;
    }

    public boolean hasUUID(UUID uuid) {
        return UUIDs.contains(uuid);
    }

    @Override
    public String toString() {
        return "ActiveInstance{" +
                "def=" + def.getId() +
                ", UUIDs=" + UUIDs.size() +
                ", mobs=" + mobs.size() +
                ", bossAlive=" + isBossAlive() +
                ", started=" + started +
                ", lootPhase=" + lootPhase +
                '}';
    }
}