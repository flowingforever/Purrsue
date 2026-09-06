package pro.fazeclan.river.purrsue.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import pro.fazeclan.river.purrsue.Purrsue;

public class ItemUtil {

    public static ItemStack generateMace() {
        return ItemType.MACE.createItemStack(meta -> {
            meta.addAttributeModifier(
                    Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(Purrsue.getKey("base_mace_damage"), 5, AttributeModifier.Operation.ADD_NUMBER)
            );
            meta.addAttributeModifier(
                    Attribute.ATTACK_SPEED,
                    new AttributeModifier(Purrsue.getKey("base_mace_speed"), 3.4, AttributeModifier.Operation.ADD_NUMBER)
            );
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
    }

}
