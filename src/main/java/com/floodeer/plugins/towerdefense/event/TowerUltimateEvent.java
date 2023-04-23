package com.floodeer.plugins.towerdefense.event;

import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerUltimateEvent extends Event implements Cancellable {

    private boolean isCancelled;

    private final Game game;
    private final Tower.PlacedTower tower;
    private final Enums.TowerUltimate ultimate;

    public TowerUltimateEvent(Game game, Tower.PlacedTower tower, Enums.TowerUltimate ult) {
        this.game = game;
        this.tower = tower;
        this.ultimate = ult;
    }

    public Game getGame() {
        return game;
    }


    public Tower.PlacedTower getTower() {
        return tower;
    }

    public Enums.TowerUltimate getUltimate() {
        return ultimate;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
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
