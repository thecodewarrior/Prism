package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.SerializerNotFoundException
import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FallbackSerializerFactoryTest: PrismTest() {
    override fun createPrism(): ReferencePrism<*> = Prism<ReferenceSerializer<*>>().also { prism ->
        prism.register(FallbackSerializerFactory(prism))
    }

    @Test
    fun getSerializer_withObject_shouldReturnFallback() {
        assertEquals(FallbackSerializerFactory.FallbackSerializer::class.java, prism[Mirror.types.any].value.javaClass)
    }

    @Test
    fun getSerializer_withPrimitive_shouldThrow() {
        assertThrows<SerializerNotFoundException> {
            prism[Mirror.types.int]
        }
    }

    @Test
    fun serialize_withObject_shouldReturnLeaf() {
        val theObject = Any()
        val leaf = prism[Mirror.types.any].value.write(theObject)
        assertEquals(LeafNode(FallbackValue(theObject)), leaf)
    }

    @Test
    fun deserialize_withLeaf_shouldReturnObject() {
        val theObject = Any()
        val theLeaf = LeafNode(FallbackValue(theObject))
        val value = prism[Mirror.types.any].value.read(theLeaf, null)
        assertEquals(theObject, value)
    }

    @Test
    fun deserialize_withWrongNodeType_shouldThrow() {
    }
}