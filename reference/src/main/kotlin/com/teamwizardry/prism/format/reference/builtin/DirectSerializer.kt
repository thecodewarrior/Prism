package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.format.RefNode

open class DirectSerializer<T: Any>: ReferenceSerializer<T>() {
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

