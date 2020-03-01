package pw.dotdash.bending.api.ray;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkNotNull;

public interface Raycast {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    static boolean advanceAll(Iterable<Raycast> rays, BiPredicate<Raycast, Location<World>> block) {
        checkNotNull(rays, "rays");
        checkNotNull(block, "block");

        boolean successful = false;
        for (Raycast ray : rays) {
            final boolean result = ray.advance(block);
            if (result) {
                successful = true;
            }
        }
        return successful;
    }

    Location<World> getOrigin();

    Vector3d getDirection();

    double getRange();

    double getSpeed();

    double getSpeedFactor();

    boolean isDiagonalChecked();

    boolean advance(BiPredicate<Raycast, Location<World>> block);

    interface Builder extends ResettableBuilder<Raycast, Builder> {

        Builder origin(Location<World> origin);

        Builder direction(Vector3d direction);

        Builder range(double range);

        Builder speed(double speed);

        Builder checkDiagonals(boolean checkDiagonals);

        Raycast build();
    }
}