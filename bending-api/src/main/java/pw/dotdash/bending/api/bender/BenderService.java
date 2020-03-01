package pw.dotdash.bending.api.bender;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a service for managing benders.
 */
public interface BenderService {

    /**
     * Gets the singleton instance of the {@link BenderService}.
     *
     * @return The singleton service instance
     */
    static BenderService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(BenderService.class);
    }

    /**
     * Gets a {@link Bender} player by their UUID.
     *
     * @param uniqueId The UUID to get the bender player from
     * @return The bender, if available
     */
    Optional<Bender> getBender(UUID uniqueId);

    /**
     * Gets a {@link Bender} player from the given {@link Player}.
     *
     * <p>If a bender does not already exist with the specified {@link UUID},
     * it will be created.</p>
     *
     * @param player The player to get a bender for
     * @return The bender
     */
    Bender getOrCreateBender(Player player);
}