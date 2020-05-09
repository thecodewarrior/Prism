package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.ArrayMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.base.analysis.ArrayAnalyzer
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.ArrayNode
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.format.RefNode

open class ArraySerializerFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<Array<*>>()) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return ArraySerializer(prism, mirror as ArrayMirror)
    }

    class ArraySerializer(prism: ReferencePrism, type: ArrayMirror): ReferenceSerializer<Array<Any?>>(type) {
        val analyzer = ArrayAnalyzer<Any?, ReferenceSerializer<*>>(prism, type)

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: Array<Any?>?): Array<Any?> {
            node as? ArrayNode ?: throw DeserializationException("Array serializer expects an ArrayNode")
            analyzer.getReader(existing).use { reader ->
                reader.reserve(node.size)
                node.forEachIndexed { i, it ->
                    try {
                        reader.add(when (it) {
                            NullNode -> null
                            else -> reader.serializer.read(it, existing?.getOrNull(i))
                        })
                    } catch(e: Exception) {
                        throw DeserializationException("Error deserializing element $i", e)
                    }
                }
                return reader.apply()
            }
        }

        override fun serialize(value: Array<Any?>): RefNode {
            val node = ArrayNode()
            analyzer.getWriter(value).use { writer ->
                writer.elements.forEach {
                    if(it == null)
                        node.add(NullNode)
                    else
                        node.add(writer.serializer.write(it))
                }
            }
            return node
        }
    }
}
