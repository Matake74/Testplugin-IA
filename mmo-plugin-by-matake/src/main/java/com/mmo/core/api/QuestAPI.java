package com.mmo.core.api;

import com.mmo.core.CorePlugin;
import com.mmo.core.managers.GroupQuestManager;
import com.mmo.core.managers.QuestManager;

import java.util.UUID;

/**
 * API publique pour gérer les quêtes solo et de groupe
 * Utilise des UUID pour identifier les joueurs
 */
public class QuestAPI {

    // -----------------------------
    //  QUÊTES SOLO
    // -----------------------------

    /**
     * Démarre une quête solo pour un joueur
     * @param uuid L'UUID du joueur qui démarre la quête
     * @param questId L'identifiant de la quête
     */
    public static void startSoloQuest(UUID uuid, String questId) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            qm.startSoloQuest(uuid, questId);
        }
    }

    /**
     * Complète l'étape actuelle d'une quête solo
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     */
    public static void completeSoloStage(UUID uuid, String questId) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            qm.completeSoloStage(uuid, questId);
        }
    }

    /**
     * Abandonne une quête solo
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     */
    public static void abandonSoloQuest(UUID uuid, String questId) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            qm.abandonQuest(uuid, questId);
        }
    }

    /**
     * Vérifie si un joueur a complété une quête
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     * @return true si la quête est complétée
     */
    public static boolean hasCompletedQuest(UUID uuid, String questId) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            return qm.hasCompletedQuest(uuid, questId);
        }
        return false;
    }

    /**
     * Récupère l'objectif actuel d'un joueur
     * @param uuid L'UUID du joueur
     * @return La description de l'objectif actuel ou null
     */
    public static String getCurrentObjective(UUID uuid) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            return qm.getCurrentObjective(uuid);
        }
        return null;
    }

    // -----------------------------
    //  QUÊTES DE GROUPE
    // -----------------------------

    /**
     * Démarre une quête de groupe (le leader doit être dans un groupe)
     * @param leaderId L'UUID du leader du groupe
     * @param questId L'identifiant de la quête
     */
    public static void startGroupQuest(UUID leaderId, String questId) {
        GroupQuestManager gm = getGroupQuestManager();
        if (gm != null) {
            gm.startGroupQuest(leaderId, questId);
        }
    }

    /**
     * Complète l'étape actuelle d'une quête de groupe
     * @param uuid L'UUID d'un membre du groupe
     * @param questId L'identifiant de la quête
     */
    public static void completeGroupStage(UUID uuid, String questId) {
        GroupQuestManager gm = getGroupQuestManager();
        if (gm != null) {
            gm.completeGroupStage(uuid, questId);
        }
    }

    /**
     * Abandonne une quête de groupe (seul le leader peut abandonner)
     * @param leaderId L'UUID du leader du groupe
     * @param questId L'identifiant de la quête
     */
    public static void abandonGroupQuest(UUID leaderId, String questId) {
        GroupQuestManager gm = getGroupQuestManager();
        if (gm != null) {
            gm.abandonGroupQuest(leaderId, questId);
        }
    }

    /**
     * Vérifie si un groupe a une quête active
     * @param uuid L'UUID d'un membre du groupe
     * @param questId L'identifiant de la quête
     * @return true si le groupe a cette quête active
     */
    public static boolean hasActiveGroupQuest(UUID uuid, String questId) {
        GroupQuestManager gm = getGroupQuestManager();
        if (gm != null) {
            return gm.hasActiveGroupQuest(uuid, questId);
        }
        return false;
    }

    // -----------------------------
    //  PROGRESSION
    // -----------------------------

    /**
     * Incrémente un compteur d'objectif pour une quête
     * Ex: tuer 10 mobs, collecter 5 items
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     * @param objectiveKey La clé de l'objectif
     * @param amount Montant à incrémenter
     */
    public static void incrementObjective(UUID uuid, String questId, String objectiveKey, int amount) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            qm.incrementObjective(uuid, questId, objectiveKey, amount);
        }
    }

    /**
     * Vérifie la progression d'un objectif spécifique
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     * @param objectiveKey La clé de l'objectif
     * @return La progression actuelle
     */
    public static int getObjectiveProgress(UUID uuid, String questId, String objectiveKey) {
        QuestManager qm = getQuestManager();
        if (qm != null) {
            return qm.getObjectiveProgress(uuid, questId, objectiveKey);
        }
        return 0;
    }

    // -----------------------------
    //  HELPERS
    // -----------------------------

    private static QuestManager getQuestManager() {
        CorePlugin plugin = CorePlugin.getInstance();
        return plugin != null ? plugin.getQuestManager() : null;
    }

    private static GroupQuestManager getGroupQuestManager() {
        CorePlugin plugin = CorePlugin.getInstance();
        return plugin != null ? plugin.getGroupQuestManager() : null;
    }
}
