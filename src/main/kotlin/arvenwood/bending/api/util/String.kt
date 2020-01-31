package arvenwood.bending.api.util

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

fun String.toText(): Text =
    TextSerializers.FORMATTING_CODE.deserialize(this)

fun String.toText(formattingCode: Char): Text =
    TextSerializers.formattingCode(formattingCode).deserialize(this)