package com.teamwizardry.prism.format.reference

import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.SerializerFactory

abstract class ReferenceSerializerFactory(
    prism: ReferencePrism<*>, pattern: TypeMirror,
    predicates: (TypeMirror) -> Boolean = { true }
): SerializerFactory<ReferenceSerializer<*>>(prism, pattern, predicates) {
}