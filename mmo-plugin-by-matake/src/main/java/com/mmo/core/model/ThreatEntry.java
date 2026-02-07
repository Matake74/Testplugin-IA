package com.mmo.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Représente les menaces accumulées sur un mob spécifique
 * Stocke la menace de chaque joueur et gère la décroissance
 */
public class ThreatEntry {

    private final UUID mobId;
    private final Map<UUID, Double> threatMap = new HashMap<>();

    public ThreatEntry(UUID mobId) {
        this.mobId = mobId;
    }

    public UUID getMobId() {
        return mobId;
    }

    /**
     * Ajoute de la menace pour un joueur
     * @param uuid UUID du joueur
     * @param amount Montant de menace à ajouter (peut être négatif pour réduire)
     */
    public void addThreat(UUID uuid, double amount) {
        threatMap.merge(uuid, amount, Double::sum);
        // Assurer que la menace ne descend jamais en dessous de 0
        threatMap.computeIfPresent(uuid, (id, value) -> Math.max(0, value));
    }

    /**
     * Supprime complètement la menace d'un joueur
     * @param uuid UUID du joueur
     */
    public void removeThreat(UUID uuid) {
        threatMap.remove(uuid);
    }

    /**
     * Réduit la menace de tous les joueurs (décroissance passive)
     * @param amount Montant à soustraire
     */
    public void decay(double amount) {
        threatMap.replaceAll((id, value) -> Math.max(0, value - amount));
    }

    /**
     * Récupère le joueur avec le plus de menace
     * @return UUID du joueur avec le plus de menace, ou null si aucune menace
     */
    public UUID getTopTarget() {
        return threatMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Récupère la menace d'un joueur spécifique
     * @param uuid UUID du joueur
     * @return Niveau de menace (0 si aucune menace)
     */
    public double getThreat(UUID uuid) {
        return threatMap.getOrDefault(uuid, 0.0);
    }

    /**
     * Récupère toutes les menaces actives
     * @return Map immuable des menaces par joueur
     */
    public Map<UUID, Double> getAllThreats() {
        return Collections.unmodifiableMap(threatMap);
    }

    /**
     * Vérifie si cette entrée n'a plus de menace active
     * @return true si toutes les menaces sont à 0 ou si aucun joueur n'est enregistré
     */
    public boolean isEmpty() {
        return threatMap.isEmpty() || 
               threatMap.values().stream().allMatch(v -> v <= 0);
    }

    /**
     * Réinitialise toutes les menaces
     */
    public void clear() {
        threatMap.clear();
    }
}
