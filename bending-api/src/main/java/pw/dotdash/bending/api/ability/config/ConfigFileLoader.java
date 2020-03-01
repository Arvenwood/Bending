package pw.dotdash.bending.api.ability.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import pw.dotdash.bending.api.ability.Ability;
import pw.dotdash.bending.api.ability.AbilityType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConfigFileLoader implements Function<AbilityType, Optional<Ability>> {

    private final Path file;

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    @Nullable
    private ConfigurationNode node;

    public ConfigFileLoader(Path file) {
        this.file = checkNotNull(file, "file");
        this.loader = HoconConfigurationLoader.builder().setPath(file).build();
    }

    @Override
    public Optional<Ability> apply(AbilityType abilityType) {
        checkNotNull(abilityType, "abilityType");

        if (this.node == null) {
            try {
                this.node = this.loader.load();
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        final ConfigurationNode node = this.node.getNode(abilityType.getId());
        if (node.isVirtual()) {
            return Optional.empty();
        }
        return abilityType.load(node);
    }
}