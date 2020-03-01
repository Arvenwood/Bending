package pw.dotdash.bending.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameRegistryEvent
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.*
import org.spongepowered.api.plugin.Plugin
import pw.dotdash.bending.api.ability.*
import pw.dotdash.bending.api.ability.config.ConfigFileLoader
import pw.dotdash.bending.api.bender.BenderService
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Element
import pw.dotdash.bending.api.protection.BuildProtection
import pw.dotdash.bending.api.protection.BuildProtectionService
import pw.dotdash.bending.api.protection.PvpProtection
import pw.dotdash.bending.api.protection.PvpProtectionService
import pw.dotdash.bending.api.temp.TempBlock
import pw.dotdash.bending.api.temp.TempBlockService
import pw.dotdash.bending.plugin.ability.*
import pw.dotdash.bending.plugin.bender.SimpleBenderService
import pw.dotdash.bending.plugin.command.CommandBending
import pw.dotdash.bending.plugin.element.SimpleElement
import pw.dotdash.bending.plugin.feature.FeatureManager
import pw.dotdash.bending.plugin.protection.SimpleBuildProtectionService
import pw.dotdash.bending.plugin.protection.SimplePvpProtectionService
import pw.dotdash.bending.plugin.registry.*
import pw.dotdash.bending.plugin.service.SimpleEffectService
import pw.dotdash.bending.plugin.service.SimpleTempBlockService
import pw.dotdash.bending.plugin.temp.SimpleTempBlock
import pw.dotdash.bending.plugin.util.registerBuilderSupplier
import pw.dotdash.bending.plugin.util.registerModule
import pw.dotdash.bending.plugin.util.setProvider
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

@Plugin(
    id = "bending", name = "Bending", version = "0.1.0",
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

    private lateinit var transientBlockService: SimpleTempBlockService
    private lateinit var scoreboardManager: ScoreboardManager

    init {
        if (Files.notExists(this.abilitiesDir)) {
            Files.createDirectories(this.abilitiesDir)
        }
    }

    @Listener
    fun onConstruct(event: GameConstructionEvent) {
        ASYNC = Sponge.getScheduler().createAsyncExecutor(this).asCoroutineDispatcher()
        SYNC = Sponge.getScheduler().createSyncExecutor(this).asCoroutineDispatcher()
        LOGGER = this.logger
    }

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        this.logger.info("Registering catalog builders and modules...")

        Sponge.getRegistry()
            .registerBuilderSupplier<AbilityConfig.Builder>(SimpleAbilityConfig::Builder)
            .registerBuilderSupplier<AbilityContext.Builder>(SimpleAbilityContext::Builder)
            .registerBuilderSupplier<AbilityContextKey.Builder<*>> { SimpleAbilityContextKey.Builder<Any>() }
            .registerBuilderSupplier<AbilityExecutionType.Builder>(SimpleAbilityExecutionType::Builder)
            .registerBuilderSupplier<AbilityType.Builder>(SimpleAbilityType::Builder)
            .registerBuilderSupplier<Element.Builder>(SimpleElement::Builder)
            .registerBuilderSupplier<TempBlock.Builder>(SimpleTempBlock::Builder)

        Sponge.getRegistry()
            .registerModule<AbilityConfig>(AbilityConfigRegistryModule)
            .registerModule<AbilityContextKey<*>>(AbilityContextKeyRegistryModule)
            .registerModule<AbilityExecutionType>(AbilityExecutionTypeRegistryModule)
            .registerModule<AbilityType>(AbilityTypeRegistryModule)
            .registerModule<Element>(ElementRegistryModule)
            .registerModule<BuildProtection>(BuildProtectionRegistryModule)
            .registerModule<PvpProtection>(PvpProtectionRegistryModule)
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        this.logger.info("Enabling available plugin integrations...")

        val manager = FeatureManager()
        manager.register(this, this.logger)

        this.logger.info("Registering services...")

        Sponge.getServiceManager().setProvider<AbilityService>(this, SimpleAbilityService(this))
        Sponge.getServiceManager().setProvider<BenderService>(this, SimpleBenderService())
        Sponge.getServiceManager().setProvider<EffectService>(this, SimpleEffectService())

        this.transientBlockService = SimpleTempBlockService()
        Sponge.getServiceManager().setProvider<TempBlockService>(this, this.transientBlockService)

        this.logger.info("Registering commands...")

        Sponge.getCommandManager().register(this, CommandBending.SPEC, "bending", "b")

        this.logger.info("Registering listeners...")

        Sponge.getEventManager().registerListeners(this, BendingListener())
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

    @Listener
    fun onRegisterAbilityConfig(event: GameRegistryEvent.Register<AbilityConfig>) {
        this.logger.info("Loading default ability config...")

        val defaultConf: Path = this.abilitiesDir.resolve("default.conf")

        Sponge.getAssetManager().getAsset(this, "abilities/default.conf").get()
            .copyToFile(defaultConf, false, true)

        event.register(AbilityConfig.builder()
            .id("bending:default")
            .name("Default")
            .loader(ConfigFileLoader(defaultConf))
            .build())
    }
}