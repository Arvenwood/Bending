package arvenwood.bending.api.ability

sealed class AbilityResult {

    object Success : AbilityResult() {
        override fun toString(): String = "Success"
    }

    abstract class Error : AbilityResult()

    object ErrorOnCooldown : Error() {
        override fun toString(): String = "ErrorOnCooldown"
    }

    object ErrorNoTarget : Error() {
        override fun toString(): String = "ErrorNoTarget"
    }

    object ErrorUnderWater : Error() {
        override fun toString(): String = "ErrorUnderWater"
    }

    object ErrorDied : Error() {
        override fun toString(): String = "ErrorDied"
    }

    object ErrorProtected : Error() {
        override fun toString(): String = "ErrorProtected"
    }

    object ErrorOutOfRange : Error() {
        override fun toString(): String = "ErrorOutOfRange"
    }
}