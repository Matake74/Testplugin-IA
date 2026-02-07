package com.mmo.core.managers;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.mmo.core.model.profession.*;
import java.util.*;

/**
 * Gestionnaire principal du système de professions
 */
public class ProfessionManager {

    private final JavaPlugin plugin;

    private final Map<ProfessionType, ProfessionDef> professions = new EnumMap<>(ProfessionType.class);
    private final Map<UUID, UUIDProfessionData> UUIDs = new HashMap<>();

    public ProfessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadProfessions();
    }

    /**
     * Charge les définitions de professions depuis la configuration
     */
    private void loadProfessions() {
        try {
            // TODO: Charger depuis config/professions.yml via l'API Hytale
            // Pour l'instant, définitions par défaut
            professions.put(ProfessionType.LUMBERJACK, 
                new ProfessionDef(ProfessionType.LUMBERJACK, "Bûcheron", 50, "linear:100"));
            professions.put(ProfessionType.MINER, 
                new ProfessionDef(ProfessionType.MINER, "Mineur", 50, "linear:100"));
            professions.put(ProfessionType.BLACKSMITH_WEAPON, 
                new ProfessionDef(ProfessionType.BLACKSMITH_WEAPON, "Forgeron d'Armes", 50, "exp:1.1:100"));
            professions.put(ProfessionType.BLACKSMITH_ARMOR, 
                new ProfessionDef(ProfessionType.BLACKSMITH_ARMOR, "Forgeron d'Armures", 50, "exp:1.1:100"));
            professions.put(ProfessionType.TAILOR, 
                new ProfessionDef(ProfessionType.TAILOR, "Tailleur", 50, "exp:1.1:100"));
            professions.put(ProfessionType.LEATHERWORKER, 
                new ProfessionDef(ProfessionType.LEATHERWORKER, "Travailleur du Cuir", 50, "exp:1.1:100"));
            professions.put(ProfessionType.FARMER, 
                new ProfessionDef(ProfessionType.FARMER, "Fermier", 50, "linear:100"));
            professions.put(ProfessionType.COOK, 
                new ProfessionDef(ProfessionType.COOK, "Cuisinier", 50, "exp:1.1:100"));
            professions.put(ProfessionType.JEWELER, 
                new ProfessionDef(ProfessionType.JEWELER, "Joaillier", 50, "exp:1.15:150"));
            
            plugin.getLogger().info("[ProfessionManager] " + professions.size() + " professions chargées.");
        } catch (Exception e) {
            plugin.getLogger().error("[ProfessionManager] Erreur lors du chargement des professions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde les données avant l'arrêt
     */
    public void shutdown() {
        // TODO: Sauvegarder les données des joueurs
        plugin.getLogger().info("[ProfessionManager] Arrêt du gestionnaire de professions.");
    }

    /**
     * Récupère ou crée les données de profession d'un joueur
     */
    private UUIDProfessionData getData(UUID uuid) {
        return UUIDs.computeIfAbsent(uuid, UUIDProfessionData::new);
    }

    // ==================== API PUBLIQUE ====================

    public int getLevel(UUID uuid, ProfessionType type) {
        return getData(uuid).getLevel(type);
    }

    public double getXp(UUID uuid, ProfessionType type) {
        return getData(uuid).getXp(type);
    }

    public void setLevel(UUID uuid, ProfessionType type, int level) {
        UUIDProfessionData data = getData(uuid);
        data.setLevel(type, level);
        data.setXp(type, 0.0);
    }

    /**
     * Ajoute de l'XP et gère les montées de niveau
     */
    public void addXp(UUID uuid, ProfessionType type, double amount) {
        UUIDProfessionData data = getData(uuid);
        ProfessionDef def = professions.get(type);
        if (def == null) return;

        data.addXp(type, amount);
        
        // TODO: Envoyer un message au joueur via l'API Hytale
        // UUID.sendMessage("[Métiers] +" + (int) amount + " XP en " + def.getName());

        // Gérer les montées de niveau
        while (true) {
            int level = data.getLevel(type);
            if (level >= def.getMaxLevel()) break;

            double needed = getXpRequired(def, level);
            if (data.getXp(type) < needed) break;

            data.addXp(type, -needed);
            data.setLevel(type, level + 1);
            
            // TODO: Envoyer message de level up
            // UUID.sendMessage("[Métiers] Votre métier " + def.getName() + " passe niveau " + (level + 1) + ".");
        }
    }

    /**
     * Calcule l'XP requise pour le prochain niveau
     */
    private double getXpRequired(ProfessionDef def, int level) {
        String curve = def.getXpCurve();
        if (curve.startsWith("linear:")) {
            String[] parts = curve.split(":");
            double base = Double.parseDouble(parts[1]);
            return base * level;
        }
        if (curve.startsWith("exp:")) {
            String[] parts = curve.split(":");
            double factor = Double.parseDouble(parts[1]);
            double base = Double.parseDouble(parts[2]);
            return base * Math.pow(factor, level - 1);
        }
        return 100 * level;
    }

    public boolean hasLevel(UUID uuid, ProfessionType type, int required) {
        return getLevel(uuid, type) >= required;
    }

    public ProfessionDef getProfession(ProfessionType type) {
        return professions.get(type);
    }

    // ==================== RECETTES ====================

    public boolean canCraft(UUID uuid, String recipeId) {
        // TODO: Vérifier via RecipeManager
        return hasRecipe(uuid, recipeId);
    }

    public void unlockRecipe(UUID uuid, String recipeId) {
        getData(uuid).unlockRecipe(recipeId);
    }

    public boolean hasRecipe(UUID uuid, String recipeId) {
        return getData(uuid).hasRecipe(recipeId);
    }

    // ==================== BONUS ====================

    /**
     * Calcule le bonus de récolte basé sur le niveau
     * @return Multiplicateur (1.0 = pas de bonus, 1.5 = +50%, etc.)
     */
    public double getGatheringBonus(UUID uuid, ProfessionType type) {
        int level = getLevel(uuid, type);
        // +2% par niveau, max +100% au niveau 50
        return 1.0 + (level * 0.02);
    }

    /**
     * Calcule le bonus de qualité de craft
     * @return Multiplicateur de qualité
     */
    public double getCraftingQualityBonus(UUID uuid, ProfessionType type) {
        int level = getLevel(uuid, type);
        // +1% par niveau, max +50% au niveau 50
        return 1.0 + (level * 0.01);
    }

    /**
     * Tire au sort pour une double récolte
     * @return true si le joueur obtient une double récolte
     */
    public boolean rollDoubleGather(UUID uuid, ProfessionType type) {
        int level = getLevel(uuid, type);
        // 1% de chance par niveau, max 50%
        double chance = Math.min(level * 0.01, 0.5);
        return Math.random() < chance;
    }

    // ==================== STATISTIQUES ====================

    public int getTotalCrafts(UUID uuid, ProfessionType type) {
        return getData(uuid).getTotalCrafts(type);
    }

    public int getTotalGathers(UUID uuid, ProfessionType type) {
        return getData(uuid).getTotalGathers(type);
    }

    public void incrementCrafts(UUID uuid, ProfessionType type) {
        getData(uuid).incrementCrafts(type);
    }

    public void incrementGathers(UUID uuid, ProfessionType type) {
        getData(uuid).incrementGathers(type);
    }
}
