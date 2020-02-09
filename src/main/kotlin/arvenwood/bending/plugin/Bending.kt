package arvenwood.bending.plugin

import arvenwood.bending.api.ability.Ability
import arvenwood.bending.api.ability.AbilityExecutionType
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.config.AbilityConfig
import arvenwood.bending.api.config.AbilityConfigService
import arvenwood.bending.api.config.simple.FolderAbilityConfigLoader
import arvenwood.bending.api.element.Element
import arvenwood.bending.api.protection.BuildProtection
import arvenwood.bending.api.protection.BuildProtectionService
import arvenwood.bending.api.protection.PvpProtection
import arvenwood.bending.api.protection.PvpProtectionService
import arvenwood.bending.api.service.AbilityService
import arvenwood.bending.api.service.BenderService
import arvenwood.bending.api.service.EffectService
import arvenwood.bending.api.service.TransientBlockService
import arvenwood.bending.api.util.registerBuilderSupplier
import arvenwood.bending.api.util.registerModule
import arvenwood.bending.api.util.setProvider
import arvenwood.bending.plugin.ability.SimpleAbilityExecutionType
import arvenwood.bending.plugin.ability.SimpleAbilityType
import arvenwood.bending.plugin.command.CommandBending
import arvenwood.bending.plugin.config.SimpleAbilityConfigService
import arvenwood.bending.plugin.element.SimpleElement
import arvenwood.bending.plugin.feature.FeatureManager
import arvenwood.bending.plugin.protection.SimpleBuildProtectionService
import arvenwood.bending.plugin.protection.SimplePvpProtectionService
import arvenwood.bending.plugin.registry.AbilityTypeCatalogRegistryModule
import arvenwood.bending.plugin.registry.BuildProtectionCatalogRegistryModule
import arvenwood.bending.plugin.registry.ElementCatalogRegistryModule
import arvenwood.bending.plugin.registry.PvpProtectionCatalogRegistryModule
import arvenwood.bending.plugin.service.SimpleAbilityService
import arvenwood.bending.plugin.service.SimpleBenderService
import arvenwood.bending.plugin.service.SimpleEffectService
import arvenwood.bending.plugin.service.SimpleTransientBlockService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.*
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import java.nio.file.Path
import javax.inject.Inject

@Plugin(
    id = "bending", name = "Bending", version = "0.1.0",
    dependencies = [
        Dependency(id = "placeholderapi", version = "[4.5.1,)", optional = true)
    ],
    description = "Avatar: The Last Airbender in Minecraft!",
    url = "https://ore.spongepowered.org/doot/Bending",
    authors = ["doot"]
)
class Bending @Inject constructor(
    private val logger: Logger,
    private val owner: PluginContainer,
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
    fun onConstruct(event: GameConstructionEvent) {
        ASYNC = Sponge.getScheduler().createAsyncExecutor(this).asCoroutineDispatcher()
        SYNC = Sponge.getScheduler().createSyncExecutor(this).asCoroutineDispatcher()
        LOGGER = this.logger
    }

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        this.logger.info("Copying default ability config, if absent...")

        Sponge.getAssetManager().getAsset(this, "abilities/default.conf").get()
            .copyToFile(this.abilitiesDir.resolve("default.conf"), false, true)

        this.logger.info("Registering catalog builders and modules...")

        Sponge.getRegistry()
            .registerBuilderSupplier<AbilityExecutionType.Builder>(SimpleAbilityExecutionType::Builder)
            .registerBuilderSupplier<Element.Builder>(SimpleElement::Builder)
            .registerBuilderSupplier<AbilityType.Builder<*>> { SimpleAbilityType.Builder<Ability<*>>() }

        Sponge.getRegistry()
            .registerModule<AbilityType<*>>(AbilityTypeCatalogRegistryModule)
            .registerModule<Element>(ElementCatalogRegistryModule)
            .registerModule<BuildProtection>(BuildProtectionCatalogRegistryModule)
            .registerModule<PvpProtection>(PvpProtectionCatalogRegistryModule)

        Sponge.getEventManager().registerListeners(this, CatalogRegistration)
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        this.logger.info("Enabling available plugin integrations...")

        val manager = FeatureManager()
        manager.register(this, this.logger)

        this.logger.info("Registering services...")

        Sponge.getServiceManager().setProvider<AbilityService>(this, SimpleAbilityService)
        Sponge.getServiceManager().setProvider<BenderService>(this, SimpleBenderService())
        Sponge.getServiceManager().setProvider<EffectService>(this, SimpleEffectService())

        this.transientBlockService = SimpleTransientBlockService()
        Sponge.getServiceManager().setProvider<TransientBlockService>(this, this.transientBlockService)

        this.abilityConfigService = SimpleAbilityConfigService()
        Sponge.getServiceManager().setProvider<AbilityConfigService>(this, this.abilityConfigService)

        this.logger.info("Registering commands...")

        Sponge.getCommandManager().register(this, CommandBending.SPEC, "bending", "b")

        this.logger.info("Registering listeners...")

        Sponge.getEventManager().registerListeners(this, BendingListener())
    }

    @Listener
    fun onPostInit(event: GamePostInitializationEvent) {
        this.logger.info("Loading ability configs...")

        this.loadConfigs()
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        this.logger.info("Reloading ability configs...")

        this.loadConfigs()
    }

    private fun loadConfigs() {
        val configs: List<AbilityConfig> = FolderAbilityConfigLoader(this.abilitiesDir).load()
        this.logger.info("Loaded ability configs: " + configs.distinctBy { it.name }.joinToString { it.name })
        AbilityConfigService.get().registerAll(configs)
    }

    @Listener
    fun onStarted(event: GameStartingServerEvent) {
        this.logger.info("Registering protection services...")

        Sponge.getServiceManager().setProvider<BuildProtectionService>(this, SimpleBuildProtectionService())
        Sponge.getServiceManager().setProvider<PvpProtectionService>(this, SimplePvpProtectionService())

        this.logger.info("Starting tasks...")

        this.transientBlockService.start(this)

        this.scoreboardManager = ScoreboardManager()
        Sponge.getEventManager().registerListeners(this, this.scoreboardManager)
    }

    @Listener
    fun onStopping(event: GameStoppingServerEvent) {
        this.logger.info("Stopping tasks...")

        this.transientBlockService.stop()
    }
}