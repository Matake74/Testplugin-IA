package com.mmo.core.commands;

import com.hypixel.hytale.server.command.Command;
import com.hypixel.hytale.server.command.CommandSender;
import com.hypixel.hytale.server.UUID.UUID;
import com.mmo.core.managers.GroupQuestManager;
import com.mmo.core.managers.QuestManager;

/**
 * Commande /quest pour gérer les quêtes solo et de groupe
 */
public class QuestCommand implements Command {

    private final QuestManager questManager;
    private final GroupQuestManager groupQuestManager;

    public QuestCommand(QuestManager questManager, GroupQuestManager groupQuestManager) {
        this.questManager = questManager;
        this.groupQuestManager = groupQuestManager;
    }

    @Override
    public String getName() {
        return "quest";
    }

    @Override
    public String getDescription() {
        return "Gère les quêtes solo et de groupe";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof UUID)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return;
        }

        UUID UUID = (UUID) sender;

        if (args.length < 2) {
            showUsage(UUID);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "solo":
                handleSoloQuest(UUID, args);
                break;

            case "group":
                handleGroupQuest(UUID, args);
                break;

            default:
                showUsage(UUID);
                break;
        }
    }

    /**
     * Gère les commandes de quêtes solo
     */
    private void handleSoloQuest(UUID UUID, String[] args) {
        if (args.length < 3) {
            UUID.sendMessage("/quest solo start <id> | solo complete <id> | solo abandon <id>");
            return;
        }

        String action = args[1].toLowerCase();
        String questId = args[2];

        switch (action) {
            case "start":
                questManager.startSoloQuest(UUID.getUuid(), questId);
                break;
            case "complete":
                questManager.completeSoloStage(UUID.getUuid(), questId);
                break;
            case "abandon":
                questManager.abandonQuest(UUID.getUuid(), questId);
                break;
            default:
                UUID.sendMessage("/quest solo start <id> | solo complete <id> | solo abandon <id>");
                break;
        }
    }

    /**
     * Gère les commandes de quêtes de groupe
     */
    private void handleGroupQuest(UUID UUID, String[] args) {
        if (args.length < 3) {
            UUID.sendMessage("/quest group start <id> | group complete <id> | group abandon <id>");
            return;
        }

        String action = args[1].toLowerCase();
        String questId = args[2];

        switch (action) {
            case "start":
                groupQuestManager.startGroupQuest(UUID.getUuid(), questId);
                break;
            case "complete":
                groupQuestManager.completeGroupStage(UUID.getUuid(), questId);
                break;
            case "abandon":
                groupQuestManager.abandonGroupQuest(UUID.getUuid(), questId);
                break;
            default:
                UUID.sendMessage("/quest group start <id> | group complete <id> | group abandon <id>");
                break;
        }
    }

    /**
     * Affiche l'utilisation de la commande
     */
    private void showUsage(UUID UUID) {
        UUID.sendMessage("=== Commandes de Quêtes ===");
        UUID.sendMessage("/quest solo start <id> - Démarre une quête solo");
        UUID.sendMessage("/quest solo complete <id> - Complète l'étape actuelle");
        UUID.sendMessage("/quest solo abandon <id> - Abandonne la quête");
        UUID.sendMessage("/quest group start <id> - Démarre une quête de groupe (leader seulement)");
        UUID.sendMessage("/quest group complete <id> - Complète l'étape du groupe");
        UUID.sendMessage("/quest group abandon <id> - Abandonne la quête de groupe (leader seulement)");
    }
}
