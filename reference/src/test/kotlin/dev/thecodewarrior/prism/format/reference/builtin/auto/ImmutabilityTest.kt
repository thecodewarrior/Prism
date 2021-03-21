package dev.thecodewarrior.prism.format.reference.builtin.auto

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.InstantiationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.annotation.Refract
import dev.thecodewarrior.prism.annotation.RefractConstructor
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.builtin.FallbackSerializerFactory
import dev.thecodewarrior.prism.format.reference.builtin.ObjectSerializerFactory
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.format.RefNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import dev.thecodewarrior.prism.format.reference.testsupport.assertCause
import dev.thecodewarrior.prism.format.reference.testsupport.assertMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ImmutabilityTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ObjectSerializerFactory(prism))

        prism.register(object : ReferenceSerializer<MutableType>() {
            override fun deserialize(node: RefNode, existing: MutableType?): MutableType {
                node as? LeafNode ?: throw DeserializationException("MutableType serializer expects a leaf node")
                val value = node.value as Int
                return existing?.also { it.value = value } ?: MutableType(value)
            }

            override fun serialize(value: MutableType): RefNode {
                return LeafNode(value.value)
            }
        })
        prism.register(object : ReferenceSerializer<ImmutableType>() {
            override fun deserialize(node: RefNode, existing: ImmutableType?): ImmutableType {
                node as? LeafNode ?: throw DeserializationException("ImmutableType serializer expects a leaf node")
                val value = node.value as Int
                return ImmutableType(value)
            }

            override fun serialize(value: ImmutableType): RefNode {
                return LeafNode(value.value)
            }
        })
        prism.register(object : ReferenceSerializer<SmartType>() {
            override fun deserialize(node: RefNode, existing: SmartType?): SmartType {
                node as? LeafNode ?: throw DeserializationException("SmartType serializer expects a leaf node")
                val value = node.value as Int
                return if(existing?.value == value) existing else SmartType(value)
            }

            override fun serialize(value: SmartType): RefNode {
                return LeafNode(value.value)
            }
        })
    }

    /** An object that is mutated in the serializer, only creating a new instance when there is no existing one */
    private data class MutableType(var value: Int)
    /** An object that is created in the serializer every time */
    private data class ImmutableType(val value: Int)
    /** An object that is created in the serializer only if [value] needs to change */
    private data class SmartType(val value: Int)

    @Refract
    private data class MutableFieldImmutableType(private var _field: ImmutableType) {
        var setterCalls = 0
        @Refract
        var field: ImmutableType
            get() = _field
            set(value) {
                setterCalls++
                _field = value
            }
    }

    @Test
    fun deserialize_withMutableField_andImmutableType_shouldAssignNewValue() {
        val node = ObjectNode.build {
            "field" *= 2
        }
        val original = MutableFieldImmutableType(ImmutableType(1))
        val deserialized = prism[Mirror.reflect<MutableFieldImmutableType>()].value.read(node, original)
        assertSame(original, deserialized)
        deserialized as MutableFieldImmutableType
        assertEquals(ImmutableType(2), deserialized.field)
        assertEquals(1, deserialized.setterCalls)
    }


    @Refract
    private data class MutableFieldSmartType(private var _field: SmartType) {
        var setterCalls = 0
        @Refract
        var field: SmartType
            get() = _field
            set(value) {
                setterCalls++
                _field = value
            }
    }

    @Test
    fun deserialize_withMutableField_andChangingSmartType_shouldAssignNewValue() {
        val node = ObjectNode.build {
            "field" *= 2
        }
        val original = MutableFieldSmartType(SmartType(1))
        val deserialized = prism[Mirror.reflect<MutableFieldSmartType>()].value.read(node, original)
        assertSame(original, deserialized)
        deserialized as MutableFieldSmartType
        assertEquals(SmartType(2), deserialized.field)
        assertEquals(1, deserialized.setterCalls)
    }

    @Test
    fun deserialize_withMutableField_andUnchangedSmartType_shouldNotCallSetter() {
        val node = ObjectNode.build {
            "field" *= 1
        }
        val original = MutableFieldSmartType(SmartType(1))
        val deserialized = prism[Mirror.reflect<MutableFieldSmartType>()].value.read(node, original)
        assertSame(original, deserialized)
        deserialized as MutableFieldSmartType
        assertSame(original.field, deserialized.field)
        assertEquals(0, deserialized.setterCalls)
    }

    @Refract
    private data class ImmutableFieldImmutableType(private var _field: ImmutableType) {
        @Refract
        val field: ImmutableType
            get() = _field
    }

    @Test
    fun deserialize_withImmutableField_andImmutableType_shouldThrow() {
        val node = ObjectNode.build {
            "field" *= 1
        }
        val original = ImmutableFieldImmutableType(ImmutableType(1))
        assertThrows<DeserializationException> {
            prism[Mirror.reflect<ImmutableFieldImmutableType>()].value.read(node, original)
        }.assertMessage("Error deserializing dev.thecodewarrior.prism.format.reference.builtin.auto" +
            ".ImmutabilityTest.ImmutableFieldImmutableType")
            .assertCause<InstantiationException>().assertMessage("No instantiators exist")
    }

    @Refract
    private data class ImmutableFieldSmartType(private var _field: SmartType) {
        @Refract
        val field: SmartType
            get() = _field
    }

    @Test
    fun deserialize_withImmutableField_andChangingSmartType_shouldThrow() {
        val node = ObjectNode.build {
            "field" *= 2
        }
        val original = ImmutableFieldSmartType(SmartType(1))
        assertThrows<DeserializationException> {
            prism[Mirror.reflect<ImmutableFieldSmartType>()].value.read(node, original)
        }.assertMessage("Error deserializing dev.thecodewarrior.prism.format.reference.builtin.auto" +
            ".ImmutabilityTest.ImmutableFieldSmartType")
            .assertCause<InstantiationException>().assertMessage("No instantiators exist")
    }

    @Test
    fun deserialize_withImmutableField_andUnchangedSmartType_shouldSucceed() {
        val node = ObjectNode.build {
            "field" *= 1
        }
        val original = ImmutableFieldSmartType(SmartType(1))
        val deserialized = prism[Mirror.reflect<ImmutableFieldSmartType>()].value.read(node, original)
        assertSame(original, deserialized)
        deserialized as ImmutableFieldSmartType
        assertSame(original.field, deserialized.field)
    }
    @Refract
    private data class PostMutateOrder @RefractConstructor constructor(@Refract val field: Int) {
        @Refract
        var someProperty: Int = 0
    }

    @Test
    fun `deserializing with an immutable property should set mutable properties based on deltas with the new object`() {
        val node = ObjectNode.build {
            "field" *= 2
            "someProperty" *= 2
        }
        val existing = PostMutateOrder(1)
        existing.someProperty = 2 // if the serializer bases the required properties
        val deserialized = prism[Mirror.reflect<PostMutateOrder>()].value.read(node, existing)
        assertNotSame(existing, deserialized)
        deserialized as PostMutateOrder
        assertEquals(2, deserialized.field)
        assertEquals(2, deserialized.someProperty)
    }
}
