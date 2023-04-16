package com.floodeer.plugins.towerdefense.game.towers;

import com.floodeer.plugins.towerdefense.game.Game;
import com.floodeer.plugins.towerdefense.utils.update.UpdateEvent;
import com.floodeer.plugins.towerdefense.utils.update.UpdateType;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class TowerManager implements Listener  {

    @Getter @Setter private boolean isRunning;

    private List<Game> games;

   public TowerManager() {
       games = Lists.newArrayList();
   }

   public void add(Game game) {
       games.add(game);
   }

   public void remove(Game game) {
       games.remove(game);
   }

   @EventHandler
   public void onUpdate(UpdateEvent event) {
       if(event.getType() == UpdateType.FAST) { //10ms
           if(isRunning) {
               //tick
           }
       }
   }
}
