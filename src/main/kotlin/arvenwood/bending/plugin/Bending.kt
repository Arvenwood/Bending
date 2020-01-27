package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.*
import arvenwood.bending.api.element.Element
import arvenwood.bending.api.service.*
import arvenwood.bending.api.util.get
import arvenwood.bending.api.util.index
import arvenwood.bending.api.util.setProvider
import arvenwood.bending.plugin.registry.AbilityTypeCatalogRegisterModule
import arvenwood.bending.plugin.registry.ElementCatalogRegistryModule
import arvenwood.bending.plugin.service.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.block.InteractBlockEvent
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextStyles
import javax.inject.Inject

@Plugin(id = "bending", name = "Bending", version = "0.1.0")
class Bending @Inject constructor(private val logger: Logger) {

    companion object {
        @JvmStatic
        internal lateinit var ASYNC: CoroutineDispatcher

        @JvmStatic
        internal lateinit var SYNC: CoroutineDispatcher
    }

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        Sponge.getRegistry().registerModule(AbilityType::class.java, AbilityTypeCatalogRegisterModule())
        Sponge.getRegistry().registerModule(Element::class.java, ElementCatalogRegistryModule())
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        ASYNC = Sponge.getScheduler().createAsyncExecutor(this).asCoroutineDispatcher()
        SYNC = Sponge.getScheduler().createSyncExecutor(this).asCoroutineDispatcher()

        registerServices()
        registerCommands()
    }

    private fun registerServices() {
        this.logger.info("Registering services...")

        Sponge.getServiceManager().setProvider<AbilityService>(this, SimpleAbilityService)
        Sponge.getServiceManager().setProvider<BenderService>(this, SimpleBenderService())
        Sponge.getServiceManager().setProvider<ProtectionService>(this, EmptyProtectionService)
        Sponge.getServiceManager().setProvider<CooldownService>(this, SimpleCooldownService())
    }

    private fun registerCommands() {
        this.logger.info("Registering commands...")

        val bind = CommandSpec.builder()
            .arguments(GenericArguments.catalogedElement(Text.of("ability"), AbilityType::class.java))
            .executor { src, args ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                val ability: AbilityType<*> = args.requireOne("ability")
                BenderService.get()[src].selectedAbility = ability.default
                src.sendMessage(ChatTypes.ACTION_BAR, Text.of("Selected Ability: ", ability.element.color, ability.name))

                CommandResult.success()
            }
            .build()

        val bending = CommandSpec.builder()
            .child(bind, "bind", "b")
            .build()

        Sponge.getCommandManager().register(this, bending, "bending", "b")
    }

    @Listener
    fun onJoin(event: ClientConnectionEvent.Join) {
        displayAbility(event.targetEntity)
    }

    @Listener
    fun onChangeSlot(event: ChangeInventoryEvent.Held, @First player: Player) {
        val oldSlot: Int = event.originalSlot.index
        val newSlot: Int = event.finalSlot.index

        if (oldSlot == newSlot) return

        displayAbility(player)
    }

    private fun displayAbility(player: Player) {
        val type: AbilityType<Ability<*>>? = BenderService.get()[player].selectedAbility?.type
        if (type != null) {
            val onCooldown: Boolean = CooldownService.get().hasCooldown(player, type)
            if (onCooldown) {
                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, TextStyles.STRIKETHROUGH, type.name))
            } else {
                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(type.element.color, type.name))
            }
        } else {
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of("<none>"))
        }
    }

    @Listener
    fun onLeftClick(event: InteractBlockEvent.Primary.MainHand, @First player: Player) {
        val bender: Bender = BenderService.get()[player]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.LEFT_CLICK)
    }

    @Listener
    fun onRightClick(event: InteractEntityEvent.Secondary.MainHand, @First player: Player) {
        val bender: Bender = BenderService.get()[player]
        val ability: Ability<*> = bender.selectedAbility ?: return
        bender.execute(ability, AbilityExecutionType.RIGHT_CLICK)
    }

    @Listener
    fun onSneak(event: ChangeDataHolderEvent.ValueChange, @First player: Player) {
        if (event.endResult[Keys.IS_SNEAKING] == true) {
            val bender: Bender = BenderService.get()[player]
            val ability: Ability<*> = bender.selectedAbility ?: return
            bender.execute(ability, AbilityExecutionType.SNEAK)
        }
    }
}