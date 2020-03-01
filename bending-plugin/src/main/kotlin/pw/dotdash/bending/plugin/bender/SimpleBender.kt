package pw.dotdash.bending.plugin.bender

import com.google.common.collect.Sets
import com.google.common.collect.Table
import com.google.common.collect.Tables
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.AbilityContextKeys.*
import pw.dotdash.bending.api.bender.Bender
import pw.dotdash.bending.api.event.ExecuteAbilityEvent
import pw.dotdash.bending.api.event.SetCooldownEvent
import pw.dotdash.bending.api.util.selectedSlotIndex
import pw.dotdash.bending.api.util.unwrap
import pw.dotdash.bending.plugin.Bending
import pw.dotdash.bending.plugin.ability.SimpleAbilityContext
import pw.dotdash.bending.plugin.ability.SimpleAbilityTask
import pw.dotdash.bending.plugin.data.DataQueries
import pw.dotdash.bending.plugin.util.set
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.MutableIterator
import kotlin.collections.MutableSet
import kotlin.collections.mapTo
import kotlin.collections.plusAssign
import kotlin.collections.set
import kotlin.collections.toCollection

class SimpleBender(private val uniqueId: UUID) : Bender {

    private val equippedAbilities: Array<Ability?> = arrayOfNulls(size = 9)

    private val runningAbilities: MutableSet<SimpleAbilityTask> = Sets.newIdentityHashSet<SimpleAbilityTask>()

    private val cooldownMap = IdentityHashMap<AbilityType, Long>()

    private val waitingMap: Table<AbilityType, AbilityExecutionType, CompletableFuture<Void?>> =
        Tables.newCustomTable(IdentityHashMap()) { IdentityHashMap<AbilityExecutionType, CompletableFuture<Void?>>() }

    override fun getUniqueId(): UUID = this.uniqueId

    override fun getPlayer(): Optional<Player> =
        Sponge.getServer().getPlayer(this.uniqueId)

    override fun getHotbar(): List<Optional<Ability>> =
        this.equippedAbilities.mapTo(ArrayList(9)) { Optional.ofNullable(it) }

    override fun getSelectedAbility(): Optional<Ability> =
        this.player.flatMap { player: Player -> this.getEquippedAbility(player.selectedSlotIndex) }

    override fun setSelectedAbility(ability: Ability?): Boolean {
        val player: Player = this.player.unwrap() ?: return false

        this.setEquippedAbility(player.selectedSlotIndex, ability)
        return true
    }

    override fun getEquippedAbility(hotbarIndex: Int): Optional<Ability> {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        return Optional.ofNullable(this.equippedAbilities[hotbarIndex])
    }

    override fun setEquippedAbility(hotbarIndex: Int, ability: Ability?): Boolean {
        check(hotbarIndex in 0..8) { "Invalid hotbar index: $hotbarIndex" }
        this.equippedAbilities[hotbarIndex] = ability
        return true
    }

    override fun clearEquippedAbilities() {
        for (i: Int in 0..8) {
            this.equippedAbilities[i] = null
        }
    }

    override fun getRunningAbilities(): Collection<AbilityTask> = this.runningAbilities.toCollection(Sets.newIdentityHashSet())

    override fun execute(ability: Ability, executionType: AbilityExecutionType): Optional<AbilityTask> {
        val player: Player = this.player.unwrap() ?: throw IllegalStateException("Player must be online.")

        if (executionType !in ability.type.executionTypes) {
            // This ability is executed differently.
            return Optional.empty()
        }

        val cont: CompletableFuture<Void?>? = this.waitingMap.remove(ability.type, executionType)
        if (cont != null) {
            // Found a waiting ability.
            cont.complete(null)
            return Optional.empty()
        }

        if (this.hasCooldown(ability.type)) {
            // This ability is on cooldown.
            return Optional.empty()
        }

        val context = SimpleAbilityContext()

        // Set how the ability was initiated.
        context[EXECUTION_TYPE] = executionType

        if (executionType == AbilityExecutionTypes.FALL) {
            context[FALL_DISTANCE] = player.getOrElse(Keys.FALL_DISTANCE, 0F)
        }

        // Pre-load some values into the context.
        context[PLAYER] = player
        context[BENDER] = this
        ability.prepare(Cause.of(EventContext.empty(), player), context)

        if (!ability.validate(context)) {
            // Should we try to run the ability?
            return Optional.empty()
        }

        val event = ExecuteAbilityEvent(player, this, ability, executionType, Sponge.getCauseStackManager().currentCause)
        Sponge.getEventManager().post(event)
        if (event.isCancelled) {
            return Optional.empty()
        }

        if (ability.cooldown > 0) {
            // Set the cooldown. Don't spam your abilities!
            this.setCooldown(ability.type, ability.cooldown)
        }

        ability.preempt(context, executionType)

        val task = SimpleAbilityTask(this, ability, ability.type, context, executionType)
        try {
            ability.execute(context, executionType, task)
        } catch (e: Exception) {
            player.sendMessage(Text.of(TextColors.RED, "Failed to execute ability: ", e.message.toString()))
            return Optional.empty()
        }

        if (!task.currentTask.isPresent) {
            // They didn't actually schedule a task...
            return Optional.empty()
        }

        this.runningAbilities += task
        return Optional.of(task)
    }

    internal fun cancelAbility(task: SimpleAbilityTask) {
        this.runningAbilities.remove(task)
    }

    override fun waitForExecution(type: AbilityType, executionType: AbilityExecutionType): CompletableFuture<Void?> {
        val future: CompletableFuture<Void?> = CompletableFuture<Void?>().whenComplete { _: Void?, t: Throwable? ->
            this.waitingMap.remove(type, executionType)

            if (t != null && t !is CancellationException) {
                Bending.LOGGER.error("Ability failed to execute", t)
            }
        }
        this.waitingMap.put(type, executionType, future)?.cancel(false)
        return future
    }

    override fun cancel(type: AbilityType): Boolean {
        var found = false
        val iter: MutableIterator<SimpleAbilityTask> = this.runningAbilities.iterator()
        while (iter.hasNext()) {
            val task = iter.next()
            if (type === task.type) {
                iter.remove()
                task.cancel()
                found = true
            }
        }
        return found
    }

    override fun hasCooldown(type: AbilityType): Boolean {
        val cooldown: Long = this.cooldownMap[type] ?: return false
        val current: Long = System.currentTimeMillis()
        if (cooldown <= current) {
            this.cooldownMap.remove(type)
            return false
        }
        return true
    }

    override fun setCooldown(type: AbilityType, duration: Long): Boolean {
        val player: Player? = this.player.unwrap()

        val next: Long = System.currentTimeMillis() + duration

        if (player != null) {
            val event = SetCooldownEvent(player, type, next, Sponge.getCauseStackManager().currentCause)
            Sponge.getEventManager().post(event)
            if (event.isCancelled) {
                return false
            }
        }

        this.cooldownMap[type] = next
        return true
    }

    override fun removeCooldown(type: AbilityType): Long {
        val player: Player? = this.player.unwrap()

        if (player != null) {
            val event = SetCooldownEvent(player, type, null, Sponge.getCauseStackManager().currentCause)
            Sponge.getEventManager().post(event)
            if (event.isCancelled) {
                return 0
            }
        }

        return this.cooldownMap.remove(type) ?: 0
    }

    override fun toContainer(): DataContainer =
        DataContainer.createNew()
            .set(DataQueries.PLAYER_UNIQUE_ID, this.uniqueId)

    override fun getContentVersion(): Int = 1
}