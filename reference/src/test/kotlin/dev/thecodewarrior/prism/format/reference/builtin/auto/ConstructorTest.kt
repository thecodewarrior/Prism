package dev.thecodewarrior.prism.format.reference.builtin.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.InstantiationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.base.analysis.auto.InvalidRefractSignatureException
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FallbackSerializerFactory
import dev.thecodewarrior.prism.format.reference.builtin.ObjectSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import dev.thecodewarrior.prism.format.reference.testsupport.assertCause
import dev.thecodewarrior.prism.format.reference.testsupport.assertMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ConstructorTest: PrismTest() {
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
    private class NonAnnotatedConstructor {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NonAnnotatedConstructor) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    @Test
    fun deserialize_withNonAnnotatedConstructor_shouldThrow() {
        assertThrows<DeserializationException> {
            prism[Mirror.reflect<NonAnnotatedConstructor>()].value.read(ObjectNode(), null)
        }.assertCause<InstantiationException>().assertMessage("No instantiators exist")
    }

    @RefractClass
    private class AnnotatedConstructor @RefractConstructor constructor() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is AnnotatedConstructor) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    @Test
    fun deserialize_withEmptyClass_andNoExistingObject_shouldReturnNewInstance() {
        val theObject = prism[Mirror.reflect<AnnotatedConstructor>()].value.read(ObjectNode(), null)
        assertEquals(AnnotatedConstructor(), theObject)
    }

    @RefractClass
    private data class FullConstructor @RefractConstructor constructor(@Refract var refracting: Int)

    @Test
    fun deserialize_withFullConstructor_andNoExistingObject_shouldReturnNewInstance() {
        val serializer = prism[Mirror.reflect<FullConstructor>()].value
        val theObject = FullConstructor(1)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    private data class ConstructorAnnotationNames(@Refract var refracting: Int, var unused: Int) {
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
    private data class PartialConstructor(@Refract var refracting: Int, @Refract var secondRefractingField: Int) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, 2)
    }

    @Test
    fun deserialize_withPartialConstructor_shouldCallPartialConstructorAndSetProperty() {
        val serializer = prism[Mirror.reflect<PartialConstructor>()].value
        val theObject = PartialConstructor(1, 3)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    private data class MultipleConstructors @RefractConstructor constructor(@Refract var refracting: Int, @Refract var usedPrimary: Boolean) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, false)
    }

    @Test
    fun deserialize_withMultipleConstructors_shouldCallFullConstructor() {
        val serializer = prism[Mirror.reflect<MultipleConstructors>()].value
        val theObject = MultipleConstructors(1, true)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    private data class NoAppropriateConstructors @RefractConstructor constructor(@Refract var propA: Int, @Refract var propB: Int) {
        @Refract var propC: Int = 0
    }

    @Test
    fun deserialize_withNoAppropriateConstructors_shouldCallFullConstructor() {
        val serializer = prism[Mirror.reflect<MultipleConstructors>()].value
        val theObject = MultipleConstructors(1, true)
        val node = serializer.write(theObject)
        val theDeserializedObject = serializer.read(node, null)
        assertEquals(theObject, theDeserializedObject)
    }

    @RefractClass
    private data class RefractingConstructor(@Refract var refracting: Int, @Refract var secondRefractingField: Int) {
        @RefractConstructor
        constructor(refracting: Int): this(refracting, 2)
    }

    @RefractClass
    private class MistypedConstructorParameters @RefractConstructor constructor(intField: Int, booleanField: Int) {
        @Refract var intField: Int = 0
        @Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMistypedConstructorParameters_shouldThrow() {
        assertThrows<InvalidRefractSignatureException>() {
            prism[Mirror.reflect<MistypedConstructorParameters>()].value
        }.assertMessage("Some constructor parameters have types that aren't equal to their corresponding property: " +
            "[booleanField]")
    }

    @RefractClass
    private class SubtypedConstructorParameters @RefractConstructor constructor(intField: Int, listField: ArrayList<String>) {
        @Refract var intField: Int = 0
        @Refract var listField: List<String>? = null
    }

    @Test
    fun getSerializer_withSubtypedConstructorParameters_shouldThrow() {
        assertThrows<InvalidRefractSignatureException>() {
            prism[Mirror.reflect<SubtypedConstructorParameters>()].value
        }.assertMessage("Some constructor parameters have types that aren't equal to their corresponding property: " +
            "[listField]")
    }

    @RefractClass
    private class MisnamedConstructorParameters @RefractConstructor constructor(intField: Int, oopsField: Boolean) {
        @Refract var intField: Int = 0
        @Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMisnamedConstructorParameters_shouldThrow() {
        assertThrows<InvalidRefractSignatureException> {
            prism[Mirror.reflect<MisnamedConstructorParameters>()].value
        }.assertMessage("Some constructor parameter names have no corresponding property: [oopsField]")
    }

    @RefractClass
    private class MisnamedAnnotationParameters @RefractConstructor(["intField", "whoops"]) constructor(arg1: Int, arg2: Boolean) {
        @Refract var intField: Int = 0
        @Refract var booleanField: Boolean = false
    }

    @Test
    fun getSerializer_withMisnamedAnnotationParameters_shouldThrow() {
        assertThrows<InvalidRefractSignatureException> {
            prism[Mirror.reflect<MisnamedAnnotationParameters>()].value
        }.assertMessage("Some constructor parameter names have no corresponding property: [whoops]")
    }
}
