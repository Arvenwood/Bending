package pw.dotdash.bending.api.ability;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.Collection;
import java.util.Optional;

/**
 * A method of executing an ability.
 */
@CatalogedBy(AbilityExecutionTypes.class)
public interface AbilityExecutionType extends CatalogType {

    /**
     * Creates a new {@link Builder} to build an {@link AbilityExecutionType}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Attempts to retrieve the {@link AbilityExecutionType} based on
     * the string id given.
     *
     * @param id The case insensitive string id
     * @return The ability execution type, if available
     */
    static Optional<AbilityExecutionType> get(String id) {
        return Sponge.getRegistry().getType(AbilityExecutionType.class, id);
    }

    /**
     * Gets a collection of all available {@link AbilityExecutionType}s.
     *
     * @return A collection of all known ability execution types
     */
    static Collection<AbilityExecutionType> getAll() {
        return Sponge.getRegistry().getAllOf(AbilityExecutionType.class);
    }

    /**
     * Represents a builder to create an {@link AbilityExecutionType}.
     */
    interface Builder extends ResettableBuilder<AbilityExecutionType, Builder> {

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
         * Builds a new {@link AbilityExecutionType}, provided that the
         * {@link #id(String)} and {@link #name(String)} are set.
         *
         * @return The generated ability execution type
         */
        AbilityExecutionType build();
    }
}