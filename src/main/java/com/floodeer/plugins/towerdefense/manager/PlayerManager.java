package com.floodeer.plugins.towerdefense.manager;


import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, GamePlayer> onlinePlayers = Maps.newHashMap();

    public PlayerManager() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player.getUniqueId());
        }
    }

    public void addPlayer(UUID uuid) {
        if (!this.onlinePlayers.containsKey(uuid)) {
            final GamePlayer gamePlayer = new GamePlayer(uuid);
            onlinePlayers.put(uuid, gamePlayer);
        }
    }

    public void addPlayer(Player player) {
        boolean present = getAll().stream().map(GamePlayer::getPlayer).anyMatch(cur -> cur.equals(player));
        if (!present) {
            final GamePlayer gamePlayer = new GamePlayer(player);
            onlinePlayers.put(gamePlayer.getUUID(), gamePlayer);
        }
    }

    public void removePlayer(UUID uuid) {
        onlinePlayers.remove(uuid);
    }

    public GamePlayer getPlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
    }

    public GamePlayer getPlayer(Player player) {
        return onlinePlayers.values().stream().filter(cur -> cur.getPlayer().equals(player)).findAny().orElse(null);
    }

    public GamePlayer getPlayer(String name) {
        for (GamePlayer gPlayer: onlinePlayers.values()) {
            if (gPlayer.getName().equals(name)) {
                return gPlayer;
            }
        }
        return null;
    }

    public Collection<GamePlayer> getAll() {
        return onlinePlayers.values();
    }

}