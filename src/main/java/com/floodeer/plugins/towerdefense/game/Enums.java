package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.utils.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class Enums {


    public enum TowerEffects {
        BURN("BURN"),
        STRIKE("STRIKE"),
        SLOW("SLOW"),
        EXPLOSION("EXPLOSION");

        String name;

        TowerEffects(String name) {
            this.name = name;
        }
    }

    public enum Difficulty {
        EASY("&bFácil"),
        NORMAL("&aNormal"),
        HARD("&cDifícil"),
        EXPERT("&4Especialista"),
        INSANE("&4&lInsano"),
        SPECIAL("&8&lEspecial");

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
            return Util.color(name);
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
