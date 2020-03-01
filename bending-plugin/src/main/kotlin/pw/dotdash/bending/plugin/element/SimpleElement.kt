package pw.dotdash.bending.plugin.element

import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.text.format.TextColor
import pw.dotdash.bending.api.element.Element

data class SimpleElement(
    private val id: String,
    private val name: String,
    private val color: TextColor,
    private val primaryParticleType: ParticleType
) : Element {

    override fun getId(): String = this.id

    override fun getName(): String = this.name

    override fun getColor(): TextColor = this.color

    override fun getPrimaryParticleType(): ParticleType = this.primaryParticleType

    class Builder : Element.Builder {

        private var id: String? = null
        private var name: String? = null
        private var color: TextColor? = null
        private var primaryParticleType: ParticleType? = null

        override fun id(id: String): Element.Builder {
            this.id = id
            return this
        }

        override fun name(name: String): Element.Builder {
            this.name = name
            return this
        }

        override fun color(color: TextColor): Element.Builder {
            this.color = color
            return this
        }

        override fun primaryParticleType(type: ParticleType): Element.Builder {
            this.primaryParticleType = type
            return this
        }

        override fun from(value: Element): Element.Builder {
            this.id = value.id
            this.name = value.name
            this.color = value.color
            this.primaryParticleType = value.primaryParticleType
            return this
        }

        override fun reset(): Element.Builder {
            this.id = null
            this.name = null
            this.color = null
            this.primaryParticleType = null
            return this
        }

        override fun build(): Element {
            val id = checkNotNull(this.id)
            return SimpleElement(
                id = id,
                name = this.name ?: id,
                color = checkNotNull(this.color),
                primaryParticleType = checkNotNull(this.primaryParticleType)
            )
        }
    }
}