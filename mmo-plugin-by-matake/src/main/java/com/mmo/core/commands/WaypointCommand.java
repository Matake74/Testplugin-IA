package com.mmo.core.commands;

import com.hypixel.hytale.server.command.Command;
import com.hypixel.hytale.server.command.CommandSender;
import com.hypixel.hytale.server.UUID.UUID;
import com.mmo.core.managers.WaypointManager;

/**
 * Commande /waypoint pour gérer les points de repère
 */
public class WaypointCommand implements Command {

    private final WaypointManager waypointManager;

    public WaypointCommand(WaypointManager waypointManager) {
        this.waypointManager = waypointManager;
    }

    @Override
    public String getName() {
        return "waypoint";
    }

    @Override
    public String getDescription() {
        return "Gère les waypoints et points de repère";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof UUID)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return;
        }

        UUID UUID = (UUID) sender;

        if (args.length < 1) {
            showUsage(UUID);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "goto":
                if (args.length < 2) {
                    UUID.sendMessage("Usage: /waypoint goto <id>");
                    return;
                }
                waypointManager.sendWaypoint(UUID.getUuid(), args[1]);
                break;
                
            case "clear":
                waypointManager.clearWaypoint(UUID.getUuid());
                break;
                
            default:
                showUsage(UUID);
                break;
        }
    }

    /**
     * Affiche l'utilisation de la commande
     */
    private void showUsage(UUID UUID) {
        UUID.sendMessage("=== Commandes de Waypoint ===");
        UUID.sendMessage("/waypoint goto <id> - Définit un waypoint vers une destination");
        UUID.sendMessage("/waypoint clear - Efface le waypoint actuel");
    }
}
