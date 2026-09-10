package pro.fazeclan.river.purrsue.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Piston;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import pro.fazeclan.river.jarona.util.GameUtil;
import pro.fazeclan.river.purrsue.Purrsue;
import pro.fazeclan.river.purrsue.util.MessageUtil;
import pro.fazeclan.river.purrsue.util.TaggerUtil;
import pro.fazeclan.river.purrsue.util.TeamUtil;

public class GameListeners implements Listener, PacketListener {

    private final Purrsue plugin;

    public GameListeners(Purrsue plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void handleMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!TaggerUtil.checkEntityInProperWorld(victim)) return;
        event.setDamage(0.0);
        if (!attacker.getInventory().getItemInMainHand().getType().equals(Material.MACE)) return;
        if (attacker.getFallDistance() < 1.5) return;
        var world = victim.getWorld();
        var values = GameUtil.getGame(world).getGameValues(world.getUID());
        if (TeamUtil.areSameTeam(victim, attacker, values)) {
            attacker.sendMessage(MessageUtil.getPluginMessage(
                    "<red>You may not pass it on to someone on the same team as you!"
            ));
            return;
        }
        TaggerUtil.setTagger(attacker, victim, values);
    }

    @EventHandler
    private void handleFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        if (!TaggerUtil.checkEntityInProperWorld(victim)) return;
        event.setCancelled(true);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            var packet = new WrapperPlayServerEntityVelocity(event);
            Player viewer = event.getPlayer();
            var entity = SpigotConversionUtil.getEntityById(viewer.getWorld(), packet.getEntityId());
            if (!(entity instanceof Player affected)) return;
            if (!TaggerUtil.checkEntityInProperWorld(affected)) return;

            var config = plugin.getConfig();

            if (affected.hasPotionEffect(PotionEffectType.GLOWING)) {
                var horizontalMultiplier = config.getDouble("wielder.horizontal-knockback-multiplier", 0.5);
                var verticalMultiplier = config.getDouble("wielder.vertical-knockback-multiplier", 1);
                packet.setVelocity(packet.getVelocity().multiply(horizontalMultiplier, verticalMultiplier, horizontalMultiplier));
            } else {
                var horizontalMultiplier = config.getDouble("player.horizontal-knockback-multiplier", 0.9);
                var verticalMultiplier = config.getDouble("player.vertical-knockback-multiplier", 1);
                packet.setVelocity(packet.getVelocity().multiply(horizontalMultiplier, verticalMultiplier, horizontalMultiplier));
            }
            event.markForReEncode(true);
        }

        if (event.getPacketType() == PacketType.Play.Server.EXPLOSION) {
            var packet = new WrapperPlayServerExplosion(event);
            Player viewer = event.getPlayer();
            if (!TaggerUtil.checkEntityInProperWorld(viewer)) return;
            var kb = packet.getKnockback();
            if (kb == null) return;
            var multiplier = plugin.getConfig().getDouble("wind-charge-multiplier", 1.5);

            packet.setKnockback(kb.multiply(multiplier));
            event.markForReEncode(true);
        }
    }

    @EventHandler
    private void onJumpAboveBlock(PlayerJumpEvent event) {
        var user = event.getPlayer();
        if (!TaggerUtil.checkEntityInProperWorld(user)) return;
        var block = user.getLocation().clone().subtract(0.0, 0.1, 0.0).getBlock();
        if (block.getType().equals(Material.PISTON) || block.getType().getKey().value().contains("grate")) {
            var config = plugin.getConfig();
            user.setVelocity(user.getVelocity().clone().add(new Vector(0.0, config.getDouble("pad-boost", 1.0), 0.0)));
            user.getWorld().playSound(user, "minecraft:block.piston.extend", SoundCategory.PLAYERS, 1f, 1f);

            if (block.getType().equals(Material.PISTON)) {
                var blockData = (Piston) block.getBlockData();
                blockData.setExtended(true);
                block.setBlockData(blockData);

                var world = block.getWorld();
                var headLoc = block.getLocation().clone().add(0.0, 1.0, 0.0);
                world.setBlockData(headLoc, BlockType.PISTON_HEAD.createBlockData(meta -> meta.setFacing(BlockFace.UP)));

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!block.getLocation().isWorldLoaded()) {
                        return;
                    }

                    blockData.setExtended(false);
                    block.setBlockData(blockData);
                    world.setBlockData(headLoc, BlockType.AIR.createBlockData());
                }, 40);
            }
        }
    }

}
