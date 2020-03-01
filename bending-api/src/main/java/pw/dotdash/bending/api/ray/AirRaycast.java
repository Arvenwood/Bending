package pw.dotdash.bending.api.ray;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.dotdash.bending.api.effect.EffectService;
import pw.dotdash.bending.api.util.BlockTypeUtil;
import pw.dotdash.bending.api.util.VectorUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of utilities primarily used by air abilities' raycasts.
 */
public final class AirRaycast {

    public static boolean extinguishFlames(Location<World> test) {
        checkNotNull(test, "test");

        if (test.getBlockType() != BlockTypes.FIRE) {
            return false;
        }

        test.setBlockType(BlockTypes.AIR);
        test.getExtent().spawnParticles(EffectService.getInstance().getExtinguishEffect(), test.getPosition());

        return true;
    }

    public static boolean coolLava(Location<World> test) {
        checkNotNull(test, "test");

        if (test.getBlockType() != BlockTypes.LAVA && test.getBlockType() != BlockTypes.FLOWING_LAVA) {
            return false;
        }

        if (test.getBlockType() == BlockTypes.FLOWING_LAVA) {
            test.setBlockType(BlockTypes.AIR);
        } else if (test.get(Keys.FLUID_LEVEL).get() == 0) {
            test.setBlockType(BlockTypes.OBSIDIAN);
        } else {
            test.setBlockType(BlockTypes.COBBLESTONE);
        }

        return true;
    }

    public static boolean toggleDoor(Location<World> test) {
        checkNotNull(test, "test");

        if (!BlockTypeUtil.DOORS.contains(test.getBlockType())) {
            return false;
        }

        test.offer(Keys.OPEN, !test.getOrElse(Keys.OPEN, false));

        return true;
    }

    public static boolean toggleLever(Location<World> test) {
        checkNotNull(test, "test");

        if (test.getBlockType() != BlockTypes.LEVER) {
            return false;
        }

        test.offer(Keys.POWERED, !test.getOrElse(Keys.POWERED, false));

        return true;
    }

    public static boolean pushEntity(Raycast ray, Player source, Entity target,
                              boolean canPushSelf, double knockbackSelf, double knockbackOther) {
        checkNotNull(ray, "ray");
        checkNotNull(source, "source");
        checkNotNull(target, "target");

        final boolean isSelf = source.getUniqueId().equals(target.getUniqueId());
        double knockback = knockbackOther;

        if (isSelf) {
            if (!canPushSelf) {
                // Ignore us.
                return false;
            }

            knockback = knockbackSelf;
        }

        knockback *= (1 - target.getLocation().getPosition().distance(ray.getOrigin().getPosition()) / (2 * ray.getRange()));

        if (BlockTypeUtil.isSolid(target.getLocation().add(0.0, -0.5, 0.0).getBlockType())) {
            knockback *= 0.85;
        }

        final double max = ray.getSpeed() / ray.getSpeedFactor();

        Vector3d push = ray.getDirection();
        if (Math.abs(push.getY()) > max && !isSelf) {
            push = VectorUtil.setY(push, push.getY() < 0 ? -max : max);
        }

        push = push.normalize().mul(knockback);

        if (Math.abs(target.getVelocity().dot(push)) > knockback
                && VectorUtil.getAngleBetween(target.getVelocity(), push) > Math.PI / 3) {
            push = push.normalize().add(target.getVelocity()).mul(knockback);
        }

        target.setVelocity(push.min(4.0, 4.0, 4.0).max(-4.0, -4.0, -4.0));
        return true;
    }

    private AirRaycast() {
        throw new AssertionError("Don't instantiate me!");
    }
}