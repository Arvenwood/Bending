package arvenwood.bending.plugin.ability

import arvenwood.bending.api.ability.AbilityExecutionType.*
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Elements
import arvenwood.bending.plugin.ability.air.*
import arvenwood.bending.plugin.ability.earth.EarthBlastAbility
import arvenwood.bending.plugin.ability.earth.EarthTunnelAbility
import arvenwood.bending.plugin.ability.fire.*

object AbilityTypes {

    /**
     * @see AirAgilityAbility
     */
    @JvmField
    val AIR_AGILITY: AbilityType<AirAgilityAbility> = AbilityType.builder<AirAgilityAbility>()
        .id("bending:air_agility")
        .name("AirAgility")
        .element(Elements.AIR)
        .executionTypes(SPRINT_ON, SPRINT_OFF)
        .loader(::AirAgilityAbility)
        .build()

    /**
     * @see AirBlastAbility
     */
    @JvmField
    val AIR_BLAST: AbilityType<AirBlastAbility> = AbilityType.builder<AirBlastAbility>()
        .id("bending:air_blast")
        .name("AirBlast")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK, SNEAK)
        .loader(::AirBlastAbility)
        .build()

    /**
     * @see AirBurstAbility
     */
    @JvmField
    val AIR_BURST: AbilityType<AirBurstAbility> = AbilityType.builder<AirBurstAbility>()
        .id("bending:air_burst")
        .name("AirBurst")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK, SNEAK, FALL)
        .loader(::AirBurstAbility)
        .build()

    /**
     * @See AirJumpAbility
     */
    @JvmField
    val AIR_JUMP: AbilityType<AirJumpAbility> = AbilityType.builder<AirJumpAbility>()
        .id("bending:air_jump")
        .name("AirJump")
        .element(Elements.AIR)
        .executionTypes(SNEAK)
        .loader(::AirJumpAbility)
        .build()

    /**
     * @see AirScooterAbility
     */
    @JvmField
    val AIR_SCOOTER: AbilityType<AirScooterAbility> = AbilityType.builder<AirScooterAbility>()
        .id("bending:air_scooter")
        .name("AirScooter")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK)
        .loader(::AirScooterAbility)
        .build()

    /**
     * @see AirShieldAbility
     */
    @JvmField
    val AIR_SHIELD: AbilityType<AirShieldAbility> = AbilityType.builder<AirShieldAbility>()
        .id("bending:air_shield")
        .name("AirShield")
        .element(Elements.AIR)
        .executionTypes(SNEAK)
        .loader(::AirShieldAbility)
        .build()

    /**
     * @see AirSpoutAbility
     */
    @JvmField
    val AIR_SPOUT: AbilityType<AirSpoutAbility> = AbilityType.builder<AirSpoutAbility>()
        .id("bending:air_spout")
        .name("AirSpout")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK)
        .loader(::AirSpoutAbility)
        .build()

    /**
     * @see AirSuctionAbility
     */
    @JvmField
    val AIR_SUCTION: AbilityType<AirSuctionAbility> = AbilityType.builder<AirSuctionAbility>()
        .id("bending:air_suction")
        .name("AirSuction")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK, SNEAK)
        .loader(::AirSuctionAbility)
        .build()

    /**
     * @see AirSwipeAbility
     */
    @JvmField
    val AIR_SWIPE: AbilityType<AirSwipeAbility> = AbilityType.builder<AirSwipeAbility>()
        .id("bending:air_swipe")
        .name("AirSwipe")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK, SNEAK)
        .loader(::AirSwipeAbility)
        .build()

    /**
     * @see AirTornadoAbility
     */
    @JvmField
    val AIR_TORNADO: AbilityType<AirTornadoAbility> = AbilityType.builder<AirTornadoAbility>()
        .id("bending:air_tornado")
        .name("AirTornado")
        .element(Elements.AIR)
        .executionTypes(SNEAK)
        .loader(::AirTornadoAbility)
        .build()

    /**
     * @see EarthBlastAbility
     */
    @JvmField
    val EARTH_BLAST: AbilityType<EarthBlastAbility> = AbilityType.builder<EarthBlastAbility>()
        .id("bending:earth_blast")
        .name("EarthBlast")
        .element(Elements.EARTH)
        .executionTypes(LEFT_CLICK, SNEAK)
        .loader(::EarthBlastAbility)
        .build()

    /**
     * @see EarthTunnelAbility
     */
    @JvmField
    val EARTH_TUNNEL: AbilityType<EarthTunnelAbility> = AbilityType.builder<EarthTunnelAbility>()
        .id("bending:earth_tunnel")
        .name("EarthTunnel")
        .element(Elements.EARTH)
        .executionTypes(SNEAK)
        .loader(::EarthTunnelAbility)
        .build()

    /**
     * @see FireBlastAbility
     */
    @JvmField
    val FIRE_BLAST : AbilityType<FireBlastAbility> = AbilityType.builder<FireBlastAbility>()
        .id("bending:fire_blast")
        .name("FireBlast")
        .element(Elements.FIRE)
        .executionTypes(LEFT_CLICK)
        .loader(::FireBlastAbility)
        .build()

    /**
     * @see FireCombustionAbility
     */
    @JvmField
    val FIRE_COMBUSTION : AbilityType<FireCombustionAbility> = AbilityType.builder<FireCombustionAbility>()
        .id("bending:fire_combustion")
        .name("FireCombustion")
        .element(Elements.FIRE)
        .executionTypes(SNEAK)
        .loader(::FireCombustionAbility)
        .build()

    /**
     * @see FireJetAbility
     */
    @JvmField
    val FIRE_JET : AbilityType<FireJetAbility> = AbilityType.builder<FireJetAbility>()
        .id("bending:fire_jet")
        .name("FireJet")
        .element(Elements.FIRE)
        .executionTypes(LEFT_CLICK)
        .loader(::FireJetAbility)
        .build()

    /**
     * @see FireShieldAbility
     */
    @JvmField
    val FIRE_SHIELD : AbilityType<FireShieldAbility> = AbilityType.builder<FireShieldAbility>()
        .id("bending:fire_shield")
        .name("FireShield")
        .element(Elements.FIRE)
        .executionTypes(SNEAK)
        .loader(::FireShieldAbility)
        .build()

    /**
     * @see FireWallAbility
     */
    @JvmField
    val FIRE_WALL : AbilityType<FireWallAbility> = AbilityType.builder<FireWallAbility>()
        .id("bending:fire_wall")
        .name("FireWall")
        .element(Elements.FIRE)
        .executionTypes(LEFT_CLICK)
        .loader(::FireWallAbility)
        .build()
}