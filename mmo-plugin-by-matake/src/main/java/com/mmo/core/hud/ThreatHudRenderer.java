package com.mmo.core.hud;

import com.hytale.server.entity.LivingEntity;
import com.hytale.server.UUID.UUID;
import com.mmo.core.api.ThreatAPI;

/**
 * Renderer pour afficher les informations de menace dans le HUD
 * Compatible avec ThreatAPI et le système HUD de CorePlugin
 */
public class ThreatHudRenderer {

    /**
     * Met à jour l'affichage de la menace pour un joueur
     * Affiche la menace totale du joueur sur sa cible actuelle
     */
    public static void updateThreatDisplay(UUID UUID) {
        // Récupérer la cible actuelle du joueur
        if (UUID.getTarget() == null) {
            clearThreatDisplay(UUID);
            return;
        }

        // Vérifier que la cible est un LivingEntity
        if (!(UUID.getTarget() instanceof LivingEntity)) {
            clearThreatDisplay(UUID);
            return;
        }

        LivingEntity mob = (LivingEntity) UUID.getTarget();
        
        // Récupérer la menace via l'API
        double threat = ThreatAPI.getThreat(mob, UUID);
        
        // Si pas de menace, ne rien afficher
        if (threat <= 0) {
            clearThreatDisplay(UUID);
            return;
        }

        // Récupérer le joueur avec le plus de menace
        UUID topThreat = ThreatAPI.getTopThreat(mob);
        boolean hasAggro = topThreat != null && topThreat.equals(UUID);

        // Formater l'affichage
        String displayText = formatThreatDisplay(threat, hasAggro);
        int color = hasAggro ? 0xFF0000 : 0xFFAA00; // Rouge si aggro, orange sinon

        // Envoyer au HUD
        // TODO: Adapter selon l'API HUD de Hytale
        // UUID.sendHud(HudLayer.THREAT, new HudElement(displayText, color));
        
        UUID.sendMessage(displayText); // Temporaire pour les tests
    }

    /**
     * Affiche la menace de tous les joueurs d'un groupe sur un mob
     * Utile pour les tanks qui veulent surveiller la menace du groupe
     */
    public static void updateGroupThreatDisplay(UUID UUID, LivingEntity mob) {
        // TODO: Récupérer les membres du groupe via PartyManager
        // Party party = CorePlugin.getInstance().getPartyManager().getParty(UUID);
        // if (party == null) return;

        // StringBuilder display = new StringBuilder("Menaces:\n");
        // for (UUID member : party.getMembers()) {
        //     double threat = ThreatAPI.getThreat(mob, member);
        //     if (threat > 0) {
        //         display.append(String.format("  %s: %d\n", member.getName(), (int)threat));
        //     }
        // }

        // UUID.sendHud(HudLayer.THREAT_GROUP, new HudElement(display.toString(), 0xFFFFFF));
    }

    /**
     * Affiche un indicateur d'aggro (vous avez l'attention du mob)
     */
    public static void showAggroIndicator(UUID UUID, boolean hasAggro) {
        if (hasAggro) {
            // TODO: Afficher un indicateur visuel d'aggro
            // UUID.sendHud(HudLayer.AGGRO_INDICATOR, new HudElement("⚠ AGGRO", 0xFF0000));
            UUID.sendMessage("§c⚠ AGGRO");
        } else {
            // clearAggroIndicator(UUID);
        }
    }

    /**
     * Efface l'affichage de menace
     */
    private static void clearThreatDisplay(UUID UUID) {
        // TODO: Adapter selon l'API HUD de Hytale
        // UUID.clearHud(HudLayer.THREAT);
    }

    /**
     * Formate l'affichage de la menace
     */
    private static String formatThreatDisplay(double threat, boolean hasAggro) {
        String aggroIndicator = hasAggro ? "⚠ " : "";
        return String.format("%sMenace: %d", aggroIndicator, (int)threat);
    }

    /**
     * Calcule un pourcentage de menace par rapport au top threat
     * Utile pour afficher des barres de progression
     */
    public static int getThreatPercentage(UUID UUID, LivingEntity mob) {
        double UUIDThreat = ThreatAPI.getThreat(mob, UUID);
        if (UUIDThreat <= 0) return 0;

        UUID topUUID = ThreatAPI.getTopThreat(mob);
        if (topUUID == null) return 0;

        double topThreat = ThreatAPI.getThreat(mob, topUUID);
        if (topThreat <= 0) return 0;

        return (int)((UUIDThreat / topThreat) * 100);
    }

    /**
     * Couleur basée sur le niveau de menace
     * Vert = faible, Orange = moyen, Rouge = élevé/aggro
     */
    public static int getThreatColor(int percentage) {
        if (percentage >= 90) return 0xFF0000; // Rouge
        if (percentage >= 70) return 0xFF5500; // Orange foncé
        if (percentage >= 50) return 0xFFAA00; // Orange
        if (percentage >= 30) return 0xFFFF00; // Jaune
        return 0x00FF00; // Vert
    }
}
