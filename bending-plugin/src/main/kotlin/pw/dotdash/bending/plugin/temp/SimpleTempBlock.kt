package pw.dotdash.bending.plugin.temp

import org.spongepowered.api.block.BlockState
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.temp.TempBlock
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class SimpleTempBlock(
    private val location: Location<World>,
    private val oldState: BlockState,
    private val newState: BlockState,
    private val revertAtMilli: Long,
    private val onRevert: Consumer<TempBlock>?
) : TempBlock {

    private var isReverted: Boolean = false

    override fun getLocation(): Location<World> = this.location

    override fun getOldState(): BlockState = this.oldState

    override fun getNewState(): BlockState = this.newState

    override fun getRevertTime(): Instant = Instant.ofEpochMilli(this.revertAtMilli)

    override fun getRevertTimeMilli(): Long = this.revertAtMilli

    override fun isReverted(): Boolean = this.isReverted

    override fun revert(): Boolean {
        if (this.isReverted) return false

        this.location.block = this.oldState
        this.onRevert?.accept(this)
        this.isReverted = true
        return true
    }

    class Builder : TempBlock.Builder {

        private var location: Location<World>? = null
        private var newState: BlockState? = null
        private var revertDelayMilli: Long = 0
        private var onRevert: Consumer<TempBlock>? = null

        override fun location(location: Location<World>): TempBlock.Builder {
            this.location = location
            return this
        }

        override fun newState(newState: BlockState): TempBlock.Builder {
            this.newState = newState
            return this
        }

        override fun revertDelay(duration: Long, unit: TimeUnit): TempBlock.Builder {
            this.revertDelayMilli = unit.toMillis(duration)
            return this
        }

        override fun onRevert(onRevert: Consumer<TempBlock>): TempBlock.Builder {
            this.onRevert = onRevert
            return this
        }

        override fun from(value: TempBlock): TempBlock.Builder {
            throw UnsupportedOperationException()
        }

        override fun reset(): TempBlock.Builder {
            this.location = null
            this.newState = null
            this.revertDelayMilli = 0
            this.onRevert = null
            return this
        }

        override fun build(): TempBlock {
            val location: Location<World> = checkNotNull(this.location)

            val revertAtMilli: Long =
                if (this.revertDelayMilli == 0L) Long.MAX_VALUE
                else System.currentTimeMillis() + this.revertDelayMilli

            return SimpleTempBlock(
                location = location,
                oldState = location.block,
                newState = checkNotNull(this.newState),
                revertAtMilli = revertAtMilli,
                onRevert = this.onRevert
            )
        }
    }
}