package com.mmo.core.managers;

import com.hytale.server.entity.LivingEntity;
import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.model.ThreatEntry;

import java.util.*;

/**
 * Gestionnaire du système de menaces (aggro/threat) pour les mobs
 * Intégré dans CorePlugin, cohérent avec ThreatAPI
 */
public class ThreatManager {

    private final CorePlugin plugin;

    // Stockage des menaces par mob UUID
    private final Map<UUID, ThreatEntry> threats = new HashMap<>();

    // Paramètres configurables
    private final double baseThreatMultiplier;
    private final double healThreatMultiplier;
    private final double tauntBonusThreat;
    private final double blockThreatMultiplier;
    private final double decayPerSecond;

    public ThreatManager(CorePlugin plugin) {
        this.plugin = plugin;

        // Charger la configuration depuis le plugin
        // Pour l'instant, valeurs par défaut
        this.baseThreatMultiplier = 1.0;
        this.healThreatMultiplier = 0.5;
        this.tauntBonusThreat = 200.0;
        this.blockThreatMultiplier = 1.5;
        this.decayPerSecond = 2.0;

        // Démarrer la tâche de décroissance automatique
        startDecayTask();
        
        plugin.getLogger().info("[ThreatManager] Initialisé avec succès.");
    }

    /**
     * Démarre la tâche périodique de décroissance des menaces
     * Toutes les secondes, réduit toutes les menaces actives
     */
    private void startDecayTask() {
        // TODO: Adapter selon l'API Scheduler de Hytale
        // Exemple conceptuel :
        // plugin.getServer().getScheduler().runRepeating(() -> {
        //     for (ThreatEntry entry : threats.values()) {
        //         entry.decay(decayPerSecond);
        //     }
        //     threats.entrySet().removeIf(e -> e.getValue().isEmpty());
        // }, 20, 20); // 20 ticks = 1 seconde
    }

    /**
     * Arrêt propre du manager
     */
    public void shutdown() {
        threats.clear();
        plugin.getLogger().info("[ThreatManager] Arrêté.");
    }

    /**
     * Récupère ou crée une entrée de menace pour un mob
     */
    private ThreatEntry getOrCreate(LivingEntity mob) {
        return threats.computeIfAbsent(mob.getUuid(), id -> new ThreatEntry(id));
    }

    /**
     * Ajoute de la menace basée sur les dégâts infligés
     */
    public void addDamageThreat(LivingEntity mob, UUID UUID, double damage) {
        ThreatEntry entry = getOrCreate(mob);
        entry.addThreat(UUID.getUuid(), damage * baseThreatMultiplier);
        updateTarget(mob, entry);
    }

    /**
     * Ajoute de la menace basée sur les soins effectués
     * Les soins génèrent généralement 50% de menace par rapport aux dégâts
     */
    public void addHealThreat(LivingEntity mob, UUID healer, double healAmount) {
        ThreatEntry entry = getOrCreate(mob);
        entry.addThreat(healer.getUuid(), healAmount * healThreatMultiplier);
        updateTarget(mob, entry);
    }

    /**
     * Ajoute de la menace basée sur les dégâts bloqués (tank)
     */
    public void addBlockThreat(LivingEntity mob, UUID tank, double blockedDamage) {
        ThreatEntry entry = getOrCreate(mob);
        entry.addThreat(tank.getUuid(), blockedDamage * blockThreatMultiplier);
        updateTarget(mob, entry);
    }

    /**
     * Force un mob à cibler un joueur (provocation/taunt)
     */
    public void taunt(LivingEntity mob, UUID tank) {
        ThreatEntry entry = getOrCreate(mob);
        entry.addThreat(tank.getUuid(), tauntBonusThreat);
        updateTarget(mob, entry);
    }

    /**
     * Récupère le niveau de menace actuel d'un joueur sur un mob
     */
    public double getThreat(LivingEntity mob, UUID UUID) {
        ThreatEntry entry = threats.get(mob.getUuid());
        if (entry == null) return 0.0;
        return entry.getThreat(UUID.getUuid());
    }

    /**
     * Obtient le joueur avec le plus de menace sur un mob
     */
    public UUID getTopThreat(LivingEntity mob) {
        ThreatEntry entry = threats.get(mob.getUuid());
        if (entry == null) return null;

        UUID topuuid = entry.getTopTarget();
        if (topuuid == null) return null;

        // TODO: Adapter selon l'API UUID de Hytale
        // return plugin.getServer().getUUIDManager().getUUID(topuuid);
        return null; // Placeholder
    }

    /**
     * Réinitialise toute la menace d'un mob
     */
    public void resetThreat(LivingEntity mob) {
        threats.remove(mob.getUuid());
        // Optionnel : réinitialiser la cible du mob
        mob.setTarget(null);
    }

    /**
     * Supprime la menace d'un joueur spécifique sur un mob
     */
    public void removeThreat(LivingEntity mob, UUID UUID) {
        ThreatEntry entry = threats.get(mob.getUuid());
        if (entry != null) {
            entry.removeThreat(UUID.getUuid());
            updateTarget(mob, entry);
        }
    }

    /**
     * Réduit la menace d'un joueur d'un certain pourcentage
     */
    public void reduceThreat(LivingEntity mob, UUID UUID, double percentage) {
        ThreatEntry entry = threats.get(mob.getUuid());
        if (entry != null) {
            double currentThreat = entry.getThreat(UUID.getUuid());
            double reduction = currentThreat * Math.min(1.0, Math.max(0.0, percentage));
            entry.addThreat(UUID.getUuid(), -reduction);
            updateTarget(mob, entry);
        }
    }

    /**
     * Met à jour la cible du mob en fonction de la menace la plus élevée
     */
    private void updateTarget(LivingEntity mob, ThreatEntry entry) {
        UUID topuuid = entry.getTopTarget();
        if (topuuid == null) {
            mob.setTarget(null);
            return;
        }

        // TODO: Adapter selon l'API UUID de Hytale
        // UUID target = plugin.getServer().getUUIDManager().getUUID(topuuid);
        // if (target != null) {
        //     mob.setTarget(target);
        // }
    }

    /**
     * Récupère toutes les menaces d'un mob
     * Utile pour l'affichage HUD
     */
    public Map<UUID, Double> getAllThreats(LivingEntity mob) {
        ThreatEntry entry = threats.get(mob.getUuid());
        if (entry == null) return Collections.emptyMap();
        return entry.getAllThreats();
    }
}
