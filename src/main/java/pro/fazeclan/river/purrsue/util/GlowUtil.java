package pro.fazeclan.river.purrsue.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GlowUtil {

    public static void setGlowOfPlayerToWorld(World world, Player target, NamedTextColor color) {
        for (var viewer : world.getPlayers()) {
            setGlowOfPlayer(target, viewer, color);
        }
    }

    public static void removeGlowOfPlayerToWorld(World world, Player target) {
        for (var viewer : world.getPlayers()) {
            removeGlowOfPlayer(target, viewer);
        }
    }

    public static void setGlowOfPlayer(Player target, Player viewer, NamedTextColor color) {
        WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                Component.empty(),
                Component.empty(),
                WrapperPlayServerTeams.NameTagVisibility.NEVER,
                WrapperPlayServerTeams.CollisionRule.NEVER,
                color,
                WrapperPlayServerTeams.OptionData.NONE
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(
                viewer,
                new WrapperPlayServerTeams(
                        "purrsue_" + target.getName(),
                        WrapperPlayServerTeams.TeamMode.CREATE,
                        teamInfo,
                        target.getName()
                )
        );
    }

    public static void removeGlowOfPlayer(Player target, Player viewer) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, new WrapperPlayServerTeams(
                "purrsue_" + target.getName(),
                WrapperPlayServerTeams.TeamMode.REMOVE,
                (WrapperPlayServerTeams.ScoreBoardTeamInfo) null,
                target.getName()
        ));
    }

}
