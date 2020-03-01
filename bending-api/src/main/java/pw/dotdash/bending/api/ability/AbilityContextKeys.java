package pw.dotdash.bending.api.ability;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.dotdash.bending.api.bender.Bender;

import java.util.Collection;

/**
 * Standard keys for use within {@link AbilityContext}s.
 */
public final class AbilityContextKeys {

    public static final AbilityContextKey<Collection<Location<World>>> AFFECTED_LOCATIONS = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "AFFECTED_LOCATIONS");

    public static final AbilityContextKey<Collection<Entity>> AFFECTED_ENTITIES = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "AFFECTED_ENTITIES");

    public static final AbilityContextKey<Bender> BENDER = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "BENDER");

    public static final AbilityContextKey<Location<World>> CURRENT_LOCATION = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "CURRENT_LOCATION");

    public static final AbilityContextKey<Vector3d> DIRECTION = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "DIRECTION");

    public static final AbilityContextKey<AbilityExecutionType> EXECUTION_TYPE = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "EXECUTION_TYPE");

    public static final AbilityContextKey<Float> FALL_DISTANCE = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "FALL_DISTANCE");

    public static final AbilityContextKey<Location<World>> ORIGIN = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "ORIGIN");

    public static final AbilityContextKey<Player> PLAYER = DummyObjectProvider.createExtendedFor(AbilityContextKey.class, "PLAYER");

    private AbilityContextKeys() {
        throw new AssertionError("Don't instantiate me!");
    }
}