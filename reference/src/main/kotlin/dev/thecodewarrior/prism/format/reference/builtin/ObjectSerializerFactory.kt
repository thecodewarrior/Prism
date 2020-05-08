package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.base.analysis.auto.ObjectAnalyzer
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.format.RefNode

open class ObjectSerializerFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<Any>(), {
    (it as? ClassMirror)?.annotations?.any { it is RefractClass } == true
}) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return ObjectSerializer(prism, mirror)
    }

    class ObjectSerializer(prism: ReferencePrism, type: TypeMirror): ReferenceSerializer<Any>(type) {
        val analyzer = ObjectAnalyzer<Any?, ReferenceSerializer<*>>(prism, type.asClassMirror())

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: Any?): Any {
            val state = analyzer.getState()
            node as? ObjectNode ?: throw DeserializationException("Object serializer expects an ObjectNode")
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
