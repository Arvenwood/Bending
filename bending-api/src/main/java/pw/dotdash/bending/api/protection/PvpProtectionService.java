package pw.dotdash.bending.api.protection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Represents a service used for checking pvp protection.
 */
public interface PvpProtectionService {

    /**
     * Gets the singleton instance of the {@link PvpProtectionService}.
     *
     * @return The singleton service instance
     */
    static PvpProtectionService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(PvpProtectionService.class);
    }

    /**
     * Checks if the target is protected from being affected by the source.
     *
     * @param source The player source
     * @param target The entity target
     * @return True if the entity is protected
     */
    boolean isProtected(Player source, Entity target);
}