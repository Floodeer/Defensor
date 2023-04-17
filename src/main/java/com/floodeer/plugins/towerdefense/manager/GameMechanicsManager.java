package com.floodeer.plugins.towerdefense.manager;

import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.towers.Tower;
import com.floodeer.plugins.towerdefense.utils.update.UpdateEvent;
import com.floodeer.plugins.towerdefense.utils.update.UpdateType;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameMechanicsManager implements Listener {

    @Getter private Map<Game, List<Enemy>> arenas;
    @Getter private Map<Game, List<Enemy.AliveEnemy>> arenasEntities;

    @Getter private LinkedHashMap<String, Enemy> bosses;
    @Getter private LinkedHashMap<String, Enemy> enemies;
    @Getter private LinkedHashMap<String, Tower> towers;

    private final List<Enemy.AliveEnemy> remove = Lists.newArrayList();

    public void spawn(Game game, Enemy paramEnemy, int amount) {
        List<Enemy> list = this.arenas.get(game);
        for (int i = 0; i < amount;) {
            list.add(paramEnemy);
            i++;
        }
    }

    @EventHandler
    public void onGameTick(UpdateEvent event) {
        if(event.getType() == UpdateType.FAST) { //ticks every 500ms (10 ticks, 0.5s)
            for (Game game : arenasEntities.keySet()) {
                List<Enemy.AliveEnemy> enemies = arenasEntities.get(game);
                removeInvalidEnemies(enemies);
                moveEnemies(game, enemies);
            }
        }else if(event.getType() == UpdateType.SEC) {//ticks every 1000ms (20 ticks, 1s)
            for (Game arena : arenas.keySet()) {
                List<Enemy> list = arenas.get(arena);
                if (list.isEmpty())
                    continue;
                arenasEntities.get(arena).add(list.get(0).spawn(arena.getArena().getLocation("MOB_SPAWN"), arena.getDifficulty()));
                list.remove(0);
            }
        }
    }

    public List<Enemy.AliveEnemy> getActiveEnemies(Game game) {
        return this.arenasEntities.get(game);
    }

    private void removeInvalidEnemies(List<Enemy.AliveEnemy> enemies) {
        List<Enemy.AliveEnemy> remove = Lists.newArrayList();
        for (Enemy.AliveEnemy enemy : enemies) {
            if (!enemy.getEntity().isValid()) {
                remove.add(enemy);
            }
        }
        enemies.removeAll(remove);
    }

    private void moveEnemies(Game arena, List<Enemy.AliveEnemy> enemies) {
        List<Enemy.AliveEnemy> remove = Lists.newArrayList();
        for (Enemy.AliveEnemy enemy : enemies) {
            LivingEntity entity = enemy.getEntity();
            if (!entity.isValid()) {
                remove.add(enemy);
                continue;
            }
            Location targetLocation = arena.getArena().getPathIndex().get(enemy.getPathIndex());
            Vector direction = targetLocation.toVector().subtract(entity.getLocation().toVector()).normalize();
            entity.setVelocity(direction.multiply(-enemy.getSpeed()));
            double distanceSquared = entity.getLocation().distanceSquared(targetLocation);
            if (distanceSquared < 1.0D + enemy.getSpeed()) {
                enemy.setPathIndex(enemy.getPathIndex()+1);
                if (enemy.getPathIndex() >= arena.getArena().getPathIndex().size()) {
                    entity.remove();
                    remove.add(enemy);
                    arena.damage(enemy.getDamage());
                }
            }
        }
        enemies.removeAll(remove);
    }
}
