package com.mmo.core.model.party;

import com.hytale.server.UUID.UUID;

import java.util.*;

/**
 * Représente un groupe de joueurs (party)
 */
public class Party {

    private final UUID partyId;
    private UUID leader;
    private final List<UUID> members;
    private final int maxSize;
    private final long creationTime;

    public Party(UUID leader, int maxSize) {
        this.partyId = UUID.randomUUID();
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader);
        this.maxSize = maxSize;
        this.creationTime = System.currentTimeMillis();
    }

    // -----------------------------
    //  GETTERS
    // -----------------------------

    public UUID getPartyId() {
        return partyId;
    }

    public UUID getLeader() {
        return leader;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public int getSize() {
        return members.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getCreationTime() {
        return creationTime;
    }

    // -----------------------------
    //  MODIFICATIONS
    // -----------------------------

    public void setLeader(UUID newLeader) {
        if (members.contains(newLeader)) {
            this.leader = newLeader;
        }
    }

    public void addMember(UUID uuid) {
        if (!isFull() && !members.contains(uuid)) {
            members.add(uuid);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        
        // Si le leader part et qu'il reste des membres, promouvoir quelqu'un
        if (UUID.equals(leader) && !members.isEmpty()) {
            leader = members.get(0);
        }
    }

    /**
     * Promeut le prochain membre comme leader
     * @return Le nouveau leader ou null si le groupe est vide
     */
    public UUID promoteNextLeader() {
        if (members.isEmpty()) return null;
        
        leader = members.get(0);
        return leader;
    }

    // -----------------------------
    //  VÉRIFICATIONS
    // -----------------------------

    public boolean isLeader(UUID uuid) {
        return leader != null && leader.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Vérifie si tous les membres sont en ligne
     */
    public boolean allMembersOnline() {
        for (UUID member : members) {
            if (!member.isOnline()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Récupère uniquement les membres en ligne
     */
    public List<UUID> getOnlineMembers() {
        List<UUID> online = new ArrayList<>();
        for (UUID member : members) {
            if (member.isOnline()) {
                online.add(member);
            }
        }
        return online;
    }

    // -----------------------------
    //  UTILITAIRES
    // -----------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return partyId.equals(party.partyId);
    }

    @Override
    public int hashCode() {
        return partyId.hashCode();
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + partyId +
                ", leader=" + (leader != null ? leader.getName() : "null") +
                ", size=" + members.size() +
                "/" + maxSize +
                '}';
    }
}
