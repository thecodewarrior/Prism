package dev.thecodewarrior.prism.format.reference.builtin.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.RefractClass
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FallbackSerializerFactory
import dev.thecodewarrior.prism.format.reference.builtin.ObjectSerializerFactory
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class BaseObjectTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism<ReferenceSerializer<*>>().also { prism ->
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

    @Test
    fun getSerializer_withArray_shouldReturnFallbackSerializer() {
        val serializer = prism[Mirror.reflect<Array<String>>()].value
        assertEquals(FallbackSerializerFactory.FallbackSerializer::class.java, serializer.javaClass)
    }
}
