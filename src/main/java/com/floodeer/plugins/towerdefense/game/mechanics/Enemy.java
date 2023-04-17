package com.floodeer.plugins.towerdefense.game.mechanics;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.utils.Util;
import lombok.Getter;
import lombok.Setter;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Enemy {

    @Getter private final String name;

    @Getter @Setter private double speed;
    @Getter @Setter private int health;
    @Getter @Setter private int damage;
    @Getter @Setter private int killExp;
    @Getter @Setter private int killCoins;
    @Getter @Setter private int waveUnlocked;
    @Getter private int cost;

    @Getter private EntityType entity;
    @Getter private EntityEquipment entityEquipment;

    public Enemy(String name, FileConfiguration configFile, boolean boss) {
        this.name = name;

        String configPath = boss ? "Bosses." : "Enemies." + name + ".";

        cost = configFile.getInt(configPath + "Cost");
        health = configFile.getInt(configPath + "Health");
        damage = configFile.getInt(configPath + "Damage");
        speed = configFile.getInt(configPath + "Speed");
        killExp = configFile.getInt(configPath + "Exp-Per-Kill");
        killCoins = configFile.getInt(configPath + "Coins-Per-Kill");

        this.speed = 0.25D;
    }

    public AliveEnemy spawn(Location location, Enums.Difficulty difficulty) {
        LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, getEntity());
        switch(mob.getType()) {
            case ZOMBIE:
                ((Zombie)mob).setAdult();
                break;
            case SLIME:
                ((Slime)mob).setSize(3);
                break;
            case MAGMA_CUBE:
                ((MagmaCube)mob).setSize(3);
                break;
        }

        mob.setCollidable(false);
        mob.getEquipment().setItemInMainHand(entityEquipment.getItemOnHand());
        mob.getEquipment().setArmorContents(entityEquipment.toArray());

        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.getHealth() * difficulty.getHealthModifier());
        mob.setHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        mob.setCustomName(Util.getHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
        mob.setCustomNameVisible(true);
        mob.setMetadata("DefensorEntity", (MetadataValue)new FixedMetadataValue(Defensor.get(), this.name.toLowerCase()));
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, Integer.MAX_VALUE));
        if (mob.getVehicle() != null)
            mob.getVehicle().remove();

        return new AliveEnemy(mob, this);
    }


    public class AliveEnemy {
        private Game game;

        @Getter private LivingEntity entity;

        @Getter @Setter private int pathIndex;
        @Getter @Setter private int damage;
        @Getter @Setter private double health;
        @Getter @Setter private double originalSpeed;
        @Getter @Setter private double speed;
        @Getter @Setter private int killExp;
        @Getter @Setter private int killCoins;

        @Getter @Setter private BukkitTask speedTask;

        public AliveEnemy(LivingEntity entity, Enemy enemy) {
            this.pathIndex = 0;

            this.entity = entity;

            this.damage = enemy.getDamage();
            this.speed = enemy.getSpeed();
            this.originalSpeed = enemy.getSpeed();
            this.killCoins = enemy.getKillCoins();
            this.killExp = enemy.getKillExp();

            this.entity = entity;
        }
    }


    @Getter @Setter
    private static class EntityEquipment {
        private ItemStack helmet, chestplate, leggings, boots, itemOnHand;

        ItemStack[] toArray() {
            return new ItemStack[] {helmet, chestplate, leggings, boots};
        }
    }
}
