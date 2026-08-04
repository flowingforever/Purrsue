package pro.fazeclan.river.persue.game;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import org.alexdev.unlimitednametags.api.UNTPaperAPI;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.fazeclan.river.jarona.game.Game;
import pro.fazeclan.river.jarona.util.WorldlessLocation;
import pro.fazeclan.river.persue.Persue;
import pro.fazeclan.river.persue.util.ItemUtil;
import pro.fazeclan.river.persue.util.SpinUtil;
import pro.fazeclan.river.persue.util.TaggerUtil;

import java.io.File;
import java.util.List;

public class FreeForAllGame extends Game {

    public FreeForAllGame() {
        super(
                "<green>Persue: Free For All</green>",
                Persue.getKey("ffa"),
                true,
                true,
                4
        );
    }

    @Override
    public void init(World world, List<Player> players) {
        var api = UNTPaperAPI.getInstance();
        var config = YamlConfiguration.loadConfiguration(new File(world.getWorldFolder(), "map_config.yml"));
        var spawn = WorldlessLocation.deserialize("spawn", config).toLocation(world);

        var movementBlocker = ItemType.GRAY_STAINED_GLASS_PANE.createItemStack(meta -> {
            meta.addAttributeModifier(Attribute.JUMP_STRENGTH, new AttributeModifier(
                    Persue.getKey("jump_strength"),
                    -1.0,
                    AttributeModifier.Operation.ADD_SCALAR,
                    EquipmentSlotGroup.CHEST
            ));
            meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    Persue.getKey("movement_speed"),
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

        SpinUtil.arrangePlayers(spawn, 3.0, players);
        SpinUtil.spin(players, spawn, ItemUtil.generateMace(), selected -> {
            for (Player player : players) {
                player.getEquipment().setChestplate(null);
            }
            TaggerUtil.setTagger(null, selected);
        });
    }

    @Override
    public void tick(World world, List<Player> players) {

    }

    @Override
    public void end(World world, List<Player> players) {

    }

}
