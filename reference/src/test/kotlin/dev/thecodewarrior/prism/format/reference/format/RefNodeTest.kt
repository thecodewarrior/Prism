package dev.thecodewarrior.prism.format.reference.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RefNodeTest {
    @Test
    fun node_shouldEqualItself() {
        val leaf = LeafNode("hi")

        var node: RefNode = leaf
        assertTrue(node == node)

        node = ObjectNode().also { it["hi"] = leaf }
        assertTrue(node == node)

        node = ArrayNode().also { it.add(leaf) }
        assertTrue(node == node)
    }

    @Test
    fun node_shouldEqualIdentical() {
        assertTrue(LeafNode("hi") == LeafNode("hi"))
        assertTrue(ObjectNode().also { it["hi"] = LeafNode("hi") } == ObjectNode().also { it["hi"] = LeafNode("hi") })
        assertTrue(ArrayNode().also { it.add(LeafNode("hi")) } == ArrayNode().also { it.add(LeafNode("hi")) })
    }

    @Test
    fun node_shouldNotEqualDifferent() {
        assertFalse(LeafNode("hi") == LeafNode("bye"))
        assertFalse(ObjectNode().also { it["hi"] = LeafNode("hi") } == ObjectNode().also { it["bye"] = LeafNode("bye") })
        assertFalse(ArrayNode().also { it.add(LeafNode("hi")) } == ArrayNode().also { it.add(LeafNode("bye")) })
    }

    @Test
    fun objectBuilder() {
        val fromBuilder = ObjectNode.build {
            "auto_wrapped" *= 5
            "node" *= LeafNode("hi")
            "object" *= obj {
                "hi" *= "bye"
            }
            "array" *= array {
                n+ "hi"
            }
        }

        val manual = ObjectNode()
        manual["auto_wrapped"] = LeafNode(5)
        manual["node"] = LeafNode("hi")
        manual["object"] = ObjectNode().also { it["hi"] = LeafNode("bye") }
        manual["array"] = ArrayNode().also { it.add(LeafNode("hi")) }

        assertEquals(manual, fromBuilder)
    }

    @Test
    fun arrayBuilder() {
        val fromBuilder = ArrayNode.build {
            n+ 5
            n+ LeafNode("hi")
            n+ obj {
                "hi" *= "bye"
            }
            n+ array {
                n+ "hi"
            }
        }

        val manual = ArrayNode()
        manual.add(LeafNode(5))
        manual.add(LeafNode("hi"))
        manual.add(ObjectNode().also { it["hi"] = LeafNode("bye") })
        manual.add(ArrayNode().also { it.add(LeafNode("hi")) })

        assertEquals(manual, fromBuilder)
    }
}