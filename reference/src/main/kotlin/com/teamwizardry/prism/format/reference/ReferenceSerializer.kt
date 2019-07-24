package com.teamwizardry.prism.format.reference

import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.DeserializationException
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.SerializationException
import com.teamwizardry.prism.Serializer
import com.teamwizardry.prism.format.reference.format.RefNode

typealias ReferencePrism<T> = Prism<ReferenceSerializer<T>>

abstract class ReferenceSerializer<T: Any>: Serializer<T> {
    constructor(type: TypeMirror): super(type)
    constructor(): super()

    protected abstract fun deserialize(node: RefNode, existing: T?): T
    protected abstract fun serialize(value: T): RefNode

    @Suppress("UNCHECKED_CAST")
    fun read(node: RefNode, existing: Any?): Any {
        try {
            return deserialize(node, existing as T?)
        } catch (e: Throwable) {
            throw DeserializationException("Error deserializing $type", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun write(value: Any): RefNode {
        try {
            return serialize(value as T)
        } catch (e: Throwable) {
            throw SerializationException("Error serializing $type", e)
        }
    }
}