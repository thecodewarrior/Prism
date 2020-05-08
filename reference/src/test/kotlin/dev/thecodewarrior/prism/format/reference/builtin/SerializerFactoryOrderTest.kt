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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SerializerFactoryOrderTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism<ReferenceSerializer<*>>().also { prism ->
        prism.register(BroadFactory(prism))
        prism.register(NarrowFactory(prism))
    }

    @Test
    fun getSerializer_withBroadType_shouldReturnBroadSerializer() {
        assertEquals(BroadFactory.BroadSerializer::class.java, prism[Mirror.reflect<BroadType>()].value.javaClass)
    }

    @Test
    fun getSerializer_withNarrowType_shouldReturnNarrowSerializer() {
        assertEquals(NarrowFactory.NarrowSerializer::class.java, prism[Mirror.reflect<NarrowType>()].value.javaClass)
    }

    @Test
    fun getSerializer_withNarrowThenBroadType_shouldReturnNarrowThenBroadSerializer() {
        assertEquals(NarrowFactory.NarrowSerializer::class.java, prism[Mirror.reflect<NarrowType>()].value.javaClass)
        assertEquals(BroadFactory.BroadSerializer::class.java, prism[Mirror.reflect<BroadType>()].value.javaClass)
    }

    @Test
    fun getSerializer_withBroadThenNarrowType_shouldReturnBroadThenNarrowSerializer() {
        assertEquals(NarrowFactory.NarrowSerializer::class.java, prism[Mirror.reflect<NarrowType>()].value.javaClass)
        assertEquals(BroadFactory.BroadSerializer::class.java, prism[Mirror.reflect<BroadType>()].value.javaClass)
    }

    open class BroadType
    class NarrowType: BroadType()

    class BroadFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<BroadType>()) {
        override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
            return BroadSerializer()
        }

        class BroadSerializer: ReferenceSerializer<Any>() {
            override fun deserialize(node: RefNode, existing: Any?): Any {
                null!!
            }

            override fun serialize(value: Any): RefNode {
                null!!
            }
        }
    }

    class NarrowFactory(prism: ReferencePrism): ReferenceSerializerFactory(prism, Mirror.reflect<NarrowType>()) {
        override fun create(mirror: TypeMirror): ReferenceSerializer<*> {
            return NarrowSerializer()
        }

        class NarrowSerializer: ReferenceSerializer<Any>() {
            override fun deserialize(node: RefNode, existing: Any?): Any {
                null!!
            }

            override fun serialize(value: Any): RefNode {
                null!!
            }
        }
    }
}