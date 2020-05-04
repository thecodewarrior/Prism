package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.ArrayNode
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ArraySerializerFactoryTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ArraySerializerFactory(prism))
    }

    @Test
    fun getSerializer_shouldReturnArraySerializer() {
        val serializer = prism[Mirror.reflect<Array<String>>()].value
        assertEquals(ArraySerializerFactory.ArraySerializer::class.java, serializer.javaClass)
        assertSame(Mirror.reflect<Array<String>>(), serializer.type)
    }

    @Test
    fun serialize_shouldReturnPopulatedArrayNode() {
        val theArray = arrayOf("first", "second", null, "fourth")
        val node = prism[Mirror.reflect<Array<String?>>()].value.write(theArray)
        assertEquals(ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }, node)
    }

    @Test
    fun deserialize_withArrayNode_andExistingValue_withSameSize_shouldUseExistingValue() {
        val targetArray = arrayOf("first", "second", null, "fourth")

        val theArray = arrayOf<String?>(null, null, null, null)
        val theNode = ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }
        val deserialized = prism[Mirror.reflect<Array<String?>>()].value.read(theNode, theArray)

        assertSame(theArray, deserialized)
        assertEquals(Array<String>::class.java, deserialized.javaClass)
        @Suppress("UNCHECKED_CAST")
        assertArrayEquals(targetArray, deserialized as Array<String?>)
    }

    @Test
    fun deserialize_withArrayNode_andExistingValue_withDifferentSize_shouldCreateNewArray() {
        val targetArray = arrayOf("first", "second", null, "fourth")

        val theArray = arrayOf<String?>(null, null)
        val theNode = ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }
        val deserialized = prism[Mirror.reflect<Array<String?>>()].value.read(theNode, theArray)

        assertNotSame(theArray, deserialized)
        assertEquals(Array<String>::class.java, deserialized.javaClass)
        @Suppress("UNCHECKED_CAST")
        assertArrayEquals(targetArray, deserialized as Array<String?>)
    }

    @Test
    fun deserialize_withArrayNode_andNoExistingValue_shouldCreateNewArray() {
        val targetArray = arrayOf("first", "second", null, "fourth")

        val theNode = ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }
        val deserialized = prism[Mirror.reflect<Array<String?>>()].value.read(theNode, null)

        assertEquals(Array<String>::class.java, deserialized.javaClass)
        @Suppress("UNCHECKED_CAST")
        assertArrayEquals(targetArray, deserialized as Array<String?>)
    }

    @Test
    fun deserialize_withWrongNodeType_shouldThrow() {
        assertThrows<DeserializationException> {
            prism[Mirror.reflect<Array<String?>>()].value.read(LeafNode("whoops!"), null)
        }
    }
}