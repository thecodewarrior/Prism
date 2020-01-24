package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.RefNode

open class DirectSerializer<T: Any>: ReferenceSerializer<T> {
    constructor(type: TypeMirror): super(type)
    constructor(): super()

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(node: RefNode, existing: T?): T {
        return (node as LeafNode).value as T
    }

    override fun serialize(value: T): RefNode {
        return LeafNode(value)
    }
}

object StringSerializer: DirectSerializer<String>()
object LongSerializer: DirectSerializer<Long>()
object IntSerializer: DirectSerializer<Int>()
object ShortSerializer: DirectSerializer<Short>()
object ByteSerializer: DirectSerializer<Byte>()
object CharSerializer: DirectSerializer<Char>()
object DoubleSerializer: DirectSerializer<Double>()
object FloatSerializer: DirectSerializer<Float>()
object BooleanSerializer: DirectSerializer<Boolean>()

object PrimitiveLongSerializer: DirectSerializer<Long>(Mirror.types.long)
object PrimitiveIntSerializer: DirectSerializer<Int>(Mirror.types.int)
object PrimitiveShortSerializer: DirectSerializer<Short>(Mirror.types.short)
object PrimitiveByteSerializer: DirectSerializer<Byte>(Mirror.types.byte)
object PrimitiveCharSerializer: DirectSerializer<Char>(Mirror.types.char)
object PrimitiveDoubleSerializer: DirectSerializer<Double>(Mirror.types.double)
object PrimitiveFloatSerializer: DirectSerializer<Float>(Mirror.types.float)
object PrimitiveBooleanSerializer: DirectSerializer<Boolean>(Mirror.types.boolean)

