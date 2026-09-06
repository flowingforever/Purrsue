package pro.fazeclan.river.purrsue.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.fazeclan.river.jarona.condition.Condition;
import pro.fazeclan.river.jarona.game.GameValues;
import pro.fazeclan.river.jarona.util.ConditionUtil;
import pro.fazeclan.river.purrsue.Purrsue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TaggerUtil {

    public static void setTagger(@Nullable Player oldTagger, Player newTagger, GameValues values) {
        // visual changes (nametag & condition)
        var world = newTagger.getWorld();

        double initialTime = values.getValue("initial_time_" + newTagger.getUniqueId(), 19 * 20L);
        if (oldTagger != null) {
            long newTime = Math.max((long) (initialTime * 20L) - 10, 9 * 20L);
            values.setValue("initial_time_" + newTagger.getUniqueId(), newTime / 20L);
        }
        values.setValue("tick_" + newTagger.getUniqueId(), 0L);
        newTagger.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                -1,
                0,
                true,
                false,
                true
        ));

        var condition = ConditionUtil.getPlayerConditions(newTagger)
                .getOrCreate(
                        "tagger",
                        new Condition() {
                            @Override
                            public boolean getAvailable() {
                                return true;
                            }

                            @Override
                            public void reset() {

                            }
                        }
                );

        condition.setHud(_ -> {
            var duration = Math.max(
                    0,
                    values.getValue("initial_time_" + newTagger.getUniqueId(), 19 * 20L) - values.getValue("tick_" + newTagger.getUniqueId(), 0L)
            );
            return "<red>You'll explode in</red> <sprite:blocks:block/tnt_side> <red>" + String.format("%.1f", duration / 20.0) + "s!</red>";
        });
        condition.setHudCondition((_, p) -> p.hasPotionEffect(PotionEffectType.GLOWING));

        world.spawn(newTagger.getLocation().clone().setRotation(0, 0), TextDisplay.class, td -> {
            var mm = MiniMessage.miniMessage();
            var transformation = td.getTransformation();
            transformation.getScale().set(2.0, 2.0, 2.0);
            transformation.getTranslation().add(0.0f, 0.5f, 0.0f);
            td.setBillboard(Display.Billboard.CENTER);
            td.setTransformation(transformation);
            td.setSeeThrough(true);

            Bukkit.getScheduler().runTaskTimer(
                    Purrsue.getInstance(),
                    task -> {
                        if (!td.isValid()) {
                            task.cancel();
                        }

                        double duration = Math.max(
                                0,
                                values.getValue("initial_time_" + newTagger.getUniqueId(), 19 * 20L) - values.getValue("tick_" + newTagger.getUniqueId(), 0L)
                        ) / 20.0;
                        td.text(mm.deserialize("<sprite:blocks:block/tnt_side> <red>" + String.format("%.1f", duration) + "s</red>"));
                    },
                    0,
                    2
            );

            newTagger.addPassenger(td);
        });

        // actually giving items
        newTagger.give(ItemUtil.generateMace());
        newTagger.getEquipment().setItemInOffHand(ItemType.WIND_CHARGE.createItemStack(64));

        if (oldTagger != null) {
            // visual changes (nametag)
            for (var entity : oldTagger.getPassengers()) {
                if (entity instanceof TextDisplay) {
                    entity.remove();
                }
            }
            oldTagger.removePotionEffect(PotionEffectType.GLOWING);

            // actually removing items
            var oldTaggerInventory = oldTagger.getInventory();
            oldTaggerInventory.forEach(itemStack -> {
                if (itemStack == null) return;
                if (itemStack.isEmpty()) return;
                if (itemStack.getType().equals(Material.WIND_CHARGE) || itemStack.getType().equals(Material.MACE)) {
                    oldTaggerInventory.removeItemAnySlot(itemStack);
                }
            });
        }
    }

    public static boolean checkEntityInProperWorld(Entity entity) {
        return entity.getWorld().getKey().namespace().equalsIgnoreCase("purrsue");
    }

    public static void reassignWielders(List<Player> players, GameValues values) {
        var alivePlayers = getAlivePlayers(players);
        int aliveCount = alivePlayers.size();
        if (alivePlayers.isEmpty()) return;
        int newTaggerAmount = (int) Math.floor(aliveCount / 4.0) + 1;
        for (int i = 0; i < newTaggerAmount; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(aliveCount);
            var tagger = alivePlayers.get(randomIndex);
            setTagger(null, tagger, values);
        }
    }

    public static void blowUpIfNecessary(List<Player> players, GameValues values) {
        for (Player wielder : getWielders(players)) {
            if (values.getValue("tick_" + wielder.getUniqueId(), 0L) <= values.getValue("initial_time_" + wielder.getUniqueId(), 19 * 20L)) continue;

            var world = wielder.getWorld();
            for (var entity : wielder.getPassengers()) {
                if (entity instanceof TextDisplay) {
                    entity.remove();
                }
            }
            wielder.setGameMode(GameMode.SPECTATOR);
            wielder.removePotionEffect(PotionEffectType.GLOWING);
            world.spawnParticle(
                    Particle.EXPLOSION,
                    wielder.getLocation(),
                    20,
                    1, 1, 1
            );
            world.playSound(
                    wielder.getLocation(),
                    "minecraft:entity.generic.explode",
                    SoundCategory.PLAYERS,
                    1f,
                    1f
            );
        }
    }

    public static List<Player> getAlivePlayers(List<Player> players) {
        return players.stream().filter(player -> !player.getGameMode().isInvulnerable()).toList();
    }

    public static List<Player> getWielders(List<Player> players) {
        return getAlivePlayers(players).stream().filter(p -> p.hasPotionEffect(PotionEffectType.GLOWING)).toList();
    }

}
