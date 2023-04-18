package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.utils.GameDataFile;
import com.floodeer.plugins.towerdefense.utils.GameDataYaml;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GameArena {

    @Getter private final String name;

    @Getter private GameDataFile gameDataFile;
    @Getter private File gameFolder;

    @Getter private final List<Location> path;

    public GameArena(String name) {
        this.name = name;
        if(doesMapExists()) {
            gameFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps" + File.separator + name);
            gameDataFile = GameDataYaml.getMap(name);
        }

        path = Lists.newArrayList();
    }

    public void load() {
        gameDataFile.getStringList("Locations.path").forEach(cur -> path.add(Util.getLocationFromString(cur)));
    }

    public void addPath(Location l) {
        if (!gameDataFile.contains("Locations.path"))
            gameDataFile.createNewStringList("Locations.path", new ArrayList<>());

        List<String> way = gameDataFile.getStringList("Locations.path");
        way.add(Util.getStringFromLocation(l, true));
        gameDataFile.set("Locations.path", way);
        gameDataFile.save();
    }


    public void create(Enums.Difficulty difficulty) {
        gameDataFile = GameDataYaml.getMap(this.name);
        gameDataFile.add("Name", this.name);
        gameDataFile.add("Difficulty", difficulty.toString());
        gameDataFile.add("Max-Health", difficulty.getPlayerHealth());
        gameDataFile.add("Map.MinPlayers", 1);
        gameDataFile.add("Map.MaxPlayers", 4);
        gameDataFile.save();

        gameFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps" + File.separator + name);

    }

    public void deleteArena() {

    }

    public Enums.Difficulty getDifficulty() {
        return Enums.Difficulty.fromName(gameDataFile.getString("Difficulty"));
    }


    public void setMinPlayers(int x) {
        gameDataFile.set("Map.MinPlayers", x);
        gameDataFile.save();
    }

    public void setMaxPlayers(int x) {
        gameDataFile.set("Map.MaxPlayers", x);
        gameDataFile.save();
    }

    public int getMinPlayers() {
        return gameDataFile.getInteger("Map.MinPlayers");
    }

    public int getMaxPlayers() {
        return gameDataFile.getInteger("Map.MaxPlayers");
    }

    public Location getLocation(String type) {
        double x, z, y;
        float yaw, pitch;
        String world;
        world = gameDataFile.getString("Locations." + type + ".world");
        x = gameDataFile.getDouble("Locations." + type + ".x");
        y = gameDataFile.getDouble("Locations." + type + ".y");
        z =	gameDataFile.getDouble("Locations." + type + ".z");
        yaw = (float) gameDataFile.getDouble("Locations." + type + ".yaw");
        pitch = (float) gameDataFile.getDouble("Locations." + type + ".pitch");

        if(world == null || world.isEmpty() || Bukkit.getWorld(world) == null)
            return null;
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public void setLocation(LocationType type, Location loc) {
        gameDataFile.set("Locations." + LocationType.toString(type) + ".world", loc.getWorld().getName());
        gameDataFile.set("Locations." + LocationType.toString(type) + ".x", loc.getX());
        gameDataFile.set("Locations." + LocationType.toString(type) + ".y", loc.getY());
        gameDataFile.set("Locations." + LocationType.toString(type) + ".z", loc.getZ());
        gameDataFile.set("Locations." + LocationType.toString(type) + ".pitch", loc.getPitch());
        gameDataFile.set("Locations." + LocationType.toString(type) + ".yaw", loc.getYaw());
        gameDataFile.save();
    }

    public boolean doesMapExists() {
        File mapsFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps");
        for (File files : mapsFolder.listFiles()) {
            if (files.getName().equalsIgnoreCase(this.name)) {
                return true;
            }
        }
        return false;
    }

    public enum LocationType {
       MOB_SPAWN("MOB_SPAWN"), PLAYER_SPAWN("PLAYER_SPAWN"), LOBBY("LOBBY");

        String type;

        LocationType(String str) {
            this.type = str;
        }

        @Override
        public String toString() {
            return type;
        }

        public static String toString(LocationType type) {
            return type.toString();
        }
    }
}
