package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.mirror.Mirror
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.annotation.Refract
import com.teamwizardry.prism.annotation.RefractClass
import com.teamwizardry.prism.format.reference.ReferencePrism
import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.ObjectNode
import com.teamwizardry.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class ObjectSerializerFactoryTest: PrismTest() {
    override fun createPrism(): ReferencePrism<*> = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ObjectSerializerFactory(prism))
    }

    class NonAnnotatedClass
    @RefractClass
    class AnnotatedClass

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
    class EmptyObject

    @Test
    fun serialize_withEmptyClass_shouldReturnEmptyObject() {
        val theObject = EmptyObject()
        val node = prism[Mirror.reflect<EmptyObject>()].value.write(theObject)
        assertEquals(ObjectNode(), node)
    }

    @RefractClass
    class NoRefractingFields {
        val nonRefracting: Int = 0
    }

    @Test
    fun serialize_withNoRefractingFields_shouldReturnEmptyObject() {
        val theObject = NoRefractingFields()
        val node = prism[Mirror.reflect<NoRefractingFields>()].value.write(theObject)
        assertEquals(ObjectNode(), node)
    }

    @RefractClass
    data class RefractingFields(@Refract var refracting: Int)

    @Test
    fun serialize_withRefractingFields_shouldReturnObjectWithKeysForProperties() {
        val theObject = RefractingFields(0)
        val node = prism[Mirror.reflect<RefractingFields>()].value.write(theObject)
        assertEquals(ObjectNode.build {
            "refracting" *= 0
        }, node)
    }

    @Test
    fun deserialize_withRefractingFields_andExistingObject_shouldReturnEmptyObject() {
        val node = ObjectNode.build {
            "refracting" *= 5
        }
        val theObject = prism[Mirror.reflect<RefractingFields>()].value.read(node, RefractingFields(0))
        assertEquals(RefractingFields(5), theObject)
    }
}
