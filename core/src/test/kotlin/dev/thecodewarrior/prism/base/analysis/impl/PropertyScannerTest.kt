package dev.thecodewarrior.prism.base.analysis.impl

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.mirror.type.TypeMirror
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractGetter
import dev.thecodewarrior.prism.annotation.RefractImmutable
import dev.thecodewarrior.prism.annotation.RefractMutable
import dev.thecodewarrior.prism.testsupport.IdentityPrism
import dev.thecodewarrior.prism.testsupport.IdentitySerializer
import dev.thecodewarrior.prism.testsupport.assertInstanceOf
import dev.thecodewarrior.reflectcase.ReflectTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@Suppress("LocalVariableName")
internal class PropertyScannerTest: ReflectTest(
    "dev.thecodewarrior.prism.annotation.*"
) {
    /**
     * Asserts that a property is properly configured. Returns a callback suitable for [assertAll].
     */
    inline fun <reified T: ObjectProperty<*>> assertProperty(
        actual: ObjectProperty<*>,
        name: String,
        immutable: Boolean,
        type: TypeMirror? = null,
        serializer: IdentitySerializer? = null
    ): () -> Unit {
        return {
            assertInstanceOf<T>(actual)
            assertEquals(name, actual.name)
            assertEquals(immutable, actual.isImmutable)
            if (type != null)
                assertEquals(type, actual.type)
            if (serializer != null)
                assertEquals(serializer, actual.serializer)
        }
    }

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
            assertProperty<FieldProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<FieldProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `getters and setters should be scanned to correctly-configured AccessorProperty properties`() {
        val X by sources.add(
            "X", """
            class X {
                @RefractGetter("immutable") int getImmutableField() { NOP; }
                @RefractGetter("mutable") int getMutableField() { NOP; }
                @RefractSetter("mutable") void setMutableField(int value) { NOP; }
            }
            """.trimIndent()
        )
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            assertProperty<AccessorProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<AccessorProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `kotlin properties with backing fields should be scanned to correctly-configured AccessorProperty properties`() {
        class X {
            @Refract("immutable")
            val immutable: Int = 0

            @Refract("mutable")
            var mutable: Int = 0
        }
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            assertProperty<AccessorProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<AccessorProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `kotlin properties without backing fields should be scanned to correctly-configured AccessorProperty properties`() {
        class X {
            @Refract("immutable")
            val immutable: Int
                get() = 0

            @Refract("mutable")
            var mutable: Int
                get() = 0
                set(value) {}
        }
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            assertProperty<AccessorProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<AccessorProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `@JvmField kotlin properties should be scanned to correctly-configured FieldProperty properties`() {
        class X {
            @Refract("immutable")
            @JvmField
            val immutable: Int = 0

            @Refract("mutable")
            @JvmField
            var mutable: Int = 0
        }
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            assertProperty<FieldProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<FieldProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `directly annotating a kotlin property's backing field should scan it to a correctly-configured FieldProperty`() {
        class X {
            @field:Refract("immutable")
            val immutable: Int = 0

            @field:Refract("mutable")
            var mutable: Int = 0
        }
        sources.compile()
        // sort by name so they're in [immutable, mutable] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java)).sortedBy { it.name }
        assertEquals(2, properties.size)
        assertAll(
            assertProperty<FieldProperty<*>>(
                properties[0],
                name = "immutable",
                immutable = true,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
            assertProperty<FieldProperty<*>>(
                properties[1],
                name = "mutable",
                immutable = false,
                type = Mirror.types.int,
                serializer = IdentityPrism[Mirror.types.int].value
            ),
        )
    }

    @Test
    fun `all three property types should be able to coexist without conflicts`() {
        class X {
            @RefractGetter("accessor")
            fun accessor(): Int = 0

            @field:Refract("field")
            val field: Int = 0

            @Refract("property")
            val property: Int = 0
        }
        sources.compile()
        // sort by name so they're in [accessor, field, property] order
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java)).sortedBy { it.name }
        assertEquals(3, properties.size)
        assertAll(
            assertProperty<AccessorProperty<*>>(
                properties[0],
                name = "accessor",
                immutable = true,
            ),
            assertProperty<FieldProperty<*>>(
                properties[1],
                name = "field",
                immutable = true,
            ),
            assertProperty<AccessorProperty<*>>(
                properties[2],
                name = "property",
                immutable = true,
            ),
        )
    }

    // # @RefractImmutable
    @Test
    fun `adding '@RefractImmutable' to a non-final field should mark the resulting property as immutable`() {
        val X by sources.add(
            "X", """
            class X {
                @RefractImmutable @Refract("field") int field = 0;
            }
            """.trimIndent()
        )
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X))
        assertEquals(1, properties.size)
        assertTrue(properties[0].isImmutable)
    }

    @Test
    fun `adding '@RefractImmutable' to a final field should throw an error`() {
        val X by sources.add(
            "X", """
            class X {
                @RefractImmutable @Refract("field") final int field = 0;
            }
            """.trimIndent()
        )
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X))
    }

    @Test
    fun `adding '@RefractImmutable' to a kotlin 'var' should mark the resulting property as immutable`() {
        class X {
            @RefractImmutable
            @Refract("property")
            var property: Int = 0
        }
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java))
        assertEquals(1, properties.size)
        assertTrue(properties[0].isImmutable)
    }

    @Test
    fun `adding '@RefractImmutable' to a kotlin 'val' should throw an error`() {
        class X {
            @RefractImmutable
            @Refract("property")
            val property: Int = 0
        }
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java))
    }

    // # @RefractMutable
    @Test
    fun `adding '@RefractMutable' to a final field should mark the resulting property as mutable`() {
        val X by sources.add(
            "X", """
            class X {
                @RefractMutable @Refract("field") final int field = 0;
            }
            """.trimIndent()
        )
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X))
        assertEquals(1, properties.size)
        assertFalse(properties[0].isImmutable)
    }

    @Test
    fun `adding '@RefractMutable' to a non-final field should throw an error`() {
        val X by sources.add(
            "X", """
            class X {
                @RefractMutable @Refract("field") int field = 0;
            }
            """.trimIndent()
        )
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X))
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'val' with a backing field should return a HybridMutableProperty`() {
        class X {
            @RefractMutable
            @Refract("property")
            val property: Int = 0
        }
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java))
        assertEquals(1, properties.size)
        assertInstanceOf<HybridMutableProperty<*>>(properties[0])
        assertFalse(properties[0].isImmutable)
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'val' without a backing field should throw an error`() {
        class X {
            @RefractMutable
            @Refract("property")
            val property: Int
                get() = 0
        }
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java))
    }

    @Test
    fun `adding '@RefractMutable' to a kotlin 'var' should throw an error`() {
        class X {
            @RefractMutable
            @Refract("property")
            var property: Int = 0
        }
        sources.compile()
        val properties = PropertyScanner.scan(IdentityPrism, Mirror.reflectClass(X::class.java))
    }

    // adding both RefractMutable and RefractImmutable should throw an error

    // static fields/methods should throw

    // scanned properties should be alphabetical regardless of the originating class or underlying member name

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