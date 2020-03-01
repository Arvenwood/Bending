package pw.dotdash.bending.api.ability;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * A specific configuration of abilities.
 */
public interface AbilityConfig extends CatalogType {

    /**
     * Creates a new {@link Builder} to build a {@link AbilityConfig}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Attempts to retrieve the {@link AbilityConfig} based on
     * the string id given.
     *
     * @param id The case insensitive string id
     * @return The found AbilityConfig, if available
     */
    static Optional<AbilityConfig> get(String id) {
        return Sponge.getRegistry().getType(AbilityConfig.class, id);
    }

    /**
     * Gets a collection of all available {@link AbilityConfig}s.
     *
     * @return A collection of all known AbilityConfigs
     */
    static Collection<AbilityConfig> getAll() {
        return Sponge.getRegistry().getAllOf(AbilityConfig.class);
    }

    AbilityConfigLoader getLoader();

    /**
     * Loads the ability configuration for the given {@link AbilityType}.
     *
     * @param type The ability type to load
     * @return The ability configuration, if available
     */
    default Optional<Ability> load(AbilityType type) {
        return getLoader().load(type);
    }

    /**
     * Represents a builder to create a {@link AbilityConfig}.
     */
    interface Builder extends ResettableBuilder<AbilityConfig, Builder> {

        /**
         * Sets the string id to be used for {@link CatalogType#getId()}.
         *
         * @param id The string id
         * @return This builder, for chaining
         */
        Builder id(String id);

        /**
         * Sets the human readable name to be used for {@link CatalogType#getName()}.
         *
         * @param name The human readable name
         * @return This builder, for chaining
         */
        Builder name(String name);

        /**
         * Sets the loader used to create a specific configuration of the given
         * ability type.
         *
         * @param loader The ability type configuration loader
         * @return This builder, for chaining
         */
        Builder loader(AbilityConfigLoader loader);

        /**
         * Builds a new {@link AbilityExecutionType}, provided that the
         * {@link #id(String)}, {@link #name(String)}, and
         * {@link #loader(AbilityConfigLoader)} are set, and an{@link AbilityType} is
         * registered.
         *
         * @return The generated AbilityConfig
         */
        AbilityConfig build();
    }
}