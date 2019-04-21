package com.teamwizardry.prism.formats.netty.serializers.builtin.special

import com.teamwizardry.mirror.ArrayReflect
import com.teamwizardry.mirror.Mirror
import com.teamwizardry.mirror.type.ArrayMirror
import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.Prism
import com.teamwizardry.prism.SerializerFactory
import com.teamwizardry.prism.formats.netty.NettySerializer
import com.teamwizardry.prism.formats.netty.readBooleanArray
import com.teamwizardry.prism.formats.netty.writeBooleanArray
import io.netty.buffer.ByteBuf

object ArraySerializerFactory : SerializerFactory<NettySerializer<*>>(Mirror.reflect<Array<Any?>>()) {
    override fun create(prism: Prism<NettySerializer<*>>, mirror: TypeMirror): NettySerializer<*> {
        return ArraySerializer(prism, mirror as ArrayMirror)
    }

    private class ArraySerializer(prism: Prism<NettySerializer<*>>, type: ArrayMirror) : NettySerializer<Array<Any?>>(type) {
        val arrayType: ArrayMirror = type
        val componentSerializer: NettySerializer<*> by prism.get(arrayType.component)

        override fun deserialize(buf: ByteBuf, existing: Array<Any?>?, syncing: Boolean): Array<Any?> {
            val nulls = buf.readBooleanArray()

            val array: Array<Any?>
            if(existing?.size == nulls.size) {
                array = existing
            } else {
                @Suppress("UNCHECKED_CAST")
                array = ArrayReflect.newInstanceRaw(arrayType.component.java as Class<*>, nulls.size) as Array<Any?>
            }

            for (i in 0 until nulls.size) {
                array[i] = if (nulls[i]) null else componentSerializer.read(buf, array[i], syncing)
            }
            return array
        }

        override fun serialize(buf: ByteBuf, value: Array<Any?>, syncing: Boolean) {
            val nulls = BooleanArray(value.size) { value[it] == null }
            buf.writeBooleanArray(nulls)


            for (i in value.indices) {
                value[i]?.also {
                    componentSerializer.write(buf, it, syncing)
                }
            }
        }
    }
}
