package arvenwood.bending.api.service

import arvenwood.bending.api.element.Element
import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.Sponge
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleType

interface EffectService {

    companion object {
        @JvmStatic
        fun get(): EffectService =
            Sponge.getServiceManager().provideUnchecked(EffectService::class.java)
    }

    fun getParticleType(element: Element): ParticleType

    fun createParticle(element: Element, quantity: Int, offset: Vector3d): ParticleEffect

    fun createRandomParticle(element: Element, quantity: Int): ParticleEffect
}