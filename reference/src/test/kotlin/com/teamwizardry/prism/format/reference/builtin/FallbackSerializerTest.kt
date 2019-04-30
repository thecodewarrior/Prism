package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.SerializerNotFoundException
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

internal class FallbackSerializerTest: PrismTest() {
    override fun createPrism(): Prism<ReferenceSerializer<*>> = Prism<ReferenceSerializer<*>>()
        .register(FallbackSerializer)

    @Test
    fun getSerializer_withObject_shouldReturnFallback() {
        assertSame(FallbackSerializer, prism[Mirror.types.any].value)
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
        assertEquals(LeafNode(theObject), leaf)
    }

    @Test
    fun deserialize_withLeaf_shouldReturnObject() {
        val theObject = Any()
        val theLeaf = LeafNode(theObject)
        val value = prism[Mirror.types.any].value.read(theLeaf, null)
        assertEquals(theObject, value)
    }
}