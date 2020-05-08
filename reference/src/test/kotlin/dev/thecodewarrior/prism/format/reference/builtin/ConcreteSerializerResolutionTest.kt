package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.SerializerNotFoundException
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.ReferenceSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.RefNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ConcreteSerializerResolutionTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism()

    @Test
    fun getSerializer_withExactConcreteSerializer_shouldReturnConcreteSerializer() {
        class X
        val serializer = object: NopSerializer<X>() {}
        prism.register(
            serializer
        )
        assertSame(serializer, prism[Mirror.reflect<X>()].value)
    }

    @Test
    fun getSerializer_withOnlySuperclassConcreteSerializer_shouldThrow() {
        open class X
        class XSub: X()
        val serializer = object: NopSerializer<X>() {}
        prism.register(
            serializer
        )
        assertThrows<SerializerNotFoundException> {
            prism[Mirror.reflect<XSub>()].value
        }
    }

    abstract class NopSerializer<T: Any>: ReferenceSerializer<T>() {
        override fun deserialize(node: RefNode, existing: T?): T { null!! }
        override fun serialize(value: T): RefNode { null!! }
    }
}