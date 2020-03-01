package pw.dotdash.bending.plugin.ability

import org.spongepowered.api.Sponge
import org.spongepowered.api.scheduler.SpongeExecutorService
import pw.dotdash.bending.api.ability.AbilityService

class SimpleAbilityService(private val plugin: Any) : AbilityService {

    private val syncExecutor: SpongeExecutorService =
        Sponge.getScheduler().createSyncExecutor(this.plugin)

    override fun getAbilityDelayMilli(): Long = 50

    override fun getSyncExecutor(): SpongeExecutorService = this.syncExecutor
}