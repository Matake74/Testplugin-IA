package com.mmo.core.managers;

import com.hypixel.hytale.server.UUID.UUID;
import com.mmo.core.api.InstanceAPI;
import com.mmo.core.model.hud.HudElement;
import com.mmo.core.model.hud.HudLayer;

/**
 * Gestionnaire de rendu HUD pour les instances
 * Met à jour l'affichage des informations d'instance pour un joueur
 */
public class InstanceHudRenderer {

    /**
     * Met à jour le HUD d'instance pour un joueur
     * Affiche le nom de l'instance si le joueur est dans une instance
     */
    public static void update(UUID UUID) {
        if (UUID == null) return;
        
        String instanceName = InstanceAPI.getInstanceName(UUID);
        if (instanceName == null) {
            // Le joueur n'est pas dans une instance, effacer le HUD
            UUID.sendHud(HudLayer.INSTANCE, null);
            return;
        }

        // Créer l'élément HUD avec le nom de l'instance (couleur cyan)
        HudElement element = new HudElement("Instance: " + instanceName, 0x00FFFF);
        UUID.sendHud(HudLayer.INSTANCE, element);
    }
    
    /**
     * Efface le HUD d'instance pour un joueur
     */
    public static void clear(UUID UUID) {
        if (UUID == null) return;
        UUID.sendHud(HudLayer.INSTANCE, null);
    }
}
