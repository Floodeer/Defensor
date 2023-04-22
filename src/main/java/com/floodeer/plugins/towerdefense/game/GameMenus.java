package com.floodeer.plugins.towerdefense.game;

import com.floodeer.plugins.towerdefense.Defensor;
import com.floodeer.plugins.towerdefense.database.data.GamePlayer;
import com.floodeer.plugins.towerdefense.game.mechanics.Tower;
import com.floodeer.plugins.towerdefense.utils.Util;
import org.bukkit.entity.Player;

public class GameMenus {

    public static void buildTowerMenu(Player player) {
        int i = 0;
        Defensor.get().getIconCore().create(player, Util.color("&e&lTorres"), 9 * 6, event -> {
            if(Defensor.get().getMechanicsManager().getTowers().values().stream().anyMatch(cur -> cur.getItem().equals(event.getClickedItem()))) {
                Tower tower = Defensor.get().getMechanicsManager().getTowers().values().stream().filter(cur -> cur.getItem().equals(event.getClickedItem())).findFirst().get();
                GamePlayer gp = GamePlayer.get(player);
                gp.getGame().addTower(gp, gp.getPlayer().getLocation(), tower);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        for (Tower tower : Defensor.get().getMechanicsManager().getTowers().values()) {
            Defensor.get().getIconCore().setOptionMetadata(player, i++, tower.getItem());
        }


        Defensor.get().getIconCore().show(player);
    }
}
