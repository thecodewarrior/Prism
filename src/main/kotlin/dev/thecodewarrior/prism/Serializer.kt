package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror

/**
 * A serializer for some type [T]. Each format will define its own abstract subclass that contains that format's
 * read/write methods. If no [TypeMirror] is passed to the constructor, the resolved type parameter is used.
 *
 * Serializers only work on *precisely* the type they're created for. If serializers for a whole range of types are
 * required, use a [SerializerFactory]
 *
 * @param T the type being serialized
 */
public abstract class Serializer<T: Any> {
    public val type: TypeMirror

    public constructor(type: TypeMirror) {
        this.type = type
    }

    public constructor() {
        this.type = Mirror.reflectClass(this.javaClass).findSuperclass(Serializer::class.java)!!.typeParameters[0]
    }
}