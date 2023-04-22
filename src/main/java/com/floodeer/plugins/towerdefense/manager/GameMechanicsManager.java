package com.floodeer.plugins.towerdefense.manager;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import com.floodeer.plugins.towerdefense.utils.update.UpdateEvent;
import com.floodeer.plugins.towerdefense.utils.update.UpdateType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameMechanicsManager implements Listener {

    @Getter private final Map<Game, List<Enemy>> arenas;
    @Getter private final Map<Game, List<Enemy.AliveEnemy>> arenasEntities;

    @Getter private final LinkedHashMap<String, Enemy> bosses;
    @Getter private final LinkedHashMap<String, Enemy> enemies;
    @Getter private final LinkedHashMap<String, Tower> towers;


    public GameMechanicsManager() {
        arenas = Maps.newHashMap();
        arenasEntities = Maps.newHashMap();

        bosses = Maps.newLinkedHashMap();
        enemies = Maps.newLinkedHashMap();
        towers = Maps.newLinkedHashMap();

        loadEnemies();
        loadTowers();
    }

    public void start(Game game) {
        this.arenas.put(game, Lists.newArrayList());
        this.arenasEntities.put(game, Lists.newArrayList());
    }

    public void stop(Game game) {
        if(!arenas.containsKey(game))
            return;

        this.arenas.remove(game);
        for (Enemy.AliveEnemy aliveEnemy : this.arenasEntities.get(game)) {
            if (aliveEnemy.getEntity().isValid())
                aliveEnemy.getEntity().remove();
        }
        this.arenasEntities.remove(game);
    }

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
                if(game.getState() != Enums.GameState.IN_GAME)
                    return;

                List<Enemy.AliveEnemy> enemies = arenasEntities.get(game);
                removeInvalidEnemies(enemies);
                moveEnemies(game, enemies);
            }
        }else if(event.getType() == UpdateType.SEC) {//Spawn controller, ticks every 1000ms (20 ticks, 1s)
            for (Game game : arenas.keySet()) {
                if(game.getState() != Enums.GameState.IN_GAME)
                    return;
                List<Enemy> list = arenas.get(game);
                if (list.isEmpty())
                    continue;

                arenasEntities.get(game).add(list.get(0).spawn(game.getArena().getLocation("MOB_SPAWN"), game.getDifficulty()));
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

            Location targetLocation = arena.getArena().getPath().get(enemy.getPathIndex());
            enemy.getEntity().setVelocity(enemy.getEntity().getLocation().toVector().subtract(targetLocation.toVector()).normalize().multiply(-enemy.getSpeed()));
            double distanceSquared = entity.getLocation().distanceSquared(targetLocation);

            if (distanceSquared < 1.0D) {
                enemy.setPathIndex(enemy.getPathIndex()+1);
                if (enemy.getPathIndex() >= arena.getArena().getPath().size()) {
                    enemy.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, enemy.getEntity().getLocation(), 5, 0, 0, 0, 0.1F);
                    entity.remove();
                    remove.add(enemy);
                    arena.damage(enemy.getDamage());
                }
            }
        }
        enemies.removeAll(remove);
    }

    private void loadEnemies() {
        this.enemies.clear();
        File file = new File(Defensor.get().getDataFolder(), "enemies.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if(config.getConfigurationSection("Enemies") == null || config.getConfigurationSection("Enemies").getKeys(false).isEmpty()) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(Defensor.get().getResource("enemies.yml"))));
            config.options().copyDefaults(true);

            try {
                config.save(file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        for (String enemyName : config.getConfigurationSection("Enemies").getKeys(false)) {
            this.enemies.put(enemyName.toLowerCase(), new Enemy(config, enemyName, false));
        }

        if (config.getConfigurationSection("Bosses") != null && !config.getConfigurationSection("Bosses").getKeys(false).isEmpty()) {
            for (String bossName : config.getConfigurationSection("Bosses").getKeys(false)) {
                this.bosses.put(bossName.toLowerCase(), new Enemy(config, bossName, true));
            }
        }
    }

    private void loadTowers() {
        this.towers.clear();

        File file = new File(Defensor.get().getDataFolder(), "towers.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if(config.getConfigurationSection("Towers") == null || config.getConfigurationSection("Towers").getKeys(false).isEmpty()) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(Defensor.get().getResource("towers.yml"))));
            config.options().copyDefaults(true);
            try {
                config.save(file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        for (String towerName : config.getConfigurationSection("Towers").getKeys(false)) {
            this.towers.put(towerName.toLowerCase(), new Tower(config, towerName));
        }
    }
}
