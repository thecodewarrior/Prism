package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.ArrayNode
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ListSerializerFactoryTest: PrismTest() {
    override fun createPrism(): ReferencePrism<*> = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(ListSerializerFactory(prism))
    }

// TODO - support plain List using ArrayList as the default
//    @Test
//    fun getSerializer_withList_shouldReturnArrayListSerializer() {
//        val serializer = prism[Mirror.reflect<List<String>>()].value
//        assertEquals(ListSerializerFactory.ListSerializer::class.java, serializer.javaClass)
//        serializer as ListSerializerFactory.ListSerializer
//        assertSame(Mirror.reflect<List<String>>(), serializer.type)
//    }

    @Test
    fun getSerializer_withArrayList_shouldReturnArrayListSerializer() {
        val serializer = prism[Mirror.reflect<ArrayList<String>>()].value
        assertEquals(ListSerializerFactory.ListSerializer::class.java, serializer.javaClass)
        assertSame(Mirror.reflect<ArrayList<String>>(), serializer.type)
    }

    @Test
    fun serialize_withList_shouldReturnPopulatedArrayNode() {
        val theList = ArrayList<String?>()
        theList.addAll(listOf("first", "second", null, "fourth"))
        val node = prism[Mirror.reflect<ArrayList<String?>>()].value.write(theList)
        assertEquals(ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }, node)
    }

    @Test
    fun deserialize_ArrayList_withArrayNode_andExistingValue_shouldClearFillAndReturnList() {
        val targetList = ArrayList<String?>()
        targetList.addAll(listOf("first", "second", null, "fourth"))

        val theList = ArrayList<String?>()
        val theNode = ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }
        val deserialized = prism[Mirror.reflect<ArrayList<String?>>()].value.read(theNode, theList)

        assertSame(theList, deserialized)
        assertEquals(targetList, deserialized)
    }

    @Test
    fun deserialize_ArrayList_withArrayNode_andNoExistingValue_shouldCreateFillAndReturnList() {
        val targetList = ArrayList<String?>()
        targetList.addAll(listOf("first", "second", null, "fourth"))

        val theNode = ArrayNode.build {
            n+ "first"
            n+ "second"
            n+ NullNode
            n+ "fourth"
        }
        val deserialized = prism[Mirror.reflect<ArrayList<String?>>()].value.read(theNode, null)

        assertEquals(ArrayList::class.java, deserialized.javaClass)
        assertEquals(targetList, deserialized)
    }

    @Test
    fun deserialize_ArrayList_withWrongNodeType_shouldThrow() {
        assertThrows<DeserializationException> {
            prism[Mirror.reflect<ArrayList<String?>>()].value.read(LeafNode("whoops!"), null)
        }
    }

    /**
     * While the setup for this here is contrived, this can easily happen if you have a layer of indirection, like this:
     * ```
     * class CoolObject {
     *     val recursion: List<CoolObject> = listOf()
     * }
     * // later...
     * prism[Mirror.reflect<List<CoolObject>>()].value.write(aList)
     * ```
     */
    @Test
    fun serialize_nestedListsWithSameSerializer_shouldCorrectlySerializeLists() {
        @Suppress("NestedLambdaShadowedImplicitParameter")
        val list = Foo().also {
            it.add(Foo().also {
                it.add(Foo())
                it.add(Foo())
            })
            it.add(Foo().also {
                it.add(Foo())
                it.add(Foo())
                it.add(Foo())
            })
        }

        val targetNode = ArrayNode.build {
            n+ array {
                n+ array {}
                n+ array {}
            }
            n+ array {
                n+ array {}
                n+ array {}
                n+ array {}
            }
        }

        val node = prism[Mirror.reflect<Foo>()].value.write(list)

        assertEquals(targetNode, node)
    }

    /**
     * While the setup for this here is contrived, this can easily happen if you have a layer of indirection, like this:
     * ```
     * class CoolObject {
     *     val recursion: List<CoolObject> = listOf()
     * }
     * // later...
     * prism[Mirror.reflect<List<CoolObject>>()].value.read(aNode, aList)
     * ```
     */
    @Test
    fun deserialize_nestedListsWithSameSerializer_shouldCorrectlySerializeLists() {
        @Suppress("NestedLambdaShadowedImplicitParameter")
        val targetList = Foo().also {
            it.add(Foo().also {
                it.add(Foo())
                it.add(Foo())
            })
            it.add(Foo().also {
                it.add(Foo())
                it.add(Foo())
                it.add(Foo())
            })
        }

        val node = ArrayNode.build {
            n+ array {
                n+ array {}
                n+ array {}
            }
            n+ array {
                n+ array {}
                n+ array {}
                n+ array {}
            }
        }

        val list = prism[Mirror.reflect<Foo>()].value.read(node, null)

        assertEquals(targetList, list)
    }

    class Foo: ArrayList<Foo>()
}