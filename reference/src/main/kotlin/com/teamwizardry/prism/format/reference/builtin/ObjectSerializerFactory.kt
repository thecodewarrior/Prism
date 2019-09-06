package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.ClassMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.DeserializationException
import com.teamwizardry.prism.annotation.RefractClass
import com.teamwizardry.prism.base.analysis.auto.ObjectAnalyzer
import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.ReferenceSerializerFactory
import com.teamwizardry.prism.format.reference.format.NullNode
import com.teamwizardry.prism.format.reference.format.ObjectNode
import com.teamwizardry.prism.format.reference.format.RefNode

open class ObjectSerializerFactory(prism: ReferencePrism<*>): ReferenceSerializerFactory(prism, Mirror.reflect<Any>(), {
    (it as ClassMirror).annotations.any { it is RefractClass }
}) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return ObjectSerializer(prism, mirror)
    }

    class ObjectSerializer(prism: ReferencePrism<*>, type: TypeMirror): ReferenceSerializer<Any>(type) {
        val analyzer = ObjectAnalyzer<Any?, ReferenceSerializer<*>>(prism, type.asClassMirror())

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: Any?): Any {
            val state = analyzer.getState()
            state.clear()
            val node = node as? ObjectNode ?: throw DeserializationException("Object serializer expects an ObjectNode")
            analyzer.properties.forEach { property ->
                val existingProperty = existing?.let { property.getValue(it) }
                state.setValue(property, property.serializer.read(node[property.name]!!, existingProperty))
            }
            val newValue = state.apply(existing)
            analyzer.releaseState(state)
            return newValue
        }

        override fun serialize(value: Any): RefNode {
            val state = analyzer.getState()
            val node = ObjectNode()
            state.populate(value)
            state.values.forEach { (property, propertyValue) ->
                val v = propertyValue.value
                if(propertyValue.isPresent) {
                    if(v == null)
                        node[property.name] = NullNode
                    else
                        node[property.name] = property.serializer.write(v)
                }
            }
            analyzer.releaseState(state)
            return node
        }
    }
}
