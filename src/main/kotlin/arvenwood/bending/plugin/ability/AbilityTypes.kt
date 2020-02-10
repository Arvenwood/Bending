package arvenwood.bending.plugin.ability

import arvenwood.bending.api.ability.AbilityExecutionTypes.FALL
import arvenwood.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import arvenwood.bending.api.ability.AbilityExecutionTypes.SNEAK
import arvenwood.bending.api.ability.AbilityExecutionTypes.SPRINT_OFF
import arvenwood.bending.api.ability.AbilityExecutionTypes.SPRINT_ON
import arvenwood.bending.api.ability.AbilityType
import arvenwood.bending.api.element.Elements
import arvenwood.bending.plugin.ability.air.*
import arvenwood.bending.plugin.ability.earth.EarthBlastAbility
import arvenwood.bending.plugin.ability.earth.EarthTunnelAbility
import arvenwood.bending.plugin.ability.fire.*
import org.spongepowered.api.text.Text

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
        .description(
            Text.of(
                "AirBlast is the most fundamental bending technique of an airbender." +
                        "\nIt allows the bender to be extremely agile and possess great mobility, " +
                        "but also has many utility options, such as cooling lava, opening doors and flicking levers."
            )
        )
        .instructions(
            Text.of(
                "(Push) LEFT CLICK while aiming at an entity to push them back." +
                        "\n(Throw) Tap SNEAK to select a location and LEFT CLICK in a direction to throw entities away from the selected location."
            )
        )
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
        .description(
            Text.of(
                "AirBurst is one of the most powerful abilities in the airbender's arsenal." +
                        "\nIt allows the bender to create space between them and whoever is close to them." +
                        "\nAirBurst is extremely useful when you're surrounded by mobs, of if you're low in health and need to escape." +
                        "\nIt can also be useful for confusing your target also."
            )
        )
        .instructions(
            Text.of(
                "(Sphere) Hold SNEAK until particles appear and then release shift to create air that expands outwards, pushing entities back. " +
                        "\n(Cone) While charging the move with SNEAK, LEFT CLICK to send the burst in a cone only going in one direction." +
                        "\nIf you fall from great height while you are on this slot, the burst will automatically activate."
            )
        )
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
        .description(
            Text.of(
                "AirScooter is a fast means of transportation." +
                        "\nIt can be used to escape from enemies or confuse them by using air scooter around them."
            )
        )
        .instructions(
            Text.of(
                "SPRINT, JUMP, and LEFT CLICK while in the air to activate air scooter. " +
                        "You will then move forward in the direction you're looking."
            )
        )
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
        .description(
            Text.of(
                "Air Shield is one of the most powerful defensive techniques in existence." +
                        "\nThis ability is mainly used when you are low health and need protection." +
                        "\nIt's also useful when you're surrounded by mobs."
            )
        )
        .instructions(
            Text.of(
                "Hold SNEAK and a shield of air will form around you, blocking projectiles and pushing entities back."
            )
        )
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
        .description(
            Text.of(
                "This ability gives the airbender limited sustained levitation." +
                        "\nIt allows an airbender to gain a height advantage to escape from mobs, players or just to dodge from attacks." +
                        "\nThis ability is also useful for building as it allows you to reach great heights."
            )
        )
        .instructions(
            Text.of(
                "LEFT CLICK to activate a spout beneath you and hold SPACE to go higher. " +
                        "If you wish to go lower, simply hold SNEAK. To disable this ability, LEFT CLICK once again."
            )
        )
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
        .description(
            Text.of(
                "AirSuction is a basic ability that allows you to manipulation an entity's movement." +
                        "\nIt can be used to bring someone back to you when they're running away, or even to get yourself to great heights."
            )
        )
        .instructions(
            Text.of(
                "(Pull) LEFT CLICK while aiming at a target to pull them towards you." +
                        "\n(Throw) SNEAK to select a point and then LEFT CLICK at a target or yourself to send you or your target to the point that you selected."
            )
        )
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
        .description(
            Text.of(
                "AirSwipe is the most commonly used damage ability in an airbender's arsenal." +
                        "\nAn arc of air will flow from you towards the direction you're facing, cutting and pushing back anything in its path." +
                        "\nThis ability will extinguish fires, cool lava, and cut things like grass, mushrooms, and flowers."
            )
        )
        .instructions(
            Text.of(
                "(Uncharged) Simply LEFT CLICK to send an air swipe out that will damage targets that it comes into contact with." +
                        "\n(Charged) Hold SNEAK until particles appear, then release SNEAK to send a more powerful air swipe out that damages entity's that it comes into contact with."
            )
        )
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
        .description(
            Text.of(
                "Tornado is one of the most powerful and advanced abilities that an Airbender knows." +
                        "\nIf the tornado meets a player or mob, it will push them around." +
                        "\nTornado can also be used to push back projectiles and used for mobility." +
                        "\nUse a tornado directly under you to propel yourself upwards."
            )
        )
        .instructions(
            Text.of(
                "Hold SNEAK and a tornado will form gradually wherever you look."
            )
        )
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
    val FIRE_BLAST: AbilityType<FireBlastAbility> = AbilityType.builder<FireBlastAbility>()
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
    val FIRE_COMBUSTION: AbilityType<FireCombustionAbility> = AbilityType.builder<FireCombustionAbility>()
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
    val FIRE_JET: AbilityType<FireJetAbility> = AbilityType.builder<FireJetAbility>()
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
    val FIRE_SHIELD: AbilityType<FireShieldAbility> = AbilityType.builder<FireShieldAbility>()
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
    val FIRE_WALL: AbilityType<FireWallAbility> = AbilityType.builder<FireWallAbility>()
        .id("bending:fire_wall")
        .name("FireWall")
        .element(Elements.FIRE)
        .executionTypes(LEFT_CLICK)
        .loader(::FireWallAbility)
        .build()
}