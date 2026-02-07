package com.mmo.core.api;

import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.managers.ProfessionManager;
import com.mmo.core.model.profession.ProfessionType;
import java.util.UUID;

/**
 * API publique pour gérer les professions et métiers
 * Utilise des UUID au lieu de UUID objects
 */
public class ProfessionAPI {

    // -----------------------------
    //  EXPÉRIENCE ET NIVEAUX
    // -----------------------------

    /**
     * Ajoute de l'expérience à une profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @param amount Montant d'XP à ajouter
     */
    public static void addXp(UUID uuid, ProfessionType type, double amount) {
        ProfessionManager pm = getManager();
        if (pm != null) {
            pm.addXp(uuid, type, amount);
        }
    }

    /**
     * Récupère le niveau actuel d'une profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return Le niveau de la profession
     */
    public static int getLevel(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getLevel(uuid, type) : 0;
    }

    /**
     * Vérifie si un joueur a le niveau requis dans une profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @param required Le niveau requis
     * @return true si le joueur a le niveau requis ou supérieur
     */
    public static boolean hasLevel(UUID uuid, ProfessionType type, int required) {
        ProfessionManager pm = getManager();
        return pm != null && pm.hasLevel(uuid, type, required);
    }

    /**
     * Récupère l'expérience actuelle d'une profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return L'expérience actuelle
     */
    public static double getXp(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getXp(uuid, type) : 0.0;
    }

    /**
     * Définit le niveau d'une profession (admin/debug)
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @param level Le niveau à définir
     */
    public static void setLevel(UUID uuid, ProfessionType type, int level) {
        ProfessionManager pm = getManager();
        if (pm != null) {
            pm.setLevel(uuid, type, level);
        }
    }

    // -----------------------------
    //  RECETTES ET CRAFTING
    // -----------------------------

    /**
     * Vérifie si un joueur peut crafter une recette
     * @param uuid L'UUID du joueur
     * @param recipeId L'identifiant de la recette
     * @return true si le joueur peut crafter
     */
    public static boolean canCraft(UUID uuid, String recipeId) {
        ProfessionManager pm = getManager();
        return pm != null && pm.canCraft(uuid, recipeId);
    }

    /**
     * Débloque une recette pour un joueur
     * @param uuid L'UUID du joueur
     * @param recipeId L'identifiant de la recette
     */
    public static void unlockRecipe(UUID uuid, String recipeId) {
        ProfessionManager pm = getManager();
        if (pm != null) {
            pm.unlockRecipe(uuid, recipeId);
        }
    }

    /**
     * Vérifie si un joueur a débloqué une recette
     * @param uuid L'UUID du joueur
     * @param recipeId L'identifiant de la recette
     * @return true si la recette est débloquée
     */
    public static boolean hasRecipe(UUID uuid, String recipeId) {
        ProfessionManager pm = getManager();
        return pm != null && pm.hasRecipe(uuid, recipeId);
    }

    // -----------------------------
    //  BONUS DE PROFESSION
    // -----------------------------

    /**
     * Calcule le bonus de récolte basé sur le niveau de profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return Le multiplicateur de bonus (1.0 = pas de bonus)
     */
    public static double getGatheringBonus(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getGatheringBonus(uuid, type) : 1.0;
    }

    /**
     * Calcule le bonus de qualité de craft basé sur le niveau
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return Le multiplicateur de qualité (1.0 = qualité de base)
     */
    public static double getCraftingQualityBonus(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getCraftingQualityBonus(uuid, type) : 1.0;
    }

    /**
     * Vérifie si le joueur a une chance de double récolte
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return true si le joueur obtient une double récolte
     */
    public static boolean rollDoubleGather(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null && pm.rollDoubleGather(uuid, type);
    }

    // -----------------------------
    //  STATISTIQUES
    // -----------------------------

    /**
     * Récupère le nombre total de crafts effectués dans une profession
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return Le nombre de crafts
     */
    public static int getTotalCrafts(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getTotalCrafts(uuid, type) : 0;
    }

    /**
     * Récupère le nombre total de ressources récoltées
     * @param uuid L'UUID du joueur
     * @param type Le type de profession
     * @return Le nombre de récoltes
     */
    public static int getTotalGathers(UUID uuid, ProfessionType type) {
        ProfessionManager pm = getManager();
        return pm != null ? pm.getTotalGathers(uuid, type) : 0;
    }

    // -----------------------------
    //  HELPER
    // -----------------------------

    private static ProfessionManager getManager() {
        CorePlugin plugin = CorePlugin.getInstance();
        return plugin != null ? plugin.getProfessionManager() : null;
    }
}
