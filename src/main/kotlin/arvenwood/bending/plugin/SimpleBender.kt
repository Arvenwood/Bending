package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.plugin.ability.SimpleAbilityContext
import arvenwood.bending.api.service.CooldownService
import arvenwood.bending.api.util.StackableBoolean
import arvenwood.bending.api.util.selectedSlotIndex
import arvenwood.bending.plugin.ability.SimpleAbilityExecution
import com.google.common.collect.Table
import com.google.common.collect.Tables
import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import java.util.*
import kotlin.coroutines.*

class SimpleBender(private val uniqueId: UUID) : Bender {

    private val player: Player get() = Sponge.getServer().getPlayer(this.uniqueId).get()

    private val equipped: Array<Ability<*>?> = arrayOfNulls(size = 9)

    private val runningMap = IdentityHashMap<Job, SimpleAbilityExecution>()

    private val awaitingMap: Table<AbilityType<*>, AbilityExecutionType, Continuation<Unit>> =
        Tables.newCustomTable<AbilityType<*>, AbilityExecutionType, Continuation<Unit>>(IdentityHashMap()) { HashMap() }

    override var flight = StackableBoolean(0)

    override var selectedAbility: Ability<*>?
        get() = this[this.player.selectedSlotIndex]
        set(value) {
            this[this.player.selectedSlotIndex] = value
        }

    override val equippedAbilities: Map<Int, Ability<*>>
        get() {
            val result = HashMap<Int, Ability<*>>()
            for (i in this.equipped.indices) {
                val ability: Ability<*> = this.equipped[i] ?: continue
                result[i] = ability
            }
            return result
        }

    override fun get(hotbarIndex: Int): Ability<*>? {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        return this.equipped[hotbarIndex]
    }

    override fun set(hotbarIndex: Int, ability: Ability<*>?) {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        val old: Ability<*>? = this.equipped[hotbarIndex]
        if (old != null) {

        }
        this.equipped[hotbarIndex] = ability
    }

    override fun clearEquipped() {
        for (i in 0..8) {
            this.equipped[i] = null
        }
    }

    override val runningAbilities: Collection<AbilityExecution> get() = runningMap.values

    private val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelAbility)

    override fun execute(ability: Ability<*>, executionType: AbilityExecutionType) {
        if (executionType::class !in ability.type.executionTypes) {
            // This ability is executed differently.
            return
        }

        val cont: Continuation<Unit>? = this.awaitingMap.remove(ability.type, executionType)
        if (cont != null) {
            // Found a waiting ability.
            cont.resume(Unit)
            return
        }

        if (CooldownService.get().hasCooldown(this.player, ability.type)) {
            // This ability is on cooldown.
            return
        }

        val context = SimpleAbilityContext()

        // Set how the ability was initiated.
        context[StandardContext.executionType] = executionType

        // Pre-load some values into the context.
        context[StandardContext.player] = this.player
        ability.prepare(this.player, context)

        if (!ability.validate(context)) {
            // Should we try to run the ability?
            return
        }

        ability.preempt(context, executionType)

        if (ability.cooldown > 0) {
            // Set the cooldown. Don't spam your abilities!
            CooldownService.get()[this.player, ability.type] = ability.cooldown
        }

        lateinit var execution: SimpleAbilityExecution
        lateinit var job: Job

        val coroutine: CoroutineContext =
            Bending.SYNC + ability + context + executionType + exceptionHandler

        job = GlobalScope.launch(coroutine) {
            ability.execute(context, executionType)
            ability.cleanup(context)
            this@SimpleBender.runningMap.remove(job)
        }
        execution = SimpleAbilityExecution(job)

        this.runningMap[job] = execution
    }

    private fun cancelAbility(context: CoroutineContext, throwable: Throwable) {
        val ability: Ability<*> = context[Ability] ?: return
        val abilityContext: AbilityContext = context[AbilityContext] ?: return

        ability.cleanup(abilityContext)

        val job: Job = context[Job]!!
        this.runningMap.remove(job)
    }

    override suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Unit =
        suspendCoroutine {
            val old: Continuation<Unit>? = this.awaitingMap.put(type, executionType, it)
            old?.resumeWithException(CancellationException("Waiting ability cancelled"))
        }

    private val waitingExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelWaiting)

    override fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Job =
        GlobalScope.launch(Bending.SYNC + type + executionType + waitingExceptionHandler) {
            awaitExecution(type, executionType)
        }

    private fun cancelWaiting(context: CoroutineContext, throwable: Throwable) {
        val ability: Ability<*> = context[Ability] ?: return
        val executionType: AbilityExecutionType = context[AbilityExecutionType] ?: return
        this.awaitingMap.remove(ability, executionType)?.resumeWithException(CancellationException("Waiting ability cancelled"))
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
}