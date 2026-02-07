package com.mmo.core.api;

import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.managers.HudManager;
import com.mmo.core.model.hud.HudElement;
import com.mmo.core.model.hud.HudLayer;
import java.util.UUID;

/**
 * API publique pour gérer l'affichage HUD (interface utilisateur)
 * Utilise des UUID au lieu de UUID objects
 */
public class HudAPI {

    // -----------------------------
    //  GESTION DES ÉLÉMENTS HUD
    // -----------------------------

    /**
     * Définit un élément HUD sur un layer spécifique pour un joueur
     * @param uuid L'UUID du joueur
     * @param layer Le layer HUD (TOP, MIDDLE, BOTTOM, etc.)
     * @param element L'élément à afficher
     */
    public static void setHud(UUID uuid, HudLayer layer, HudElement element) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.setHud(uuid, layer, element);
        }
    }

    /**
     * Efface un layer HUD pour un joueur
     * @param uuid L'UUID du joueur
     * @param layer Le layer à effacer
     */
    public static void clearHud(UUID uuid, HudLayer layer) {R
        HudManager hm = getManager();
        if (hm != null) {
            hm.clearHud(uuid, layer);
        }
    }

    /**
     * Efface tous les layers HUD pour un joueur
     * @param uuid L'UUID du joueur
     */
    public static void clearAllHud(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.clearAllHud(uuid);
        }
    }

    /**
     * Récupère l'élément HUD actuel d'un layer
     * @param uuid L'UUID du joueur
     * @param layer Le layer
     * @return L'élément HUD ou null
     */
    public static HudElement getHud(UUID uuid, HudLayer layer) {
        HudManager hm = getManager();
        return hm != null ? hm.getHud(uuid, layer) : null;
    }

    // -----------------------------
    //  HUD RAPIDES (HELPERS)
    // -----------------------------

    /**
     * Affiche un message temporaire en haut de l'écran
     * @param uuid L'UUID du joueur
     * @param message Le message à afficher
     * @param durationSeconds Durée d'affichage en secondes
     */
    public static void showTopMessage(UUID uuid, String message, int durationSeconds) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showTopMessage(uuid, message, durationSeconds);
        }
    }

    /**
     * Affiche un message de combat (dégâts, soins, etc.)
     * @param uuid L'UUID du joueur
     * @param message Le message de combat
     */
    public static void showCombatText(UUID uuid, String message) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showCombatText(uuid, message);
        }
    }

    /**
     * Affiche une barre de progression (quête, chargement, etc.)
     * @param uuid L'UUID du joueur
     * @param label Le label de la barre
     * @param current Valeur actuelle
     * @param max Valeur maximale
     */
    public static void showProgressBar(UUID uuid, String label, int current, int max) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showProgressBar(uuid, label, current, max);
        }
    }

    /**
     * Cache la barre de progression
     * @param uuid L'UUID du joueur
     */
    public static void hideProgressBar(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.hideProgressBar(uuid);
        }
    }

    // -----------------------------
    //  HUD DE GROUPE
    // -----------------------------

    /**
     * Affiche les informations du groupe (membres, santé, etc.)
     * @param uuid L'UUID du joueur
     */
    public static void showPartyHud(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showPartyHud(uuid);
        }
    }

    /**
     * Cache les informations du groupe
     * @param uuid L'UUID du joueur
     */
    public static void hidePartyHud(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.hidePartyHud(uuid);
        }
    }

    /**
     * Met à jour les informations du groupe
     * @param uuid L'UUID du joueur
     */
    public static void updatePartyHud(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.updatePartyHud(uuid);
        }
    }

    // -----------------------------
    //  HUD DE QUÊTE
    // -----------------------------

    /**
     * Affiche l'objectif de quête actuel
     * @param uuid L'UUID du joueur
     */
    public static void showQuestObjective(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showQuestObjective(uuid);
        }
    }

    /**
     * Cache l'objectif de quête
     * @param uuid L'UUID du joueur
     */
    public static void hideQuestObjective(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.hideQuestObjective(uuid);
        }
    }

    /**
     * Met à jour l'objectif de quête
     * @param uuid L'UUID du joueur
     * @param questId L'identifiant de la quête
     */
    public static void updateQuestObjective(UUID uuid, String questId) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.updateQuestObjective(uuid, questId);
        }
    }

    // -----------------------------
    //  HUD DE BOSS
    // -----------------------------

    /**
     * Affiche la barre de santé d'un boss
     * @param uuid L'UUID du joueur
     * @param bossName Le nom du boss
     * @param currentHealth Santé actuelle
     * @param maxHealth Santé maximale
     */
    public static void showBossBar(UUID uuid, String bossName, double currentHealth, double maxHealth) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showBossBar(uuid, bossName, currentHealth, maxHealth);
        }
    }

    /**
     * Met à jour la barre de santé du boss
     * @param uuid L'UUID du joueur
     * @param currentHealth Santé actuelle
     * @param maxHealth Santé maximale
     */
    public static void updateBossBar(UUID uuid, double currentHealth, double maxHealth) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.updateBossBar(uuid, currentHealth, maxHealth);
        }
    }

    /**
     * Cache la barre de santé du boss
     * @param uuid L'UUID du joueur
     */
    public static void hideBossBar(UUID uuid) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.hideBossBar(uuid);
        }
    }

    // -----------------------------
    //  NOTIFICATIONS
    // -----------------------------

    /**
     * Affiche une notification dans un coin de l'écran
     * @param uuid L'UUID du joueur
     * @param title Le titre de la notification
     * @param message Le message
     */
    public static void showNotification(UUID uuid, String title, String message) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showNotification(uuid, title, message);
        }
    }

    /**
     * Affiche une notification de succès/achievement
     * @param uuid L'UUID du joueur
     * @param achievement Le nom du succès
     */
    public static void showAchievement(UUID uuid, String achievement) {
        HudManager hm = getManager();
        if (hm != null) {
            hm.showAchievement(uuid, achievement);
        }
    }

    // -----------------------------
    //  HELPER
    // -----------------------------

    private static HudManager getManager() {
        CorePlugin plugin = CorePlugin.getInstance();
        return plugin != null ? plugin.getHudManager() : null;
    }
}
