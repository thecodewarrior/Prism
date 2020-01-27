package dev.thecodewarrior.prism.format.reference.builtin.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FallbackSerializerFactory
import dev.thecodewarrior.prism.format.reference.builtin.ObjectSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class MutableObjectTest: PrismTest() {
    override fun createPrism(): ReferencePrism<*> = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ObjectSerializerFactory(prism))
    }

    private class NonAnnotatedClass
    @RefractClass
    private class AnnotatedClass

    @Test
    fun getSerializer_withNonAnnotatedClass_shouldReturnFallbackSerializer() {
        val serializer = prism[Mirror.reflect<NonAnnotatedClass>()].value
        assertEquals(FallbackSerializerFactory.FallbackSerializer::class.java, serializer.javaClass)
    }

    @Test
    fun getSerializer_withAnnotatedClass_shouldReturnObjectSerializer() {
        val serializer = prism[Mirror.reflect<AnnotatedClass>()].value
        assertEquals(ObjectSerializerFactory.ObjectSerializer::class.java, serializer.javaClass)
        assertSame(Mirror.reflect<AnnotatedClass>(), serializer.type)
    }

    @RefractClass
    private class EmptyObject @RefractConstructor constructor() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EmptyObject) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    @Test
    fun serialize_withEmptyClass_shouldReturnEmptyNode() {
        val theObject = EmptyObject()
        val node = prism[Mirror.reflect<EmptyObject>()].value.write(theObject)
        assertEquals(ObjectNode(), node)
    }

    @Test
    fun deserialize_withEmptyClass_andExistingObject_shouldReturnSameInstance() {
        val theObject = EmptyObject()
        val node = prism[Mirror.reflect<EmptyObject>()].value.read(ObjectNode(), theObject)
        assertSame(theObject, node)
    }

    @RefractClass
    private class NoRefractingFields {
        val nonRefracting: Int = 0
    }

    @Test
    fun serialize_withNoRefractingFields_shouldReturnEmptyNode() {
        val theObject = NoRefractingFields()
        val node = prism[Mirror.reflect<NoRefractingFields>()].value.write(theObject)
        assertEquals(ObjectNode(), node)
    }

    @RefractClass
    private data class RefractingFields(@Refract var refracting: Int)

    @Test
    fun serialize_withRefractingFields_shouldReturnNodeWithKeysForProperties() {
        val theObject = RefractingFields(1)
        val node = prism[Mirror.reflect<RefractingFields>()].value.write(theObject)
        assertEquals(ObjectNode.build {
            "refracting" *= 1
        }, node)
    }

    @Test
    fun deserialize_withRefractingFields_andExistingObject_shouldReturnModifiedObject() {
        val node = ObjectNode.build {
            "refracting" *= 5
        }
        val theObject = RefractingFields(0)
        val theReturnedObject = prism[Mirror.reflect<RefractingFields>()].value.read(node, theObject)
        assertSame(theObject, theReturnedObject)
        assertEquals(RefractingFields(5), theObject)
    }
}
