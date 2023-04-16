package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import lombok.Getter;

import java.util.List;

public class Game {

    private @Getter String name;

    private @Getter List<Enemy.AliveEnemy> enemies;

}
