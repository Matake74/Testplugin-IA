package com.mmo.core.commands;

import com.hypixel.hytale.server.command.Command;
import com.hypixel.hytale.server.command.CommandSender;
import com.hypixel.hytale.server.UUID.UUID;
import com.mmo.core.managers.InstanceManager;

/**
 * Commande /instance pour gérer les instances
 * Usage:
 *   /instance solo <id>     - Lancer une instance solo
 *   /instance group <id>    - Lancer une instance de groupe
 *   /instance raid <id>     - Lancer une instance de raid
 *   /instance leave         - Quitter l'instance actuelle
 */
public class InstanceCommand implements Command {

    private final InstanceManager instanceManager;

    public InstanceCommand(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof UUID)) {
            sender.sendMessage("[Instance] Cette commande est réservée aux joueurs.");
            return;
        }
        UUID UUID = (UUID) sender;

        if (args.length < 1) {
            sendUsage(UUID);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "solo":
                if (args.length < 2) {
                    UUID.sendMessage("Usage: /instance solo <id>");
                    return;
                }
                instanceManager.startSoloInstance(UUID, args[1]);
                break;

            case "group":
            case "raid":
                if (args.length < 2) {
                    UUID.sendMessage("Usage: /instance " + args[0] + " <id>");
                    return;
                }
                instanceManager.startGroupOrRaidInstance(UUID, args[1]);
                break;

            case "leave":
                instanceManager.leaveInstance(UUID);
                break;

            default:
                sendUsage(UUID);
                break;
        }
    }

    private void sendUsage(UUID UUID) {
        UUID.sendMessage("=== Commandes Instance ===");
        UUID.sendMessage("/instance solo <id>     §7- Lancer une instance solo");
        UUID.sendMessage("/instance group <id>    §7- Lancer une instance de groupe");
        UUID.sendMessage("/instance raid <id>     §7- Lancer une instance de raid");
        UUID.sendMessage("/instance leave         §7- Quitter l'instance actuelle");
    }
}
