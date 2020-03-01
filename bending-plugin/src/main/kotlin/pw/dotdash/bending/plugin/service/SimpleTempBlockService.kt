package pw.dotdash.bending.plugin.service

import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import pw.dotdash.bending.api.temp.TempBlock
import pw.dotdash.bending.api.temp.TempBlockService
import java.util.*
import kotlin.collections.HashMap

class SimpleTempBlockService : TempBlockService {

    private val tempBlockMap = HashMap<Location<World>, TempBlock>()

    private val revertQueue: PriorityQueue<TempBlock> = PriorityQueue(Comparator.comparing(TempBlock::getRevertTimeMilli))

    private var task: Task? = null

    fun start(plugin: Any) {
        this.task = Task.builder()
            .name("TransientBlockService-Revert")
            .intervalTicks(1L)
            .execute(fun() {
                val currentTime: Long = System.currentTimeMillis()

                while (this.revertQueue.isNotEmpty()) {
                    val block: TempBlock = this.revertQueue.peek()

                    if (block.revertTimeMilli <= currentTime) {
                        this.revertQueue.poll()
                        block.revert()
                    } else {
                        break
                    }
                }
            })
            .submit(plugin)
    }

    fun stop() {
        this.task?.cancel()

        while (this.revertQueue.isNotEmpty()) {
            val block: TempBlock = this.revertQueue.poll()
            block.revert()
        }
    }

    override fun get(location: Location<World>): Optional<TempBlock> =
        Optional.ofNullable(this.tempBlockMap[location])

    override fun contains(location: Location<World>): Boolean =
        location in this.tempBlockMap

    override fun register(block: TempBlock): Boolean {
        if (block.location in this.tempBlockMap) return false

        block.location.block = block.newState
        this.tempBlockMap[block.location] = block
        return true
    }

    override fun unregister(block: TempBlock): Boolean {
        return this.tempBlockMap.remove(block.location)?.revert() != null
    }

    override fun revertAll() {
        for (block: TempBlock in this.tempBlockMap.values) {
            block.revert()
        }
        this.tempBlockMap.clear()
    }
}