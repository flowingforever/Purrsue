package pro.fazeclan.river.purrsue.util;

import gg.lode.sign.api.SignAPI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.fazeclan.river.jarona.condition.Condition;
import pro.fazeclan.river.jarona.condition.TimedCondition;
import pro.fazeclan.river.jarona.game.GameValues;
import pro.fazeclan.river.jarona.util.ConditionUtil;
import pro.fazeclan.river.jarona.util.NicknameUtil;
import pro.fazeclan.river.jarona.util.WorldUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TaggerUtil {

    public static void setTagger(@Nullable Player oldTagger, Player newTagger, GameValues values) {
        // visual changes (nametag & condition)
        var api = SignAPI.getNametagManager();
        api.get(newTagger).setLines(List.of(
                "<red><shadow:#000000FF>TAGGED</shadow></red>",
                NicknameUtil.getNickname(newTagger)
        ));
        var world = newTagger.getWorld();
        double initialTime = values.getValue("initial_time", 19 * 20L);
        long newTime = Math.max((long) (initialTime * 20L) - 5, 9 * 20L);
        values.setValue("initial_time", newTime / 20L);
        values.setValue("tick", 0L);
        newTagger.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                -1,
                0,
                true,
                false,
                true
        ));

        var worldTC = ConditionUtil.getWorldConditions(world)
                        .getOrCreate(
                                "game_" + world.getKey().value(),
                                new Condition() {
                                    @Override
                                    public boolean getAvailable() {
                                        return true;
                                    }

                                    @Override
                                    public void reset() {}
                                }
                        );

        worldTC.setHud(_ -> {
            var duration = values.getValue("initial_time", 20L) - values.getValue("tick", 0L);
            return "<red><sprite:items:item/mace> " + String.format("%.1f", duration / 20.0) + "s</red>";
        });

        worldTC.setHudCondition((_, _) -> true);

        // actually giving items
        newTagger.give(ItemUtil.generateMace());
        newTagger.getEquipment().setItemInOffHand(ItemType.WIND_CHARGE.createItemStack(64));

        if (oldTagger != null) {
            // visual changes (nametag)
            api.get(oldTagger).setLines(List.of(NicknameUtil.getNickname(oldTagger)));
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

    public static void blowUpAndReassign(List<Player> players, GameValues values) {
        for (Player player : players) {
            if (player.getInventory().contains(Material.MACE)) {
                var world = player.getWorld();
                player.setGameMode(GameMode.SPECTATOR);
                world.spawnParticle(
                        Particle.EXPLOSION,
                        player.getLocation(),
                        20,
                        1, 1, 1
                );
                world.playSound(
                        player.getLocation(),
                        "minecraft:entity.generic.explode",
                        SoundCategory.PLAYERS,
                        1f,
                        1f
                );
            }
        }

        var alivePlayers = getAlivePlayers(players);
        int aliveCount = alivePlayers.size();
        if (alivePlayers.isEmpty()) return;
        int newTaggerAmount = Math.max(1, (int) Math.round(aliveCount / 4.0));
        for (int i = 0; i < newTaggerAmount; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(aliveCount);
            var tagger = alivePlayers.get(randomIndex);
            setTagger(null, tagger, values);
        }
    }

    public static List<Player> getAlivePlayers(List<Player> players) {
        return players.stream().filter(player -> !player.getGameMode().isInvulnerable()).toList();
    }

}
