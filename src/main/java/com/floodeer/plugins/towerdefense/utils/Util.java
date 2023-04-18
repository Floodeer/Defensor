package com.floodeer.plugins.towerdefense.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    private static String healthString = "";

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static List<String> colorList(List<String> list) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String str : list) {
            strings.add(ChatColor.translateAlternateColorCodes('&', color(str)));
        }

        return strings;
    }



    public static String getHealth(double health, double maxHealth) {
        if(healthString.equalsIgnoreCase("")) {
            for (int i = 0; i < 10; i++) {
                healthString = healthString + StringEscapeUtils.unescapeJava("▌");
            }
        }

        if (health == maxHealth)
            return ChatColor.GREEN + healthString;
        int i = (int)(health / maxHealth * 10.0D);
        if (i < 1)
            return ChatColor.GRAY + healthString;
        return ChatColor.GREEN + healthString.substring(0, i) + ChatColor.GRAY + healthString.substring(i, healthString.length());
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
        //TODO remove EffectLib dependency
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

    public static String getStringFromLocation(Location loc, boolean center) {
        return loc.getWorld().getName() + ", " + (loc.getBlockX() + (center ? 0.5D : 0.0D)) + ", " + loc.getBlockY() + ", " + (loc.getBlockZ() + (center ? 0.5D : 0.0D)) + ", " + loc.getYaw() + ", " + loc.getPitch();
    }

    public static Location getLocationFromString(String paramString) {
        String[] locationData = paramString.split(", ");
        World world = Bukkit.getWorld(locationData[0]);
        double x = Double.parseDouble(locationData[1]), y = Double.parseDouble(locationData[2]), z = Double.parseDouble(locationData[3]);
        float pitch = Float.parseFloat(locationData[4]), yaw = Float.parseFloat(locationData[5]);
        return new Location(world, x, y, z, pitch, yaw);
    }
}
