package com.floodeer.plugins.towerdefense.event;

import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerKillEnemyEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Enemy.AliveEnemy enemy;
    private final Tower.PlacedTower tower;
    private final Game game;

    public TowerKillEnemyEvent(Game game, Enemy.AliveEnemy enemy, Tower.PlacedTower tower) {
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
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
