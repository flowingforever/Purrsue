package pro.fazeclan.river.purrsue.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MessageUtil {

    public static Component getPluginMessage(String message) {
        return MiniMessage.miniMessage().deserialize("<#7AE4FF>\uD83C\uDFF9</#7AE4FF> " + message);
    }

}
