package com.floodeer.plugins.towerdefense.event;

import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerTickEvent extends Event {

    private final int tick;
    private final Game game;
    private final Tower.PlacedTower tower;

    public TowerTickEvent(Game game, Tower.PlacedTower tower, int tick) {
        this.game = game;
        this.tower = tower;
        this.tick = tick;
    }

    public Game getGame() {
        return game;
    }

    public int getTick() {
        return tick;
    }

    public Tower.PlacedTower getTower() {
        return tower;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
