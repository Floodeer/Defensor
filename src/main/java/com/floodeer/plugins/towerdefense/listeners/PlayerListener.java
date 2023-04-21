package com.floodeer.plugins.towerdefense.listeners;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.utils.Runner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new Runner(Defensor.get()).delay(8).run(() -> Defensor.get().getPlayerManager().addPlayer(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Defensor.get().getPlayerManager().removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Defensor.get().getPlayerManager().removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(GamePlayer.get(e.getPlayer()).isInGame())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(GamePlayer.get(e.getPlayer()).isInGame())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(FoodLevelChangeEvent e) {
        if(GamePlayer.get(e.getEntity().getUniqueId()).isInGame())
            e.setCancelled(true);
    }
}
