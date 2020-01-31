package arvenwood.bending.plugin

import arvenwood.bending.api.Bender
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigLoader
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
import arvenwood.bending.plugin.ability.air.*
import arvenwood.bending.plugin.ability.fire.*
import arvenwood.bending.plugin.config.SimpleAbilityConfigService
import arvenwood.bending.plugin.protection.GriefDefenderProtection
import arvenwood.bending.plugin.protection.SimpleBuildProtectionService
import arvenwood.bending.plugin.protection.SimplePvpProtectionService
import arvenwood.bending.plugin.registry.HashMapCatalogRegistryModule
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
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent
import org.spongepowered.api.event.game.state.*
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.chat.ChatTypes
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.text.format.TextColors.LIGHT_PURPLE
import org.spongepowered.api.text.format.TextColors.RESET
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
    }

    private val abilitiesDir: Path = this.configDir.resolve("abilities")
    private val abilityDirLoader: AbilityConfigLoader = FolderAbilityConfigLoader(this.abilitiesDir)

    private lateinit var transientBlockService: SimpleTransientBlockService

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        ASYNC = Sponge.getScheduler().createAsyncExecutor(this).asCoroutineDispatcher()
        SYNC = Sponge.getScheduler().createSyncExecutor(this).asCoroutineDispatcher()

        this.logger.info("Copying default ability config, if absent...")

        Sponge.getAssetManager().getAsset(this, "abilities/default.conf").get()
            .copyToFile(this.abilitiesDir.resolve("default.conf"), false, true)

        this.logger.info("Registering catalog modules...")

        Sponge.getRegistry().registerModule<AbilityType<*>>(HashMapCatalogRegistryModule())
        Sponge.getRegistry().registerModule<Element>(HashMapCatalogRegistryModule())
        Sponge.getRegistry().registerModule<BuildProtection>(HashMapCatalogRegistryModule())
        Sponge.getRegistry().registerModule<PvpProtection>(HashMapCatalogRegistryModule())
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
        Sponge.getServiceManager().setProvider<CooldownService>(this, SimpleCooldownService())
        Sponge.getServiceManager().setProvider<EffectService>(this, SimpleEffectService())
        Sponge.getServiceManager().setProvider<AbilityConfigService>(this, SimpleAbilityConfigService())
    }

    private fun registerCommands() {
        this.logger.info("Registering commands...")

        val bind: CommandSpec = CommandSpec.builder()
            .permission("bending.user.bind.base")
            .arguments(
                GenericArguments.catalogedElement(Text.of("ability"), AbilityType::class.java),
                GenericArguments.optional(GenericArguments.string(Text.of("config")), "default")
            )
            .executor { src: CommandSource, args: CommandContext ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                val type: AbilityType<*> = args.requireOne("ability")
                val configName: String = args.requireOne("config")

                val config: AbilityConfig = AbilityConfigService.get()[configName, type]
                    ?: throw CommandException(Text.of("Unknown ability config."))

                if (!src.hasPermission("bending.user.bind.config.$configName")) {
                    throw CommandPermissionException(Text.of("You do not have permission to use that config!"))
                }

                BenderService.get()[src.uniqueId].selectedAbility = config.ability
                src.sendMessage(
                    ChatTypes.ACTION_BAR,
                    Text.of("Selected Ability (config ", LIGHT_PURPLE, config.name, RESET, "): ", type.element.color, type.name)
                )

                CommandResult.success()
            }
            .build()

        val clear: CommandSpec = CommandSpec.builder()
            .permission("bending.user.clear")
            .executor { src: CommandSource, _: CommandContext ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                BenderService.get()[src.uniqueId].clearEquipped()
                src.sendMessage(Text.of(TextColors.GREEN, "Cleared all equipped abilities."))

                CommandResult.success()
            }
            .build()

        val copy: CommandSpec = CommandSpec.builder()
            .permission("bending.user.copy")
            .arguments(GenericArguments.player(Text.of("player")))
            .executor { src, args ->
                if (src !is Player) throw CommandException(Text.of("You must be a player to run this command!"))

                val target: Player = args.requireOne("player")

                val srcBender: Bender = BenderService.get()[src.uniqueId]
                val targetBender: Bender = BenderService.get()[target.uniqueId]

                srcBender.clearEquipped()
                for ((index, ability) in targetBender.equippedAbilities) {
                    srcBender[index] = ability
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
        registerPlaceholders()

        this.logger.info("Loading ability configs...")

        AbilityConfigService.get().registerAll(this.abilityDirLoader.load())
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
    }

    @Listener
    fun onRegisterElement(event: GameRegistryEvent.Register<Element>) {
        // Register the classical elements.

        event.register(Elements.Water)
        event.register(Elements.Earth)
        event.register(Elements.Fire)
        event.register(Elements.Air)
    }

    @Listener
    fun onRegisterAbilityType(event: GameRegistryEvent.Register<AbilityType<*>>) {
        // Register the builtin abilities.

        event.register(AirAgilityAbility)
        event.register(AirBlastAbility)
        event.register(AirJumpAbility)
        event.register(AirShieldAbility)
        event.register(AirSpoutAbility)
        event.register(AirTornadoAbility)

        event.register(CombustionAbility)
        event.register(FireBlastAbility)
        event.register(FireJetAbility)
        event.register(FireShieldAbility)
        event.register(FireWallAbility)
    }

    @Listener
    fun onRegisterBuildProtection(event: GameRegistryEvent.Register<BuildProtection>) {
        // Register GriefDefender protection if found.
        val griefdefender = GriefDefenderProtection.load()
        if (griefdefender != null) {
            if (GriefDefender.getCore().isProtectionModuleEnabled(Flags.BLOCK_PLACE)
                && GriefDefender.getCore().isProtectionModuleEnabled(Flags.BLOCK_BREAK)) {
                this.logger.info("GriefDefender found. Enabling build protection...")
                event.register(griefdefender)
            }
        }
    }

    @Listener
    fun onRegisterPvpProtection(event: GameRegistryEvent.Register<PvpProtection>) {
        // Register GriefDefender protection if found.
        val griefDefender = GriefDefenderProtection.load()
        if (griefDefender != null) {
            if (GriefDefender.getCore().isProtectionModuleEnabled(Flags.ENTITY_DAMAGE)) {
                this.logger.info("GriefDefender found. Enabling pvp protection...")
                event.register(griefDefender)
            }
        }
    }
}