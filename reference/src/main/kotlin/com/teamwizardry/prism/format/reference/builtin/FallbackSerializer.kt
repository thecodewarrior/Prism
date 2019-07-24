package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.prism.DeserializationException
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.format.RefNode

object FallbackSerializer: ReferenceSerializer<Any>() {
    override fun deserialize(node: RefNode, existing: Any?): Any {
        node as? LeafNode ?: throw DeserializationException("Fallback serializer expects a LeafNode")
        val wrapper = node.value as? FallbackValue ?: throw DeserializationException("Fallback serializer expects a FallbackValue wrapper")
        return wrapper.value
    }

    override fun serialize(value: Any): RefNode {
        return LeafNode(FallbackValue(value))
    }
}

data class FallbackValue(val value: Any)

