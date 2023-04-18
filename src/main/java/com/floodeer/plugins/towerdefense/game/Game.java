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

    @Getter private final String name;
    @Getter Game game = this;

    //If using @Getter, GameArena has to be set using @Setter
    @Getter @Setter private GameArena arena;

    @Getter @Setter Enums.GameState state;
    @Getter Enums.Difficulty difficulty;

    @Getter @Setter private int timer = 0;
    @Getter @Setter private int startCountdown;

    @Getter private Map<GamePlayer, Double> players;
    @Getter private List<Tower.PlacedTower> towers;
    @Getter  LinkedHashMap<Enemy, Integer> summon;

    @Getter @Setter private int currentWave;
    @Getter @Setter private int currentHealth;
    @Getter @Setter private int maxEnemies;
    @Getter @Setter private int enemyCoins;
    @Getter @Setter private int enemyCost;
    @Getter @Setter private boolean waveActive;

    public Game(String name, boolean load) {
        this.name = name;
        setArena(new GameArena(name));

        if (load) {
            setState(Enums.GameState.PRE_GAME);
            getArena().load();
            Defensor.get().getServer().getScheduler().runTaskLater(Defensor.get(), this::loadGame, 20L);
        }
    }

    private void loadGame() {
        towers = Lists.newArrayList();
        players = Maps.newHashMap();
        summon = Maps.newLinkedHashMap();

        this.difficulty = getArena().getDifficulty();

        Defensor.get().getServer().getPluginManager().registerEvents(this, Defensor.get());
    }

    public void addPlayer(GamePlayer gp) {
        gp.setGame(this);
        gp.getPlayer().teleport(getArena().getLocation(GameArena.LocationType.LOBBY.toString()));
        getPlayers().put(gp, 3000D);
    }

    public void removePlayer(GamePlayer gp, boolean force, boolean leave) {
        gp.setGame(null);
    }

    public void start() {
        setState(Enums.GameState.IN_GAME);
        getPlayers().keySet().forEach(cur -> cur.getPlayer().teleport(getArena().getLocation(GameArena.LocationType.PLAYER_SPAWN.toString())));

        setCurrentHealth(getDifficulty().playerHealth);
        setEnemyCoins(getDifficulty().enemyCoinsPerWave);
        setMaxEnemies(getDifficulty().enemiesPerWave);

        Defensor.get().getMechanicsManager().start(this);
        nextWave();
    }


    @EventHandler
    public void onGameTick(UpdateEvent event) {
        if (event.getType() == UpdateType.FAST) { //ticks every 500ms (10 ticks, 0.5s), Tower and Scoreboard logic

            updateScoreboard();

            if(getState() == Enums.GameState.IN_GAME)
                getTowers().forEach(Tower.PlacedTower::onTick);

        } else if (event.getType() == UpdateType.SEC) { //ticks every 1000ms (20 ticks, 1s), Wave logic
            if(getState() != Enums.GameState.IN_GAME)
                return;

            if (isWaveActive() && !hasLivingEnemies()) {
                setWaveActive(false);
                nextWave();
            }
            ++timer;
        }
    }

    public void nextWave() {
        if (getCurrentWave() == getDifficulty().finalWave) {
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
        }, 5 * 20); //TODO testing
    }

    public void spawnEnemies() {

        int totalEnemies = getEnemies().size();

        if (totalEnemies >= this.maxEnemies) {
            return;
        }

        List<Enemy> availableEnemies = new ArrayList<>((Defensor.get().getMechanicsManager().getEnemies()).values());

        if (getDifficulty() == Enums.Difficulty.PESADELO)
            Collections.shuffle(availableEnemies);
        else
            Collections.reverse(availableEnemies);

        int maxEnemies = (int) (this.maxEnemies * 0.25D);

        while (totalEnemies < this.maxEnemies) {
            boolean enemySummoned = false;

            for (Enemy enemy : availableEnemies) {
                if (this.enemyCoins >= enemy.getCost() && this.currentWave >= enemy.getWaveUnlocked()) {
                    this.enemyCoins -= enemy.getCost();
                    this.summon.put(enemy, this.summon.containsKey(enemy) ? this.summon.get(enemy) + 1 : 1);
                    enemySummoned = true;
                    break;
                }
            }

            if (!enemySummoned) {
                break;
            }

            totalEnemies++;

            getSummon().entrySet().removeIf(entry -> entry.getValue() >= maxEnemies && entry.getKey().getCost() > 0);
            availableEnemies.removeAll(getSummon().keySet());
        }

        getSummon().keySet().forEach(cur -> Defensor.get().getMechanicsManager().spawn(getGame(), cur, summon.get(cur)));
        this.summon.clear();
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

    public void shutdown(boolean force, boolean recreate) {

    }

    public void restore(boolean recreate) {
        Defensor.get().getMechanicsManager().stop(this);
        Defensor.get().getGameManager().recreateGame(this);
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
            if (getState() == Enums.GameState.IN_GAME || getState() == Enums.GameState.ENDING) {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        Util.color("&fMapa: &a" + getName()),
                        Util.color("&fPlayers: &a" + getPlayers().size()),
                        Util.createSpacer(),
                        Util.color("&fRodada: &a" + getCurrentWave()),
                        Util.color("&fDificuldade: " + getDifficulty().getColoredName()),
                        Util.color("&fVida: &a" + getCurrentHealth()),
                        Util.createSpacer(),
                        Util.color("&fTempo: &a" + TimeUtils.formatScoreboard(getTimer()))));
            } else {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        Util.color("&fMapa: &a" + getName()),
                        Util.color("&fPlayers: &b" + getPlayers().size() + "/" + getArena().getMaxPlayers()),
                        Util.color("&fNecessários: &b" + getArena().getMinPlayers()),
                        Util.createSpacer(),
                        Util.color("&fEstado: &f" + getState().toString()),
                        Util.createSpacer(),
                        Util.color("&fSaldo: &b" + player.getBalance())));
            }
        });
    }

    public List<Enemy.AliveEnemy> getEnemies() {
        return Defensor.get().getMechanicsManager().getActiveEnemies(this);
    }

    public boolean hasLivingEnemies() {
        return (getEnemies().size() > 0);
    }
}
