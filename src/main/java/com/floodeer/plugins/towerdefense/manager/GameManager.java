package com.floodeer.plugins.towerdefense.manager;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.utils.FileUtils;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class GameManager {

    private final Queue<GamePlayer> queue = Lists.newLinkedList();
    private final List<Game> games = Lists.newArrayList();
    private final File maps;

    public GameManager() {
        maps = new File(Defensor.get().getDataFolder(), "maps");
        if (maps.listFiles() != null) {
            for (File files : maps.listFiles()) {
                try {
                    createGame(files.getName().replaceAll(".yml", ""), true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        Bukkit.getScheduler().runTaskTimer(Defensor.get(), () -> {
             // getGames().stream().filter(g -> g.canStart).forEach(Game::checkForStart);
        }, 20, 120);
    }

    public List<Game> getGames() {
        return games;
    }

    public Game createGame(String gameName, boolean load) {
        Game game = new Game(gameName, load);
        games.add(game);
        return game;
    }

    public void finish(String gameName) {
        games.removeIf(cur -> cur.getName().equalsIgnoreCase(gameName));
        createGame(gameName, true);
    }

    public Game recreateGame(Game game) {
        this.games.remove(game);
        return createGame(game.getName(), true);
    }

    public void deleteGame(String name)  {
        Game game = getGameFromName(name);
        if (game.getState() == Enums.GameState.IN_GAME)
            game.shutdown(true, false);

        game.getArena().deleteArena();
        File dataDirectory = new File (Defensor.get().getDataFolder(), "maps");
        File target = new File (dataDirectory, name);
        try {
            FileUtils.deleteDirectory(target);
        } catch (IOException e) {
            e.printStackTrace();
        }

        games.remove(game);
    }

    public boolean doesMapExists(String name) {
        File mapsFolder = new File(Defensor.get().getDataFolder() + File.separator + "maps");

        for (File files : mapsFolder.listFiles()) {
            if (files.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void shutdownGames() {
        getGames().forEach(game -> game.shutdown(true, false));
    }

    public Game getGameFromName(String name) {
        return games.stream().filter(game -> game.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Game findGameFor(GamePlayer gp) {
        Game result = null;
        List<Game> games = getGames();
        Collections.shuffle(games);
        for (Game g : games) {
            if (g.getPlayers().size() > 0) {
                if (canJoin(g)) {
                    result = g;
                    break;
                }
            }
            if (canJoin(g))
                result = g;
        }
        if (result == null) {
            queue.add(gp);
            return null;
        }

        return result;
    }

    public Game getNextGame(GamePlayer optionalPlayer) {
        for (Game game : games) {
            if (game.getState() == Enums.GameState.PRE_GAME || game.getState() == Enums.GameState.STARTING) {
                if (game.getPlayers().size() <= game.getArena().getMaxPlayers()) {
                    return game;
                } else if (optionalPlayer != null && game.getPlayers().size() >= game.getArena().getMaxPlayers() && optionalPlayer.getPlayer().hasPermission("td.joinfull"))
                    return game;
            }
        }
        return null;
    }

    private boolean canJoin(Game g) {
            return g.getState() == Enums.GameState.PRE_GAME && g.getPlayers().size() < g.getArena().getMaxPlayers();
    }

}
