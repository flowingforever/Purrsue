package pro.fazeclan.river.purrsue.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pro.fazeclan.river.purrsue.util.ItemUtil;
import pro.fazeclan.river.purrsue.util.SpinUtil;

import java.util.List;

public class TestCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("test")
                .requires(ctx -> ctx.getSender().hasPermission("persue.dev.test"))
                .then(
                        Commands.literal("spin")
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getSender() instanceof Player viewer)) {
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    var center = viewer.getLocation();
                                    var players = (List<Player>) Bukkit.getOnlinePlayers().stream().toList();

                                    SpinUtil.arrangePlayers(center, 5.0, players);
                                    SpinUtil.spin(players, center, ItemUtil.generateMace());

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("location", ArgumentTypes.finePosition())
                                                .executes(ctx -> {
                                                    if (!(ctx.getSource().getSender() instanceof Player viewer)) {
                                                        return Command.SINGLE_SUCCESS;
                                                    }

                                                    var center = ctx.getArgument("location", FinePositionResolver.class).resolve(ctx.getSource());

                                                    var loc = center.toLocation(viewer.getWorld());
                                                    var players = (List<Player>) Bukkit.getOnlinePlayers().stream().toList();

                                                    SpinUtil.arrangePlayers(loc, 5.0, players);
                                                    SpinUtil.spin(players, loc, ItemUtil.generateMace());

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                );
    }

}
