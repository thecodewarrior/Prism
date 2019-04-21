package com.teamwizardry.prism

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror

abstract class SerializerFactory<T: Serializer<*>>(val pattern: TypeMirror) {
    init {
        if(Prism.isFullyConcrete(this.pattern))
            throw InvalidTypeException("$pattern is fully concrete")
    }

    abstract fun create(prism: Prism<T>, mirror: TypeMirror): T

    private companion object {
        val serializerRaw = Mirror.reflectClass(Serializer::class.java)
    }
}