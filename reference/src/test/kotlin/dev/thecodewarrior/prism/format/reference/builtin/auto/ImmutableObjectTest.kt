package dev.thecodewarrior.prism.format.reference.builtin.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FallbackSerializerFactory
import dev.thecodewarrior.prism.format.reference.builtin.ObjectSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.format.RefNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ImmutableObjectTest: PrismTest() {
    override fun createPrism(): ReferencePrism<*> = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ObjectSerializerFactory(prism))

        prism.register(object : ReferenceSerializer<MutableType>() {
            override fun deserialize(node: RefNode, existing: MutableType?): MutableType {
                node as? LeafNode ?: throw DeserializationException("MutableType serializer expects a leaf node")
                val value = node.value as Int
                return existing?.also { it.value = value } ?: MutableType(value)
            }

            override fun serialize(value: MutableType): RefNode {
                return LeafNode(value.value)
            }
        })
        prism.register(object : ReferenceSerializer<ImmutableType>() {
            override fun deserialize(node: RefNode, existing: ImmutableType?): ImmutableType {
                node as? LeafNode ?: throw DeserializationException("MutableType serializer expects a leaf node")
                return ImmutableType(node.value as Int)
            }

            override fun serialize(value: ImmutableType): RefNode {
                return LeafNode(value.value)
            }
        })
    }

    private data class MutableType(var value: Int)
    private data class ImmutableType(var value: Int)

    @RefractClass
    private data class MutableFieldImmutableType(private var _field: ImmutableType) {
        var setterCalls = 0
        @Refract
        var field: ImmutableType
            get() = _field
            set(value) {
                setterCalls++
                _field = value
            }
    }

    @Test
    fun deserialize_withMutableField_andImmutableType_shouldAssignNewValue() {
        val node = ObjectNode.build {
            "field" *= 2
        }
        val theObject = MutableFieldImmutableType(ImmutableType(1))
        val theReturnedObject = prism[Mirror.reflect<MutableFieldImmutableType>()].value.read(node, theObject)
        Assertions.assertSame(theObject, theReturnedObject)
        theReturnedObject as MutableFieldImmutableType
        assertEquals(ImmutableType(2), theReturnedObject.field)
        assertEquals(1, theReturnedObject.setterCalls)
    }

}
