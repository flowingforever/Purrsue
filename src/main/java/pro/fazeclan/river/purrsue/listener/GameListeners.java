package pro.fazeclan.river.purrsue.listener;

import io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import pro.fazeclan.river.purrsue.util.TaggerUtil;

public class GameListeners implements Listener {

    @EventHandler
    private void handleMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!TaggerUtil.checkPlayerInProperWorld(victim)) return;
        event.setDamage(0.0);
        if (!attacker.getInventory().getItemInMainHand().getType().equals(Material.MACE)) return;
        if (attacker.getFallDistance() < 1.5) return;
        TaggerUtil.setTagger(attacker, victim);
    }

    @EventHandler
    private void handleFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (!TaggerUtil.checkPlayerInProperWorld(victim)) return;
        event.setCancelled(true);
    }

}
