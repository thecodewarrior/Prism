package com.teamwizardry.prism

import com.teamwizardry.mirror.type.ConcreteTypeMirror
import com.teamwizardry.mirror.type.TypeMirror

abstract class SerializerFactory<T: Serializer>(val pattern: TypeMirror) {
    abstract fun create(mirror: TypeMirror): T
}