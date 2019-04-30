package com.teamwizardry.prism.formats.netty.serializers.builtin.primitives

import com.teamwizardry.prism.formats.netty.NettySerializer
import com.teamwizardry.prism.formats.netty.readString
import com.teamwizardry.prism.formats.netty.readVarInt
import com.teamwizardry.prism.formats.netty.writeString
import com.teamwizardry.prism.formats.netty.writeVarInt
import io.netty.buffer.ByteBuf

object SerializeByteArray: NettySerializer<ByteArray>() {
    override fun deserialize(buf: ByteBuf, existing: ByteArray?, syncing: Boolean): ByteArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else ByteArray(length)
        arr.indices.forEach { arr[it] = buf.readByte() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: ByteArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeByte(it.toInt()) }
    }

}

object SerializeCharArray: NettySerializer<CharArray>() {
    override fun deserialize(buf: ByteBuf, existing: CharArray?, syncing: Boolean): CharArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else CharArray(length)
        arr.indices.forEach { arr[it] = buf.readChar() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: CharArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeChar(it.toInt()) }
    }

}

object SerializeShortArray: NettySerializer<ShortArray>() {
    override fun deserialize(buf: ByteBuf, existing: ShortArray?, syncing: Boolean): ShortArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else ShortArray(length)
        arr.indices.forEach { arr[it] = buf.readShort() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: ShortArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeShort(it.toInt()) }
    }

}

object SerializeIntArray: NettySerializer<IntArray>() {
    override fun deserialize(buf: ByteBuf, existing: IntArray?, syncing: Boolean): IntArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else IntArray(length)
        arr.indices.forEach { arr[it] = buf.readInt() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: IntArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeInt(it) }
    }

}

object SerializeLongArray: NettySerializer<LongArray>() {
    override fun deserialize(buf: ByteBuf, existing: LongArray?, syncing: Boolean): LongArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else LongArray(length)
        arr.indices.forEach { arr[it] = buf.readLong() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: LongArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeLong(it) }
    }

}

object SerializeFloatArray: NettySerializer<FloatArray>() {
    override fun deserialize(buf: ByteBuf, existing: FloatArray?, syncing: Boolean): FloatArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else FloatArray(length)
        arr.indices.forEach { arr[it] = buf.readFloat() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: FloatArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeFloat(it) }
    }

}

object SerializeDoubleArray: NettySerializer<DoubleArray>() {
    override fun deserialize(buf: ByteBuf, existing: DoubleArray?, syncing: Boolean): DoubleArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else DoubleArray(length)
        arr.indices.forEach { arr[it] = buf.readDouble() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: DoubleArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeDouble(it) }
    }

}

object SerializeBooleanArray: NettySerializer<BooleanArray>() {
    override fun deserialize(buf: ByteBuf, existing: BooleanArray?, syncing: Boolean): BooleanArray {
        val length = buf.readVarInt()
        val arr = if (existing != null && existing.size == length) existing else BooleanArray(length)
        arr.indices.forEach { arr[it] = buf.readBoolean() }
        return arr
    }

    override fun serialize(buf: ByteBuf, value: BooleanArray, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeBoolean(it) }
    }

}

object SerializeStringArray: NettySerializer<Array<String>>() {
    override fun deserialize(buf: ByteBuf, existing: Array<String>?, syncing: Boolean): Array<String> {
        val length = buf.readVarInt()
        if (existing != null && existing.size == length) {
            existing.indices.forEach { existing[it] = buf.readString() }
            return existing
        } else {
            return Array(length) { buf.readString() }
        }
    }

    override fun serialize(buf: ByteBuf, value: Array<String>, syncing: Boolean) {
        buf.writeVarInt(value.size)
        value.forEach { buf.writeString(it) }
    }

}
