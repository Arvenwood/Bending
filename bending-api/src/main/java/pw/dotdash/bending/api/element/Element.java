package pw.dotdash.bending.api.element;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.Collection;
import java.util.Optional;

/**
 * A classical bending element, or something else like chiblocking.
 *
 * @see Elements
 */
@CatalogedBy(Elements.class)
public interface Element extends CatalogType {

    /**
     * Creates a new {@link Builder} to build an {@link Element}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Attempts to retrieve the {@link Element} based on the string id given.
     *
     * @param id The case insensitive string id
     * @return The element, if available
     */
    static Optional<Element> get(String id) {
        return Sponge.getRegistry().getType(Element.class, id);
    }

    /**
     * Gets a collection of all available {@link Element}s.
     *
     * @return A collection of all known elements
     */
    static Collection<Element> getAll() {
        return Sponge.getRegistry().getAllOf(Element.class);
    }

    TextColor getColor();

    ParticleType getPrimaryParticleType();

    /**
     * Represents a builder to create an {@link Element}.
     */
    interface Builder extends ResettableBuilder<Element, Builder> {

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

        Builder color(TextColor color);

        Builder primaryParticleType(ParticleType type);

        /**
         * Builds a new {@link Element}, provided that the {@link #id(String)},
         * {@link #name(String)}, and {@link #color(TextColor)} are set.
         *
         * @return The generated element
         */
        Element build();
    }
}