package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.plugin.ability.SimpleAbilityContext
import arvenwood.bending.api.util.StackableBoolean
import arvenwood.bending.api.util.selectedSlotIndex
import arvenwood.bending.plugin.ability.SimpleAbilityJob
import com.google.common.collect.Table
import com.google.common.collect.Tables
import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*
import kotlin.coroutines.*

class SimpleBender(private val uniqueId: UUID) : Bender {

    private val player: Player get() = Sponge.getServer().getPlayer(this.uniqueId).get()

    private val equipped: Array<Ability<*>?> = arrayOfNulls(size = 9)

    private val runningMap = IdentityHashMap<Job, SimpleAbilityJob>()

    private val awaitingMap: Table<AbilityType<*>, AbilityExecutionType, Continuation<Unit>> =
        Tables.newCustomTable<AbilityType<*>, AbilityExecutionType, Continuation<Unit>>(IdentityHashMap()) {
            EnumMap(AbilityExecutionType::class.java)
        }

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

    override val runningAbilities: Collection<AbilityJob> get() = runningMap.values

    private val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler(this::cancelAbility)

    override fun execute(ability: Ability<*>, executionType: AbilityExecutionType) {
        val player: Player = this.player

        if (executionType !in ability.type.executionTypes) {
            // This ability is executed differently.
            return
        }

//        player.sendMessage(Text.of("Executing ${ability.type.name} by $executionType"))

        val cont: Continuation<Unit>? = this.awaitingMap.remove(ability.type, executionType)
        if (cont != null) {
            // Found a waiting ability.
            cont.resume(Unit)
            return
        }

//        player.sendMessage(Text.of("No waiting ability."))


        if (this.hasCooldown(ability.type)) {
            // This ability is on cooldown.
            return
        }

//        player.sendMessage(Text.of("No cooldown."))


        val context = SimpleAbilityContext()

        // Set how the ability was initiated.
        context[StandardContext.executionType] = executionType

        if (executionType == AbilityExecutionType.FALL) {
            context[StandardContext.fallDistance] = player.getOrElse(Keys.FALL_DISTANCE, 0F)
            player.sendMessage(Text.of("Set fall distance."))
        }

        // Pre-load some values into the context.
        context[StandardContext.player] = player
        ability.prepare(player, context)

//        player.sendMessage(Text.of("Prepared."))

        if (!ability.validate(context)) {
            // Should we try to run the ability?
            return
        }

//        player.sendMessage(Text.of("Validated."))

        ability.preempt(context, executionType)

//        player.sendMessage(Text.of("Preempted."))

        if (ability.cooldown > 0) {
            // Set the cooldown. Don't spam your abilities!
            this.setCooldown(ability.type, ability.cooldown)

//            player.sendMessage(Text.of("Cooldown set."))
        }

        lateinit var execution: SimpleAbilityJob
        lateinit var job: Job

        val coroutine: CoroutineContext =
            Bending.SYNC + ability + context + executionType + exceptionHandler

        job = GlobalScope.launch(coroutine) {
            ability.execute(context, executionType)
            ability.cleanup(context)
            this@SimpleBender.runningMap.remove(job)
        }
        execution = SimpleAbilityJob(job)

        this.runningMap[job] = execution

//        player.sendMessage(Text.of("Ability executed."))
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

    private val cooldownMap = IdentityHashMap<AbilityType<*>, Long>()

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
        this.cooldownMap[type] = System.currentTimeMillis() + duration
    }

    override fun removeCooldown(type: AbilityType<*>): Long? =
        this.cooldownMap.remove(type)
}