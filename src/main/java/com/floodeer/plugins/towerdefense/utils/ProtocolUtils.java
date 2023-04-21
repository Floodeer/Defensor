package com.floodeer.plugins.towerdefense.utils;

import com.floodeer.plugins.towerdefense.Defensor;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ProtocolUtils {

    public static void playNPCAttackAnimation(NPC entity) {

        HolographicDisplaysAPI api = HolographicDisplaysAPI.get(Defensor.get());
        Hologram hologram = api.createHologram(entity.getEntity().getLocation().clone().add(0, 3, 0));
        hologram.getLines().appendText(Util.color("&9&lElectro Attack!"));

        entity.getEntity().setGlowing(true);

        new BukkitRunnable() {
            int abilityStep = 0;
            int damageStep = 9;
            int strikeDelay = 0;
            Location location = entity.getEntity().getLocation();

            public void run() {
                this.abilityStep += 1;
                ++strikeDelay;
                if ((this.abilityStep == 80)) {
                    hologram.delete();
                    entity.getEntity().setGlowing(false);
                    cancel();
                    return;
                }
                this.damageStep += 1;
                if (this.damageStep == 10) {
                    ((LivingEntity) entity.getEntity()).swingMainHand();
                    this.damageStep = 0;
                    Util.getCircle(this.location.clone().add(0.0D, 1.0D, 0.0D), 5.0D, 40).stream().forEach(l -> {
                        l.getWorld().spawnParticle(Particle.SPELL_WITCH, l, 3, 0F, 0F, 0F, 0.1F);
                        l.getWorld().spawnParticle(Particle.CLOUD, l, 1, 0F, 0F, 0F, 0.01F);
                    });
                    if (strikeDelay == 20) {
                        location.getWorld().strikeLightningEffect(location);
                        ((LivingEntity) entity.getEntity()).swingMainHand();
                        strikeDelay = 0;
                    }
                }
            }
        }.runTaskTimer(Defensor.get(), 0L, 1L);
    }
}
