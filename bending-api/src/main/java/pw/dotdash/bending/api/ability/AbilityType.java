package pw.dotdash.bending.api.ability;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ResettableBuilder;
import pw.dotdash.bending.api.element.Element;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * A type of ability.
 */
public interface AbilityType extends CatalogType {

    /**
     * Creates a new {@link Builder} to build a {@link AbilityType}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Attempts to retrieve the {@link AbilityType} based on
     * the string id given.
     *
     * @param id The case insensitive string id
     * @return The found ability type, if available
     */
    static Optional<AbilityType> get(String id) {
        return Sponge.getRegistry().getType(AbilityType.class, id);
    }

    /**
     * Gets a collection of all available {@link AbilityType}s.
     *
     * @return A collection of all known ability types
     */
    static Collection<AbilityType> getAll() {
        return Sponge.getRegistry().getAllOf(AbilityType.class);
    }

    /**
     * Gets the {@link Element} this {@link AbilityType} is associated with.
     *
     * @return The associated element
     */
    Element getElement();

    /**
     * Gets all possible ways of executing the ability.
     *
     * @return All ability execution types
     */
    Collection<AbilityExecutionType> getExecutionTypes();

    /**
     * Gets instructions on how to execute the ability.
     *
     * @return The instructions, if available
     */
    Optional<Text> getInstructions();

    /**
     * Gets the description of what the ability does.
     *
     * @return The description, if available
     */
    Optional<Text> getDescription();

    Text show();

    /**
     * Loads an ability configuration by the given {@link ConfigurationNode}.
     *
     * @param node The node used for loading
     * @return The ability configuration, if available
     */
    Optional<Ability> load(ConfigurationNode node);

    /**
     * Represents a builder to create a {@link AbilityType}.
     */
    interface Builder extends ResettableBuilder<AbilityType, Builder> {

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
         * Sets the associated element.
         *
         * @param element The associated element
         * @return This builder, for chaining
         */
        Builder element(Element element);

        Builder executionTypes(Collection<AbilityExecutionType> executionTypes);

        Builder executionTypes(AbilityExecutionType... executionTypes);

        Builder instructions(Text instructions);

        Builder description(Text description);

        Builder configLoader(Function<ConfigurationNode, Optional<Ability>> configLoader);

        default Builder loader(Function<ConfigurationNode, Ability> configLoader) {
            return this.configLoader(node -> Optional.of(configLoader.apply(node)));
        }

        /**
         * Builds a new {@link AbilityType}, provided that the
         * {@link #id(String)}, {@link #name(String)},
         * {@link #element(Element)}, {@link #executionTypes(Collection)},
         * and {@link #configLoader(Function)} are set.
         *
         * @return The generated ability type
         */
        AbilityType build();
    }
}