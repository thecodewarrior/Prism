package dev.thecodewarrior.prism.formats.netty

import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.PrismException
import dev.thecodewarrior.prism.Serializer
import io.netty.buffer.ByteBuf

abstract class NettySerializer<T: Any>: Serializer<T> {
    constructor(type: TypeMirror): super(type)
    constructor(): super()

    protected abstract fun deserialize(buf: ByteBuf, existing: T?, syncing: Boolean): T
    protected abstract fun serialize(buf: ByteBuf, value: T, syncing: Boolean)

    @Suppress("UNCHECKED_CAST")
    fun read(buf: ByteBuf, existing: Any?, syncing: Boolean): Any {
        try {
            return deserialize(buf, existing as T?, syncing)
        } catch (e: Exception) {
            throw PrismException("Error deserializing $type", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun write(buf: ByteBuf, value: Any, syncing: Boolean) {
        try {
            return serialize(buf, value as T, syncing)
        } catch (e: Exception) {
            throw PrismException("Error serializing $type", e)
        }
    }
}