package pro.fazeclan.river.purrsue.util;

import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;

@Getter
public class Team {

    private final String id;
    private final String name;
    private final TextColor color;
    private final String icon;

    public Team(String id, String name, TextColor color, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

}
