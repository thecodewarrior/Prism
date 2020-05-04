package dev.thecodewarrior.prism.format.reference.builtin

import dev.thecodewarrior.mirror.Mirror
import dev.thecodewarrior.prism.DeserializationException
import dev.thecodewarrior.prism.Prism
import dev.thecodewarrior.prism.format.reference.ReferencePrism
import dev.thecodewarrior.prism.format.reference.ReferenceSerializer
import dev.thecodewarrior.prism.format.reference.format.ArrayNode
import dev.thecodewarrior.prism.format.reference.format.LeafNode
import dev.thecodewarrior.prism.format.reference.format.NullNode
import dev.thecodewarrior.prism.format.reference.format.ObjectNode
import dev.thecodewarrior.prism.format.reference.testsupport.PrismTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Random

internal class RandomOrderListSerializerFactoryTest: PrismTest() {
    override fun createPrism(): ReferencePrism = Prism<ReferenceSerializer<*>>().also { prism ->
        registerPrimitives(prism)
        prism.register(FallbackSerializerFactory(prism))
        prism.register(RandomOrderListSerializerFactory(prism))
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
        assertEquals(RandomOrderListSerializerFactory.RandomOrderListSerializer::class.java, serializer.javaClass)
        assertSame(Mirror.reflect<ArrayList<String>>(), serializer.type)
    }

    @Test
    fun serialize_withAllElements_shouldReturnCorrectNode() {
        val theList = ArrayList<String?>()
        theList.addAll(listOf("first", "second", "third", "fourth"))
        val node = prism[Mirror.reflect<ArrayList<String?>>()].value.write(theList)

        val targetNode = ObjectNode.build {
            "length" *= theList.size
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 2
                    "value" *= "third"
                }
                n+ obj {
                    "index" *= 3
                    "value" *= "fourth"
                }
            }
        }

        assertEquals(targetNode, node)
    }

    @Test
    fun serialize_withNullElement_shouldReturnCorrectNode() {
        val theList = ArrayList<String?>()
        theList.addAll(listOf("first", "second", null, "fourth"))
        val node = prism[Mirror.reflect<ArrayList<String?>>()].value.write(theList)

        val targetNode = ObjectNode.build {
            "length" *= theList.size
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 3
                    "value" *= "fourth"
                }
            }
        }

        assertEquals(targetNode, node)
    }

    @Test
    fun serialize_withTrailingNullElement_shouldReturnCorrectNode() {
        val theList = ArrayList<String?>()
        theList.addAll(listOf("first", "second", "third", null))
        val node = prism[Mirror.reflect<ArrayList<String?>>()].value.write(theList)

        val targetNode = ObjectNode.build {
            "length" *= theList.size
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 2
                    "value" *= "third"
                }
            }
        }

        assertEquals(targetNode, node)
    }

    @Test
    fun deserialize_withAllElements_shouldDeserializeCorrectly() {
        val targetList = ArrayList<String?>()
        targetList.addAll(listOf("first", "second", "third", "fourth"))

        val theNode = ObjectNode.build {
            "length" *= 4
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 2
                    "value" *= "third"
                }
                n+ obj {
                    "index" *= 3
                    "value" *= "fourth"
                }
            }.also {
                it.shuffle(Random(4.toLong()))
            }
        }

        val deserialized = prism[Mirror.reflect<ArrayList<String?>>()].value.read(theNode, null)

        assertEquals(ArrayList::class.java, deserialized.javaClass)
        assertEquals(targetList, deserialized)
    }

    @Test
    fun deserialize_withNullElement_shouldDeserializeCorrectly() {
        val targetList = ArrayList<String?>()
        targetList.addAll(listOf("first", "second", null, "fourth"))

        val theNode = ObjectNode.build {
            "length" *= 4
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 3
                    "value" *= "fourth"
                }
            }.also {
                it.shuffle(Random(4.toLong()))
            }
        }

        val deserialized = prism[Mirror.reflect<ArrayList<String?>>()].value.read(theNode, null)

        assertEquals(ArrayList::class.java, deserialized.javaClass)
        assertEquals(targetList, deserialized)
    }

    @Test
    fun deserialize_withTrailingNullElement_shouldDeserializeCorrectly() {
        val targetList = ArrayList<String?>()
        targetList.addAll(listOf("first", "second", "third", null))

        val theNode = ObjectNode.build {
            "length" *= 4
            "data" *= ArrayNode.build {
                n+ obj {
                    "index" *= 0
                    "value" *= "first"
                }
                n+ obj {
                    "index" *= 1
                    "value" *= "second"
                }
                n+ obj {
                    "index" *= 2
                    "value" *= "third"
                }
            }.also {
                it.shuffle(Random(4.toLong()))
            }
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
}