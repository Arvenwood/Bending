package pw.dotdash.bending.api.ability;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

/**
 * An enumeration of known {@link AbilityExecutionType}s used throughout the plugin and extensions.
 */
public final class AbilityExecutionTypes {

    /**
     * The ability is activated by a quick successive combination of other abilities.
     */
    public static final AbilityExecutionType COMBO = DummyObjectProvider.createFor(AbilityExecutionType.class, "COMBO");

    /**
     * The ability is activated by falling from a great height.
     *
     * <p>Use {@link AbilityContextKeys#FALL_DISTANCE} to check the distance fallen.</p>
     */
    public static final AbilityExecutionType FALL = DummyObjectProvider.createFor(AbilityExecutionType.class, "FALL");

    /**
     * The ability is activated by jumping.
     */
    public static final AbilityExecutionType JUMP = DummyObjectProvider.createFor(AbilityExecutionType.class, "JUMP");

    /**
     * The ability is activated by punching.
     */
    public static final AbilityExecutionType LEFT_CLICK = DummyObjectProvider.createFor(AbilityExecutionType.class, "LEFT_CLICK");

    /**
     * The ability is activated upon equipping.
     */
    public static final AbilityExecutionType PASSIVE = DummyObjectProvider.createFor(AbilityExecutionType.class, "PASSIVE");

    /**
     * The ability is activated by sneaking.
     */
    public static final AbilityExecutionType SNEAK = DummyObjectProvider.createFor(AbilityExecutionType.class, "SNEAK");

    /**
     * The ability is activated by stopping sprint.
     */
    public static final AbilityExecutionType SPRINT_OFF = DummyObjectProvider.createFor(AbilityExecutionType.class, "SPRINT_OFF");

    /**
     * The ability is activated by starting to sprint.
     */
    public static final AbilityExecutionType SPRINT_ON = DummyObjectProvider.createFor(AbilityExecutionType.class, "SPRINT_ON");

    private AbilityExecutionTypes() {
        throw new AssertionError("Don't instantiate me!");
    }
}