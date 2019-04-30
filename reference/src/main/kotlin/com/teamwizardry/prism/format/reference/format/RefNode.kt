package com.teamwizardry.prism.format.reference.format

sealed class RefNode {

}

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

    companion object {
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

    companion object {
        fun build(init: ArrayNodeBuilder.() -> Unit): ArrayNode {
            val builder = ArrayNodeBuilder()
            builder.init()
            return builder.build()
        }
    }
}

data class LeafNode(val value: Any): RefNode()

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

    fun leaf(value: Any): LeafNode {
        return LeafNode(value)
    }

    abstract fun build(): RefNode
}

class ObjectNodeBuilder: BuilderNode() {
    private val values = mutableMapOf<String, RefNode>()


    operator fun String.timesAssign(node: BuilderNode) {
        values[this@timesAssign] = node.build()
    }

    operator fun String.timesAssign(node: RefNode) {
        values[this@timesAssign] = node
    }

    operator fun String.timesAssign(value: Any) {
        values[this@timesAssign] = LeafNode(value)
    }

    override fun build(): ObjectNode {
        val obj = ObjectNode()
        values.forEach { k, v ->
            obj[k] = v
        }
        return obj
    }
}

class ArrayNodeBuilder: BuilderNode() {
    private val values = mutableListOf<RefNode>()

    operator fun BuilderNode.unaryPlus() {
        this@ArrayNodeBuilder.values.add(this@unaryPlus.build())
    }

    operator fun RefNode.unaryPlus() {
        values.add(this@unaryPlus)
    }

    operator fun Any.unaryPlus() {
        values.add(LeafNode(this@unaryPlus))
    }

    override fun build(): ArrayNode {
        val arr = ArrayNode()
        values.forEach { arr.add(it) }
        return arr
    }
}
