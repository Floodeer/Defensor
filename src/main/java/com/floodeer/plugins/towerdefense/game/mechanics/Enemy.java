package com.floodeer.plugins.towerdefense.game.mechanics;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.utils.ItemFactory;
import com.floodeer.plugins.towerdefense.utils.Util;
import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    @Getter private final int cost;

    @Getter private final EntityType entity;
    @Getter private final EntityEquipment entityEquipment;

    public Enemy(FileConfiguration configFile, String name, boolean boss) {
        this.name = name;

        String configPath = boss ? "Bosses." : "Enemies." + name + ".";

        entity = EntityType.valueOf(configFile.getString(configPath + "Entity"));
        cost = configFile.getInt(configPath + "Cost");
        health = configFile.getInt(configPath + "Health");
        damage = configFile.getInt(configPath + "Damage");
        speed = configFile.getDouble(configPath + "Speed");
        killExp = configFile.getInt(configPath + "Exp-Per-Kill");
        killCoins = configFile.getInt(configPath + "Coins-Per-Kill");

        entityEquipment = new EntityEquipment(new ItemStack[]{
                ItemFactory.parse(configFile.getString(configPath + "Equipment.Helmet")),
                ItemFactory.parse(configFile.getString(configPath + "Equipment.Chestplate")),
                ItemFactory.parse(configFile.getString(configPath + "Equipment.Leggings")),
                ItemFactory.parse(configFile.getString(configPath + "Equipment.Boots")),
                ItemFactory.parse(configFile.getString(configPath + "Equipment.Hand"))});
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
        mob.getEquipment().setItemInMainHand(entityEquipment.getItemOnHand() == null ? ItemFactory.create(Material.AIR) : entityEquipment.getHandItem());
        mob.getEquipment().setHelmet(getEntityEquipment().getHelmet());
        mob.getEquipment().setChestplate(getEntityEquipment().getChestplate());
        mob.getEquipment().setLeggings(getEntityEquipment().getLeggings());
        mob.getEquipment().setBoots(getEntityEquipment().getBoots());
        
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.getHealth() * difficulty.getHealthModifier());
        mob.setHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        mob.setCustomName(Util.getHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
        mob.setCustomNameVisible(true);
        mob.setMetadata("DefensorEntity", new FixedMetadataValue(Defensor.get(), this.name.toLowerCase()));

        mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999, 9999));
        if (mob.getVehicle() != null)
            mob.getVehicle().remove();

        return new AliveEnemy(mob, this);
    }


    public class AliveEnemy {
        private Game game;

        @Getter private LivingEntity entity;

        @Getter @Setter private int pathIndex;
        @Getter @Setter private Location targetLocation;
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


    private static class EntityEquipment {

        @Getter
        private final ItemStack helmet, chestplate, leggings, boots, itemOnHand;

        public EntityEquipment(ItemStack[] items) {
                helmet = items[0];
                chestplate = items[1];
                leggings = items[2];
                boots = items[3];
                itemOnHand = items[4];
        }

        ItemStack getHandItem() {
            return itemOnHand;
        }

        ItemStack[] getArmor() {
            return new ItemStack[] {helmet, chestplate, leggings, boots};
        }
    }
}
