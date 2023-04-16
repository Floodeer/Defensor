package com.floodeer.plugins.towerdefense;

import com.floodeer.plugins.towerdefense.utils.update.Updater;
import de.slikey.effectlib.EffectManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public final class Defensor extends JavaPlugin {

    private static Defensor main;
    @Getter private EffectManager effectManager;

    public static Defensor get() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;

        File skins = new File(getDataFolder(), "skins");
        if(!skins.exists())
            skins.mkdirs();

        effectManager = new EffectManager(this);
        getServer().getScheduler().runTaskTimer(this, new Updater(this), 20, 1);
    }

    @Override
    public void onDisable() {

    }

}
