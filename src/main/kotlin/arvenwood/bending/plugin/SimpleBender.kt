package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.event.ExecuteAbilityEvent
import arvenwood.bending.api.event.SetCooldownEvent
import arvenwood.bending.api.util.selectedSlotIndex
import arvenwood.bending.plugin.ability.SimpleAbilityContext
import arvenwood.bending.plugin.ability.SimpleAbilityJob
import arvenwood.bending.plugin.util.enumMap
import arvenwood.bending.plugin.util.table
import com.google.common.collect.Table
import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*
import kotlin.coroutines.*

class SimpleBender(private val uniqueId: UUID) : Bender {

    private val runningMap = IdentityHashMap<Job, SimpleAbilityJob>()
    private val cooldownMap = IdentityHashMap<AbilityType<*>, Long>()
    private val waitingMap: Table<AbilityType<*>, AbilityExecutionType, Continuation<Unit>> =
        table(IdentityHashMap()) { enumMap<AbilityExecutionType, Continuation<Unit>>() }

    private val equipped: Array<Ability<*>?> = arrayOfNulls(size = 9)

    private val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelAbility)
    private val waitingExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelWaiting)

    override val player: Player get() = Sponge.getServer().getPlayer(this.uniqueId).get()

    override val equippedAbilities: List<Ability<*>?> get() = this.equipped.toCollection(ArrayList(9))

    override val runningAbilities: Collection<AbilityJob> get() = runningMap.values

    override var selectedAbility: Ability<*>?
        get() = this.getEquipped(this.player.selectedSlotIndex)
        set(value) {
            this.setEquipped(this.player.selectedSlotIndex, value)
        }

    override fun getEquipped(hotbarIndex: Int): Ability<*>? {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        return this.equipped[hotbarIndex]
    }

    override fun setEquipped(hotbarIndex: Int, ability: Ability<*>?) {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        this.equipped[hotbarIndex] = ability
    }

    override fun clearEquipped() {
        for (i in 0..8) {
            this.equipped[i] = null
        }
    }

    override fun execute(ability: Ability<*>, executionType: AbilityExecutionType) {
        val player: Player = this.player

        if (executionType !in ability.type.executionTypes) {
            // This ability is executed differently.
            return
        }

        val cont: Continuation<Unit>? = this.waitingMap.remove(ability.type, executionType)
        if (cont != null) {
            // Found a waiting ability.
            cont.resume(Unit)
            return
        }

        if (this.hasCooldown(ability.type)) {
            // This ability is on cooldown.
            return
        }

        val context = SimpleAbilityContext()

        // Set how the ability was initiated.
        context[StandardContext.executionType] = executionType

        if (executionType == AbilityExecutionType.FALL) {
            context[StandardContext.fallDistance] = player.getOrElse(Keys.FALL_DISTANCE, 0F)
        }

        // Pre-load some values into the context.
        context[StandardContext.player] = player
        context[StandardContext.bender] = this
        ability.prepare(player, context)

        if (!ability.validate(context)) {
            // Should we try to run the ability?
            return
        }

        val event = ExecuteAbilityEvent(player, this, ability.type, executionType, Sponge.getCauseStackManager().currentCause)
        Sponge.getEventManager().post(event)
        if (event.isCancelled) {
            return
        }

        if (ability.cooldown > 0) {
            // Set the cooldown. Don't spam your abilities!
            this.setCooldown(ability.type, ability.cooldown)
        }

        ability.preempt(context, executionType)

        lateinit var execution: SimpleAbilityJob
        lateinit var job: Job

        val coroutine: CoroutineContext =
            Bending.SYNC + exceptionHandler + ability + context + executionType

        job = GlobalScope.launch(coroutine) {
            ability.execute(context, executionType)
            ability.cleanup(context)
            this@SimpleBender.runningMap.remove(job)
        }
        execution = SimpleAbilityJob(job)

        this.runningMap[job] = execution
    }

    private fun cancelAbility(context: CoroutineContext, throwable: Throwable) {
        val ability: Ability<*> = context[Ability] ?: return
        val abilityContext: AbilityContext = context[AbilityContext] ?: return

        ability.cleanup(abilityContext)

        val job: Job = context[Job]!!
        this.runningMap.remove(job)

        if (throwable !is CancellationException) {
            Bending.LOGGER.error("Ability failed to execute", throwable)
        }
    }

    override suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Unit =
        suspendCoroutine {
            val old: Continuation<Unit>? = this.waitingMap.put(type, executionType, it)
            old?.resumeWithException(CancellationException("Waiting ability cancelled"))
        }

    override fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Job =
        GlobalScope.launch(Bending.SYNC + waitingExceptionHandler + type + executionType) {
            awaitExecution(type, executionType)
        }

    private fun cancelWaiting(context: CoroutineContext, throwable: Throwable) {
        val ability: Ability<*> = context[Ability] ?: return
        val executionType: AbilityExecutionType = context[AbilityExecutionType] ?: return
        this.waitingMap.remove(ability, executionType)?.resumeWithException(CancellationException("Waiting ability cancelled"))

        if (throwable !is CancellationException) {
            Bending.LOGGER.error("Ability failed to execute", throwable)
        }
    }

    override fun cancel(type: AbilityType<*>): Boolean {
        var found = false

        val iter = this.runningMap.iterator()
        while (iter.hasNext()) {
            val (_, execution) = iter.next()

            if (type === execution.type) {
                iter.remove()
                execution.cancel()
                found = true
            }
        }

        return found
    }

    override fun hasCooldown(type: AbilityType<*>): Boolean {
        val cooldown: Long = this.cooldownMap[type] ?: return false
        val current: Long = System.currentTimeMillis()
        if (cooldown <= current) {
            this.cooldownMap.remove(type)
            return false
        }
        return true
    }

    override fun setCooldown(type: AbilityType<*>, duration: Long) {
        val next: Long = System.currentTimeMillis() + duration

        val event = SetCooldownEvent(this.player, type, next, Sponge.getCauseStackManager().currentCause)
        Sponge.getEventManager().post(event)
        if (event.isCancelled) {
            return
        }

        this.cooldownMap[type] = next
    }

    override fun removeCooldown(type: AbilityType<*>): Long? {
        val event = SetCooldownEvent(this.player, type, null, Sponge.getCauseStackManager().currentCause)
        Sponge.getEventManager().post(event)
        if (event.isCancelled) {
            return null
        }

        return this.cooldownMap.remove(type)
    }
}