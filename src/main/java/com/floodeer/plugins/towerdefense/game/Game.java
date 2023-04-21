package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.game.mechanics.Enemy;
import com.floodeer.plugins.towerdefense.game.towers.Tower;
import com.floodeer.plugins.towerdefense.utils.Runner;
import com.floodeer.plugins.towerdefense.utils.TimeUtils;
import com.floodeer.plugins.towerdefense.utils.Util;
import com.floodeer.plugins.towerdefense.utils.update.UpdateEvent;
import com.floodeer.plugins.towerdefense.utils.update.UpdateType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
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
        getPlayers().put(gp, 3000D);
        gp.getPlayer().teleport(getArena().getLocation(GameArena.LocationType.LOBBY.toString()));

        Runner.make(Defensor.get()).delay(5).run(() -> {
            gp.clearInventory(true);
            gp.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
    }

    public void removePlayer(GamePlayer gp, boolean force, boolean leave) {
        gp.clearInventory(false);
        gp.restoreInventory();

        gp.setGame(null);
        gp.setSpectator(false);
        gp.getPlayer().setGameMode(GameMode.SURVIVAL);
        gp.getPlayer().setAllowFlight(false);
        gp.getPlayer().setFlying(false);

        if(!force && getState() == Enums.GameState.IN_GAME && !gp.isSpectator()) {
            gp.setGamesPlayed(gp.getGamesPlayed()+1);
        }

        if(!force) {
            getPlayers().remove(gp);
        }
        GameScoreboard.removeScore(gp.getPlayer());
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
                spawnEnemies();
            }
        }, 5 * 20); //TODO testing
    }

    public void spawnEnemies() {
        int sum = this.summon.values().stream().mapToInt(Integer::intValue).sum();

        if (sum < this.maxEnemies) {
            List<Enemy> enemies = new ArrayList<>((Defensor.get().getMechanicsManager().getEnemies()).values());
            Collections.reverse(enemies);
            int maxToSummon = (int)(this.maxEnemies * 0.25D);
            while (sum < this.maxEnemies) {
                for (Enemy enemy : enemies) {
                    if (this.enemyCoins >= enemy.getCost() && this.currentWave >= enemy.getWaveUnlocked()) {
                        this.enemyCoins -= enemy.getCost();
                        this.summon.put(enemy, this.summon.containsKey(enemy) ? this.summon.get(enemy) + 1 : 1);
                        break;
                    }
                }
                sum++;
                for (Iterator<Map.Entry<Enemy, Integer>> it = this.summon.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Enemy, Integer> entry = it.next();
                    if (entry.getValue() >= maxToSummon && entry.getKey().getCost() > 0) {
                        it.remove();
                        enemies.remove(entry.getKey());
                    }
                }
            }
        }
        for (Map.Entry<Enemy, Integer> entry : this.summon.entrySet()) {
            Defensor.get().getMechanicsManager().spawn(this, entry.getKey(), entry.getValue());
        }
        this.summon.clear();
    }

    public void damage(int amount) {
        if (getState() == Enums.GameState.IN_GAME) {
            setCurrentHealth(getCurrentHealth() - amount);
            if (getCurrentHealth() <= 0) {
                endGame(false);
            }
        }

        playSound(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1F, 0.5F);
    }

    public void endGame(boolean winner) {
        setState(Enums.GameState.ENDING);
        getEnemies().forEach(entity -> entity.getEntity().remove());
        getEnemies().clear();
        summon.clear();

        getPlayers().keySet().forEach(cur -> {
            cur.setWins(cur.getWins() + 1);
            cur.addMoney(getDifficulty().getRewardedCoins());

            cur.msg(ChatColor.STRIKETHROUGH + "-------------------------");
            cur.msg(" ");
            cur.msg(winner ? "&a&lVitória!" : "&c&lDerrota");
            cur.msg(" ");
            cur.msg("&eRodadas: &b" + getCurrentWave());
            cur.msg("&eRecompensa: &b" + (winner ? getDifficulty().getRewardedCoins() : getDifficulty().getRewardedCoins()/2));
            cur.msg(" ");
            cur.msg(ChatColor.STRIKETHROUGH + "----------------------------------");
        });

        Runner.make(Defensor.get()).delay(200).run(() -> shutdown(false, true));
    }

    public void shutdown(boolean force, boolean recreate) {
        getPlayers().keySet().forEach(cur -> {
            if(force) {
                cur.msg("&cA partida foi encerrada por um administrador.");
            }
            removePlayer(cur, true, false);
        });

        Runner.make(Defensor.get()).delay(25).run(() -> restore(recreate));
    }

    private void restore(boolean recreate) {
        Defensor.get().getMechanicsManager().stop(this);

        if(recreate)
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

    private void sendActionBar(String text) {
        getPlayers().keySet().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.sendActionBar(Component.text(text)));
    }

    private void sendTitle(String title, String subtitle) {
        getPlayers().keySet().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.showTitle(Title.title(Component.text(title), Component.text(subtitle))));
    }

    private void playSound(Sound sound, float volume, float pitch) {
        getPlayers().keySet().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.playSound(cur.getLocation(), sound, volume, pitch));
    }
}
