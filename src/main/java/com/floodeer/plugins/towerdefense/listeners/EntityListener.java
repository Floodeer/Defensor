package com.floodeer.plugins.towerdefense.listeners;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.floodeer.plugins.towerdefense.Defensor;
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
    public void onEntityDamageEvent(EntityDamageEvent event) {

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
