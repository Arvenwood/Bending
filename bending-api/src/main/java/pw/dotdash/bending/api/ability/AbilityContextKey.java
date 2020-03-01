package pw.dotdash.bending.api.ability;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.annotation.CatalogedBy;

/**
 * A key for values in the {@link AbilityContext}.
 *
 * @param <E> The type of value stored with this key
 */
@CatalogedBy(AbilityContextKeys.class)
public interface AbilityContextKey<E> extends CatalogType {

    /**
     * Creates a new {@link Builder} to build a {@link AbilityContextKey}.
     *
     * @param type The class the key will allow access to
     * @param <E> The type of value stored with this key
     * @return The builder
     */
    @SuppressWarnings("unchecked")
    static <E> Builder<E> builder(TypeToken<E> type) {
        return Sponge.getRegistry().createBuilder(Builder.class).type(type);
    }

    /**
     * Gets the allowed type for the value of this key.
     *
     * @return The allowed type
     */
    TypeToken<E> getAllowedType();

    /**
     * Represents a builder to create a {@link AbilityContextKey}.
     *
     * @param <E> The type of value stored with this key
     */
    interface Builder<E> extends ResettableBuilder<AbilityContextKey<E>, Builder<E>> {

        /**
         * Sets the allowed type of value for this key.
         *
         * @param type The allowed type
         * @return This builder, for chaining
         */
        Builder<E> type(TypeToken<E> type);

        /**
         * Sets the string id to be used for {@link CatalogType#getId()}.
         *
         * @param id The string id
         * @return This builder, for chaining
         */
        Builder<E> id(String id);

        /**
         * Sets the human readable name to be used for {@link CatalogType#getName()}.
         *
         * @param name The human readable name
         * @return This builder, for chaining
         */
        Builder<E> name(String name);

        /**
         * Builds a new {@link AbilityContextKey}, provided that the
         * {@link #type(TypeToken)}, {@link #id(String)}, and {@link #name(String)}
         * are set.
         *
         * @return The generated ability context key
         */
        AbilityContextKey<E> build();

        @Override
        Builder<E> from(AbilityContextKey<E> value) throws UnsupportedOperationException;

        @Override
        Builder<E> reset();
    }
}