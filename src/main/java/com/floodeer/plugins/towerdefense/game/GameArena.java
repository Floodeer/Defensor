package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.utils.GameDataFile;
import com.floodeer.plugins.towerdefense.utils.GameDataYaml;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.List;

public class GameArena {

    @Getter private final String name;

    @Getter private GameDataFile gameDataFile;
    @Getter private File gameFolder;

    @Getter private final List<Location> pathIndex;

    public GameArena(String name) {
        this.name = name;
        if(doesMapExists()) {
            gameFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps" + File.separator + name);
            gameDataFile = GameDataYaml.getMap(name);
        }

        pathIndex = Lists.newArrayList();
    }

    public void load() {
        if (!pathIndex.isEmpty())
            pathIndex.clear();

        pathIndex.addAll(paths());
    }


    public void create(Enums.Difficulty difficulty) {
        gameDataFile = GameDataYaml.getMap(this.name);
        gameDataFile.add("Name", this.name);
        gameDataFile.add("Difficulty", difficulty.toString());
        gameDataFile.add("Max-Health", difficulty.getPlayerHealth());
        gameDataFile.add("Map.MinPlayers", 1);
        gameDataFile.add("Map.MaxPlayers", 4);
        gameDataFile.save();


    }

    private List<Location> paths() {
        List<Location> locations = Lists.newArrayList();
        List<String> way = gameDataFile.getStringList("Locations.paths");
        for (String s : way) {
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) new JSONParser().parse(s);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            locations.add(Util.getLocation(jsonObject, true));
        }
        return locations;
    }

    public void addPath(Location l) {
        if (!gameDataFile.contains("Locations.paths")) {
            gameDataFile.createNewStringList("Locations.paths", Lists.newArrayList());
        }
        List<String> way = gameDataFile.getStringList("Locations.paths");
        way.add(Util.saveLocation(l, true));
        gameDataFile.set("Locations.paths", way);
        gameDataFile.save();
    }

    public List<Location> getPathIndex() {
        return pathIndex;
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

    public boolean doesMapExists() {
        File mapsFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps");
        for (File files : mapsFolder.listFiles()) {
            if (files.getName().equalsIgnoreCase(this.name)) {
                return true;
            }
        }
        return false;
    }

}
