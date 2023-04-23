package com.floodeer.plugins.towerdefense.event;

import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TowerSkillEvent extends Event implements Cancellable {

    private boolean isCancelled;

    private final Tower.PlacedTower tower;
    private final Game game;
    private Enums.TowerEffects skill;

    public TowerSkillEvent(Game game, Tower.PlacedTower tower, Enums.TowerEffects skill) {
        this.tower = tower;
        this.skill = skill;
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public Tower.PlacedTower getTower() {
        return tower;
    }

    public Enums.TowerEffects getSkill() {
        return skill;
    }

    public void setSkill(Enums.TowerEffects skill) {
        this.skill = skill;
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
