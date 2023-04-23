package com.floodeer.plugins.towerdefense.listeners;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.event.TowerUltimateEvent;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.floodeer.plugins.towerdefense.utils.VelocityUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class UltimateListener implements Listener  {

    @EventHandler
    public void onTowerUltimate(TowerUltimateEvent e) {
        Tower.PlacedTower tower = e.getTower();
        Game game = e.getGame();

        tower.setUlting(true);
        tower.getEntity().getHologram().getLines().remove(1);
        tower.getEntity().getHologram().getLines().appendText(Util.color( "&c&l" + tower.getUltimate().toString()));

        switch(tower.getUltimate()) {
            case BURNING_SOUL:
                tower.getLoc().getWorld().playSound(tower.getLoc(), Sound.ENTITY_PLAYER_BREATH, 5, 0.2F);
                new BukkitRunnable() {
                    final Location location = tower.getLoc().clone();
                    int iterations = 0;
                    int damageDelay = 0;
                    boolean burst = false;
                    @Override
                    public void run() {
                        ++iterations;
                        ++damageDelay;
                        if(iterations >= 100 ||  tower.getEntity() == null || game == null || game.getState() != Enums.GameState.IN_GAME) {
                            if(tower.getEntity() != null) {
                                tower.setUlting(false);
                                tower.setUltimateEnergy(0);
                            }
                            cancel();
                            return;
                        }
                        for (int i = 0; i < 50; i++) {
                            Vector v = VelocityUtils.getRandomVector().multiply(10);

                            location.add(v);
                            location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0,0, 0);
                            location.subtract(v);
                        }
                        if(damageDelay >= 20) {
                            game.getEnemies().stream().filter(enemy -> enemy.getEntity().getLocation().distance(tower.getLoc()) < 10).forEach(cur -> {
                                tower.damage(cur, 1.5);
                                cur.getEntity().setFireTicks(20);
                                location.getWorld().spawnParticle(Particle.FLAME, cur.getEntity().getLocation(), 25, .4, .4,.4, .1);

                                if(!burst) {
                                    tower.damage(cur, 20);
                                    burst = true;
                                }
                            });
                            damageDelay = 0;
                        }
                    }
                }.runTaskTimer(Defensor.get(), 0, 1);
                break;
        }
    }
}
