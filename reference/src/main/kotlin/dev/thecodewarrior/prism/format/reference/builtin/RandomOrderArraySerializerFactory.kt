package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.base.analysis.ArrayAnalyzer
import dev.thecodewarrior.prism.base.analysis.ListAnalyzer
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.ArrayNode
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.format.RefNode
import java.util.Random

open class RandomOrderArraySerializerFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<Array<*>>()) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return RandomOrderListSerializer(prism, mirror as ArrayMirror)
    }

    class RandomOrderListSerializer(prism: ReferencePrism, type: ArrayMirror): ReferenceSerializer<Array<Any?>>(type) {
        val analyzer = ArrayAnalyzer<Any?, ReferenceSerializer<*>>(prism, type)

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: Array<Any?>?): Array<Any?> {
            val state = analyzer.getState()
            if(node !is ObjectNode)
                throw DeserializationException("Random list serializer expects an ObjectNode")
            val length = node.getLeaf("length").value as Int
            val arrayNode = node.getArray("data")
            state.padToLength(length)
            arrayNode.forEachIndexed { i, entry ->
                try {
                    entry as ObjectNode
                    val index = entry.getLeaf("index").value as Int
                    val valueNode = entry["value"]!!
                    state.set(index, analyzer.elementSerializer.read(valueNode, existing?.getOrNull(index)))
                } catch(e: Exception) {
                    throw DeserializationException("Error deserializing entry $i", e)
                }
            }
            val newValue = state.apply(existing)
            analyzer.releaseState(state)
            return newValue
        }

        override fun serialize(value: Array<Any?>): RefNode {
            val state = analyzer.getState()
            val arrayNode = ArrayNode()
            state.populate(value)
            state.buffer.forEachIndexed { index, v ->
                if(v != null)
                    arrayNode.add(ObjectNode.build {
                        "index" *= index
                        "value" *= analyzer.elementSerializer.write(v)
                    })
            }
            analyzer.releaseState(state)
            return ObjectNode.build {
                "length" *= value.size
                "data" *= arrayNode
            }
        }
    }
}
