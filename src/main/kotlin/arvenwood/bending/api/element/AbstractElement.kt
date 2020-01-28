package arvenwood.bending.api.element

abstract class AbstractElement(private val id: String, private val name: String = id) : Element {
    override fun getId(): String = id
    override fun getName(): String = name

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is AbstractElement -> false
        else -> this.id == other.id && this.name == other.name
    }

    override fun hashCode(): Int =
        31 * id.hashCode() + name.hashCode()
}