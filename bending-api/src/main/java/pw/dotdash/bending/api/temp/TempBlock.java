package pw.dotdash.bending.api.temp;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A block that exists for a short time before being reverted back to its
 * original state.
 *
 * @see TempBlockService
 */
public interface TempBlock {

    /**
     * Creates a new {@link Builder} to build a {@link TempBlock}.
     *
     * @return The builder
     */
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    /**
     * Gets the location where the block is changed.
     *
     * @return The location
     */
    Location<World> getLocation();

    /**
     * Gets the block state that the block is reverted back to.
     *
     * @return The old block state
     */
    BlockState getOldState();

    /**
     * Gets the block state that the block is temporarily set to.
     *
     * @return The new block state
     */
    BlockState getNewState();

    /**
     * Gets the {@link Instant} of when the block will be reverted.
     *
     * @return The reversion epoch time
     */
    Instant getRevertTime();

    /**
     * Gets the epoch time of when the block will be reverted.
     *
     * @return The reversion epoch time
     */
    long getRevertTimeMilli();

    /**
     * Checks if the block has been reverted to its previous state.
     *
     * @return True if the block has been reverted
     */
    boolean isReverted();

    /**
     * Reverts the block to its previous state.
     *
     * @return True if the operation was successful
     */
    boolean revert();

    /**
     * Represents a builder to create a {@link TempBlock}.
     */
    interface Builder extends ResettableBuilder<TempBlock, Builder> {

        /**
         * Sets the location used for saving and reverting.
         *
         * @param location The location
         * @return This builder, for chaining
         */
        Builder location(Location<World> location);

        /**
         * Sets the block state that the location is temporarily set to.
         *
         * @param newState The new block state
         * @return This builder, for chaining
         */
        Builder newState(BlockState newState);

        /**
         * Sets the block type that the location is temporarily set to.
         *
         * @param newType The new block type
         * @return This builder, for chaining
         * @see #newState(BlockState)
         */
        default Builder newState(BlockType newType) {
            return newState(BlockState.builder().blockType(newType).build());
        }

        /**
         * Sets the delay of how long until the block is reverted.
         *
         * @param duration The duration
         * @param unit The unit of duration
         * @return This builder, for chaining
         */
        Builder revertDelay(long duration, TimeUnit unit);

        /**
         * Sets the callback that's ran when the block is reverted.
         *
         * @param onRevert The callback
         * @return This builder, for chaining
         */
        Builder onRevert(Consumer<TempBlock> onRevert);

        /**
         * Builds a new {@link TempBlock}, provided that the
         * {@link #location(Location)} and {@link #newState(BlockState)} are
         * set.
         *
         * @return The generated temporary block
         * @see TempBlockService#register(TempBlock)
         */
        TempBlock build();

        @Override
        Builder from(TempBlock value) throws UnsupportedOperationException;
    }
}