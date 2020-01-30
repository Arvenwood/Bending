package arvenwood.bending.api.service

import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.util.ResettableBuilder
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant
import java.util.concurrent.TimeUnit

interface TransientBlockService {

    companion object {
        @JvmStatic
        fun get(): TransientBlockService =
            Sponge.getServiceManager().provideUnchecked(TransientBlockService::class.java)
    }

    operator fun get(location: Location<World>): Snapshot?

    fun createSnapshotBuilder(): Snapshot.Builder

    operator fun contains(location: Location<World>): Boolean

    fun revert(location: Location<World>): Boolean

    fun revertAll()

    interface Snapshot {

        val location: Location<World>

        val oldState: BlockState

        val newState: BlockState

        val revertAt: Instant

        val revertAtMilli: Long

        val isReverted: Boolean

        fun revert()

        interface Builder : ResettableBuilder<Snapshot, Builder> {

            fun location(location: Location<World>): Builder

            fun newState(newState: BlockState): Builder

            fun delay(duration: Long, unit: TimeUnit): Builder

            fun onRevert(onRevert: () -> Unit): Builder

            fun submit(): Snapshot
        }
    }
}

fun TransientBlockService.Snapshot.Builder.newState(type: BlockType): TransientBlockService.Snapshot.Builder =
    this.newState(BlockState.builder().blockType(type).build())