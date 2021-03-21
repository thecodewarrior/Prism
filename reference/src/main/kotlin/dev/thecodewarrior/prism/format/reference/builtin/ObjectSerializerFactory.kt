package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.ClassMirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.base.analysis.ObjectAnalyzer
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.format.RefNode

open class ObjectSerializerFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<Any>(), {
    (it as? ClassMirror)?.annotations?.any { it is Refract } == true
}) {
    override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
        return ObjectSerializer(prism, mirror)
    }

    class ObjectSerializer(prism: ReferencePrism, type: TypeMirror): ReferenceSerializer<Any>(type) {
        val analyzer = ObjectAnalyzer<Any, ReferenceSerializer<*>>(prism, type.asClassMirror())

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(node: RefNode, existing: Any?): Any {
            if(node !is ObjectNode) throw DeserializationException("Object serializer expects an ObjectNode")

            analyzer.getReader(existing).use { reader ->
                reader.properties.forEach { property ->
                    node[property.name]?.also {
                        if(it == NullNode)
                            property.setValue(null)
                        else
                            property.setValue(property.serializer.read(it, property.existing))
                    }
                }
                return reader.build()
            }
        }

        override fun serialize(value: Any): RefNode {
            val node = ObjectNode()
            analyzer.getWriter(value).use { writer ->
                writer.properties.forEach { property ->
                    val propertyValue = property.value
                    if(propertyValue == null)
                        node[property.name] = NullNode
                    else
                        node[property.name] = property.serializer.write(propertyValue)
                }
            }
            return node
        }
    }
}
