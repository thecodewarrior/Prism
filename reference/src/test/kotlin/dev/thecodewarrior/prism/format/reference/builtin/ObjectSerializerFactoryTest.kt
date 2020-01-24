package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
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
    class EmptyObject @RefractConstructor constructor() {
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
    fun serialize_withEmptyClass_shouldReturnEmptyObject() {
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

    @Test
    fun deserialize_withEmptyClass_andNoExistingObject_shouldReturnNewInstance() {
        val theObject = prism[Mirror.reflect<EmptyObject>()].value.read(ObjectNode(), null)
        assertEquals(EmptyObject(), theObject)
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
    data class RefractingFields(@field:Refract var refracting: Int)

    @Test
    fun serialize_withRefractingFields_shouldReturnObjectWithKeysForProperties() {
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

    @RefractClass
    data class RefractingFullConstructor @RefractConstructor constructor(@field:Refract var refracting: Int)

    @Test
    fun deserialize_withRefractingFullConstructor_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<RefractingFullConstructor>()].value
        val theObject = RefractingFullConstructor(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class RefractingPartialConstructor(@field:Refract var refracting: Int, @field:Refract var secondRefractingField: Int) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, 2)
    }

    @Test
    fun deserialize_withRefractingPartialConstructor_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<RefractingPartialConstructor>()].value
        val theObject = RefractingPartialConstructor(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class RefractingMultipleConstructor @RefractConstructor constructor(@field:Refract var refracting: Int, @field:Refract var usedPrimary: Boolean) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, false)
    }

    @Test
    fun deserialize_withRefractingMultipleConstructor_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<RefractingMultipleConstructor>()].value
        val theObject = RefractingMultipleConstructor(1, true)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }
}
