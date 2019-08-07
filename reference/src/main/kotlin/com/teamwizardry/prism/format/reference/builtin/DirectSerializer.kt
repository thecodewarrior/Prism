package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.format.RefNode

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

object PrimitiveLongSerializer: DirectSerializer<Long>(Mirror.types.long)
object PrimitiveIntSerializer: DirectSerializer<Int>(Mirror.types.int)
object PrimitiveShortSerializer: DirectSerializer<Short>(Mirror.types.short)
object PrimitiveByteSerializer: DirectSerializer<Byte>(Mirror.types.byte)
object PrimitiveCharSerializer: DirectSerializer<Char>(Mirror.types.char)
object PrimitiveDoubleSerializer: DirectSerializer<Double>(Mirror.types.double)
object PrimitiveFloatSerializer: DirectSerializer<Float>(Mirror.types.float)

