package pro.fazeclan.river.purrsue.util;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import pro.fazeclan.river.jarona.game.GameValues;
import pro.fazeclan.river.jarona.util.NametagUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamUtil {

    private static final List<Team> possibleTeams = List.of(
            new Team("copper", "Copper Team", TextColor.fromHexString("#E77C56"), "<sprite:items:item/copper_ingot>"),
            new Team("iron", "Iron Team", TextColor.fromHexString("#D8D8D8"), "<sprite:items:item/iron_ingot>"),
            new Team("gold", "Gold Team", TextColor.fromHexString("#FAD64A"), "<sprite:items:item/gold_ingot>"),
            new Team("emerald", "Emerald Team", TextColor.fromHexString("#17DD62"), "<sprite:items:item/emerald>"),
            new Team("lapis", "Lapis Team", TextColor.fromHexString("#5A82E2"), "<sprite:items:item/lapis_lazuli>"),
            new Team("diamond", "Diamond Team", TextColor.fromHexString("#2CE0D8"), "<sprite:items:item/diamond>"),
            new Team("amethyst", "Amethyst Team", TextColor.fromHexString("#B38EF3"), "<sprite:items:item/amethyst_shard>"),
            new Team("prismarine", "Prismarine Team", TextColor.fromHexString("#A4D1C2"), "<sprite:items:item/prismarine_shard>"),
            new Team("resin", "Resin Team", TextColor.fromHexString("#E37B2B"), "<sprite:items:item/resin_clump>"),
            new Team("shulker", "Shulker Team", TextColor.fromHexString("#976997"), "<sprite:items:item/shulker_shell>"),
            new Team("redstone", "Redstone Team", TextColor.fromHexString("#AA0F01"), "<sprite:items:item/redstone>")
    );

    public static void splitPlayersIntoTeams(List<Player> players, GameValues values, int teamCount) {
        var randomizedPlayers = new ArrayList<>(players);
        Collections.shuffle(randomizedPlayers);

        var teams = Lists.partition(randomizedPlayers, (int) Math.ceil(randomizedPlayers.size() / (double) teamCount));
        if (teams.size() > possibleTeams.size()) {
            return;
        }

        var randomizedTeams = new ArrayList<>(possibleTeams);
        Collections.shuffle(randomizedTeams);
        for (var playerTeam : teams) {
            var team = randomizedTeams.removeFirst();
            for (var player : playerTeam) {
                values.setValue("team_" + player.getUniqueId(), team);
                NametagUtil.setName(
                        player,
                        values,
                        (t, v, ctx, vl) ->
                                team.getIcon() + " <" + team.getColor().asHexString() + ">%jarona_nickname%</" + team.getColor().asHexString() + ">"
                );
            }
        }
    }

    public static boolean areSameTeam(Player p1, Player p2, GameValues values) {
        var t1 = getTeam(p1, values);
        if (t1 == null) return false;
        var t2 = getTeam(p2, values);
        if (t2 == null) return false;
        return t1.getId().equals(t2.getId());
    }

    public static Team getTeam(Player player, GameValues values) {
        return values.getValue("team_" + player.getUniqueId(), (Team) null);
    }

}
