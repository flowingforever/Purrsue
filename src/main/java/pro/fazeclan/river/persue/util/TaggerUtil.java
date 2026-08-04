package pro.fazeclan.river.persue.util;

import org.alexdev.unlimitednametags.api.UNTPaperAPI;
import org.alexdev.unlimitednametags.config.Settings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import pro.fazeclan.river.jarona.condition.TimedCondition;
import pro.fazeclan.river.jarona.util.ConditionUtil;
import pro.fazeclan.river.persue.Persue;

import javax.annotation.Nullable;
import java.util.ArrayList;

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
        var worldPDC = world.getPersistentDataContainer();
        long initialTime = worldPDC
                        .getOrDefault(
                                Persue.getKey("initial_time"),
                                PersistentDataType.LONG,
                                19 * 1000L
                        );
        long newTime = Math.max(initialTime - 300, 9 * 1000L);
        worldPDC.set(
                Persue.getKey("initial_time"),
                PersistentDataType.LONG,
                newTime
        );
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

}
