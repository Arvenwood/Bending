package pw.dotdash.bending.api.protection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Represents a service used for checking build protection.
 */
public interface BuildProtectionService {

    /**
     * Gets the singleton instance of the {@link BuildProtectionService}.
     *
     * @return The singleton service instance
     */
    static BuildProtectionService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(BuildProtectionService.class);
    }

    /**
     * Checks if the target is protected from being affected by the source.
     *
     * @param source The player source
     * @param target The block target
     * @return True if the block is protected
     */
    boolean isProtected(Player source, Location<World> target);
}