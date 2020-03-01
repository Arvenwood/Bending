package pw.dotdash.bending.plugin.service

import com.flowpowered.math.vector.Vector3d
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import pw.dotdash.bending.api.effect.EffectService
import pw.dotdash.bending.api.element.Element
import pw.dotdash.bending.api.util.VectorUtil

class SimpleEffectService : EffectService {

    private val extinguishEffect: ParticleEffect =
        ParticleEffect.builder()
            .type(ParticleTypes.SMOKE)
            .quantity(4)
            .offset(VectorUtil.VECTOR_0_4)
            .build()

    override fun getExtinguishEffect(): ParticleEffect = this.extinguishEffect

    override fun createParticle(element: Element, quantity: Int, offset: Vector3d): ParticleEffect =
        ParticleEffect.builder()
            .type(element.primaryParticleType)
            .quantity(quantity)
            .offset(offset)
            .build()

    override fun createRandomParticle(element: Element, quantity: Int): ParticleEffect =
        ParticleEffect.builder()
            .type(element.primaryParticleType)
            .quantity(quantity)
            .offset(Vector3d(Math.random(), Math.random(), Math.random()))
            .build()
}