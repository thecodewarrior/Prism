package dev.thecodewarrior.prism.format.reference

import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.SerializationException
import dev.thecodewarrior.prism.Serializer
import dev.thecodewarrior.prism.format.reference.format.RefNode

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