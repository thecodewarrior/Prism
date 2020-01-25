package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.base.analysis.auto.InvalidRefractSignatureException
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    data class FullConstructor @RefractConstructor constructor(@field:Refract var refracting: Int)

    @Test
    fun deserialize_withFullConstructor_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<FullConstructor>()].value
        val theObject = FullConstructor(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class ConstructorAnnotationNames(@field:Refract var refracting: Int, var unused: Int) {
        @RefractConstructor(["refracting"])
        constructor(differentName: Int) : this(differentName, 1)
    }

    @Test
    fun deserialize_withConstructorAnnotationNames_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<ConstructorAnnotationNames>()].value
        val theObject = ConstructorAnnotationNames(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class PartialConstructor(@field:Refract var refracting: Int, @field:Refract var secondRefractingField: Int) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, 2)
    }

    @Test
    fun deserialize_withPartialConstructor_andNoExistingObject_shouldCallPartialConstructor() {
        val serializer = prism[Mirror.reflect<PartialConstructor>()].value
        val theObject = PartialConstructor(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class MultipleConstructors @RefractConstructor constructor(@field:Refract var refracting: Int, @field:Refract var usedPrimary: Boolean) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, false)
    }

    @Test
    fun deserialize_withMultipleConstructors_andNoExistingObject_shouldCallFullConstructor() {
        val serializer = prism[Mirror.reflect<MultipleConstructors>()].value
        val theObject = MultipleConstructors(1, true)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    data class RefractingConstructor(@field:Refract var refracting: Int, @field:Refract var secondRefractingField: Int) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, 2)
    }

    @RefractClass
    class MistypedConstructorParameters @RefractConstructor constructor(intField: Int, booleanField: Int) {
        @field:Refract var intField: Int = 0
        @field:Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMistypedConstructorParameters_shouldThrow() {
        val e = assertThrows<InvalidRefractSignatureException>() {
            prism[Mirror.reflect<MistypedConstructorParameters>()].value
        }
        assertEquals("Some constructor parameters have types that aren't equal to their corresponding property: " +
            "[booleanField]", e.message)
    }

    @RefractClass
    class SubtypedConstructorParameters @RefractConstructor constructor(intField: Int, listField: ArrayList<String>) {
        @field:Refract var intField: Int = 0
        @field:Refract var listField: List<String>? = null
    }

    @Test
    fun getSerializer_withSubtypedConstructorParameters_shouldThrow() {
        val e = assertThrows<InvalidRefractSignatureException>() {
            prism[Mirror.reflect<SubtypedConstructorParameters>()].value
        }
        assertEquals("Some constructor parameters have types that aren't equal to their corresponding property: " +
            "[listField]", e.message)
    }

    @RefractClass
    class MisnamedConstructorParameters @RefractConstructor constructor(intField: Int, oopsField: Boolean) {
        @field:Refract var intField: Int = 0
        @field:Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMisnamedConstructorParameters_shouldThrow() {
        val e = assertThrows<InvalidRefractSignatureException> {
            prism[Mirror.reflect<MisnamedConstructorParameters>()].value
        }
        assertEquals("Some constructor parameter names have no corresponding property: [oopsField]", e.message)
    }

    @RefractClass
    class MisnamedAnnotationParameters @RefractConstructor(["intField", "whoops"]) constructor(arg1: Int, arg2: Boolean) {
        @field:Refract var intField: Int = 0
        @field:Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMisnamedAnnotationParameters_shouldThrow() {
        val e = assertThrows<InvalidRefractSignatureException> {
            prism[Mirror.reflect<MisnamedAnnotationParameters>()].value
        }
        assertEquals("Some constructor parameter names have no corresponding property: [whoops]", e.message)
    }
}
