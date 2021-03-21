package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.testsupport.IdentityPrism
import dev.thecodewarrior.prism.testsupport.assertInstanceOf
import dev.thecodewarrior.reflectcase.ReflectTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class PropertyScannerTest: ReflectTest(
    "dev.thecodewarrior.prism.annotation.*"
) {
    // # Basics
    @Test
    fun `a class with no @Refract annotated fields or methods should have no properties`() {
        val X by sources.add(
            "X", """
            class X {
                int field;
                int getValue() { NOP; }
            }
            """.trimIndent()
        )
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X))
        assertEquals(0, properties.size)
    }

    @Test
    fun `fields should be scanned to correctly-configured FieldProperty properties`() {
        val X by sources.add(
            "X", """
            class X {
                @Refract("immutable") final int immutableField = 0;
                @Refract("mutable") int mutableField;
            }
            """.trimIndent()
        )
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            {
                val property = assertInstanceOf<FieldProperty<*>>(properties[0])
                assertEquals("immutable", property.name)
                assertTrue(property.isImmutable)
                assertEquals(Mirror.types.int, property.type)
            },
            {
                val property = assertInstanceOf<FieldProperty<*>>(properties[1])
                assertEquals("mutable", property.name)
                assertFalse(property.isImmutable)
                assertEquals(Mirror.types.int, property.type)
            }
        )
    }

    @Test
    fun `getters and setters should be scanned to correctly-configured AccessorProperty properties`() {
    }

    @Test
    fun `kotlin properties with backing fields should be scanned to correctly-configured AccessorProperty properties`() {
    }

    @Test
    fun `kotlin properties without backing fields should be scanned to correctly-configured AccessorProperty properties`() {
    }

    @Test
    fun `@JvmField kotlin properties should be scanned to correctly-configured FieldProperty properties`() {
    }

    @Test
    fun `directly annotated backing fields property should be scanned to correctly-configured FieldProperty properties`() {
    }

    @Test
    fun `all three property types should be able to coexist without conflicts`() {
    }

    // # @RefractImmutable
    @Test
    fun `adding '@RefractImmutable' to a non-final field should mark the resulting property as immutable`() {
    }

    @Test
    fun `adding '@RefractImmutable' to a final field should throw an error`() {
    }

    @Test
    fun `adding '@RefractImmutable' to a kotlin 'var' should mark the resulting property as immutable`() {
    }

    @Test
    fun `adding '@RefractImmutable' to a kotlin 'val' should throw an error`() {
    }

    // # @RefractMutable
    @Test
    fun `adding '@RefractMutable' to a final field should mark the resulting property as mutable`() {
    }

    @Test
    fun `adding '@RefractMutable' to a non-final field should throw an error`() {
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'val' with a backing field should return a HybridMutableProperty`() {
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'val' without a backing field should throw an error`() {
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'var' should throw an error`() {
    }

    // # Empty names - should these be allowed?
    // a field with an empty name
    // a kotlin property with an empty name
    // a getter/setter with an empty name

    // # Error cases
    // a setter with no getter
    // duplicate fields // specific error?
    // duplicate setters
    // duplicate getters
    // duplicate properties
    // duplicate field+getter/setter, field+property, getter/setter+property
    // mismatched getter/setter types
    // wrong signature for getter or setter (getter with params, setter with no params, setter with return)

    // # Error reporting
    // a single error
    // multiple errors
    // errors only during the initial scan
    // errors only when resolving candidates
    // errors during the initial scan *and* when resolving candidates

    @Test
    fun scan() {
    }
}