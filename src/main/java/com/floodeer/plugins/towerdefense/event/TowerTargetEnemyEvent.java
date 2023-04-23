package com.floodeer.plugins.towerdefense.event;

import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerTargetEnemyEvent extends Event implements Cancellable {

    private boolean isCancelled;

    private final Enemy.AliveEnemy enemy;
    private final Tower.PlacedTower tower;
    private final Game game;

    public TowerTargetEnemyEvent(Game game, Enemy.AliveEnemy enemy, Tower.PlacedTower tower) {
        this.enemy = enemy;
        this.tower = tower;
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public Enemy.AliveEnemy getEnemy() {
        return enemy;
    }

    public Tower.PlacedTower getTower() {
        return tower;
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
