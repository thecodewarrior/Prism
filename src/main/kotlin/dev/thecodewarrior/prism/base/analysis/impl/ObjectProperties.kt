package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.member.FieldMirror
import dev.thecodewarrior.mirror.member.MethodMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.base.analysis.AutoSerializationException

internal class FieldProperty<S: Serializer<*>>(
    name: String,
    type: TypeMirror,
    prism: Prism<S>,
    isImmutable: Boolean,
    private val field: FieldMirror
): ObjectProperty<S>(name, type, prism, isImmutable) {

    override fun getValue(target: Any): Any? {
        return field.get(target)
    }

    override fun setValue(target: Any, value: Any?) {
        field.set(target, value)
    }
}

internal class AccessorProperty<S: Serializer<*>>(
    name: String,
    type: TypeMirror,
    prism: Prism<S>,
    private val getter: MethodMirror,
    private val setter: MethodMirror?
): ObjectProperty<S>(name, type, prism, setter == null) {

    override fun getValue(target: Any): Any? {
        return getter.call(target)
    }

    override fun setValue(target: Any, value: Any?) {
        if(setter == null)
            throw AutoSerializationException("$name is read-only")
        setter.call<Unit>(target, value)
    }
}
