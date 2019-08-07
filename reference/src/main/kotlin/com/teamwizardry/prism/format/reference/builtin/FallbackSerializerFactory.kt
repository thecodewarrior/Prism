package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.DeserializationException
import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.ReferenceSerializerFactory
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.format.RefNode

class FallbackSerializerFactory(prism: ReferencePrism<*>): ReferenceSerializerFactory(prism, Mirror.reflect<Any>()) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return FallbackSerializer(mirror)
    }

    class FallbackSerializer(type: TypeMirror): ReferenceSerializer<Any>(type) {
        override fun deserialize(node: RefNode, existing: Any?): Any {
            node as? LeafNode ?: throw DeserializationException("Fallback serializer expects a LeafNode")
            val wrapper = node.value as? FallbackValue ?: throw DeserializationException("Fallback serializer expects a FallbackValue wrapper")
            return wrapper.value
        }

        override fun serialize(value: Any): RefNode {
            return LeafNode(FallbackValue(value))
        }
    }
}

data class FallbackValue(val value: Any)

