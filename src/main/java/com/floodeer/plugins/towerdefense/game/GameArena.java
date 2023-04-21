package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.utils.GameDataFile;
import com.floodeer.plugins.towerdefense.utils.GameDataYaml;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameArena {

    @Getter private final String name;

    private GameDataFile gameDataFile;
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
        getGameDataFile().getStringList("Locations.path").forEach(cur -> path.add(Util.getLocationFromString(cur)));
    }

    public void addPath(Location l) {
        if (!getGameDataFile().contains("Locations.path"))
            getGameDataFile().createNewStringList("Locations.path", new ArrayList<>());

        List<String> way = getGameDataFile().getStringList("Locations.path");
        way.add(Util.getStringFromLocation(l, true));
        getGameDataFile().set("Locations.path", way);
        getGameDataFile().save();
    }

    public GameDataFile getGameDataFile() {
        if(gameDataFile == null)
            gameDataFile = GameDataYaml.getMap(this.name);

        return gameDataFile;
    }



    public void create(Enums.Difficulty difficulty) {
        gameDataFile = GameDataYaml.getMap(this.name);
        getGameDataFile().add("Name", this.name);
        getGameDataFile().add("Difficulty", difficulty.toString());
        getGameDataFile().add("Max-Health", difficulty.getPlayerHealth());
        getGameDataFile().add("Map.MinPlayers", 1);
        getGameDataFile().add("Map.MaxPlayers", 4);
        getGameDataFile().save();

        gameFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps" + File.separator + name);

    }

    public void deleteArena() {

    }

    public Enums.Difficulty getDifficulty() {
        return Enums.Difficulty.fromName(getGameDataFile().getString("Difficulty"));
    }


    public void setMinPlayers(int x) {
        getGameDataFile().set("Map.MinPlayers", x);
        getGameDataFile().save();
    }

    public void setMaxPlayers(int x) {
        getGameDataFile().set("Map.MaxPlayers", x);
        getGameDataFile().save();
    }

    public int getMinPlayers() {
        return getGameDataFile().getInteger("Map.MinPlayers");
    }

    public int getMaxPlayers() {
        return getGameDataFile().getInteger("Map.MaxPlayers");
    }

    public Location getLocation(String type) {
        double x, z, y;
        float yaw, pitch;
        String world;
        world = getGameDataFile().getString("Locations." + type + ".world");
        x = getGameDataFile().getDouble("Locations." + type + ".x");
        y = getGameDataFile().getDouble("Locations." + type + ".y");
        z =	getGameDataFile().getDouble("Locations." + type + ".z");
        yaw = (float) getGameDataFile().getDouble("Locations." + type + ".yaw");
        pitch = (float) getGameDataFile().getDouble("Locations." + type + ".pitch");

        if(world == null || world.isEmpty() || Bukkit.getWorld(world) == null)
            return null;
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public void setLocation(LocationType type, Location loc) {
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".world", loc.getWorld().getName());
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".x", loc.getX());
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".y", loc.getY());
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".z", loc.getZ());
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".pitch", loc.getPitch());
        getGameDataFile().set("Locations." + LocationType.toString(type) + ".yaw", loc.getYaw());
        getGameDataFile().save();
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
