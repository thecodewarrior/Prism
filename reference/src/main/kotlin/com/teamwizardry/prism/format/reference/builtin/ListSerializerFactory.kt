package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.DeserializationException
import com.teamwizardry.prism.base.analysis.ListAnalyzer
import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.ReferenceSerializerFactory
import com.teamwizardry.prism.format.reference.format.ArrayNode
import com.teamwizardry.prism.format.reference.format.NullNode
import com.teamwizardry.prism.format.reference.format.RefNode

open class ListSerializerFactory(prism: ReferencePrism<*>): ReferenceSerializerFactory(prism, Mirror.reflect<List<*>>()) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return ListSerializer(prism, mirror)
    }

    class ListSerializer(prism: ReferencePrism<*>, type: TypeMirror): ReferenceSerializer<MutableList<Any?>>(type) {
        val analyzer = ListAnalyzer<Any?, ReferenceSerializer<*>>(prism, type.asClassMirror())

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: MutableList<Any?>?): MutableList<Any?> {
            analyzer.clear()
            node as? ArrayNode ?: throw DeserializationException("List serializer expects an ArrayNode")
            node.forEachIndexed { i, it ->
                try {
                    when (it) {
                        NullNode -> analyzer.buffer.add(null)
                        else -> analyzer.elementSerializer.read(it, existing?.getOrNull(i))
                    }
                } catch(e: Exception) {
                    throw DeserializationException("Error deserializing element $i", e)
                }
            }
            return analyzer.apply(existing)
        }

        override fun serialize(value: MutableList<Any?>): RefNode {
            val node = ArrayNode()
            analyzer.populate(value)
            analyzer.buffer.forEach {
                if(it == null)
                    node.add(NullNode)
                else
                    node.add(analyzer.elementSerializer.write(it))
            }
            return node
        }
    }
}
