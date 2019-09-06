package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror

abstract class Serializer<T> {
    val type: TypeMirror

    constructor(type: TypeMirror) {
        this.type = type
    }

    constructor() {
        this.type = Mirror.reflectClass(this.javaClass).findSuperclass(Serializer::class.java)!!.typeParameters[0]
    }
}