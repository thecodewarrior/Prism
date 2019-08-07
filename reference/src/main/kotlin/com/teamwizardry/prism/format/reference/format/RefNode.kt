package com.teamwizardry.prism.format.reference.format

sealed class RefNode

class ObjectNode private constructor(private val map: MutableMap<String, RefNode>): RefNode(), MutableMap<String, RefNode> by map {
    constructor(): this(mutableMapOf())

    fun getObject(key: String): ObjectNode {
        return this[key] as ObjectNode
    }

    fun getArray(key: String): ArrayNode {
        return this[key] as ArrayNode
    }

    fun getLeaf(key: String): LeafNode {
        return this[key] as LeafNode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjectNode) return false

        if (map != other.map) return false

        return true
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        return "{\n" + map.entries.sortedBy { it.key }.joinToString(",\n") {
            "\"${it.key}\": ${it.value},".prependIndent("  ")
        } + "\n}"
    }

    companion object {
        /**
         * Syntax:
         * ```
         * ObjectNode.build {
         *     "key" *= value
         *     "key" *= array {
         *         ...
         *     }
         *     "key" *= obj {
         *         ...
         *     }
         *     "key" *= someNode
         * }
         * ```
         */
        fun build(init: ObjectNodeBuilder.() -> Unit): ObjectNode {
            val builder = ObjectNodeBuilder()
            builder.init()
            return builder.build()
        }
    }
}

class ArrayNode private constructor(private val list: MutableList<RefNode>): RefNode(), MutableList<RefNode> by list {
    constructor(): this(mutableListOf())

    fun getObject(index: Int): ObjectNode {
        return this[index] as ObjectNode
    }

    fun getArray(index: Int): ArrayNode {
        return this[index] as ArrayNode
    }

    fun getLeaf(index: Int): LeafNode {
        return this[index] as LeafNode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayNode) return false

        if (list != other.list) return false

        return true
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    override fun toString(): String {
        return "[\n" + list.joinToString(",\n") { "$it".prependIndent("  ") } + "\n]"
    }

    companion object {
        /**
         * Syntax:
         * ```
         * ArrayNode.build {
         *     n+ value
         *     n+ array {
         *         ...
         *     }
         *     n+ obj {
         *         ...
         *     }
         *     n+ someNode
         * }
         * ```
         */
        fun build(init: ArrayNodeBuilder.() -> Unit): ArrayNode {
            val builder = ArrayNodeBuilder()
            builder.init()
            return builder.build()
        }
    }
}

data class LeafNode(val value: Any): RefNode() {
    override fun toString(): String {
        return if(value is String) "\"$value\"" else value.toString()
    }
}

object NullNode: RefNode() {
    override fun toString(): String {
        return "null"
    }
}

@DslMarker
annotation class DslNodeMarker

@DslNodeMarker
sealed class BuilderNode {
    fun array(init: ArrayNodeBuilder.() -> Unit): ArrayNode {
        val builder = ArrayNodeBuilder()
        builder.init()
        return builder.build()
    }

    fun obj(init: ObjectNodeBuilder.() -> Unit): ObjectNode {
        val builder = ObjectNodeBuilder()
        builder.init()
        return builder.build()
    }

    val Any.leaf: LeafNode get() = LeafNode(this@leaf)

    abstract fun build(): RefNode
}

/**
 * Syntax:
 * ```
 * ObjectNode.build {
 *     "key" *= value
 *     "key" *= array {
 *         ...
 *     }
 *     "key" *= obj {
 *         ...
 *     }
 *     "key" *= someNode
 * }
 * ```
 */
class ObjectNodeBuilder internal constructor(): BuilderNode() {
    private val values = mutableMapOf<String, RefNode>()

    operator fun String.timesAssign(node: RefNode) {
        values[this@timesAssign] = node
    }

    operator fun String.timesAssign(value: Any) {
        values[this@timesAssign] = LeafNode(value)
    }

    fun add(key: String, node: RefNode) {
        values[key] = node
    }

    fun add(key: String, value: Any) {
        values[key] = LeafNode(value)
    }

    override fun build(): ObjectNode {
        val obj = ObjectNode()
        values.forEach { k, v ->
            obj[k] = v
        }
        return obj
    }
}

/**
 * Syntax:
 * ```
 * ArrayNode.build {
 *     n+ value
 *     n+ array {
 *         ...
 *     }
 *     n+ obj {
 *         ...
 *     }
 *     n+ someNode
 * }
 * ```
 */
class ArrayNodeBuilder internal constructor(): BuilderNode() {
    private val values = mutableListOf<RefNode>()
    val n: ArrayNodeBuilder = this

    operator fun plus(value: Any) {
        values.add(LeafNode(value))
    }

    operator fun plus(node: RefNode) {
        values.add(node)
    }

    fun add(value: Any) {
        values.add(LeafNode(value))
    }

    fun add(node: RefNode) {
        values.add(node)
    }

    override fun build(): ArrayNode {
        val arr = ArrayNode()
        values.forEach { arr.add(it) }
        return arr
    }
}
