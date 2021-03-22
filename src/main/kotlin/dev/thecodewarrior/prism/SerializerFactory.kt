package dev.thecodewarrior.prism

import dev.thecodewarrior.mirror.type.TypeMirror

/**
 * A factory that can create specialized serializers for types that match the defined [pattern]. A type matches the
 * pattern if `pattern.isAssignableFrom(theType)`. When deciding the factory to use, the [Prism] object selects the
 * one with the most [specific][TypeMirror.specificity] pattern. In the case of equal specificity it chooses the last
 * one registered, in order to support overriding.
 */
public abstract class SerializerFactory<T: Serializer<*>>(
    public val prism: Prism<T>,
    public val pattern: TypeMirror,
    public val predicate: ((TypeMirror) -> Boolean)? = null
) {
    public abstract fun create(mirror: TypeMirror): T
}