package arvenwood.bending.api.ability

object AbilityExecutionTypes {

    @JvmField
    val COMBO: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:combo")
        .name("Combo")
        .build()

    @JvmField
    val FALL: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:fall")
        .name("Fall")
        .build()

    @JvmField
    val JUMP: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:jump")
        .name("JUMP")
        .build()

    @JvmField
    val LEFT_CLICK: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:left_click")
        .name("LeftClick")
        .build()

    @JvmField
    val RIGHT_CLICK: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:right_click")
        .name("RightClick")
        .build()

    @JvmField
    val SNEAK: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:sneak")
        .name("Sneak")
        .build()

    @JvmField
    val SPRINT_OFF: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:sprint_off")
        .name("SprintOff")
        .build()

    @JvmField
    val SPRINT_ON: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:sprint_on")
        .name("SprintOn")
        .build()

    @JvmField
    val SWAP_HAND: AbilityExecutionType = AbilityExecutionType.builder()
        .id("bending:swap_hand")
        .name("SwapHand")
        .build()
}