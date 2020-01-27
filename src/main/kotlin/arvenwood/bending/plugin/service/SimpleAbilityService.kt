package arvenwood.bending.plugin.service

import arvenwood.bending.api.service.AbilityService

object SimpleAbilityService : AbilityService {

    override val timeStep: Long = 1

    override val abilityDelayMilli: Long = 50
}