package com.mmo.core.listeners;

import com.hytale.server.events.UUID.UUIDJoinEvent;
import com.hytale.server.events.UUID.UUIDLeaveEvent;
import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.managers.HudManager;
import com.mmo.core.managers.PartyManager;

/**
 * Listener pour les événements de connexion/déconnexion
 * Gère l'initialisation et le nettoyage du HUD et des groupes
 */
public class UUIDConnectionListener {

    private final CorePlugin plugin;
    private final HudManager hudManager;
    private final PartyManager partyManager;

    public UUIDConnectionListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.hudManager = plugin.getHudManager();
        this.partyManager = plugin.getPartyManager();
    }

    /**
     * Gère la connexion d'un joueur
     */
    public void onUUIDJoin(UUIDJoinEvent event) {
        UUID UUID = event.getUUID();

        // Initialiser le HUD
        if (hudManager != null) {
            hudManager.onUUIDJoin(UUID);
        }

        // Afficher un message de bienvenue avec le système HUD
        // HudAPI.showNotification(UUID, "Bienvenue", "Connecté au serveur MMO!");

        plugin.getLogger().info("[UUIDConnection] " + UUID.getName() + " connecté.");
    }

    /**
     * Gère la déconnexion d'un joueur
     */
    public void onUUIDLeave(UUIDLeaveEvent event) {
        UUID UUID = event.getUUID();

        // Nettoyer le HUD
        if (hudManager != null) {
            hudManager.onUUIDLeave(UUID);
        }

        // Gérer la déconnexion du groupe
        if (partyManager != null) {
            partyManager.handleUUIDDisconnect(UUID);
        }

        plugin.getLogger().info("[UUIDConnection] " + UUID.getName() + " déconnecté.");
    }
}
