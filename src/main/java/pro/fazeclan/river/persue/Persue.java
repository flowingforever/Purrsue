package pro.fazeclan.river.persue;

import com.github.retrooper.packetevents.PacketEvents;
import de.tr7zw.nbtapi.NBT;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import pro.fazeclan.river.jarona.Jarona;
import pro.fazeclan.river.persue.command.ConfigCommand;
import pro.fazeclan.river.persue.command.TestCommand;
import pro.fazeclan.river.persue.game.FreeForAllGame;
import pro.fazeclan.river.persue.listener.GameListeners;

public final class Persue extends JavaPlugin {

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        PacketEvents.getAPI().init();

        NBT.preloadApi();

        var gameManager = Jarona.getInstance().getGameManager();
        gameManager.register(new FreeForAllGame());

        var command = Commands.literal("persue")
                .then(TestCommand.command())
                .then(ConfigCommand.command())
                .build();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(command);
        });

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GameListeners(), this);

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PacketEvents.getAPI().terminate();
    }

    public static Persue getInstance() {
        return JavaPlugin.getPlugin(Persue.class);
    }

    public static NamespacedKey getKey(String key) {
        return new NamespacedKey("persue", key);
    }

}
