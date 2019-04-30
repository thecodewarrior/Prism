package com.teamwizardry.prism.format.reference.builtin

import com.teamwizardry.prism.format.reference.ReferenceSerializer
import com.teamwizardry.prism.format.reference.format.LeafNode
import com.teamwizardry.prism.format.reference.format.RefNode

object FallbackSerializer: ReferenceSerializer<Any>() {
    override fun deserialize(node: RefNode, existing: Any?): Any {
        return (node as LeafNode).value
    }

    override fun serialize(value: Any): RefNode {
        return LeafNode(value)
    }
}

