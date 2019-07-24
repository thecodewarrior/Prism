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
            val state = analyzer.getState()
            state.clear()
            node as? ArrayNode ?: throw DeserializationException("List serializer expects an ArrayNode")
            node.forEachIndexed { i, it ->
                try {
                    state.buffer.add(when (it) {
                        NullNode -> null
                        else -> analyzer.elementSerializer.read(it, existing?.getOrNull(i))
                    })
                } catch(e: Exception) {
                    throw DeserializationException("Error deserializing element $i", e)
                }
            }
            val newValue = state.apply(existing)
            analyzer.releaseState(state)
            return newValue
        }

        override fun serialize(value: MutableList<Any?>): RefNode {
            val state = analyzer.getState()
            val node = ArrayNode()
            state.populate(value)
            state.buffer.forEach {
                if(it == null)
                    node.add(NullNode)
                else
                    node.add(analyzer.elementSerializer.write(it))
            }
            analyzer.releaseState(state)
            return node
        }
    }
}
