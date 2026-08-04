package pro.fazeclan.river.persue.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pro.fazeclan.river.persue.Persue;

import java.util.List;
import java.util.function.Consumer;

public class SpinUtil {

    public static void spin(List<Player> players, Location center, ItemStack item) {
        spin(players, center, item, _ -> {});
    }

    public static void spin(List<Player> players, Location center, ItemStack item, Consumer<Player> end) {
        final var spins = 6 + Math.random() * 4;
        final var finalAngle = 360 * spins + Math.random() * 360;
        final var totalTicks = 140;
        final var interpolatedTicks = 2;
        final var itemDisplay = spawnDisplay(center, item);

        new BukkitRunnable() {
            int tick = 0;
            double currentAngleDeg = 0;
            Player pointedPlayer = null;

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    itemDisplay.remove();
                    pointedPlayer.setGlowing(false);
                    end.accept(pointedPlayer);
                    cancel();
                }

                var progress = (double) tick / totalTicks;
                var eased = 1 - Math.pow(1 - progress, 3);
                var targetAngle = finalAngle * eased;
                var delta = targetAngle - currentAngleDeg;
                currentAngleDeg = targetAngle;

                rotateDisplay(delta, interpolatedTicks, itemDisplay);
                update();
                tick++;
            }

            private void update() {
                var newPointed = findPointedAt(players, center, currentAngleDeg);
                if (newPointed == pointedPlayer) {
                    return;
                }

                if (pointedPlayer != null) {
                    pointedPlayer.setGlowing(false);
                }

                if (newPointed != null) {
                    newPointed.setGlowing(true);
                    center.getWorld().playSound(center, Sound.UI_BUTTON_CLICK, 1.0f, 1.6f);
                }

                pointedPlayer = newPointed;
            }
        }.runTaskTimer(Persue.getInstance(), 0L, 1L);
    }

    public static void arrangePlayers(Location center, double radius, List<Player> players) {
        var count = players.size();
        var world = center.getWorld();

        for (int i = 0; i < count; i++) {
            var angle = 2 * Math.PI * i / count;
            var x = center.getX() + radius * Math.cos(angle);
            var z = center.getZ() + radius * Math.sin(angle);
            var loc = new Location(world, x, center.getY(), z);

            var facing = center.toVector().subtract(loc.toVector()).setY(0).normalize();
            loc.setDirection(facing);

            players.get(i).teleport(loc);
        }
    }

    public static ItemDisplay spawnDisplay(Location center, ItemStack item) {
        var pos = center.clone();
        pos.setYaw(0f);
        pos.setPitch(0f);
        return center.getWorld().spawn(pos, ItemDisplay.class, d -> {
            d.setItemStack(item);
            d.setBillboard(Display.Billboard.FIXED);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            var transformation = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f().add(0f, 0.2f, 0f),
                    new Quaternionf().rotateXYZ((float) Math.toRadians(90), 0, (float) Math.toRadians(90)),
                    transformation.getScale().mul(1.5f),
                    transformation.getRightRotation()
            ));
            d.setInterpolationDuration(2);
            d.setInterpolationDelay(0);
        });
    }

    public static void rotateDisplay(double degrees, int ticks, ItemDisplay display) {
        var t = display.getTransformation();

        var delta = new Quaternionf().rotateY((float) Math.toRadians(degrees));
        var newRotation = delta.mul(t.getLeftRotation(), new Quaternionf());

        display.setInterpolationDelay(0);
        display.setInterpolationDuration(ticks);
        display.setTransformation(new Transformation(
                t.getTranslation(),
                newRotation,
                t.getScale(),
                t.getRightRotation()
        ));
    }

    public static Player findPointedAt(List<Player> players, Location center, double currentAngleDeg) {
        var angleRad = Math.toRadians(currentAngleDeg % 360);
        var pointDir = new Vector(Math.cos(angleRad), 0, Math.sin(angleRad));

        Player closestPlayer = null;
        var bestDot = -Double.MAX_VALUE;

        for (Player p : players) {
            var toPlayer = p.getLocation().toVector().subtract(center.toVector());
            toPlayer.setY(0);
            toPlayer.normalize();

            var dot = toPlayer.dot(pointDir);
            if (dot > bestDot) {
                bestDot = dot;
                closestPlayer = p;
            }
        }
        return closestPlayer;
    }

}
