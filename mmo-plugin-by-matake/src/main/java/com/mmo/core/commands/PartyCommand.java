package com.mmo.core.commands;

import com.hytale.server.command.Command;
import com.hytale.server.command.CommandSender;
import com.hytale.server.UUID.UUID;
import com.mmo.core.managers.PartyManager;
import com.mmo.core.model.party.Party;

/**
 * Commande /party pour gérer les groupes
 * Intégrée dans CorePlugin
 */
public class PartyCommand implements Command {

    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof UUID)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return;
        }

        UUID UUID = (UUID) sender;

        if (args.length == 0) {
            sendHelp(UUID);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(UUID, args);
                break;

            case "invite":
                handleInvite(UUID, args);
                break;

            case "accept":
                handleAccept(UUID);
                break;

            case "decline":
            case "deny":
                handleDecline(UUID);
                break;

            case "leave":
                handleLeave(UUID);
                break;

            case "kick":
                handleKick(UUID, args);
                break;

            case "disband":
                handleDisband(UUID);
                break;

            case "promote":
                handlePromote(UUID, args);
                break;

            case "info":
            case "list":
                handleInfo(UUID);
                break;

            default:
                sendHelp(UUID);
                break;
        }
    }

    private void handleCreate(UUID UUID, String[] args) {
        int maxSize = 5; // Défaut : groupe normal
        
        if (args.length > 1) {
            try {
                maxSize = Integer.parseInt(args[1]);
                if (maxSize < 2 || maxSize > 40) {
                    UUID.sendMessage("§cLa taille doit être entre 2 et 40.");
                    return;
                }
            } catch (NumberFormatException e) {
                UUID.sendMessage("§cTaille invalide : " + args[1]);
                return;
            }
        }

        partyManager.createParty(UUID, maxSize);
    }

    private void handleInvite(UUID UUID, String[] args) {
        if (args.length < 2) {
            UUID.sendMessage("§cUsage: /party invite <joueur>");
            return;
        }

        // TODO: Adapter selon l'API UUID de Hytale
        // UUID target = UUID.getServer().getUUIDManager().getUUID(args[1]);
        UUID target = null; // Placeholder
        
        if (target == null) {
            UUID.sendMessage("§cJoueur introuvable : " + args[1]);
            return;
        }

        partyManager.inviteUUID(UUID, target);
    }

    private void handleAccept(UUID UUID) {
        partyManager.acceptInvite(UUID);
    }

    private void handleDecline(UUID UUID) {
        partyManager.declineInvite(UUID);
    }

    private void handleLeave(UUID UUID) {
        partyManager.leaveParty(UUID);
    }

    private void handleKick(UUID UUID, String[] args) {
        if (args.length < 2) {
            UUID.sendMessage("§cUsage: /party kick <joueur>");
            return;
        }

        // TODO: Adapter selon l'API UUID de Hytale
        // UUID target = UUID.getServer().getUUIDManager().getUUID(args[1]);
        UUID target = null; // Placeholder
        
        if (target == null) {
            UUID.sendMessage("§cJoueur introuvable : " + args[1]);
            return;
        }

        partyManager.kickMember(UUID, target);
    }

    private void handleDisband(UUID UUID) {
        partyManager.disbandParty(UUID);
    }

    private void handlePromote(UUID UUID, String[] args) {
        if (args.length < 2) {
            UUID.sendMessage("§cUsage: /party promote <joueur>");
            return;
        }

        // TODO: Adapter selon l'API UUID de Hytale
        // UUID target = UUID.getServer().getUUIDManager().getUUID(args[1]);
        UUID target = null; // Placeholder
        
        if (target == null) {
            UUID.sendMessage("§cJoueur introuvable : " + args[1]);
            return;
        }

        partyManager.promoteLeader(UUID, target);
    }

    private void handleInfo(UUID UUID) {
        Party party = partyManager.getParty(UUID);
        
        if (party == null) {
            UUID.sendMessage("§cVous n'êtes dans aucun groupe.");
            return;
        }

        UUID.sendMessage("§e§l=== Informations du Groupe ===");
        UUID.sendMessage("§eLeader : §f" + party.getLeader().getName());
        UUID.sendMessage("§eTaille : §f" + party.getSize() + "/" + party.getMaxSize());
        UUID.sendMessage("§eMembres :");
        
        for (UUID member : party.getMembers()) {
            String prefix = party.isLeader(member) ? "§6★ " : "§7- ";
            String status = member.isOnline() ? "§a●" : "§c●";
            UUID.sendMessage(prefix + status + " §f" + member.getName());
        }
    }

    private void sendHelp(UUID UUID) {
        UUID.sendMessage("§e§l=== Commandes de Groupe ===");
        UUID.sendMessage("§e/party create [taille] §7- Créer un groupe");
        UUID.sendMessage("§e/party invite <joueur> §7- Inviter un joueur §8(leader)");
        UUID.sendMessage("§e/party accept §7- Accepter une invitation");
        UUID.sendMessage("§e/party decline §7- Refuser une invitation");
        UUID.sendMessage("§e/party leave §7- Quitter le groupe");
        UUID.sendMessage("§e/party kick <joueur> §7- Expulser un membre §8(leader)");
        UUID.sendMessage("§e/party promote <joueur> §7- Promouvoir leader §8(leader)");
        UUID.sendMessage("§e/party disband §7- Dissoudre le groupe §8(leader)");
        UUID.sendMessage("§e/party info §7- Informations du groupe");
    }
}
