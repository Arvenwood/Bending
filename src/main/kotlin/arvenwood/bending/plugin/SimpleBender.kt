package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.plugin.ability.SimpleAbilityContext
import arvenwood.bending.api.service.CooldownService
import arvenwood.bending.api.util.selectedSlotIndex
import arvenwood.bending.plugin.ability.SimpleAbilityExecution
import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import java.util.*
import kotlin.coroutines.*

class SimpleBender(private val uniqueId: UUID) : Bender {

    private val player: Player get() = Sponge.getServer().getPlayer(this.uniqueId).get()

    private val equipped: Array<Ability<*>?> = arrayOfNulls(size = 9)

    private val running = IdentityHashMap<Job, SimpleAbilityExecution>()

    private val awaiting = IdentityHashMap<AbilityType<*>, EnumMap<AbilityExecutionType, Continuation<Unit>>>()

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

    override val runningAbilities: Collection<AbilityExecution> get() = running.values

    override fun execute(ability: Ability<*>, executionType: AbilityExecutionType) {
        if (executionType !in ability.type.executionTypes) {
            // This ability is executed differently.
            return
        }

        val cont: Continuation<Unit>? = this.awaiting[ability.type]?.remove(executionType)
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

        if (!ability.shouldExecute(context)) {
            // Should we try to run the ability?
            return
        }

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
            this@SimpleBender.running.remove(job)
        }
        execution = SimpleAbilityExecution(job)

        this.running[job] = execution
    }

    private val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelAbility)

    private fun cancelAbility(context: CoroutineContext, throwable: Throwable) {
        val ability: Ability<*> = context[Ability] ?: return
        val abilityContext: AbilityContext = context[AbilityContext] ?: return

        ability.cleanup(abilityContext)

        val job: Job = context[Job]!!
        this.running.remove(job)
    }

    override suspend fun awaitExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Unit =
        suspendCoroutine {
            val byExecution: MutableMap<AbilityExecutionType, Continuation<Unit>> =
                this.awaiting.computeIfAbsent(type) { EnumMap(AbilityExecutionType::class.java) }

            val old: Continuation<Unit>? = byExecution.put(executionType, it)

            old?.resumeWithException(CancellationException())
        }

    override fun deferExecution(type: AbilityType<*>, executionType: AbilityExecutionType): Deferred<Unit> =
        GlobalScope.async(Bending.SYNC) {
            awaitExecution(type, executionType)
        }
}