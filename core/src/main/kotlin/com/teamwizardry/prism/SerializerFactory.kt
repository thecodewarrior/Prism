package com.teamwizardry.prism

import com.teamwizardry.mirror.type.TypeMirror

/**
 * A factory that can create specialized serializers for types that match the defined [pattern]. A type matches the
 * pattern if `pattern.isAssignableFrom(theType)`. When deciding the factory to use, the [Prism] object selects the
 * one with the most [specific][TypeMirror.specificity] pattern. In the case of equal specificity it chooses the last
 * one registered, in order to support overriding.
 */
abstract class SerializerFactory<T: Serializer<*>>(val prism: Prism<T>, val pattern: TypeMirror, val predicate: ((TypeMirror) -> Boolean)? = null) {
    abstract fun create(mirror: TypeMirror): T
}