package arvenwood.bending.api.ability

import arvenwood.bending.api.element.Element

abstract class AbstractAbilityType<out T : Ability<T>>(
    override val element: Element,
    override val executionTypes: Set<AbilityExecutionType>,
    private val id: String,
    private val name: String = id
) : AbilityType<T> {

    override fun getId(): String = id

    override fun getName(): String = name
}