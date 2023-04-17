package com.floodeer.plugins.towerdefense.utils;

import com.floodeer.plugins.towerdefense.Defensor;

import de.slikey.effectlib.effect.ExplodeEffect;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.json.simple.JSONObject;

import java.util.Random;

public class Util {

    private static String healthString;

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; ) {
            sb.append(healthString).append("▮");
            i++;
        }
        healthString = sb.toString();
    }

    public static String getHealth(double health, double maxHealth) {

        if (health == maxHealth)
            return ChatColor.GREEN + healthString;
        int i = (int)(health / maxHealth * 10.0D);
        if (i < 1)
            return ChatColor.GRAY + healthString;
        return ChatColor.GREEN + healthString.substring(0, i) + ChatColor.GRAY + healthString.substring(i);
    }

    public static String createSpacer() {
        String build = "";

        for (int i = 0; i < 15; i++) {
            build = add(build);
        }

        return build;
    }
    private static String add(String build) {
        Random random = new Random();

        int r = random.nextInt(7) + 1;

        build = build + ChatColor.values()[r];

        return build;
    }

    public static void createFakeExplosion(Location loc, int amount, float size) {
        ExplodeEffect effect = new ExplodeEffect(Defensor.get().getEffectManager());
        effect.setDynamicOrigin(new DynamicLocation(loc));
        effect.iterations = 1;
        effect.amount = amount;
        effect.particleSize = size;
        effect.start();
    }

    public static String saveLocation(Location location, boolean yawPitch) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("world", location.getWorld().getName());
        jsonObject.put("x", location.getX());
        jsonObject.put("y", location.getY());
        jsonObject.put("z", location.getZ());
        if (yawPitch) {
            jsonObject.put("yaw", location.getYaw());
            jsonObject.put("pitch", location.getPitch());
        }
        return jsonObject.toJSONString();
    }

    public static Location getLocation(JSONObject fromJson, boolean yawpitch) {
        double x = (double) fromJson.get("x");
        double y = (double) fromJson.get("y");
        double z = (double) fromJson.get("z");
        String world = (String) fromJson.get("world");
        if (yawpitch) {
            double pitch = (double) fromJson.get("pitch");
            double yaw = (double) fromJson.get("yaw");
            return new Location(Bukkit.getWorld(world), Math.floor(x), Math.floor(y), Math.floor(z), (float) yaw,
                    (float) pitch);
        } else {
            return new Location(Bukkit.getWorld(world), Math.floor(x), Math.floor(y), Math.floor(z));
        }
    }

    public static Location getLocationExact(JSONObject fromJson, boolean yawpitch) {
        double x = (double) fromJson.get("x");
        double y = (double) fromJson.get("y");
        double z = (double) fromJson.get("z");
        String world = (String) fromJson.get("world");
        if (yawpitch) {
            double pitch = (double) fromJson.get("pitch");
            double yaw = (double) fromJson.get("yaw");
            return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
        } else {
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
    }
}
