package com.mmo.core.model.party;

import java.util.UUID;

/**
 * Représente une invitation à rejoindre un groupe
 */
public class PartyInvite {

    private final UUID inviterUuid;
    private final UUID invitedUuid;
    private final Party party;
    private final long createdAt;
    private final long expiresAt;

    public PartyInvite(UUID inviterUuid, UUID invitedUuid, Party party, long expirationMs) {
        this.inviterUuid = inviterUuid;
        this.invitedUuid = invitedUuid;
        this.party = party;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + expirationMs;
    }

    /**
     * Vérifie si l'invitation a expiré
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    /**
     * Temps restant avant expiration en secondes
     */
    public long getRemainingSeconds() {
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    // -----------------------------
    //  GETTERS
    // -----------------------------

    public UUID getInviterUuid() {
        return inviterUuid;
    }

    public UUID getInvitedUuid() {
        return invitedUuid;
    }

    public Party getParty() {
        return party;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String toString() {
        return "PartyInvite{" +
                "inviter=" + inviterUuid +
                ", invited=" + invitedUuid +
                ", party=" + party.getPartyId() +
                ", remainingSeconds=" + getRemainingSeconds() +
                '}';
    }
}
