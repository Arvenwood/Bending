package pw.dotdash.bending.api.ability;

import java.util.Collection;
import java.util.Optional;

public interface AbilityConfigLoader {

    Collection<AbilityType> getAbilityTypes();

    Optional<Ability> load(AbilityType type);

    boolean contains(AbilityType type);
}