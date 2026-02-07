package com.mmo.core.model.party;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Représente un groupe de joueurs
 */
public class Party {

    private final UUID partyId;
    private UUID leaderUuid;
    private final int maxSize;
    private final Set<UUID> members;
    private final long createdAt;

    public Party(com.hypixel.hytale.server.UUID.UUID leader, int maxSize) {
        this.partyId = UUID.randomUUID();
        this.leaderUuid = leader.getUuid();
        this.maxSize = maxSize;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.add(leader.getUuid());
        this.createdAt = System.currentTimeMillis();
    }

    // -----------------------------
    //  MEMBRES
    // -----------------------------

    public void addMember(UUID UUIDUuid) {
        if (members.size() < maxSize) {
            members.add(UUIDUuid);
        }
    }

    public void removeMember(UUID UUIDUuid) {
        members.remove(UUIDUuid);
    }

    public boolean isMember(UUID UUIDUuid) {
        return members.contains(UUIDUuid);
    }

    public Set<UUID> getMemberUuids() {
        return new HashSet<>(members);
    }

    public int getSize() {
        return members.size();
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    // -----------------------------
    //  LEADERSHIP
    // -----------------------------

    public boolean isLeader(UUID UUIDUuid) {
        return leaderUuid.equals(UUIDUuid);
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeader(UUID newLeaderUuid) {
        if (members.contains(newLeaderUuid)) {
            this.leaderUuid = newLeaderUuid;
        }
    }

    /**
     * Promeut le prochain membre comme leader
     * @return L'UUID du nouveau leader ou null si le groupe est vide
     */
    public UUID promoteNextLeader() {
        if (members.isEmpty()) return null;
        
        // Trouver le premier membre qui n'est pas l'ancien leader
        for (UUID memberUuid : members) {
            if (!memberUuid.equals(leaderUuid)) {
                this.leaderUuid = memberUuid;
                return memberUuid;
            }
        }
        
        // Si on arrive ici, tous les membres sont le même joueur (edge case)
        return members.iterator().next();
    }

    // -----------------------------
    //  GETTERS
    // -----------------------------

    public UUID getPartyId() {
        return partyId;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return partyId.equals(party.partyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partyId);
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + partyId +
                ", leader=" + leaderUuid +
                ", size=" + members.size() + "/" + maxSize +
                '}';
    }
}
