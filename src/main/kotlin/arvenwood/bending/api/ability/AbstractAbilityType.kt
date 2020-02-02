package arvenwood.bending.api.ability

import arvenwood.bending.api.element.Element
import kotlin.reflect.KClass

abstract class AbstractAbilityType<out T : Ability<T>>(
    override val element: Element,
    override val executionTypes: Set<KClass<out AbilityExecutionType>>,
    private val id: String,
    private val name: String = id
) : AbilityType<T> {

    override fun getId(): String = id

    override fun getName(): String = name
}