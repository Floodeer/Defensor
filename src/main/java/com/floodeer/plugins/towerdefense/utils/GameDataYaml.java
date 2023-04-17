package com.floodeer.plugins.towerdefense.utils;


import com.floodeer.plugins.towerdefense.Defensor;

import java.io.File;

public class GameDataYaml {

    public static GameDataFile getMap(String mapName) {
        return new GameDataFile(Defensor.get().getDataFolder().getAbsolutePath() + File.separator + "maps" + File.separator + mapName + File.separator + mapName + ".yml");
    }
}