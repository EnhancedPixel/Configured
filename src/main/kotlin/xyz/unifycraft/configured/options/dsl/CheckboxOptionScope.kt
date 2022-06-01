package xyz.unifycraft.configured.options.dsl

import java.lang.reflect.Field

class CheckboxOptionScope(
    default: Boolean,
    field: Field
) : OptionScope<Boolean>(
    default,
    field
) {
    override lateinit var name: String
}