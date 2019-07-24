package com.teamwizardry.prism

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror

abstract class Serializer<T> {
    val type: TypeMirror

    constructor(type: TypeMirror) {
        this.type = type
    }

    constructor() {
        this.type = Mirror.reflectClass(this.javaClass).findSuperclass(Serializer::class.java)!!.typeParameters[0]
    }
}