package com.mmo.core.listeners;

import com.hytale.server.entity.LivingEntity;
import com.hytale.server.events.entity.EntityDamageEvent;
import com.hytale.server.events.entity.EntityHealEvent;
import com.hytale.server.events.UUID.UUIDBlockEvent;
import com.hytale.server.events.UUID.UUIDUseSkillEvent;
import com.hytale.server.UUID.UUID;
import com.mmo.core.CorePlugin;
import com.mmo.core.managers.ThreatManager;

/**
 * Listener pour les événements liés au système de menaces
 * Intégré dans CorePlugin
 */
public class ThreatListener {

    private final CorePlugin plugin;
    private final ThreatManager threatManager;

    public ThreatListener(CorePlugin plugin) {
        this.plugin = plugin;
        this.threatManager = plugin.getThreatManager();
    }

    /**
     * Gère les dégâts infligés aux mobs
     * Ajoute de la menace au joueur qui inflige les dégâts
     */
    public void onEntityDamage(EntityDamageEvent event) {
        // Vérifier que c'est un joueur qui attaque
        if (!(event.getDamager() instanceof UUID)) return;
        
        // Vérifier que la cible est un LivingEntity (mob)
        if (!(event.getEntity() instanceof LivingEntity)) return;

        UUID UUID = (UUID) event.getDamager();
        LivingEntity mob = (LivingEntity) event.getEntity();

        double damage = event.getDamage();
        
        // Ajouter la menace via le manager
        threatManager.addDamageThreat(mob, UUID, damage);
    }

    /**
     * Gère les soins effectués
     * Les soins génèrent de la menace (50% par défaut)
     */
    public void onEntityHeal(EntityHealEvent event) {
        // Vérifier que c'est un joueur qui soigne
        if (!(event.getHealer() instanceof UUID)) return;

        UUID healer = (UUID) event.getHealer();
        
        // Pour chaque mob en combat avec le groupe, ajouter de la menace de soin
        // TODO: Implémenter la logique de détection des mobs en combat
        // Pour l'instant, on pourrait itérer sur tous les mobs proches
        
        double healAmount = event.getAmount();
        
        // Exemple : Récupérer tous les mobs dans un rayon
        // getNearbyMobs(healer, 30.0).forEach(mob -> {
        //     threatManager.addHealThreat(mob, healer, healAmount);
        // });
    }

    /**
     * Gère les dégâts bloqués par un tank
     * Génère de la menace supplémentaire (150% par défaut)
     */
    public void onUUIDBlock(UUIDBlockEvent event) {
        UUID UUID = event.getUUID();
        
        // Vérifier que l'attaquant est un LivingEntity
        if (!(event.getAttacker() instanceof LivingEntity)) return;

        LivingEntity mob = (LivingEntity) event.getAttacker();
        double blockedDamage = event.getBlockedDamage();

        // Ajouter la menace de blocage
        threatManager.addBlockThreat(mob, UUID, blockedDamage);
    }

    /**
     * Gère l'utilisation de compétences
     * Détecte notamment les compétences de provocation (taunt)
     */
    public void onUUIDUseSkill(UUIDUseSkillEvent event) {
        UUID UUID = event.getUUID();
        String skillId = event.getSkillId();

        // Vérifier si c'est une compétence de taunt
        if ("taunt".equalsIgnoreCase(skillId) || 
            "provoke".equalsIgnoreCase(skillId) || 
            "challenge".equalsIgnoreCase(skillId)) {
            
            // Vérifier que la cible est un LivingEntity
            if (!(event.getTarget() instanceof LivingEntity)) return;

            LivingEntity mob = (LivingEntity) event.getTarget();
            
            // Appliquer le taunt
            threatManager.taunt(mob, UUID);
        }
        
        // Autres compétences qui génèrent de la menace
        else if ("shout".equalsIgnoreCase(skillId) || 
                 "roar".equalsIgnoreCase(skillId)) {
            // AoE taunt - pourrait affecter plusieurs mobs
            // TODO: Implémenter selon les besoins
        }
    }

    /**
     * Méthode helper pour récupérer les mobs proches
     * TODO: Adapter selon l'API Hytale
     */
    // private List<LivingEntity> getNearbyMobs(UUID UUID, double radius) {
    //     return UUID.getWorld()
    //         .getNearbyEntities(UUID.getLocation(), radius)
    //         .stream()
    //         .filter(e -> e instanceof LivingEntity)
    //         .filter(e -> !(e instanceof UUID))
    //         .map(e -> (LivingEntity) e)
    //         .collect(Collectors.toList());
    // }
}
