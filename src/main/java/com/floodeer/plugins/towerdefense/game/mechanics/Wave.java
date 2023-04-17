package com.floodeer.plugins.towerdefense.game.mechanics;

import com.floodeer.plugins.towerdefense.game.Game;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Wave {

    @Getter private final String name;
    @Getter private final Game game;

    @Getter private final List<String> spawns;
    @Getter private final LinkedList<Enemy> enemies;
    @Getter private final ConcurrentLinkedQueue<BukkitTask> spawnTask;

    public Wave(String name, Game game) {
        this.game = game;
        this.name = name;

        spawns = Lists.newArrayList();
        enemies = Lists.newLinkedList();
        spawnTask = Queues.newConcurrentLinkedQueue();
    }
}
