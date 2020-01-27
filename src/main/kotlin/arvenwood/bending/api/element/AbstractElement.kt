package arvenwood.bending.api.element

abstract class AbstractElement(private val id: String, private val name: String = id) : Element {
    override fun getId(): String = id
    override fun getName(): String = name
}