package com.mmo.core.api;

import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.managers.PartyManager;
import com.mmo.core.model.party.Party;

import java.util.List;

/**
 * API publique pour gérer les groupes (parties)
 * 
 * Fonctionnalités :
 * - Créer des groupes
 * - Seul le leader peut inviter/kicker des membres
 * - Seul le leader peut lancer des instances
 * - Si le leader quitte, un autre joueur devient chef
 * - Si tous les joueurs quittent, le groupe est supprimé
 */
public class PartyAPI {

    // -----------------------------
    //  CRÉATION ET DISSOLUTION
    // -----------------------------

    /**
     * Crée un nouveau groupe avec le joueur comme leader
     * @param leader Le joueur qui crée le groupe
     * @param maxSize Taille maximale du groupe (généralement 5 pour groupe, 10 pour raid)
     * @return true si le groupe a été créé avec succès
     */
    public static boolean createParty(UUID leader, int maxSize) {
        return getManager().createParty(leader, maxSize);
    }

    /**
     * Crée un groupe standard (5 joueurs max)
     * @param leader Le joueur qui crée le groupe
     * @return true si le groupe a été créé avec succès
     */
    public static boolean createParty(UUID leader) {
        return getManager().createParty(leader, 5);
    }

    /**
     * Dissout complètement un groupe (accessible uniquement au leader)
     * @param leader Le leader qui dissout le groupe
     * @return true si le groupe a été dissous
     */
    public static boolean disbandParty(UUID leader) {
        return getManager().disbandParty(leader);
    }

    // -----------------------------
    //  INVITATIONS (LEADER UNIQUEMENT)
    // -----------------------------

    /**
     * Invite un joueur à rejoindre le groupe (LEADER UNIQUEMENT)
     * @param inviter Le joueur qui invite (doit être le leader)
     * @param invited Le joueur à inviter
     * @return true si l'invitation a été envoyée
     */
    public static boolean inviteUUID(UUID inviter, UUID invited) {
        return getManager().inviteUUID(inviter, invited);
    }

    /**
     * Accepte une invitation à rejoindre un groupe
     * @param UUID Le joueur qui accepte l'invitation
     * @return true si le joueur a rejoint le groupe
     */
    public static boolean acceptInvite(UUID UUID) {
        return getManager().acceptInvite(UUID);
    }

    /**
     * Refuse une invitation à rejoindre un groupe
     * @param UUID Le joueur qui refuse l'invitation
     * @return true si l'invitation a été refusée
     */
    public static boolean declineInvite(UUID UUID) {
        return getManager().declineInvite(UUID);
    }

    // -----------------------------
    //  GESTION DES MEMBRES (LEADER UNIQUEMENT)
    // -----------------------------

    /**
     * Expulse un membre du groupe (LEADER UNIQUEMENT)
     * @param leader Le leader du groupe
     * @param target Le joueur à expulser
     * @return true si le joueur a été expulsé
     */
    public static boolean kickMember(UUID leader, UUID target) {
        return getManager().kickMember(leader, target);
    }

    /**
     * Quitte le groupe
     * Si le joueur est le leader, un autre membre devient leader
     * Si c'est le dernier membre, le groupe est dissous
     * @param UUID Le joueur qui quitte
     * @return true si le joueur a quitté le groupe
     */
    public static boolean leaveParty(UUID UUID) {
        return getManager().leaveParty(UUID);
    }

    /**
     * Transfère le leadership à un autre membre (LEADER UNIQUEMENT)
     * @param currentLeader Le leader actuel
     * @param newLeader Le nouveau leader
     * @return true si le leadership a été transféré
     */
    public static boolean promoteLeader(UUID currentLeader, UUID newLeader) {
        return getManager().promoteLeader(currentLeader, newLeader);
    }

    // -----------------------------
    //  GETTERS / INFORMATIONS
    // -----------------------------

    /**
     * Vérifie si un joueur est dans un groupe
     * @param UUID Le joueur à vérifier
     * @return true si le joueur est dans un groupe
     */
    public static boolean isInParty(UUID UUID) {
        return getManager().isInParty(UUID);
    }

    /**
     * Récupère le groupe d'un joueur
     * @param UUID Le joueur
     * @return Le groupe du joueur ou null s'il n'est pas dans un groupe
     */
    public static Party getParty(UUID UUID) {
        return getManager().getParty(UUID);
    }

    /**
     * Vérifie si un joueur est le leader de son groupe
     * @param UUID Le joueur à vérifier
     * @return true si le joueur est le leader
     */
    public static boolean isLeader(UUID UUID) {
        return getManager().isLeader(UUID);
    }

    /**
     * Récupère le leader du groupe d'un joueur
     * @param UUID Un membre du groupe
     * @return Le leader du groupe ou null
     */
    public static UUID getLeader(UUID UUID) {
        return getManager().getLeader(UUID);
    }

    /**
     * Récupère tous les membres du groupe d'un joueur
     * @param UUID Un membre du groupe
     * @return La liste des membres ou null si le joueur n'est pas dans un groupe
     */
    public static List<UUID> getMembers(UUID UUID) {
        return getManager().getMembers(UUID);
    }

    /**
     * Récupère la taille du groupe
     * @param UUID Un membre du groupe
     * @return Le nombre de membres dans le groupe ou 0
     */
    public static int getPartySize(UUID UUID) {
        return getManager().getPartySize(UUID);
    }

    /**
     * Vérifie si le groupe est plein
     * @param UUID Un membre du groupe
     * @return true si le groupe est plein
     */
    public static boolean isPartyFull(UUID UUID) {
        return getManager().isPartyFull(UUID);
    }

    /**
     * Vérifie si un joueur a une invitation en attente
     * @param UUID Le joueur à vérifier
     * @return true si le joueur a une invitation active
     */
    public static boolean hasPendingInvite(UUID UUID) {
        return getManager().hasPendingInvite(UUID);
    }

    // -----------------------------
    //  LANCEMENT D'INSTANCES (LEADER UNIQUEMENT)
    // -----------------------------

    /**
     * Vérifie si le joueur peut lancer une instance (doit être leader)
     * @param UUID Le joueur à vérifier
     * @return true si le joueur est leader de son groupe
     */
    public static boolean canStartInstance(UUID UUID) {
        return isLeader(UUID);
    }

    /**
     * Prépare le groupe pour une instance
     * Vérifie que le joueur est bien le leader
     * @param leader Le leader qui lance l'instance
     * @return true si le leader peut lancer l'instance
     */
    public static boolean preparePartyForInstance(UUID leader) {
        if (!isLeader(leader)) {
            return false;
        }
        Party party = getParty(leader);
        return party != null && !party.isEmpty();
    }

    // -----------------------------
    //  UTILITAIRES
    // -----------------------------

    /**
     * Envoie un message à tous les membres du groupe
     * @param UUID Un membre du groupe
     * @param message Le message à envoyer
     */
    public static void broadcastToParty(UUID UUID, String message) {
        getManager().broadcastToParty(UUID, message);
    }

    /**
     * Envoie un message à tous les membres du groupe sauf un
     * @param UUID Un membre du groupe
     * @param message Le message à envoyer
     * @param except Le joueur à exclure
     */
    public static void broadcastToPartyExcept(UUID UUID, String message, UUID except) {
        getManager().broadcastToPartyExcept(UUID, message, except);
    }

    // -----------------------------
    //  HELPER
    // -----------------------------

    private static PartyManager getManager() {
        return CorePlugin.getInstance().getPartyManager();
    }
}
