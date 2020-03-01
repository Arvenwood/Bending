package pw.dotdash.bending.api.temp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Represents a service for managing temporary blocks.
 *
 * @see TempBlock
 */
public interface TempBlockService {

    /**
     * Gets the singleton instance of the {@link TempBlockService}.
     *
     * @return The singleton service instance
     */
    static TempBlockService getInstance() {
        return Sponge.getServiceManager().provideUnchecked(TempBlockService.class);
    }

    /**
     * Attempts to retrieve the {@link TempBlock} based on the location given.
     *
     * @param location The block's location
     * @return The temporary block, if available
     */
    Optional<TempBlock> get(Location<World> location);

    /**
     * Checks if the service contains a {@link TempBlock} at the given location.
     *
     * @param location The location to check
     * @return True if the service contains the location
     */
    boolean contains(Location<World> location);

    /**
     * Registers the given {@link TempBlock} to the service.
     *
     * <p>Registering a temporary block to the service is required for
     * reversion to work.</p>
     *
     * @param block The temporary block to register
     * @return True if the operation was successful
     */
    boolean register(TempBlock block);

    /**
     * Unregisters the given {@link TempBlock} from the service.
     *
     * @param block The temporary block to unregister
     * @return True if the operation was successful
     */
    boolean unregister(TempBlock block);

    /**
     * Reverts and unregisters all blocks currently in the service.
     */
    void revertAll();
}