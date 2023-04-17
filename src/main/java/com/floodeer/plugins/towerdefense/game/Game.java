package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.towers.Tower;
import com.floodeer.plugins.towerdefense.utils.TimeUtils;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.floodeer.plugins.towerdefense.utils.update.UpdateEvent;
import com.floodeer.plugins.towerdefense.utils.update.UpdateType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class Game implements Listener {

    @Getter
    private final String name;
    @Getter
    private final GameArena arena;
    @Getter
    Game game = this;
    @Getter
    @Setter
    Enums.GameState state;
    @Getter
    Enums.Difficulty difficulty;
    @Getter
    @Setter
    private int timer = 0;
    @Getter
    @Setter
    private int startCountdown;
    @Getter
    private Map<GamePlayer, Double> players;
    @Getter
    private LinkedHashMap<Enemy, Integer> enemies;
    @Getter
    private List<Tower.PlacedTower> towers;
    @Getter
    @Setter
    private int currentWave;
    @Getter
    @Setter
    private int currentHealth;
    @Getter
    @Setter
    private int maxEnemies;
    @Getter
    @Setter
    private int enemyCoins;
    @Getter
    @Setter
    private int enemyCost;
    @Getter
    @Setter
    private boolean waveActive;

    public Game(String name, boolean load) {
        this.name = name;
        this.arena = new GameArena(name);

        if (load) {
            setState(Enums.GameState.PRE_GAME);
            getArena().load();
            Defensor.get().getServer().getScheduler().runTaskLater(Defensor.get(), this::loadGame, 20L);
        }
    }

    private void loadGame() {
        enemies = Maps.newLinkedHashMap();
        towers = Lists.newArrayList();

        Defensor.get().getServer().getPluginManager().registerEvents(this, Defensor.get());
    }

    @EventHandler
    public void onGameTick(UpdateEvent event) {
        if (event.getType() == UpdateType.FAST) { //ticks every 500ms (10 ticks, 0.5s), Tower and Scoreboard logic

            updateScoreboard();
            getTowers().forEach(Tower.PlacedTower::onTick);

        } else if (event.getType() == UpdateType.SEC) { //ticks every 1000ms (20 ticks, 1s), Wave logic
            if (isWaveActive() && getEnemies().size() <= 0) {
                setWaveActive(false);
                nextWave();

                ++timer;
            }
        }
    }

    public void nextWave() {
        if (currentHealth == getDifficulty().finalWave) {
            endGame(true);
            return;
        }

        getPlayers().keySet().forEach(cur -> {
            if (cur.getWaveRecord() < getCurrentWave()) {
                cur.setWaveRecord(getCurrentWave());
                cur.msg("&aVocê quebrou seu recorde pessoal de Rodadas sobrevividas!");
            }
        });
        if (getCurrentWave() != 0) {
            setMaxEnemies((int) (getMaxEnemies() * getDifficulty().getEnemiesPerWaveModifier()));
        }

        Bukkit.getServer().getScheduler().runTaskLater(Defensor.get(), () -> { //1min delay between waves
            if (getState() == Enums.GameState.IN_GAME) {
                setWaveActive(true);
                setCurrentWave(getCurrentWave() + 1);
                updateScoreboard();

                Defensor.get().getMechanicsManager().getBosses().values().forEach(boss -> {
                    if (boss.getWaveUnlocked() == getCurrentWave()) {
                        Defensor.get().getMechanicsManager().spawn(getGame(), boss, 1);
                        getPlayers().keySet().forEach(player -> {
                            player.msg(ChatColor.STRIKETHROUGH + "------------ " + ChatColor.MAGIC + "A" + ChatColor.RED + " BOSS " + ChatColor.DARK_RED + ChatColor.MAGIC + "A" + ChatColor.DARK_RED + ChatColor.STRIKETHROUGH + " -------------");
                            player.msg(ChatColor.DARK_RED + "- Nome: " + ChatColor.YELLOW + boss.getName());
                            player.msg(ChatColor.DARK_RED + "- Vida: " + ChatColor.YELLOW + boss.getHealth());
                            player.msg(ChatColor.DARK_RED + "- Dano: " + ChatColor.YELLOW + boss.getDamage());
                            player.msg(ChatColor.DARK_RED + "- Velocidade: " + ChatColor.YELLOW + boss.getSpeed());
                            player.msg("" + ChatColor.DARK_RED + ChatColor.STRIKETHROUGH + "----------------------------------");
                        });
                    }
                });
            }
            spawnEnemies();
        }, 60 * 20);
    }

    public void spawnEnemies() {
        //AI summoning system for single player, may not work? not tested yet

        int totalEnemies = this.enemies.values().size();

        if (totalEnemies >= this.maxEnemies) {
            return;
        }

        List<Enemy> availableEnemies = new ArrayList<>((Defensor.get().getMechanicsManager().getEnemies()).values());

        if (getDifficulty() == Enums.Difficulty.SPECIAL)
            Collections.shuffle(availableEnemies);
        else
            Collections.reverse(availableEnemies);

        int maxEnemies = (int) (this.maxEnemies * 0.25D);

        while (totalEnemies < this.maxEnemies) {
            boolean enemySummoned = false;

            for (Enemy enemy : availableEnemies) {
                if (this.enemyCoins >= enemy.getCost() && this.currentWave >= enemy.getWaveUnlocked()) {
                    this.enemyCoins -= enemy.getCost();
                    this.enemies.put(enemy, getEnemies().containsKey(enemy) ? getEnemies().get(enemy) + 1 : 1);
                    enemySummoned = true;
                    break;
                }
            }

            if (!enemySummoned) {
                break;
            }

            totalEnemies++;

            getEnemies().entrySet().removeIf(entry -> entry.getValue() >= maxEnemies && entry.getKey().getCost() > 0);
            availableEnemies.removeAll(getEnemies().keySet());
        }

        for (Map.Entry<Enemy, Integer> entry : getEnemies().entrySet()) {
            Defensor.get().getMechanicsManager().spawn(this, entry.getKey(), entry.getValue());
        }

        this.enemies.clear();
    }

    public void damage(int amount) {
        if (getState() == Enums.GameState.IN_GAME) {
            setCurrentHealth(getCurrentHealth() - amount);
            if (getCurrentHealth() <= 0) {
                endGame(false);
            }
        }
    }

    public void endGame(boolean winner) {

    }

    private void updateScoreboard() {
        getPlayers().keySet().forEach(player -> {
            GameScoreboard scoreboard;
            if (GameScoreboard.hasScore(player.getPlayer()))
                scoreboard = GameScoreboard.getByPlayer(player.getPlayer());
            else {
                scoreboard = GameScoreboard.createScore(player.getPlayer());
                scoreboard.setTitle("&6&lDEFENSOR");
            }
            if (getState() == Enums.GameState.PRE_GAME || getState() == Enums.GameState.STARTING) {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        "&fMapa: &a" + getName(),
                        "&fPlayers: &a" + getPlayers().size(),
                        Util.createSpacer(),
                        "&fRodada: &a" + getCurrentWave(),
                        "&fDificuldade: " + getDifficulty().toString(),
                        "&fVida: &a" + getCurrentHealth(),
                        Util.createSpacer(),
                        "&fTempo: &a" + TimeUtils.formatScoreboard(getTimer())));
            } else {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        "&fMapa: &a" + getName(),
                        "&fPlayers: &b" + getPlayers().size() + "/" + getArena().getMaxPlayers(),
                        "&fNecessários: &b" + getArena().getMinPlayers(),
                        Util.createSpacer(),
                        "&fEstado: &f" + getState().toString(),
                        Util.createSpacer(),
                        "&fSaldo: &b" + player.getBalance()));
            }
        });
    }
}
