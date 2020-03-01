package pw.dotdash.bending.api.ability;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;

import java.util.Optional;

/**
 * Provides context for an ability during execution.
 */
public interface AbilityContext {

    /**
     * Creates a new {@link Builder} to build a {@link AbilityContext}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Creates an empty context.
     *
     * @return The empty context
     */
    static AbilityContext of() {
        return Sponge.getRegistry().createBuilder(Builder.class).build();
    }

    /**
     * Gets the value corresponding to the given key from the context.
     *
     * @param key The key
     * @param <E> The type of the value stored with the key
     * @return The context value, if available
     */
    <E> Optional<E> get(AbilityContextKey<E> key);

    /**
     * Gets the value corresponding to the given key from the context.
     *
     * <p>If the key is not available, {@link java.util.NoSuchElementException}
     * will be thrown.</p>
     *
     * @param key The key
     * @param <E> The type of the value stored with the key
     * @return The context value, if available
     */
    <E> E require(AbilityContextKey<E> key);

    /**
     * Sets the value corresponding to the given key in the context.
     *
     * @param key The key to set
     * @param value The value to set
     * @param <E> The type of value to store
     */
    <E> void set(AbilityContextKey<E> key, E value);

    /**
     * Removes the value corresponding to the given key from the context.
     *
     * @param key The key to remove
     * @param <E> The type of value to remove
     * @return The removed context value, if available
     */
    <E> Optional<E> remove(AbilityContextKey<E> key);

    /**
     * Gets whether the provided {@link AbilityContextKey} is included in the
     * context.
     *
     * @param key The key to check
     * @return True if the key is used and there is an entry for it
     */
    boolean contains(AbilityContextKey<?> key);

    /**
     * Represents a builder to create a {@link AbilityContext}.
     */
    interface Builder extends ResettableBuilder<AbilityContext, Builder> {

        /**
         * Adds the given context key value pair to the context.
         *
         * @param key The key to add
         * @param value The value to add
         * @param <E> The type of value to add
         * @return This builder, for chaining
         */
        <E> Builder add(AbilityContextKey<E> key, E value);

        /**
         * Builds a new {@link AbilityContext}.
         *
         * @return The generated ability context
         */
        AbilityContext build();
    }
}