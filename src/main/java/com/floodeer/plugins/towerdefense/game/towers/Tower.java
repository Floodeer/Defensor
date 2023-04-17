package com.floodeer.plugins.towerdefense.game.towers;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.utils.Util;
import lombok.Getter;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.MojangSkinGenerator;
import org.bukkit.Location;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Tower {

    private String name;
    private String displayName;

    private int cost;
    private double sell;

    @Getter @Setter private int damage;
    @Getter @Setter private int range;
    @Getter @Setter private int attackSpeed;
    @Getter @Setter private boolean multiTarget;
    @Getter @Setter private int maxTargets;

    @Getter TowerEntity tower;
    @Getter Enums.TowerEffects effects;

    @Getter private Map<Integer, Integer> damageUpgrades;
    @Getter private Map<Integer, Integer> rangeUpgrades;
    @Getter private Map<PlacedTower, String> towerSpecialSkill;

    public class PlacedTower {
        @Getter private final Game game;
        @Getter private final Player owner;
        @Getter private final TowerEntity entity;

        private Location center;
        private List<TowerLocation> locations;

        @Getter @Setter private int killsMade = 0;
        @Getter @Setter private int damageDealt = 0;
        @Getter @Setter private int damage;
        @Getter @Setter private int range;
        @Getter @Setter private int ticks;
        @Getter @Setter private int ultimateEnergy;

        @Getter private final Map<Integer, Integer> damageUpgrades;
        @Getter private final Map<Integer, Integer> rangeUpgrades;
        @Getter private final Map<PlacedTower, String> towerSpecialSkill;

        private Inventory towerInterface;

        private PlacedTower(Game game, Player owner, List<TowerLocation> locations, Location center, Hologram hologram, Tower tower) {
            this.game = game;
            this.owner = owner;
            this.center = center;
            this.locations = locations;

            this.entity = tower.getTower();

            this.damage = tower.getDamage();
            this.range = tower.getRange();
            this.damageUpgrades = tower.getDamageUpgrades();
            this.rangeUpgrades = tower.getRangeUpgrades();
            this.towerSpecialSkill = tower.getTowerSpecialSkill();
        }

        public void onTick() {
            ticks+=10;
            if(ticks >= attackSpeed) {
                ticks = 0;
            }

            int total = 0;
            for(Enemy.AliveEnemy enemy : Defensor.get().getMechanicsManager().getActiveEnemies(getGame())) {
                if(enemy.getEntity().isValid() && isInRange(enemy.getEntity().getLocation())) {
                    if (enemy.getEntity().getHealth() - this.damage > 0.5D) {
                        enemy.getEntity().setHealth(enemy.getEntity().getHealth() - this.damage);
                        enemy.getEntity().damage(0.0D);
                        this.damageDealt += this.damage;
                    } else {
                        this.damageDealt= (int)(this.damageDealt + enemy.getEntity().getHealth());
                        enemy.getEntity().damage(this.damage);
                    }
                    Util.createFakeExplosion(enemy.getEntity().getLocation(), 1, 1F);
                    switch (effects) {
                        case BURN:
                            enemy.getEntity().setFireTicks(100);
                        case SLOW:
                            enemy.setSpeed(enemy.getSpeed() * .4);
                            if (enemy.getSpeedTask() != null)
                                enemy.getSpeedTask().cancel();

                            enemy.setSpeedTask(new BukkitRunnable() {
                                @Override
                                public void run() {
                                    enemy.setSpeed(enemy.getOriginalSpeed());
                                }
                            }.runTaskLater(Defensor.get(), 20));
                            break;
                        case STRIKE:
                            enemy.getEntity().getLocation().getWorld().strikeLightningEffect(enemy.getEntity().getLocation());
                        case EXPLOSION:
                            Util.createFakeExplosion(enemy.getEntity().getLocation(), 10, 3F);
                    }
                    total++;
                    if(enemy.getEntity().isDead()) {
                        if(owner != null) {
                            //TODO add kill logic
                        }
                        this.setKillsMade(getKillsMade()+1);

                        if(!isMultiTarget() || total > getMaxTargets())
                            break;
                    }
                }
            }
        }

        public void delete() {
            if(entity.getHologram() != null)
                entity.getHologram().delete();

            if(entity.getTowerEntity() != null && entity.getTowerEntity().isValid())
                entity.getTowerEntity().remove();
        }

        public boolean contains(Location location) {
            return locations.stream().anyMatch(cur -> cur.contains(location));
        }

        public boolean isInRange(Location param1Location) {
            return this.center.getX() - param1Location.getX() * this.center.getX() - param1Location.getX() + this.center.getZ() - param1Location.getZ() * this.center.getZ() - param1Location.getZ() < this.range;
        }
    }

    private class TowerEntity {
        @Getter private final NPC towerEntity;
        @Getter private final Hologram hologram;

        public TowerEntity(NPC npc, String url, String file, boolean slim, Hologram hologram) {
            this.towerEntity = npc;
            this.hologram = hologram;

            updateSkin(url, file, slim);
        }

        public void updateSkin(String file, String url, boolean slim) {
            if(this.towerEntity != null) {

                SkinnableEntity entity = (SkinnableEntity)this.towerEntity;
                SkinTrait trait = entity.getNPC().getOrAddTrait(SkinTrait.class);

                Defensor.get().getServer().getScheduler().runTaskAsynchronously(Defensor.get(), () -> { //Leave main thread
                    try {
                        JSONObject data = null;
                        if(file != null) {
                            File skinsFolder = new File(Defensor.get().getDataFolder(), "skins");
                            File skin = new File(skinsFolder, file);
                            if (!skin.exists() || !skin.isFile() || skin.isHidden()  || !skin.getParentFile().equals(skinsFolder)) return;
                            data = MojangSkinGenerator.generateFromPNG(Files.readAllBytes(skin.toPath()), slim);
                        }else{
                            data = MojangSkinGenerator.generateFromURL(url, slim);
                        }

                        String uuid = (String) data.get("uuid");
                        JSONObject texture = (JSONObject) data.get("texture");
                        String textureEncoded = (String) texture.get("value");
                        String signature = (String) texture.get("signature");

                        Defensor.get().getServer().getScheduler().runTask(Defensor.get(), () -> { //Back to main thread
                            try {
                                trait.setSkinPersistent(uuid, signature, textureEncoded);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        });
                    }catch(Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }
    }
    private class TowerLocation {

        @Getter private final int x;
        @Getter private final int y;
        @Getter private final int z;

        private TowerLocation(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private TowerLocation(Location loc) {
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
        }

        public boolean contains(Location location) {
            return location.getX() == getX() && location.getY() == getY() && location.getZ() == getZ();
        }

        public boolean equals(Object object) {
            TowerLocation towerLocation = (TowerLocation) object;
            return this.x == towerLocation.getX() && this.y == towerLocation.getY() && this.z == towerLocation.getZ();
        }
    }
}
