package com.teamwizardry.prism

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror

abstract class Serializer<T> {
    val type: TypeMirror

    constructor(type: TypeMirror) {
        this.type = type

        if(!Prism.isFullyConcrete(this.type))
            throw InvalidTypeException("$type isn't fully concrete")
    }

    constructor() {
        var type = Mirror.reflectClass(this.javaClass)
        while(type.raw != serializerRaw) {
            type = type.superclass ?: throw IllegalStateException()
        }
        this.type = type.typeParameters[0]

        if(!Prism.isFullyConcrete(this.type))
            throw InvalidTypeException("$type isn't fully concrete")
    }

    private companion object {
        val serializerRaw = Mirror.reflectClass(Serializer::class.java)
    }
}