package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.Serializer

internal abstract class ObjectProperty<S: Serializer<*>>(
    val name: String,
    val type: TypeMirror,
    prism: Prism<S>,
    val isImmutable: Boolean,
) {
    val serializer: S by prism[type]

    abstract fun getValue(target: Any): Any?
    abstract fun setValue(target: Any, value: Any?)
}
