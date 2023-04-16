package com.floodeer.plugins.towerdefense.utils;

import com.floodeer.plugins.towerdefense.Defensor;

import de.slikey.effectlib.effect.ExplodeEffect;
import de.slikey.effectlib.util.DynamicLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;

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

    public static void createFakeExplosion(Location loc, int amount, float size) {
        ExplodeEffect effect = new ExplodeEffect(Defensor.get().getEffectManager());
        effect.setDynamicOrigin(new DynamicLocation(loc));
        effect.iterations = 1;
        effect.amount = amount;
        effect.particleSize = size;
        effect.start();
    }
}
