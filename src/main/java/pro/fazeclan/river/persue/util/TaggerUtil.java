package pro.fazeclan.river.persue.util;

import org.alexdev.unlimitednametags.api.UNTPaperAPI;
import org.alexdev.unlimitednametags.config.Settings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import pro.fazeclan.river.jarona.condition.TimedCondition;
import pro.fazeclan.river.jarona.util.ConditionUtil;
import pro.fazeclan.river.jarona.util.WorldUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TaggerUtil {

    public static void setTagger(@Nullable Player oldTagger, Player newTagger) {
        // visual changes (nametag & condition)
        var api = UNTPaperAPI.getInstance();
        api.modifyNametagProperty(newTagger, current -> {
            var groups = new ArrayList<>(current.displayGroups());
            groups.add(Settings.DisplayGroup.builder()
                            .line("<red><shadow:#000000FF>TAGGED</shadow></red>")
                            .scale(2f)
                            .yOffset(0f)
                    .build());
            return current.withDisplayGroups(groups);
        });
        var world = newTagger.getWorld();
        double initialTime = WorldUtil.getWorldValue(world, "initial_time", PersistentDataType.DOUBLE, 19.0);
        long newTime = Math.max((long) (initialTime * 1000L) - 300, 9 * 1000L);
        WorldUtil.setWorldValue(world, "initial_time", PersistentDataType.DOUBLE, newTime/1000.0);
        WorldUtil.setWorldValue(world, "tick", PersistentDataType.LONG, 0L);

        var worldTC = ConditionUtil.getWorldConditions(world)
                        .getOrCreate(
                                "game_" + world.getKey().value(),
                                new TimedCondition(
                                        TimedCondition.Type.MILLIS
                                )
                        );

        worldTC.setHud(condition -> {
            var tc = (TimedCondition) condition;
            return "<red><sprite:items:item/mace> " + String.format("%.1f", tc.getDuration() / 1000.0) + "s</red>";
        });

        worldTC.setHudCondition(_ -> true);
        worldTC.setDuration(newTime);

        // actually giving items
        newTagger.give(ItemUtil.generateMace());
        newTagger.getEquipment().setItemInOffHand(ItemType.WIND_CHARGE.createItemStack(64));

        if (oldTagger != null) {
            // visual changes (nametag)
            api.modifyNametagProperty(oldTagger, current -> {
                var groups = new ArrayList<>(current.displayGroups());
                groups.removeLast();
                return current.withDisplayGroups(groups);
            });

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

    public static boolean checkPlayerInProperWorld(Player player) {
        return player.getWorld().getKey().namespace().equalsIgnoreCase("persue");
    }

    public static void blowUpAndReassign(List<Player> players) {
        for (Player player : players) {
            if (player.getInventory().contains(Material.MACE)) {
                var world = player.getWorld();
                var loc = player.getLocation();
                player.setGameMode(GameMode.SPECTATOR);
                world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 5f, false, false);
            }
        }

        var alivePlayers = getAlivePlayers(players);
        int aliveCount = alivePlayers.size();
        if (alivePlayers.isEmpty()) return;
        int newTaggerAmount = Math.max(1, (int) Math.round(aliveCount / 4.0));
        for (int i = 0; i < newTaggerAmount; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(aliveCount);
            var tagger = alivePlayers.get(randomIndex);
            setTagger(null, tagger);
        }
    }

    public static List<Player> getAlivePlayers(List<Player> players) {
        return players.stream().filter(player -> !player.getGameMode().isInvulnerable()).toList();
    }

}
