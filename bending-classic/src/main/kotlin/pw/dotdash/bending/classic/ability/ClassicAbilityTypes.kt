package pw.dotdash.bending.classic.ability

import org.spongepowered.api.text.Text
import pw.dotdash.bending.api.ability.AbilityExecutionTypes
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.LEFT_CLICK
import pw.dotdash.bending.api.ability.AbilityExecutionTypes.SNEAK
import pw.dotdash.bending.api.ability.AbilityType
import pw.dotdash.bending.api.element.Elements
import pw.dotdash.bending.classic.ability.air.*
import pw.dotdash.bending.classic.ability.fire.*

object ClassicAbilityTypes {

    /**
     * @see AirAgilityAbility
     */
    @JvmField
    val AIR_AGILITY: AbilityType = AbilityType.builder()
        .id("bending:air_agility")
        .name("AirAgility")
        .element(Elements.AIR)
        .executionTypes(AbilityExecutionTypes.SPRINT_ON, AbilityExecutionTypes.SPRINT_OFF)
        .loader(::AirAgilityAbility)
        .build()

    /**
     * @see AirBlastAbility
     */
    @JvmField
    val AIR_BLAST: AbilityType = AbilityType.builder()
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
    val AIR_BURST: AbilityType = AbilityType.builder()
        .id("bending:air_burst")
        .name("AirBurst")
        .element(Elements.AIR)
        .executionTypes(LEFT_CLICK, SNEAK, AbilityExecutionTypes.FALL)
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
    val AIR_JUMP: AbilityType = AbilityType.builder()
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
    val AIR_SCOOTER: AbilityType = AbilityType.builder()
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
    val AIR_SHIELD: AbilityType = AbilityType.builder()
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
    val AIR_SPOUT: AbilityType = AbilityType.builder()
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
    val AIR_SUCTION: AbilityType = AbilityType.builder()
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
    val AIR_SWIPE: AbilityType = AbilityType.builder()
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
    val AIR_TORNADO: AbilityType = AbilityType.builder()
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
     * @see FireBlastAbility
     */
    @JvmField
    val FIRE_BLAST: AbilityType = AbilityType.builder()
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
    val FIRE_COMBUSTION: AbilityType = AbilityType.builder()
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
    val FIRE_JET: AbilityType = AbilityType.builder()
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
    val FIRE_SHIELD: AbilityType = AbilityType.builder()
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
    val FIRE_WALL: AbilityType = AbilityType.builder()
        .id("bending:fire_wall")
        .name("FireWall")
        .element(Elements.FIRE)
        .executionTypes(LEFT_CLICK)
        .loader(::FireWallAbility)
        .build()
}