package com.teamwizardry.prism.formats.netty.serializers.builtin.primitives

import com.teamwizardry.prism.formats.netty.NettySerializer
import io.netty.buffer.ByteBuf

/**
 * Created by TheCodeWarrior
 */

object SerializeByte: NettySerializer<Byte>() {
    override fun deserialize(buf: ByteBuf, existing: Byte?, syncing: Boolean): Byte {
        return buf.readByte()
    }

    override fun serialize(buf: ByteBuf, value: Byte, syncing: Boolean) {
        buf.writeByte(value.toInt())
    }
}

object SerializeChar: NettySerializer<Char>() {
    override fun deserialize(buf: ByteBuf, existing: Char?, syncing: Boolean): Char {
        return buf.readChar()
    }

    override fun serialize(buf: ByteBuf, value: Char, syncing: Boolean) {
        buf.writeChar(value.toInt())
    }
}

object SerializeShort: NettySerializer<Short>() {
    override fun deserialize(buf: ByteBuf, existing: Short?, syncing: Boolean): Short {
        return buf.readShort()
    }

    override fun serialize(buf: ByteBuf, value: Short, syncing: Boolean) {
        buf.writeShort(value.toInt())
    }
}

object SerializeInt: NettySerializer<Int>() {
    override fun deserialize(buf: ByteBuf, existing: Int?, syncing: Boolean): Int {
        return buf.readInt()
    }

    override fun serialize(buf: ByteBuf, value: Int, syncing: Boolean) {
        buf.writeInt(value)
    }
}

object SerializeLong: NettySerializer<Long>() {
    override fun deserialize(buf: ByteBuf, existing: Long?, syncing: Boolean): Long {
        return buf.readLong()
    }

    override fun serialize(buf: ByteBuf, value: Long, syncing: Boolean) {
        buf.writeLong(value)
    }
}

object SerializeFloat: NettySerializer<Float>() {
    override fun deserialize(buf: ByteBuf, existing: Float?, syncing: Boolean): Float {
        return buf.readFloat()
    }

    override fun serialize(buf: ByteBuf, value: Float, syncing: Boolean) {
        buf.writeFloat(value)
    }
}

object SerializeDouble: NettySerializer<Double>() {
    override fun deserialize(buf: ByteBuf, existing: Double?, syncing: Boolean): Double {
        return buf.readDouble()
    }

    override fun serialize(buf: ByteBuf, value: Double, syncing: Boolean) {
        buf.writeDouble(value)
    }
}

object SerializeBoolean: NettySerializer<Boolean>() {
    override fun deserialize(buf: ByteBuf, existing: Boolean?, syncing: Boolean): Boolean {
        return buf.readBoolean()
    }

    override fun serialize(buf: ByteBuf, value: Boolean, syncing: Boolean) {
        buf.writeBoolean(value)
    }
}

object SerializeString: NettySerializer<String>() {
    override fun deserialize(buf: ByteBuf, existing: String?, syncing: Boolean): String {
        return buf.readString()
    }

    override fun serialize(buf: ByteBuf, value: String, syncing: Boolean) {
        buf.writeString(value)
    }
}
