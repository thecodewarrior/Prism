package dev.thecodewarrior.prism.format.reference

import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.SerializerFactory

abstract class ReferenceSerializerFactory(
    prism: ReferencePrism, pattern: TypeMirror,
    predicates: (TypeMirror) -> Boolean = { true }
): SerializerFactory<ReferenceSerializer<*>>(prism, pattern, predicates) {
}