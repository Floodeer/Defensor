package com.floodeer.plugins.towerdefense.database.data;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.GameTeam;
import com.floodeer.plugins.towerdefense.utils.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GamePlayer {

    @Getter private final Player player;
    @Getter private final UUID UUID;
    @Getter private final String name;

    @Getter @Setter private int wins;
    @Getter @Setter private int gamesRlayed;
    @Getter @Setter private int waveRecord;
    @Getter @Setter private int exp;
    @Getter @Setter private int balance;
    @Getter @Setter private String rank = "Level-1";
    @Getter @Setter private double damageCaused;

    @Getter @Setter private GameTeam team;
    @Getter @Setter private Game game;

    public GamePlayer(UUID uuid) {
        this.UUID = uuid;
        this.player = Bukkit.getPlayer(uuid);
        this.name = player.getName();
    }

    public GamePlayer(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId();
        this.name = player.getName();
    }

    public static GamePlayer get(Player player) {
        return Defensor.get().getPlayerManager().getPlayer(player);
    }

    public static GamePlayer get(UUID uuid) {
        return Defensor.get().getPlayerManager().getPlayer(uuid);
    }

    public void msg(String msg) {
        player.sendMessage(Util.color(msg));
    }

    public void addMoney(int amount) {
        setBalance(getBalance()+amount);
    }

    public void removeMoney(int amount) {
        if(getBalance() - amount < 0)
            setBalance(0);

        setBalance(getBalance()-amount);
    }

    public boolean isInGame() {
        return game != null;
    }
}
