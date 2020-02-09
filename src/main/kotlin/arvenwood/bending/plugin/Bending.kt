package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityExecutionTypes
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigService
import arvenwood.bending.api.config.simple.FolderAbilityConfigLoader
import arvenwood.bending.api.element.Element
import arvenwood.bending.api.element.Elements
import arvenwood.bending.api.protection.BuildProtection
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtection
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.service.*
import arvenwood.bending.api.util.registerModule
import arvenwood.bending.api.util.setProvider
import arvenwood.bending.plugin.ability.AbilityTypes
import arvenwood.bending.plugin.ability.SimpleAbilityExecutionType
import arvenwood.bending.plugin.ability.SimpleAbilityType
import arvenwood.bending.plugin.ability.earth.EarthTunnelAbility
import arvenwood.bending.plugin.ability.fire.FireShieldAbility
import arvenwood.bending.plugin.ability.fire.FireWallAbility
import arvenwood.bending.plugin.command.abilityConfig
import arvenwood.bending.plugin.config.SimpleAbilityConfigService
import arvenwood.bending.plugin.element.SimpleElement
import arvenwood.bending.plugin.protection.GriefDefenderProtection
import arvenwood.bending.plugin.protection.SimpleBuildProtectionService
import arvenwood.bending.plugin.protection.SimplePvpProtectionService
import arvenwood.bending.plugin.registry.AbilityTypeCatalogRegistryModule
import arvenwood.bending.plugin.registry.BuildProtectionCatalogRegistryModule
import arvenwood.bending.plugin.registry.ElementCatalogRegistryModule
import arvenwood.bending.plugin.registry.PvpProtectionCatalogRegistryModule
import arvenwood.bending.plugin.service.*
import com.griefdefender.api.GriefDefender
import com.griefdefender.api.permission.flag.Flags
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import me.rojo8399.placeholderapi.PlaceholderService
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandPermissionException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.GenericArguments.player
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.*
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextColors.*
import java.nio.file.Path
import javax.inject.Inject

@Plugin(
    id = "bending", name = "Bending", version = "0.1.0",
    dependencies = [
        Dependency(id = "griefdefender", version = "[1.2.4,)", optional = true),
        Dependency(id = "placeholderapi", version = "[4.5.1,)", optional = true)
    ],
    description = "Avatar: The Last Airbender in Minecraft!",
    url = "https://ore.spongepowered.org/doot/Bending",
    authors = ["doot"]
)
class Bending @Inject constructor(
    private val logger: Logger,
    @ConfigDir(sharedRoot = false) private val configDir: Path
) {

    companion object {
        internal lateinit var ASYNC: CoroutineDispatcher
            private set

        internal lateinit var SYNC: CoroutineDispatcher
            private set

        internal lateinit var LOGGER: Logger
            private set
    }

    private val abilitiesDir: Path = this.configDir.resolve("abilities")

    private lateinit var transientBlockService: SimpleTransientBlockService
    private lateinit var abilityConfigService: SimpleAbilityConfigService

    private lateinit var scoreboardManager: ScoreboardManager

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        ASYNC = Sponge.getScheduler().createAsyncExecutor(this).asCoroutineDispatcher()
        SYNC = Sponge.getScheduler().createSyncExecutor(this).asCoroutineDispatcher()
        LOGGER = this.logger

        this.logger.info("Copying default ability config, if absent...")

        Sponge.getAssetManager().getAsset(this, "abilities/default.conf").get()
            .copyToFile(this.abilitiesDir.resolve("default.conf"), false, true)

        this.logger.info("Registering catalog modules...")

        Sponge.getRegistry().registerBuilderSupplier(AbilityExecutionType.Builder::class.java, SimpleAbilityExecutionType::Builder)
        Sponge.getRegistry().registerBuilderSupplier(Element.Builder::class.java, SimpleElement::Builder)
        Sponge.getRegistry().registerBuilderSupplier(AbilityType.Builder::class.java) { SimpleAbilityType.Builder<Ability<*>>() }

        Sponge.getRegistry().registerModule<AbilityType<*>>(AbilityTypeCatalogRegistryModule)
        Sponge.getRegistry().registerModule<Element>(ElementCatalogRegistryModule)
        Sponge.getRegistry().registerModule<BuildProtection>(BuildProtectionCatalogRegistryModule)
        Sponge.getRegistry().registerModule<PvpProtection>(PvpProtectionCatalogRegistryModule)

        Sponge.getEventManager().registerListeners(this, CatalogRegistration)
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        registerServices()
        registerCommands()

        this.logger.info("Registering listeners...")

        Sponge.getEventManager().registerListeners(this, BendingListener())
    }

    private fun registerServices() {
        this.logger.info("Registering services...")

        this.transientBlockService = SimpleTransientBlockService()
        Sponge.getServiceManager().setProvider<TransientBlockService>(this, this.transientBlockService)

        Sponge.getServiceManager().setProvider<AbilityService>(this, SimpleAbilityService)
        Sponge.getServiceManager().setProvider<BenderService>(this, SimpleBenderService())
        Sponge.getServiceManager().setProvider<ProtectionService>(this, EmptyProtectionService)
        Sponge.getServiceManager().setProvider<EffectService>(this, SimpleEffectService())

        this.abilityConfigService = SimpleAbilityConfigService()
        Sponge.getServiceManager().setProvider<AbilityConfigService>(this, this.abilityConfigService)
    }

    private fun registerCommands() {
        this.logger.info("Registering commands...")

        val bind: CommandSpec = CommandSpec.builder()
            .permission("bending.user.bind.base")
            .arguments(abilityConfig(Text.of("ability"), Text.of("config")))
            .executor { src: CommandSource, args: CommandContext ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                val config: AbilityConfig = args.requireOne("ability")

                if (!src.hasPermission("bending.user.bind.config.${config.name}")) {
                    throw CommandPermissionException(Text.of("You do not have permission to use that config!"))
                }

                BenderService.get()[src.uniqueId].selectedAbility = config.ability
                src.sendMessage(
                    ChatTypes.ACTION_BAR,
                    Text.of("Selected Ability (config ", LIGHT_PURPLE, config.name, RESET, "): ", config.type.element.color, config.type.name)
                )

                CommandResult.success()
            }
            .build()

        val clear: CommandSpec = CommandSpec.builder()
            .permission("bending.user.clear")
            .executor { src: CommandSource, _: CommandContext ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                BenderService.get()[src.uniqueId].clearEquipped()
                src.sendMessage(Text.of(GREEN, "Cleared all equipped abilities."))

                CommandResult.success()
            }
            .build()

        val copy: CommandSpec = CommandSpec.builder()
            .permission("bending.user.copy")
            .arguments(player(Text.of("player")))
            .executor { src, args ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                val target: Player = args.requireOne("player")

                val srcBender: Bender = BenderService.get()[src.uniqueId]
                val targetBender: Bender = BenderService.get()[target.uniqueId]

                srcBender.clearEquipped()
                for ((index: Int, ability: Ability<*>?) in targetBender.equippedAbilities.withIndex()) {
                    srcBender.setEquipped(index, ability)
                }

                CommandResult.success()
            }
            .build()

        val bending: CommandSpec = CommandSpec.builder()
            .permission("bending.base")
            .child(bind, "bind", "b")
            .child(clear, "clear")
            .child(copy, "copy")
            .build()

        Sponge.getCommandManager().register(this, bending, "bending", "b")
    }

    @Listener
    fun onPostInit(event: GamePostInitializationEvent) {
        this.registerPlaceholders()

        this.logger.info("Loading ability configs...")

        this.loadConfigs()
    }

    private fun loadConfigs() {
        val configs: List<AbilityConfig> = FolderAbilityConfigLoader(this.abilitiesDir).load()
        this.logger.info("Loaded ability configs: " + configs.distinctBy { it.name }.joinToString { it.name })
        AbilityConfigService.get().registerAll(configs)
    }

    private fun registerPlaceholders() {
        if (!Sponge.getPluginManager().isLoaded("placeholderapi")) return

        this.logger.info("PlaceholderAPI found. Registering placeholders...")

        val service: PlaceholderService = Sponge.getServiceManager().provide(PlaceholderService::class.java).orElse(null) ?: return
    }

    @Listener
    fun onStarting(event: GameAboutToStartServerEvent) {
        this.logger.info("Registering protection services...")

        Sponge.getServiceManager().setProvider<BuildProtectionService>(this, SimpleBuildProtectionService())
        Sponge.getServiceManager().setProvider<PvpProtectionService>(this, SimplePvpProtectionService())
    }

    @Listener
    fun onStarted(event: GameStartedServerEvent) {
        this.logger.info("Starting tasks...")

        this.transientBlockService.start(this)

        this.scoreboardManager = ScoreboardManager()
        Sponge.getEventManager().registerListeners(this, this.scoreboardManager)
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        this.logger.info("Reloading plugin...")

        this.loadConfigs()
    }

    @Listener
    fun onStopping(event: GameStoppingServerEvent) {
        this.logger.info("Stopping tasks...")

        this.transientBlockService.stop()
    }
}