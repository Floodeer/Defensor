package com.floodeer.plugins.towerdefense.listeners;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.utils.Util;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSlimeSplit(EntityTeleportEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSlimeSplit(EntityExplodeEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSlimeSplit(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity")) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if(event.getEntity() instanceof LivingEntity && event.getEntity().hasMetadata("DefensorEntity")) {
            LivingEntity mob = (LivingEntity)event.getEntity();
            if(mob.getCustomName() != null) {
                mob.setCustomName(Util.getHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
            }
        }else if(event.getEntity() instanceof Player && GamePlayer.get(event.getEntity().getUniqueId()).isInGame()) {
            event.setCancelled(true);
        }
    }

    //Paper Events
    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent e) {
        Defensor.get().getMechanicsManager().getArenasEntities().values().forEach(cur -> {
            cur.forEach(entity -> {
                if(e.getEntity() == entity.getEntity()) {
                    e.setCancelled(true);
                }
            });
        });
    }
}
