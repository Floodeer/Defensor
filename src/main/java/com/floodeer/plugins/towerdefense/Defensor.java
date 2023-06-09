package com.floodeer.plugins.towerdefense;

import com.floodeer.plugins.towerdefense.database.Database;
import com.floodeer.plugins.towerdefense.database.SQLite;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.listeners.UltimateListener;
import com.floodeer.plugins.towerdefense.listeners.EntityListener;
import com.floodeer.plugins.towerdefense.listeners.PlayerListener;
import com.floodeer.plugins.towerdefense.manager.*;
import com.floodeer.plugins.towerdefense.utils.ConfigOptions;
import com.floodeer.plugins.towerdefense.utils.IconCore;
import com.floodeer.plugins.towerdefense.utils.Items;
import com.floodeer.plugins.towerdefense.utils.update.Updater;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


public final class Defensor extends JavaPlugin {

    private static Defensor main;
    @Getter private PlayerManager playerManager;
    @Getter private GameMechanicsManager mechanicsManager;
    @Getter private GameManager gameManager;
    @Getter private ConfigOptions configOptions;
    @Getter private Database database;
    @Getter private DataManager dataManager;
    @Getter private EnergyManager energyManager;
    @Getter private IconCore iconCore;
    @Getter private Items items;

    public static Defensor get() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;

        setupDatabase();

        File skins = new File(this.getDataFolder() + File.separator + "skins");
        if(!skins.exists())
            skins.mkdirs();

        File mapsFolder = new File(this.getDataFolder() + File.separator + "maps");
        if (!mapsFolder.exists())
            mapsFolder.mkdirs();

        getServer().getScheduler().runTaskTimer(this, new Updater(this), 20, 1);

        this.playerManager = new PlayerManager();
        this.mechanicsManager = new GameMechanicsManager();
        this.gameManager = new GameManager();
        this.energyManager = new EnergyManager();
        this.iconCore = new IconCore();
        this.items = new Items();
        DefensorCommands commands = new DefensorCommands();
        getCommand("defensor").setExecutor(commands);
        getCommand("td").setExecutor(commands);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new UltimateListener(), this);
        getServer().getPluginManager().registerEvents(mechanicsManager, this);
        getServer().getPluginManager().registerEvents(iconCore, this);

        configOptions = new ConfigOptions(new File(getDataFolder(), "options.yml"));
        try {
            configOptions.load();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        saveResource("difficulties.yml", false);
       try {
           Enums.loadAll();
       }catch(IOException ex) {
           ex.printStackTrace();
       }
    }

    @Override
    public void onDisable() {
        gameManager.shutdownGames();
        getPlayerManager().shutdown();
    }

    private boolean setupDatabase()  {
        try {
            database = new SQLite();
            database.createTables();
        } catch (ClassNotFoundException | IOException | SQLException  e) {
            e.printStackTrace();
            return false;
        }

        dataManager = new DataManager();
        return true;
    }
}
