package pw.dotdash.bending.api.protection;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.Tristate;

import java.util.function.BiFunction;

/**
 * A pvp protection checker.
 */
public interface PvpProtection extends CatalogType {

    /**
     * Creates a new {@link Builder} to build an {@link PvpProtection}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Checks if the target is protected from being affected by the source.
     *
     * @param source The player source
     * @param target The entity target
     * @return The tristate result of the check
     */
    Tristate isProtected(Player source, Entity target);

    /**
     * Represents a builder to create a {@link PvpProtection}.
     */
    interface Builder extends ResettableBuilder<PvpProtection, Builder> {

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
         * Sets the function used for checking protection.
         *
         * @param function The function
         * @return This builder, for chaining
         */
        Builder function(BiFunction<Player, Entity, Tristate> function);

        /**
         * Builds a new {@link PvpProtection}, provided that the
         * {@link #id(String)}, {@link #name(String)}, and
         * {@link #function(BiFunction)} are set.
         *
         * @return The generated pvp protection
         */
        PvpProtection build();
    }
}