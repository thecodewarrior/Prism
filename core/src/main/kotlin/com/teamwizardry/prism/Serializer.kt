package com.teamwizardry.prism

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror

abstract class Serializer<T> {
    val type: TypeMirror

    constructor(type: TypeMirror) {
        this.type = type

        if(!Prism.isFullyConcrete(this.type))
            throw InvalidTypeException("Type $type isn't fully concrete. Serializer types can't include type " +
                "variables or wildcards, consider creating a SerializerFactory instead.")
    }

    constructor() {
        this.type = Mirror.reflectClass(this.javaClass).findSuperclass(Serializer::class.java)!!.typeParameters[0]

        if(!Prism.isFullyConcrete(this.type))
            throw InvalidTypeException("Type $type isn't fully concrete. Serializer types can't include type " +
                "variables or wildcards, consider creating a SerializerFactory instead.")
    }
}