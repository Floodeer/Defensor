package com.floodeer.plugins.towerdefense.game.mechanics;

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

    protected enum Difficulty {
        EASY("Fácil"),
        NORMAL("Normal"),
        HARD("Difícil"),
        EXPERT("Especialista"),
        INSANE("Insano"),
        SPECIAL("Especial");

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
    }
}
