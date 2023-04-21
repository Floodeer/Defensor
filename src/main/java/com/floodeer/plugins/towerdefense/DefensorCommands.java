package com.floodeer.plugins.towerdefense;

import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.game.Enums;
import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.game.GameArena;
import com.floodeer.plugins.towerdefense.utils.ProtocolUtils;
import com.floodeer.plugins.towerdefense.utils.Runner;
import com.floodeer.plugins.towerdefense.utils.Util;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DefensorCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return true;
        }

        if(args[0].equalsIgnoreCase("test")) {
            Player p = (Player) commandSender;
            ProtocolUtils.playNPCAttackAnimation(CitizensAPI.getDefaultNPCSelector().getSelected(p));
            p.sendMessage(Util.color("&aTestado!"));
        }
        if(args[0].equalsIgnoreCase("create")) {

            if(!commandSender.hasPermission("defensor.admin"))
                return true;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }
            String name = args[1];

            String difficulty = args.length <= 2 ? Enums.Difficulty.NORMAL.toString().toUpperCase() : args[2];

            Game game = Defensor.get().getGameManager().createGame(name, false);
            game.getArena().create(Enums.Difficulty.valueOf(difficulty));
            commandSender.sendMessage(Util.color("&aArena &e" + name + " &acriada!"));

        }else if(args[0].equalsIgnoreCase("delete")) {

            if(!commandSender.hasPermission("defensor.admin"))
                return true;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }
            
            if(Defensor.get().getGameManager().doesMapExists(args[1])) {
                Defensor.get().getGameManager().deleteGame(args[1]);
                commandSender.sendMessage(Util.color("&cArena &e" + args[1] + " &cdeletada!"));
            }else{
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
            }
        }else if(args[0].equalsIgnoreCase("finish")) {
            if (!commandSender.hasPermission("defensor.admin"))
                return true;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            if (!Defensor.get().getGameManager().doesMapExists(args[1])) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }

            Game game = Defensor.get().getGameManager().getGameFromName(args[1]);
            if (game.getState() == Enums.GameState.IN_GAME) {
                game.shutdown(true, true);
            }
            Runner.make(Defensor.get()).delay(20).run(() -> {
                game.setState(Enums.GameState.RESTORING);
                Defensor.get().getGameManager().getGames().remove(game);
                Defensor.get().getGameManager().finish(args[1]);
                commandSender.sendMessage(Util.color("&aArena reiniciada com sucesso."));
            });
        }else if(args[0].equalsIgnoreCase("setPlayerSpawn")) {
            if(!(commandSender instanceof Player))
                return false;

            if(args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player)commandSender;
            if(!commandSender.hasPermission("defensor.admin"))
                return true;

            String name = args[1];
            if(!Defensor.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Defensor.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.PLAYER_SPAWN, player.getLocation());
            player.sendMessage(Util.color("&aPlayer spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("setLobby")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player) commandSender;
            if (!commandSender.hasPermission("defensor.admin"))
                return true;

            String name = args[1];
            if (!Defensor.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Defensor.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.LOBBY, player.getLocation());
            player.sendMessage(Util.color("&aLobby spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("setMobSpawn")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player player = (Player) commandSender;
            if (!commandSender.hasPermission("defensor.admin"))
                return true;

            String name = args[1];
            if (!Defensor.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Defensor.get().getGameManager().getGameFromName(name);
            game.getArena().setLocation(GameArena.LocationType.MOB_SPAWN, player.getLocation());
            player.sendMessage(Util.color("&aLobby spawn configurado com sucesso!"));
        }else if(args[0].equalsIgnoreCase("addPath")) {
            if (!(commandSender instanceof Player))
                return false;

            if (args.length == 1) {
                commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                return true;
            }

            Player p = (Player) commandSender;
            if (!commandSender.hasPermission("defensor.admin"))
                return true;

            String name = args[1];
            if (!Defensor.get().getGameManager().doesMapExists(name)) {
                commandSender.sendMessage(Util.color("&cErro: Arena inválida"));
                return true;
            }
            Game game = Defensor.get().getGameManager().getGameFromName(name);
            game.getArena().addPath(p.getLocation());
            commandSender.sendMessage(Util.color("&aCaminho adicionado!"));
        }else if(args[0].equalsIgnoreCase("join")) {
            if (!(commandSender instanceof Player))
                return false;

            Player p = (Player)commandSender;
            if (args.length == 1) {
                if (GamePlayer.get(p.getUniqueId()) == null) {
                    p.sendMessage("&cTente novamente em alguns segundos!");
                    return true;
                }
                if (GamePlayer.get(p.getUniqueId()).isInGame()) {
                    p.sendMessage(Util.color("&cVocê já está em jogo!"));
                    return true;
                }
                if (Defensor.get().getGameManager().findGameFor(GamePlayer.get(p.getUniqueId())) == null) {
                    p.sendMessage(Util.color("Nenhuma partida encontrada."));
                } else {
                    Defensor.get().getGameManager().findGameFor(GamePlayer.get(p.getUniqueId())).addPlayer(GamePlayer.get(p.getUniqueId()));
                }
            }else {
                if (Defensor.get().getGameManager().doesMapExists(args[1])) {
                    Game game = Defensor.get().getGameManager().getGameFromName(args[1]);
                    if (game.getState() != Enums.GameState.PRE_GAME && game.getState() != Enums.GameState.STARTING) {
                        if (game.getState() == Enums.GameState.ENDING || game.getState() == Enums.GameState.RESTORING) {
                            p.sendMessage(Util.color("&cArena reiniciando!"));
                            return true;
                        }
                        p.sendMessage(Util.color("&cA partida já começou!"));
                        return true;
                    }
                    if (game.getPlayers().size() >= game.getArena().getMaxPlayers() && !p.hasPermission("defensor.joinfull")) {
                        p.sendMessage(Util.color("&cPartida lotada"));
                        return true;
                    }

                    if (GamePlayer.get(p.getUniqueId()).isInGame()) {
                        p.sendMessage(Util.color("&cVocê já está em jogo!"));
                        return true;
                    }

                    game.addPlayer(GamePlayer.get(p.getUniqueId()));
                }
            }
        }else if(args[0].equalsIgnoreCase("leave")) {
            if (!(commandSender instanceof Player))
                return false;
            GamePlayer gp = GamePlayer.get(((Player) commandSender).getUniqueId());
            if (gp.isInGame()) {
                gp.getGame().removePlayer(gp, false, true);
            }
        }else if(args[0].equalsIgnoreCase("start")) {
            Game game = null;

            if (args.length == 1) {
                if(commandSender instanceof  Player && GamePlayer.get(((Player) commandSender).getUniqueId()).isInGame()) {
                    game = GamePlayer.get(((Player) commandSender).getUniqueId()).getGame();
                }else{
                    commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                    return true;
                }
            }

            if(game == null) {
                if (!Defensor.get().getGameManager().doesMapExists(args[1])) {
                    commandSender.sendMessage(Util.color("&cErro: Arena inválida."));
                    return true;
                }
                game = Defensor.get().getGameManager().getGameFromName(args[1]);
            }
            if(game.getState() == Enums.GameState.PRE_GAME || game.getState() == Enums.GameState.STARTING) {
                commandSender.sendMessage(Util.color("&aPartida iniciada."));
                game.start();
            }

        }else if(args[0].equalsIgnoreCase("stop")) {
            Game game = null;

            if (args.length == 1) {
                if (commandSender instanceof Player && GamePlayer.get(((Player) commandSender).getUniqueId()).isInGame()) {
                    game = GamePlayer.get(((Player) commandSender).getUniqueId()).getGame();
                } else {
                    commandSender.sendMessage(Util.color("&cEspecifique o nome da arena."));
                    return true;
                }
            }

            if (game == null) {
                if (!Defensor.get().getGameManager().doesMapExists(args[1])) {
                    commandSender.sendMessage(Util.color("&cErro: Arena inválida."));
                    return true;
                }
                game = Defensor.get().getGameManager().getGameFromName(args[1]);
            }
            if (game.getState() == Enums.GameState.IN_GAME || game.getState() == Enums.GameState.ENDING) {
                commandSender.sendMessage(Util.color("&cPartida encerrada."));
                game.shutdown(true, true);
            }
        }
        return false;
    }
}
