package pro.fazeclan.river.purrsue.listener;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import pro.fazeclan.river.purrsue.util.TaggerUtil;

public class GameListeners implements Listener {

    @EventHandler
    private void handleMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        event.setDamage(0.1);
        victim.setHealth(victim.getAttribute(Attribute.MAX_HEALTH).getValue());
        if (!TaggerUtil.checkPlayerInProperWorld(damager)) return;
        if (damager.getFallDistance() < 1.5f) return;
        if (!damager.getInventory().getItemInMainHand().getType().equals(Material.MACE)) return;
        TaggerUtil.setTagger(damager, victim);
    }

    @EventHandler
    private void handleFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (!TaggerUtil.checkPlayerInProperWorld(victim)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void handleExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) return;
        if (!TaggerUtil.checkPlayerInProperWorld(victim)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void handleEntityExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) return;
        if (!TaggerUtil.checkPlayerInProperWorld(victim)) return;
        event.setCancelled(true);
    }

}
