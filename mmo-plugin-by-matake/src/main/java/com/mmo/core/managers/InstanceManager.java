package com.mmo.core.managers;

import com.hypixel.hytale.server.entity.Entity;
import com.hypixel.hytale.server.entity.LivingEntity;
import com.hypixel.hytale.server.item.ItemStack;
import com.hypixel.hytale.server.UUID.UUID;
import com.hypixel.hytale.server.world.Location;
import com.hypixel.hytale.server.world.World;
import com.mmo.core.CorePlugin;
import com.mmo.core.api.HudAPI;
import com.mmo.core.api.QuestAPI;
import com.mmo.core.model.hud.HudElement;
import com.mmo.core.model.hud.HudLayer;
import com.mmo.core.model.instance.*;
import com.mmo.core.model.party.Party;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire des instances (donjons, raids)
 * Gère les instances solo, de groupe et de raid
 */
public class InstanceManager {

    private final CorePlugin plugin;
    private final PartyManager partyManager;
    private final QuestManager questManager;
    private final ThreatManager threatManager;

    // Définitions des instances
    private final Map<String, InstanceDef> instances = new HashMap<>();
    private final Map<String, LootTableDef> lootTables = new HashMap<>();

    // Instances actives
    private final Map<UUID, ActiveInstance> UUIDInstance = new ConcurrentHashMap<>();
    private final Map<String, ActiveInstance> activeByKey = new ConcurrentHashMap<>();

    public InstanceManager(CorePlugin plugin,
                           PartyManager partyManager,
                           QuestManager questManager,
                           ThreatManager threatManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        this.questManager = questManager;
        this.threatManager = threatManager;

        loadInstances();
        loadLootTables();
        
        plugin.getLogger().info("[InstanceManager] Initialisé avec succès.");
    }

    // -----------------------------
    //  CHARGEMENT DES CONFIGURATIONS
    // -----------------------------

    /**
     * Charge les définitions d'instances depuis la configuration
     */
    private void loadInstances() {
        try {
            // Charger depuis le fichier de ressources
            InputStream input = plugin.getClass().getResourceAsStream("/config/instances.yml");
            
            if (input != null) {
                Map<String, InstanceDef> loadedInstances = 
                    com.mmo.core.util.InstanceConfigLoader.loadInstances(input);
                instances.putAll(loadedInstances);
                input.close();
                
                plugin.getLogger().info("[InstanceManager] " + instances.size() + " instances chargées depuis instances.yml");
            } else {
                plugin.getLogger().warn("[InstanceManager] Fichier instances.yml introuvable, création d'instances par défaut");
                createDefaultInstances();
            }
            
        } catch (Exception e) {
            plugin.getLogger().error("[InstanceManager] Erreur lors du chargement des instances: " + e.getMessage());
            e.printStackTrace();
            createDefaultInstances();
        }
    }

    /**
     * Crée des instances par défaut en cas d'erreur de chargement
     */
    private void createDefaultInstances() {
        // Instance solo de secours
        InstanceDef solo = new InstanceDef(
            "test_dungeon",
            "Test Dungeon",
            InstanceType.SOLO,
            1, 1,
            "instance_world",
            0, 64, 0,
            "overworld",
            0, 64, 100,
            null, null,
            10, 60,
            new ArrayList<>(),
            null,
            new ArrayList<>()
        );
        instances.put(solo.getId(), solo);
        
        plugin.getLogger().info("[InstanceManager] 1 instance par défaut créée");
    }

    /**
     * Charge les tables de loot
     */
    private void loadLootTables() {
        try {
            // Charger depuis le fichier de ressources
            InputStream input = plugin.getClass().getResourceAsStream("/config/loot_tables.yml");
            
            if (input != null) {
                Map<String, LootTableDef> loadedTables = 
                    com.mmo.core.util.InstanceConfigLoader.loadLootTables(input);
                lootTables.putAll(loadedTables);
                input.close();
                
                plugin.getLogger().info("[InstanceManager] " + lootTables.size() + " tables de loot chargées depuis loot_tables.yml");
            } else {
                plugin.getLogger().warn("[InstanceManager] Fichier loot_tables.yml introuvable, création de tables par défaut");
                createDefaultLootTables();
            }
            
        } catch (Exception e) {
            plugin.getLogger().error("[InstanceManager] Erreur lors du chargement des loot tables: " + e.getMessage());
            e.printStackTrace();
            createDefaultLootTables();
        }
    }

    /**
     * Crée des tables de loot par défaut
     */
    private void createDefaultLootTables() {
        List<LootEntry> defaultLoot = Arrays.asList(
            new LootEntry("gold_coin", 1, 10, 1.0),
            new LootEntry("health_potion", 1, 3, 0.5)
        );
        
        LootTableDef table = new LootTableDef("default_loot", defaultLoot);
        lootTables.put(table.getId(), table);
        
        plugin.getLogger().info("[InstanceManager] 1 table de loot par défaut créée");
    }

    // -----------------------------
    //  GETTERS PUBLICS
    // -----------------------------

    public InstanceDef getInstanceDef(String id) {
        return instances.get(id);
    }

    public boolean isInInstance(UUID UUID) {
        return UUID != null && UUIDInstance.containsKey(UUID.getUuid());
    }

    public ActiveInstance getActiveInstance(UUID UUID) {
        return UUID != null ? UUIDInstance.get(UUID.getUuid()) : null;
    }

    public boolean canEnterInstance(UUID UUID, String instanceId) {
        if (UUID == null) return false;
        
        InstanceDef def = instances.get(instanceId);
        if (def == null) return false;
        
        // Vérifier si déjà en instance
        if (isInInstance(UUID)) return false;
        
        // Vérifier les prérequis selon le type
        if (def.getType() == InstanceType.SOLO) {
            return true;
        } else {
            Party party = partyManager.getParty(UUID);
            if (party == null) return false;
            if (!party.isLeader(UUID.getUuid())) return false;
            
            int size = party.getSize();
            return size >= def.getMinUUIDs() && size <= def.getMaxUUIDs();
        }
    }

    // -----------------------------
    //  LANCEMENT D'INSTANCES SOLO
    // -----------------------------

    public void startSoloInstance(UUID UUID, String instanceId) {
        if (UUID == null) return;
        
        InstanceDef def = instances.get(instanceId);
        if (def == null) {
            UUID.sendMessage("§c[Instance] Instance introuvable.");
            return;
        }
        
        if (def.getType() != InstanceType.SOLO) {
            UUID.sendMessage("§c[Instance] Ce n'est pas une instance solo.");
            return;
        }

        if (isInInstance(UUID)) {
            UUID.sendMessage("§c[Instance] Vous êtes déjà dans une instance.");
            return;
        }

        // Créer l'instance active
        ActiveInstance active = new ActiveInstance(def);
        active.getUUIDs().add(UUID.getUuid());
        
        String key = def.getId() + ":" + UUID.getUuid();
        activeByKey.put(key, active);
        UUIDInstance.put(UUID.getUuid(), active);

        // Téléporter et démarrer
        teleportToInstance(UUID, def);
        scheduleStartCountdown(active);
        
        plugin.getLogger().info("[Instance] " + UUID.getName() + " a démarré l'instance solo: " + def.getName());
    }

    // -----------------------------
    //  LANCEMENT D'INSTANCES GROUP/RAID
    // -----------------------------

    public void startGroupOrRaidInstance(UUID leader, String instanceId) {
        if (leader == null) return;
        
        InstanceDef def = instances.get(instanceId);
        if (def == null) {
            leader.sendMessage("§c[Instance] Instance introuvable.");
            return;
        }
        
        if (def.getType() == InstanceType.SOLO) {
            leader.sendMessage("§c[Instance] Cette instance est solo.");
            return;
        }

        // Vérifier le groupe
        Party party = partyManager.getParty(leader);
        if (party == null) {
            leader.sendMessage("§c[Instance] Vous devez être en groupe.");
            return;
        }
        
        if (!party.isLeader(leader.getUuid())) {
            leader.sendMessage("§c[Instance] Seul le leader peut lancer l'instance.");
            return;
        }

        // Vérifier la taille du groupe
        int size = party.getSize();
        if (size < def.getMinUUIDs() || size > def.getMaxUUIDs()) {
            leader.sendMessage("§c[Instance] Nombre de joueurs invalide. Requis : "
                    + def.getMinUUIDs() + "-" + def.getMaxUUIDs());
            return;
        }

        // Vérifier que tous les membres sont disponibles
        for (UUID memberUuid : party.getMemberUuids()) {
            if (UUIDInstance.containsKey(memberUuid)) {
                UUID member = plugin.getUUIDManager().getUUID(memberUuid);
                String name = member != null ? member.getName() : "Membre";
                leader.sendMessage("§c[Instance] " + name + " est déjà dans une instance.");
                return;
            }
        }

        // Créer l'instance active
        ActiveInstance active = new ActiveInstance(def);
        String key = def.getId() + ":" + party.getPartyId();
        
        // Ajouter tous les membres
        for (UUID memberUuid : party.getMemberUuids()) {
            active.getUUIDs().add(memberUuid);
            UUIDInstance.put(memberUuid, active);
            
            UUID member = plugin.getUUIDManager().getUUID(memberUuid);
            if (member != null) {
                teleportToInstance(member, def);
            }
        }
        
        activeByKey.put(key, active);
        scheduleStartCountdown(active);
        
        plugin.getLogger().info("[Instance] Groupe de " + leader.getName() + 
                              " a démarré l'instance: " + def.getName() + " (" + size + " joueurs)");
    }

    // -----------------------------
    //  TÉLÉPORTATION
    // -----------------------------

    private void teleportToInstance(UUID UUID, InstanceDef def) {
        try {
            World world = plugin.getWorldManager().getWorld(def.getWorld());
            if (world == null) {
                plugin.getLogger().warn("[Instance] Monde introuvable: " + def.getWorld());
                UUID.sendMessage("§c[Instance] Erreur de téléportation.");
                return;
            }

            Location loc = new Location(world, def.getSpawnX(), def.getSpawnY(), def.getSpawnZ());
            UUID.teleport(loc);
            UUID.sendMessage("§a[Instance] Bienvenue dans " + def.getName() + " !");
        } catch (Exception e) {
            plugin.getLogger().error("[Instance] Erreur de téléportation: " + e.getMessage());
            UUID.sendMessage("§c[Instance] Erreur de téléportation.");
        }
    }

    private void teleportToExit(UUID UUID, InstanceDef def) {
        try {
            World world = plugin.getWorldManager().getWorld(def.getExitWorld());
            if (world == null) {
                plugin.getLogger().warn("[Instance] Monde de sortie introuvable: " + def.getExitWorld());
                return;
            }

            Location loc = new Location(world, def.getExitX(), def.getExitY(), def.getExitZ());
            UUID.teleport(loc);
            UUID.sendMessage("§e[Instance] Vous avez quitté l'instance.");
        } catch (Exception e) {
            plugin.getLogger().error("[Instance] Erreur de téléportation de sortie: " + e.getMessage());
        }
    }

    // Continued in Part 2...

    // -----------------------------
    //  COUNTDOWN ET DÉMARRAGE
    // -----------------------------

    private void scheduleStartCountdown(ActiveInstance active) {
        InstanceDef def = active.getDef();
        final int[] counter = { def.getStartCountdownSeconds() };

        int taskId = plugin.getScheduler().runRepeating(() -> {
            counter[0]--;
            
            // Notifier tous les joueurs
            for (UUID uuid : active.getUUIDs()) {
                UUID p = plugin.getUUIDManager().getUUID(uuid);
                if (p != null) {
                    p.sendMessage("§e[Instance] Début dans " + counter[0] + "s...");
                }
            }
            
            // Démarrer l'instance quand le compteur atteint 0
            if (counter[0] <= 0) {
                plugin.getScheduler().cancelTask(active.getStartCountdownTaskId());
                startInstance(active);
            }
        }, 20, 20); // Toutes les secondes

        active.setStartCountdownTaskId(taskId);
    }

    private void startInstance(ActiveInstance active) {
        active.setStarted(true);
        InstanceDef def = active.getDef();

        // Lancer les quêtes
        for (UUID uuid : active.getUUIDs()) {
            UUID p = plugin.getUUIDManager().getUUID(uuid);
            if (p == null) continue;
            
            // Quête de départ si configurée
            if (def.getType() == InstanceType.SOLO && def.getStartQuestId() != null) {
                QuestAPI.startSoloQuest(p, def.getStartQuestId());
            }
            
            // Afficher le HUD
            HudAPI.setHud(p, HudLayer.INSTANCE,
                    new HudElement("Instance : " + def.getName(), 0x00FFFF));
            
            p.sendMessage("§a[Instance] L'instance a commencé !");
        }

        // Spawn des mobs et boss
        spawnMobsAndBoss(active);
        
        // Spawn des coffres
        spawnChestsAsQuestEntities(active);
        
        plugin.getLogger().info("[Instance] Instance démarrée: " + def.getName());
    }

    // -----------------------------
    //  SPAWN MOBS ET BOSS
    // -----------------------------

    private void spawnMobsAndBoss(ActiveInstance active) {
        InstanceDef def = active.getDef();
        
        try {
            World world = plugin.getWorldManager().getWorld(def.getWorld());
            if (world == null) {
                plugin.getLogger().warn("[Instance] Monde introuvable pour spawn: " + def.getWorld());
                return;
            }

            // Spawn des mobs normaux
            for (MobPackDef pack : def.getMobPacks()) {
                for (int i = 0; i < pack.getCount(); i++) {
                    try {
                        Location loc = new Location(world, pack.getX(), pack.getY(), pack.getZ());
                        LivingEntity mob = (LivingEntity) world.spawnEntity(pack.getType(), loc);
                        active.getMobs().add(mob.getUuid());
                    } catch (Exception e) {
                        plugin.getLogger().warn("[Instance] Erreur spawn mob: " + e.getMessage());
                    }
                }
            }

            // Spawn du boss
            if (def.getBoss() != null) {
                BossDef boss = def.getBoss();
                try {
                    Location loc = new Location(world, boss.getX(), boss.getY(), boss.getZ());
                    LivingEntity bossEntity = (LivingEntity) world.spawnEntity(boss.getType(), loc);
                    active.setBossId(bossEntity.getUuid());
                    
                    plugin.getLogger().info("[Instance] Boss spawné: " + boss.getId());
                } catch (Exception e) {
                    plugin.getLogger().warn("[Instance] Erreur spawn boss: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().error("[Instance] Erreur générale spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------
    //  SPAWN DES COFFRES
    // -----------------------------

    private void spawnChestsAsQuestEntities(ActiveInstance active) {
        InstanceDef def = active.getDef();
        
        try {
            World world = plugin.getWorldManager().getWorld(def.getWorld());
            if (world == null) return;

            for (ChestDef chest : def.getChests()) {
                try {
                    Location loc = new Location(world, chest.getX(), chest.getY(), chest.getZ());
                    Entity entity = world.spawnEntity("CHEST_ENTITY", loc);
                    
                    // Marquer l'entité avec un nom custom pour l'identifier
                    if (entity != null) {
                        entity.setCustomName(chest.getQuestEntityId());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warn("[Instance] Erreur spawn coffre: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().error("[Instance] Erreur spawn coffres: " + e.getMessage());
        }
    }

    // -----------------------------
    //  GESTION DES MORTS DE MOBS
    // -----------------------------

    public void onMobDeath(LivingEntity entity) {
        if (entity == null) return;
        
        UUID uuid = entity.getUuid();
        ActiveInstance active = null;
        
        // Trouver l'instance correspondante
        for (ActiveInstance a : activeByKey.values()) {
            if (a.getMobs().contains(uuid) || 
                (a.getBossId() != null && a.getBossId().equals(uuid))) {
                active = a;
                break;
            }
        }
        
        if (active == null) return;

        // Retirer le mob de la liste
        boolean wasBoss = false;
        if (active.getMobs().remove(uuid)) {
            plugin.getLogger().debug("[Instance] Mob tué dans instance");
        } else if (active.getBossId() != null && active.getBossId().equals(uuid)) {
            active.setBossId(null);
            wasBoss = true;
            
            // Notifier les joueurs
            for (UUID UUIDUuid : active.getUUIDs()) {
                UUID p = plugin.getUUIDManager().getUUID(UUIDUuid);
                if (p != null) {
                    p.sendMessage("§6[Instance] §e§lLe boss a été vaincu !");
                }
            }
        }

        // Vérifier si tous les mobs sont morts
        if (active.getMobs().isEmpty() && active.getBossId() == null && !active.isLootPhase()) {
            startLootPhase(active);
        }
    }

    // -----------------------------
    //  PHASE DE LOOT
    // -----------------------------

    private void startLootPhase(ActiveInstance active) {
        active.setLootPhase(true);
        InstanceDef def = active.getDef();

        // Notifier les joueurs
        for (UUID uuid : active.getUUIDs()) {
            UUID p = plugin.getUUIDManager().getUUID(uuid);
            if (p != null) {
                p.sendMessage("§a[Instance] Tous les monstres sont vaincus !");
                p.sendMessage("§e[Instance] Vous avez " + def.getLootPhaseSeconds() + 
                            "s pour récupérer les coffres.");
            }
        }

        // Countdown de la phase de loot
        final int[] counter = { def.getLootPhaseSeconds() };
        int taskId = plugin.getScheduler().runRepeating(() -> {
            counter[0]--;
            
            // Avertissement à 30s, 10s, 5s
            if (counter[0] == 30 || counter[0] == 10 || counter[0] == 5) {
                for (UUID uuid : active.getUUIDs()) {
                    UUID p = plugin.getUUIDManager().getUUID(uuid);
                    if (p != null) {
                        p.sendMessage("§e[Instance] Téléportation dans " + counter[0] + "s...");
                    }
                }
            }
            
            if (counter[0] <= 0) {
                plugin.getScheduler().cancelTask(active.getLootCountdownTaskId());
                endInstance(active);
            }
        }, 20, 20);
        
        active.setLootCountdownTaskId(taskId);
    }

    private void endInstance(ActiveInstance active) {
        active.setCompleted(true);
        InstanceDef def = active.getDef();

        // Traiter chaque joueur
        for (UUID uuid : active.getUUIDs()) {
            UUID p = plugin.getUUIDManager().getUUID(uuid);
            if (p == null) continue;

            // Compléter la quête de fin si configurée
            if (def.getEndQuestId() != null) {
                // Essayer les deux types de quête
                QuestAPI.completeGroupStage(p, def.getEndQuestId());
                QuestAPI.completeSoloStage(p, def.getEndQuestId());
            }

            // Téléporter à la sortie
            teleportToExit(p, def);
            
            // Nettoyer le HUD
            HudAPI.clearHud(p, HudLayer.INSTANCE);
            
            // Retirer de l'instance
            UUIDInstance.remove(uuid);
            
            p.sendMessage("§a[Instance] Instance terminée ! Bien joué !");
        }

        // Nettoyer l'instance active
        activeByKey.values().removeIf(a -> a == active);
        
        plugin.getLogger().info("[Instance] Instance terminée: " + def.getName());
    }

    // -----------------------------
    //  INTERACTION AVEC LES COFFRES
    // -----------------------------

    public void onChestQuestEntityInteract(UUID UUID, String questEntityId) {
        if (UUID == null) return;
        
        ActiveInstance active = UUIDInstance.get(UUID.getUuid());
        if (active == null || !active.isLootPhase()) {
            UUID.sendMessage("§c[Instance] Vous ne pouvez pas ouvrir ce coffre maintenant.");
            return;
        }

        InstanceDef def = active.getDef();
        ChestDef chestDef = def.getChests().stream()
                .filter(c -> c.getQuestEntityId().equals(questEntityId))
                .findFirst().orElse(null);

        if (chestDef == null) {
            UUID.sendMessage("§c[Instance] Coffre inconnu.");
            return;
        }

        if (active.isChestOpened(chestDef.getId())) {
            UUID.sendMessage("§c[Instance] Ce coffre a déjà été ouvert.");
            return;
        }

        // Marquer comme ouvert et donner le loot
        active.setChestOpened(chestDef.getId());
        giveLootFromTable(UUID, chestDef.getLootTableId());
    }

    private void giveLootFromTable(UUID UUID, String lootTableId) {
        LootTableDef table = lootTables.get(lootTableId);
        if (table == null) {
            UUID.sendMessage("§c[Instance] Aucun loot configuré pour ce coffre.");
            plugin.getLogger().warn("[Instance] Table de loot introuvable: " + lootTableId);
            return;
        }

        Random random = new Random();
        int itemsGiven = 0;
        
        for (LootEntry entry : table.getEntries()) {
            // Roll de chance
            if (random.nextDouble() > entry.getChance()) continue;
            
            // Quantité aléatoire entre min et max
            int amount = entry.getMin();
            if (entry.getMax() > entry.getMin()) {
                amount += random.nextInt(entry.getMax() - entry.getMin() + 1);
            }
            
            try {
                // Créer et donner l'item
                ItemStack stack = plugin.getItemManager().createItem(entry.getItemId(), amount);
                UUID.getInventory().addItem(stack);
                itemsGiven++;
                
                UUID.sendMessage("§a+ " + amount + "x " + entry.getItemId());
            } catch (Exception e) {
                plugin.getLogger().warn("[Instance] Erreur création item: " + entry.getItemId());
            }
        }

        if (itemsGiven > 0) {
            UUID.sendMessage("§a[Instance] Coffre ouvert ! Butin ajouté à votre inventaire.");
        } else {
            UUID.sendMessage("§e[Instance] Le coffre était vide...");
        }
    }

    public void distributeLoot(UUID UUID) {
        // Distribution automatique de loot pour un boss/événement
        // À implémenter selon les besoins
    }

    // -----------------------------
    //  QUITTER UNE INSTANCE
    // -----------------------------

    public void leaveInstance(UUID UUID) {
        if (UUID == null) return;
        
        ActiveInstance active = UUIDInstance.get(UUID.getUuid());
        if (active == null) {
            UUID.sendMessage("§c[Instance] Vous n'êtes pas dans une instance.");
            return;
        }
        
        exitInstance(UUID);
    }

    public void exitInstance(UUID UUID) {
        if (UUID == null) return;
        
        ActiveInstance active = UUIDInstance.get(UUID.getUuid());
        if (active == null) return;
        
        InstanceDef def = active.getDef();
        
        // Téléporter
        teleportToExit(UUID, def);
        
        // Nettoyer le HUD
        HudAPI.clearHud(UUID, HudLayer.INSTANCE);
        
        // Retirer de l'instance
        active.getUUIDs().remove(UUID.getUuid());
        UUIDInstance.remove(UUID.getUuid());
        
        // Si l'instance est vide, la nettoyer
        if (active.getUUIDs().isEmpty()) {
            activeByKey.values().removeIf(a -> a == active);
            plugin.getLogger().info("[Instance] Instance nettoyée (aucun joueur restant)");
        }
        
        UUID.sendMessage("§e[Instance] Vous avez quitté l'instance.");
    }

    public void completeInstanceForUUID(UUID UUID) {
        // Marquer l'instance comme terminée pour un joueur spécifique
        // Utile pour les statistiques
    }

    // -----------------------------
    //  UTILITAIRES
    // -----------------------------

    public boolean isBoss(LivingEntity entity) {
        if (entity == null) return false;
        
        for (ActiveInstance active : activeByKey.values()) {
            if (active.getBossId() != null && active.getBossId().equals(entity.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public int getCompletionCount(UUID UUID, String instanceId) {
        // TODO: Système de persistance des complétions
        return 0;
    }

    public boolean hasReachedLimit(UUID UUID, String instanceId) {
        // TODO: Système de limite quotidienne/hebdomadaire
        return false;
    }

    // -----------------------------
    //  SHUTDOWN
    // -----------------------------

    public void shutdown() {
        // Terminer toutes les instances actives
        for (ActiveInstance active : new ArrayList<>(activeByKey.values())) {
            for (UUID UUIDUuid : active.getUUIDs()) {
                UUID p = plugin.getUUIDManager().getUUID(UUIDUuid);
                if (p != null) {
                    teleportToExit(p, active.getDef());
                    HudAPI.clearHud(p, HudLayer.INSTANCE);
                    p.sendMessage("§c[Instance] Le serveur redémarre, vous avez été téléporté.");
                }
            }
        }

        UUIDInstance.clear();
        activeByKey.clear();
        
        plugin.getLogger().info("[InstanceManager] Shutdown complete.");
    }
}
