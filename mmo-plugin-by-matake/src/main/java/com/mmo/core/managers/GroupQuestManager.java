package com.mmo.core.managers;

import com.hypixel.hytale.server.Server;
import com.hypixel.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.model.QuestDef;
import com.mmo.core.model.QuestStageDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Gestionnaire de quêtes de groupe
 */
public class GroupQuestManager {

    private final CorePlugin plugin;
    private final QuestManager questManager;
    private final WaypointManager waypointManager;
    private final PartyManager partyManager;

    /**
     * État d'une quête de groupe
     */
    private static class GroupState {
        final UUID partyId;
        final String questId;
        int stageIndex;

        GroupState(UUID partyId, String questId) {
            this.partyId = partyId;
            this.questId = questId;
            this.stageIndex = 0;
        }
    }

    private final Map<UUID, GroupState> groupQuests = new HashMap<>();

    public GroupQuestManager(CorePlugin plugin, QuestManager questManager, 
                           WaypointManager waypointManager, PartyManager partyManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.waypointManager = waypointManager;
        this.partyManager = partyManager;
    }

    /**
     * Démarre une quête de groupe
     */
    public void startGroupQuest(UUID leaderId, String questId) {
        // Récupérer le groupe du leader
        UUID partyId = partyManager.getPartyId(leaderId);
        if (partyId == null) {
            sendMessage(leaderId, "[Quête] Vous devez être dans un groupe.");
            return;
        }

        // Vérifier que le joueur est le leader
        if (!partyManager.isLeader(partyId, leaderId)) {
            sendMessage(leaderId, "[Quête] Seul le leader peut lancer la quête.");
            return;
        }

        QuestDef def = questManager.getQuest(questId);
        if (def == null) {
            sendMessage(leaderId, "[Quête] Quête introuvable.");
            return;
        }
        if (!def.isCoop()) {
            sendMessage(leaderId, "[Quête] Cette quête n'est pas une quête de groupe.");
            return;
        }

        // Créer l'état de la quête de groupe
        GroupState state = new GroupState(partyId, questId);
        groupQuests.put(partyId, state);

        // Notifier tous les membres
        Set<UUID> members = partyManager.getPartyMembers(partyId);
        for (UUID uuid : members) {
            sendMessage(uuid, "[Quête] Quête de groupe commencée : " + def.getName());
        }

        updateGroupStage(partyId, def, 0);
    }

    /**
     * Complète l'étape actuelle d'une quête de groupe
     */
    public void completeGroupStage(UUID uuid, String questId) {
        UUID partyId = partyManager.getPartyId(uuid);
        
        // Si le joueur n'est pas dans un groupe, traiter comme quête solo
        if (partyId == null) {
            questManager.completeSoloStage(uuid, questId);
            return;
        }

        GroupState state = groupQuests.get(partyId);
        if (state == null || !state.questId.equals(questId)) {
            questManager.completeSoloStage(uuid, questId);
            return;
        }

        QuestDef def = questManager.getQuest(questId);
        if (def == null) return;

        int next = state.stageIndex + 1;

        if (next >= def.getStages().size()) {
            finishGroupQuest(partyId, def);
            return;
        }

        state.stageIndex = next;
        updateGroupStage(partyId, def, next);
    }

    /**
     * Abandonne une quête de groupe
     */
    public void abandonGroupQuest(UUID leaderId, String questId) {
        UUID partyId = partyManager.getPartyId(leaderId);
        if (partyId == null) {
            sendMessage(leaderId, "[Quête] Vous devez être dans un groupe.");
            return;
        }

        if (!partyManager.isLeader(partyId, leaderId)) {
            sendMessage(leaderId, "[Quête] Seul le leader peut abandonner la quête.");
            return;
        }

        GroupState state = groupQuests.remove(partyId);
        if (state == null) {
            sendMessage(leaderId, "[Quête] Aucune quête de groupe active.");
            return;
        }

        // Notifier tous les membres
        Set<UUID> members = partyManager.getPartyMembers(partyId);
        for (UUID uuid : members) {
            sendMessage(uuid, "[Quête] Quête de groupe abandonnée.");
            waypointManager.clearWaypoint(uuid);
        }
    }

    /**
     * Vérifie si un groupe a une quête active
     */
    public boolean hasActiveGroupQuest(UUID uuid, String questId) {
        UUID partyId = partyManager.getPartyId(uuid);
        if (partyId == null) return false;

        GroupState state = groupQuests.get(partyId);
        return state != null && state.questId.equals(questId);
    }

    /**
     * Met à jour l'étape pour tous les membres du groupe
     */
    private void updateGroupStage(UUID partyId, QuestDef def, int index) {
        QuestStageDef stage = def.getStages().get(index);
        Set<UUID> members = partyManager.getPartyMembers(partyId);

        for (UUID uuid : members) {
            sendMessage(uuid, "[Quête] Objectif de groupe : " + stage.getId());

            // Mettre à jour le HUD
            if (plugin.getHudManager() != null) {
                plugin.getHudManager().updateQuestHud(uuid, "Objectif : " + stage.getId());
            }

            // Envoyer le waypoint si défini
            if (stage.getWaypointId() != null) {
                waypointManager.sendWaypoint(uuid, stage.getWaypointId());
            }
        }
    }

    /**
     * Termine une quête de groupe
     */
    private void finishGroupQuest(UUID partyId, QuestDef def) {
        Set<UUID> members = partyManager.getPartyMembers(partyId);
        
        for (UUID uuid : members) {
            sendMessage(uuid, "[Quête] Quête de groupe terminée : " + def.getName());
            waypointManager.clearWaypoint(uuid);
        }
        
        groupQuests.remove(partyId);
    }

    /**
     * Envoie un message à un joueur
     */
    private void sendMessage(UUID uuid, String message) {
        UUID UUID = getServer().getUUIDManager().getUUID(uuid);
        if (UUID != null) {
            UUID.sendMessage(message);
        }
    }

    /**
     * Récupère le serveur
     */
    private Server getServer() {
        return plugin.getServer();
    }
}
