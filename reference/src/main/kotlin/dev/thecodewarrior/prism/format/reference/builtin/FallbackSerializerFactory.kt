package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.RefNode

class FallbackSerializerFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<Any>()) {
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

