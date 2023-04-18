package com.floodeer.plugins.towerdefense.listeners;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.utils.Runner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
}
