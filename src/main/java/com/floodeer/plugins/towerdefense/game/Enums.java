package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.utils.Util;
import jdk.internal.org.jline.utils.DiffHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Enums {

    //Difficulty handling

    private static YamlConfiguration config;

    public static void loadAll() throws IOException {

        File file = new File(Defensor.get().getDataFolder() + File.separator + "difficulties.yml");
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            config = YamlConfiguration.loadConfiguration(reader);
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            config = YamlConfiguration.loadConfiguration(file);
        }

        Arrays.asList(Difficulty.values()).forEach(difficulty -> {
            String name = "Difficulties." + difficulty.name() + ".";
            difficulty.healthModifier = getConfig().getDouble(name + "health_modifier");
            difficulty.rewardedCoins = getConfig().getInt(name + "coins-when-beat");
            difficulty.finalWave = getConfig().getInt(name + "final-wave");
            difficulty.enemiesPerWave = getConfig().getInt(name + "enemies-per-wave");
            difficulty.enemiesPerWaveModifier = getConfig().getDouble(name + "enemies-wave-modifier");
            difficulty.enemyCoinsPerWave = getConfig().getInt(name + "enemy-coins-per-wave");
            difficulty.enemyCoinsWaveModifier = getConfig().getDouble(name + "enemy-coins-wave-modifier");
            difficulty.playerHealth = getConfig().getInt(name + "player-health");
        });
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    public enum TowerEffects {
        BURN("BURN"),
        STRIKE("STRIKE"),
        SLOW("SLOW"),
        EXPLOSION("EXPLOSION");

        String name;

        TowerEffects(String name) {
            this.name = name;
        }

        public static TowerEffects fromString(String str) {
            return Arrays.stream(TowerEffects.values()).filter(cur -> cur.toString().equalsIgnoreCase(str)).findAny().orElse(null);
        }
    }

    public enum TowerUltimate {
        STATIC_BLAST("Static Blast"),
        BURNING_SOUL("Burning Soul"),
        TORNADO("Tornado"),
        ELECTRONIC_PULSE("Electronic Pulse"),
        DROP_SHOCK("Drop Shock"),
        FLAME_BREATH("Flame Breath"),
        FREEZING_BREATH("Freezing Breath"),
        IRON_PUNCH("IRON PUNCH");

        String name;

        TowerUltimate(String name) { this.name = name; }

        @Override
        public String toString() {
            return name;
        }

        public static TowerUltimate fromString(String str) {
            return Arrays.stream(TowerUltimate.values()).filter(cur -> cur.toString().equalsIgnoreCase(str)).findAny().orElse(null);
        }

    }

    public enum Difficulty {
        FACIL("Facil"),
        NORMAL("Normal"),
        DIFICIL("Dificil"),
        ESPECIALISTA("Especialista"),
        PESADELO("Pesadelo"),
        ESPECIAL("Especial");

        String name;
        @Getter @Setter int rewardedCoins;
        @Getter @Setter int finalWave;
        @Getter @Setter int enemiesPerWave;
        @Getter @Setter int enemyCoinsPerWave;
        @Getter @Setter int playerHealth;

        @Getter @Setter double healthModifier;
        @Getter @Setter double enemiesPerWaveModifier;
        @Getter @Setter double enemyCoinsWaveModifier;

        @Getter @Setter ItemStack item;

        Difficulty(String difficulty) {
            this.name = difficulty;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getColoredName() {
            switch(this) {
                case FACIL:
                    return "&aFácil";
                case NORMAL:
                    return "&bNormal";
                case DIFICIL:
                    return "&cDifícil";
                case ESPECIAL:
                    return "&eEspecial";
                case PESADELO:
                    return "&e&lPesadelo";
                case ESPECIALISTA:
                    return "&9Especialista";
            }
            return toString();
        }

        public static Difficulty fromName(String str) {
            return Arrays.stream(Difficulty.values()).filter(cur -> cur.toString().equalsIgnoreCase(str)).findAny().orElse(null);
        }
    }

    public enum GameState {
        PRE_GAME("&aAguardando"),
        STARTING("&eIniciando"),
        IN_GAME("&cEm jogo"),
        ENDING("&4Encerrando"),
        RESTORING("&bReiniciando");

        String state;

        GameState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return Util.color(state);
        }
    }
}
