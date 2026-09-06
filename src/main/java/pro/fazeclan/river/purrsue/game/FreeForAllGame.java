package pro.fazeclan.river.purrsue.game;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.fazeclan.river.jarona.game.Game;
import pro.fazeclan.river.jarona.util.GameUtil;
import pro.fazeclan.river.jarona.util.NicknameUtil;
import pro.fazeclan.river.jarona.util.WorldlessLocation;
import pro.fazeclan.river.purrsue.Purrsue;
import pro.fazeclan.river.purrsue.util.ItemUtil;
import pro.fazeclan.river.purrsue.util.SpinUtil;
import pro.fazeclan.river.purrsue.util.TaggerUtil;

import java.io.File;
import java.util.List;

public class FreeForAllGame extends Game {

    public FreeForAllGame() {
        super(
                "<green>Purrsue: FFA</green>",
                Purrsue.getKey("ffa"),
                true,
                true,
                2
        );
    }

    @Override
    public void init(World world, List<Player> players) {
        var config = YamlConfiguration.loadConfiguration(new File(world.getWorldFolder(), "map_config.yml"));
        var spawn = WorldlessLocation.deserialize("spawn", config).toLocation(world);

        var movementBlocker = ItemType.GRAY_STAINED_GLASS_PANE.createItemStack(meta -> {
            meta.addAttributeModifier(Attribute.JUMP_STRENGTH, new AttributeModifier(
                    Purrsue.getKey("jump_strength"),
                    -1.0,
                    AttributeModifier.Operation.ADD_SCALAR,
                    EquipmentSlotGroup.CHEST
            ));
            meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    Purrsue.getKey("movement_speed"),
                    -1.0,
                    AttributeModifier.Operation.ADD_SCALAR,
                    EquipmentSlotGroup.CHEST
            ));
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        });
        movementBlocker.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.CHEST).build());
        for (Player player : players) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getEquipment().setChestplate(movementBlocker);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, true, false, false));
        }

        var values = getGameValues(world.getUID());

        SpinUtil.arrangePlayers(spawn, 3.0, players);
        int newTaggerAmount = (int) Math.floor(players.size() / 4.0) + 1;
        for (int i = 0; i < newTaggerAmount; i++) {
            SpinUtil.spin(players, spawn, ItemUtil.generateMace(), selected -> {
                for (Player player : players) {
                    player.getEquipment().setChestplate(null);
                }
                TaggerUtil.setTagger(null, selected, values);
                values.setValue("round_started", true);
            });
        }
    }

    @Override
    public void tick(World world, List<Player> players) {
        var values = getGameValues(world.getUID());
        if (!values.getValue("round_started", false)) return;

        if (TaggerUtil.getAlivePlayers(players).size() == 1) {
            GameUtil.endGame(world);
            values.setValue("round_started", false);
            return;
        }

        for (var wielder : TaggerUtil.getWielders(players)) {
            long tick = values.getValue("tick_" + wielder.getUniqueId(), 0L) + 1L;
            values.setValue("tick_" + wielder.getUniqueId(), tick);
        }

        TaggerUtil.blowUpIfNecessary(players, values);

        if (TaggerUtil.getWielders(players).isEmpty()) {
            TaggerUtil.reassignWielders(players, values);
        }
    }

    @Override
    public void end(World world, List<Player> players) {
        var winner = TaggerUtil.getAlivePlayers(players).getFirst();
        var miniMessage = MiniMessage.miniMessage();

        for (Player player : players) {
            player.showTitle(Title.title(
                    miniMessage.deserialize("<gray><<</gray> <head:" + winner.getUniqueId() + "> <gray>>></gray>"),
                    miniMessage.deserialize("<green>" + NicknameUtil.getNickname(winner) + "<reset><green> wins!</green>")
            ));

            for (var passenger : player.getPassengers()) { // otherwise players will not be able to get teleported out
                if (passenger instanceof TextDisplay) {
                    passenger.remove();
                }
            }
        }
    }

}
