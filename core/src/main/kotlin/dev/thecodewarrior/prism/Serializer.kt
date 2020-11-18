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

    /**
     * Checks if the object has changed. This is used by the ObjectAnalyzer when assessing whether immutable properties
     * have changed, and thus necessitate creating a new instance. The default implementation uses the `.equals()`
     * method, but some types may require different equality checks, such as equality by identity rather than value.
     *
     * For increased accuracy, some "container" types may elect to create a custom equality check that utilizes the
     * contained serializers' [didChange] methods. Doing this is not required, but is strongly recommended.
     */
    public open fun didChange(oldValue: T, newValue: T): Boolean {
        return oldValue == newValue
    }
}