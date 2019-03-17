package com.teamwizardry.prism

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.ConcreteTypeMirror
import com.teamwizardry.mirror.type.TypeMirror
import java.lang.reflect.AnnotatedParameterizedType

abstract class Serializer<T> {
    val type: TypeMirror

    init {
        var type = Mirror.reflectClass(this.javaClass)
        while(type.raw != serializerRaw) {
            type = type.superclass ?: throw IllegalStateException()
        }
        this.type = type.typeParameters[0]

        if(!Prism.isFullyConcrete(this.type))
            throw ImpossibleSerializerException("$type isn't fully concrete")
    }

    private companion object {
        val serializerRaw = Mirror.reflectClass(Serializer::class.java)
    }
}