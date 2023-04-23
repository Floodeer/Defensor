package com.floodeer.plugins.towerdefense.game.mechanics;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.event.TowerKillEnemyEvent;
import com.floodeer.plugins.towerdefense.event.TowerSkillEvent;
import com.floodeer.plugins.towerdefense.event.TowerTargetEnemyEvent;
import com.floodeer.plugins.towerdefense.event.TowerUltimateEvent;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.utils.ItemFactory;
import com.floodeer.plugins.towerdefense.utils.MathUtils;
import com.floodeer.plugins.towerdefense.utils.Runner;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.MojangSkinGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Tower {

    @Getter  private String name;
    @Getter  private String displayName;

    private int cost;
    private double sell;

    @Getter @Setter private int damage;
    @Getter @Setter private int range;
    @Getter @Setter private int attackSpeed;
    @Getter @Setter private boolean multiTarget;
    @Getter @Setter private int maxTargets;

    @Getter private String skin;
    @Getter private ItemStack item;

    @Getter List<Enums.TowerEffects> effects;

    @Getter Enums.TowerUltimate ultimate;

    @Getter private Map<Integer, Integer> damageUpgrades;
    @Getter private Map<Integer, Integer> rangeUpgrades;

    public Tower(FileConfiguration config, String name) {
        this.name = name;

        String configPath = "Towers." + name + ".";

        this.displayName = config.getString(configPath + "Display-Name");
        this.ultimate = Enums.TowerUltimate.fromString(config.getString(configPath + "Ultimate"));
        this.cost = config.getInt(configPath + "Cost");
        this.sell = this.cost * .65;
        this.damage = config.getInt(configPath + "Damage");
        this.range = config.getInt(configPath + ".Range");
        this.range *= this.range;
        this.attackSpeed = config.getInt(configPath + "Attack-Speed");
        this.damageUpgrades = Maps.newHashMap();

        this.item = ItemFactory.parse(config.getString(configPath + "Item"));
        ItemFactory.name(item, Util.color(displayName));
        ItemFactory.lore(item, Util.colorList(config.getStringList(configPath + "Lore")));

        for (String upgrades : config.getStringList(configPath + "Upgrades.Damage")) {
            int damage = Integer.parseInt(upgrades.split(":")[0]);
            if (damage > this.damage)
                this.damageUpgrades.put(damage, Integer.parseInt(upgrades.split(":")[1]));
        }

        this.rangeUpgrades = Maps.newHashMap();
        for (String upgrades : config.getStringList(configPath +  "Upgrades.Range")) {
            int range = Integer.parseInt(upgrades.split(":")[0]);
            range *= range;
            if (range > this.range)
                this.rangeUpgrades.put(range, Integer.parseInt(upgrades.split(":")[1]));
        }

        this.multiTarget = config.getStringList(configPath + "Skills").contains("MULTI-TARGET");

        this.effects = Lists.newArrayList();
        if(!config.getStringList(configPath + "Skills").isEmpty()) {
            config.getStringList(configPath + "Skills").forEach(cur -> {
                if(Enums.TowerEffects.fromString(cur) != null)
                    effects.add(Enums.TowerEffects.fromString(cur));
            });
        }

        this.skin = config.getString(configPath + "Skin");
    }

    public PlacedTower summon(Game game, Player owner, Location loc) {
        HolographicDisplaysAPI api = HolographicDisplaysAPI.get(Defensor.get());
        Hologram hologram = api.createHologram(loc.clone().add(0, 2.55, 0));
        hologram.getLines().appendText(Util.color("&e&lTorre de &b&l" + owner.getName()));

        return new PlacedTower(game, owner, loc, hologram, this);
    }

    public class PlacedTower {
        @Getter private final Game game;
        @Getter private final Player owner;
        @Getter private final TowerEntity entity;

        @Getter @Setter private int killsMade = 0;
        @Getter @Setter private int damageDealt = 0;
        @Getter @Setter private int damage;
        @Getter @Setter private int range;
        @Getter @Setter private int ticks;
        @Getter @Setter private int ultimateEnergy;

        @Getter private final Map<Integer, Integer> damageUpgrades;
        @Getter private final Map<Integer, Integer> rangeUpgrades;
        @Getter private final Enums.TowerUltimate ultimate;
        
        @Getter private Location loc;

        @Getter @Setter private boolean ulting;

        private PlacedTower(Game game, Player owner, Location loc, Hologram hologram, Tower tower) {
            this.game = game;
            this.owner = owner;

            this.damage = tower.getDamage();
            this.range = tower.getRange();
            this.damageUpgrades = tower.getDamageUpgrades();
            this.rangeUpgrades = tower.getRangeUpgrades();
            this.ultimate = tower.getUltimate();
            this.loc = loc;

            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, displayName);
            npc.spawn(loc);

            this.entity = new TowerEntity(npc, getSkin(), hologram);
        }

        public void onTick() {
            ticks = ticks + 10;

            if(getUltimateEnergy() >= 100) {
                if(!entity.isUltimateHologram()) {
                    entity.getHologram().getLines().appendText(Util.color( "&a&lSupremo pronto!"));
                    entity.getHologram().setPosition(entity.getHologram().getPosition().toLocation().clone().add(0, .35, 0));
                    entity.setUltimateHologram(true);
                }
            }else{
                if(entity.isUltimateHologram()) {
                    entity.getHologram().getLines().remove(1);
                    entity.getHologram().setPosition(entity.getHologram().getPosition().toLocation().clone().subtract(0, .35, 0));
                    entity.setUltimateHologram(false);
                }
            }

            if(ticks >= attackSpeed) {
                updateUltimate(1);

                ticks = 0;

                int total = 0;
                for(Enemy.AliveEnemy enemy : Defensor.get().getMechanicsManager().getActiveEnemies(getGame())) {
                    if(enemy.getEntity().isValid() && isInRange(enemy.getEntity().getLocation())) {

                        TowerTargetEnemyEvent towerTargetEnemyEvent = new TowerTargetEnemyEvent(game, enemy, this);
                        Defensor.get().getServer().getPluginManager().callEvent(towerTargetEnemyEvent);
                        if(towerTargetEnemyEvent.isCancelled())
                            return;

                        ((LivingEntity)getEntity().getTowerEntity().getEntity()).swingMainHand();
                        if (enemy.getEntity().getHealth() - this.damage > 0.5D) {
                            enemy.getEntity().setHealth(enemy.getEntity().getHealth() - this.damage);
                            enemy.getEntity().damage(0.0D);
                            this.damageDealt += this.damage;
                        } else {
                            this.damageDealt = (int)(this.damageDealt + enemy.getEntity().getHealth());
                            enemy.getEntity().damage(this.damage);
                        }
                        damage(enemy, damage);
                        Util.createFakeNormalExplosion(enemy.getEntity().getLocation(), 10, 0.1F);
                        if(!effects.isEmpty()) {
                            Enums.TowerEffects effect = MathUtils.random(getEffects());
                            if(MathUtils.randomBoolean(12) && effect != null) {

                                TowerSkillEvent towerSkillEvent = new TowerSkillEvent(game, this, effect);
                                Defensor.get().getServer().getPluginManager().callEvent(towerSkillEvent);
                                effect = towerSkillEvent.getSkill();

                                switch (effect) {
                                    case BURN:
                                        enemy.getEntity().setFireTicks(100);
                                        break;
                                    case SLOW:
                                        enemy.setSpeed(enemy.getSpeed() * .4);
                                        Runner.make(Defensor.get()).delay(20).run(() -> enemy.setSpeed(enemy.getOriginalSpeed()));
                                        break;
                                    case STRIKE:
                                        enemy.getEntity().getLocation().getWorld().strikeLightningEffect(enemy.getEntity().getLocation());
                                        break;
                                    case EXPLOSION:
                                        Util.createFakeExplosion(enemy.getEntity().getLocation(), 10, 3F, true);
                                        break;
                                }
                            }
                        }
                        total++;
                        if(enemy.getEntity().isDead()) {
                            if(owner != null) {
                                game.addCoins(GamePlayer.get(owner), enemy.getKillCoins());
                                GamePlayer.get(owner).setExp(GamePlayer.get(owner).getExp() + enemy.getKillExp());
                                GamePlayer.get(owner).setKills(GamePlayer.get(owner).getKills() + 1);
                            }
                            this.setKillsMade(getKillsMade()+1);
                            updateUltimate(MathUtils.random(2, 5));

                            TowerKillEnemyEvent towerKillEnemyEvent = new TowerKillEnemyEvent(game, enemy, this);
                            Defensor.get().getServer().getPluginManager().callEvent(towerKillEnemyEvent);

                            if(!isMultiTarget() || total > getMaxTargets())
                                break;
                        }
                    }
                }
            }
        }

        public void delete() {
            if(entity.getHologram() != null)
                entity.getHologram().delete();

            if(entity.getTowerEntity() != null && entity.getTowerEntity().isSpawned()) {
                entity.getTowerEntity().destroy();
                CitizensAPI.getNPCRegistry().deregister(this.entity.getTowerEntity());
            }
        }

        public boolean isInRange(Location loc) {
            return ((this.getLoc().getX() - loc.getX()) * (this.getLoc().getX() - loc.getX()) + (this.getLoc().getZ() - loc.getZ()) * (this.getLoc().getZ() - loc.getZ()) < this.range);
        }

        public void openInterface(Player player) {

            Defensor.get().getIconCore().create(player, Util.color("&6&lConfigurações"), 9 * 3, event -> {
                if(event.getPosition() == 11) {
                    //TODO
                }else if(event.getPosition() == 13) {
                    if(getUltimateEnergy() >= 100) {
                        if(isUlting()) {
                            player.sendMessage(Util.color("&cSupremo já ativado!"));
                            return;
                        }
                        TowerUltimateEvent ult = new TowerUltimateEvent(game, this, getUltimate());
                        Defensor.get().getServer().getPluginManager().callEvent(ult);
                    }else{
                        player.sendMessage(Util.color("&cCarregando: &b" + getUltimateEnergy() + "%"));
                    }
                }else if(event.getPosition() == 15) {
                    //TODO
                }

                event.setWillClose(true);
                event.setWillDestroy(true);
            });

            for (int i = 0; i < 27; i++) {
                if (i <= 9 || i >= 18 || i == 10 || i == 12 || i == 14 || i == 16 || i == 17) {
                    Defensor.get().getIconCore().setOptionMetadata(player, i, ItemFactory.create(Material.RED_STAINED_GLASS_PANE, "&f ", Arrays.asList("&c ")));
                }
            }

            Defensor.get().getIconCore().setOptionMetadata(player, 11, ItemFactory.create(Material.ARROW, "&6Alcance"));
            Defensor.get().getIconCore().setOptionMetadata(player, 13, ItemFactory.create(Material.NETHER_STAR, "&4&lSupremo", Arrays.asList("", getUltimateEnergy() >= 100 ? "&a&lPronto para ser usado!" : "&cCarregando: &b" + getUltimateEnergy() + "%")));
            Defensor.get().getIconCore().setOptionMetadata(player, 15, ItemFactory.create(Material.DIAMOND_SWORD, "&cDano"));


            Defensor.get().getIconCore().show(player);
        }

        private void updateUltimate(int x) {
            if(getUltimateEnergy() + x >= 100)
                setUltimateEnergy(100);
            else
                setUltimateEnergy(getUltimateEnergy() + x);
        }

        public void damage(Enemy.AliveEnemy enemy, double damage) {
            if (enemy.getEntity().getHealth() - damage > 0.5D) {
                enemy.getEntity().setHealth(enemy.getEntity().getHealth() - damage);
                enemy.getEntity().damage(0.0D);
                this.setDamageDealt((int) (getDamage() + damage));

            } else {
                this.setDamageDealt((int) (getDamage() + enemy.getEntity().getHealth()));
                enemy.getEntity().damage(damage);
            }
        }
    }

    public class TowerEntity {
        @Getter private final NPC towerEntity;
        @Getter private final Hologram hologram;
        @Getter @Setter private boolean ultimateHologram;

        public TowerEntity(NPC npc, String skin, Hologram hologram) {
            this.towerEntity = npc;
            this.hologram = hologram;

            if(!Util.isURL(skin))
                updateSkin(skin + ".png", null, true);
            else
                updateSkin(null, skin, true);
        }

        public void updateSkin(String file, String url, boolean slim) {

          Runner.make(Defensor.get()).delay(20).run(() -> {
              if(this.towerEntity != null) {

                  towerEntity.getOrAddTrait(SkinTrait.class);
                  SkinTrait trait = towerEntity.getOrAddTrait(SkinTrait.class);

                  Defensor.get().getServer().getScheduler().runTaskAsynchronously(Defensor.get(), () -> { //Leave main thread
                      try {
                          JSONObject data;
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
                                  towerEntity.faceLocation(towerEntity.getStoredLocation());
                              } catch (IllegalArgumentException e) {
                                  e.printStackTrace();
                              }
                          });
                      }catch(Throwable t) {
                          t.printStackTrace();
                      }
                  });
               }
          });
        }
    }
}
