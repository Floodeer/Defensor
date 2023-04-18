package com.floodeer.plugins.towerdefense.listeners;

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
    public void onSlimeSplit(EntityPickupItemEvent event) {
        if (event.getEntity().hasMetadata("DefensorEntity"))
            event.setCancelled(true);
    }
}
