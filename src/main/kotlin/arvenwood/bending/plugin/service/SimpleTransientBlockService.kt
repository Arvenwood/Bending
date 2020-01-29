package arvenwood.bending.plugin.service

import arvenwood.bending.api.service.TransientBlockService
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class SimpleTransientBlockService : TransientBlockService {

    private val locationMap = HashMap<Location<World>, SimpleSnapshot>()

    private val revertQueue: PriorityQueue<SimpleSnapshot> = PriorityQueue(Comparator.comparing(SimpleSnapshot::revertAtMilli))

    var task: Task? = null
        private set

    fun start(plugin: Any) {
        this.task = Task.builder()
            .name("TransientBlockService-Revert")
            .intervalTicks(1L)
            .execute { ->
                val currentTime: Long = System.currentTimeMillis()

                while (this.revertQueue.isNotEmpty()) {
                    val snapshot: SimpleSnapshot = this.revertQueue.peek()

                    if (snapshot.revertAtMilli <= currentTime) {
                        this.revertQueue.poll()
                        snapshot.revert()
                    } else {
                        break
                    }
                }
            }
            .submit(plugin)
    }

    override fun get(location: Location<World>): SimpleSnapshot? =
        this.locationMap[location]

    override fun contains(location: Location<World>): Boolean =
        location in this.locationMap

    override fun createSnapshotBuilder(): TransientBlockService.Snapshot.Builder =
        SimpleSnapshotBuilder()

    override fun revert(location: Location<World>): Boolean =
        this.locationMap.remove(location)?.revert() != null

    override fun revertAll() {
        for (location: Location<World> in this.locationMap.keys) {
            revert(location)
        }
    }

    inner class SimpleSnapshot(
        override val location: Location<World>,
        override val oldState: BlockState,
        override val newState: BlockState,
        override val revertAtMilli: Long,
        private val onRevert: (() -> Unit)?
    ) : TransientBlockService.Snapshot {

        override var isReverted: Boolean = false

        override val revertAt: Instant get() = Instant.ofEpochMilli(this.revertAtMilli)

        override fun revert() {
            if (this.isReverted) return

            this.location.block = this.oldState
            this.onRevert?.invoke()
            this.isReverted = true
        }
    }

    inner class SimpleSnapshotBuilder : TransientBlockService.Snapshot.Builder {

        private var location: Location<World>? = null
        private var newState: BlockState? = null
        private var delay: Long? = null
        private var delayUnit: TimeUnit? = null
        private var onRevert: (() -> Unit)? = null

        override fun location(location: Location<World>): TransientBlockService.Snapshot.Builder {
            this.location = location
            return this
        }

        override fun newState(newState: BlockState): TransientBlockService.Snapshot.Builder {
            this.newState = newState
            return this
        }

        override fun delay(duration: Long, unit: TimeUnit): TransientBlockService.Snapshot.Builder {
            this.delay = duration
            this.delayUnit = unit
            return this
        }

        override fun onRevert(onRevert: () -> Unit): TransientBlockService.Snapshot.Builder {
            this.onRevert = onRevert
            return this
        }

        override fun submit(): TransientBlockService.Snapshot {
            val location: Location<World> = requireNotNull(this.location)

            val snapshot = SimpleSnapshot(
                location,
                location.block,
                requireNotNull(this.newState),
                System.currentTimeMillis() + requireNotNull(this.delayUnit).toMillis(requireNotNull(this.delay)),
                onRevert
            )
            val old: SimpleSnapshot? = this@SimpleTransientBlockService.locationMap.put(location, snapshot)
            old?.revert()

            location.block = snapshot.newState
            return snapshot
        }

        override fun from(value: TransientBlockService.Snapshot): TransientBlockService.Snapshot.Builder =
            throw UnsupportedOperationException()

        override fun reset(): TransientBlockService.Snapshot.Builder {
            this.location = null
            this.newState = null
            this.delay = null
            this.delayUnit = null
            this.onRevert = null
            return this
        }
    }
}