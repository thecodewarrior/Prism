package com.teamwizardry.prism.formats.netty.serializers.builtin.basics

import com.teamwizardry.prism.formats.netty.NettySerializer
import com.teamwizardry.prism.formats.netty.readVarInt
import com.teamwizardry.prism.formats.netty.writeVarInt
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.awt.Color
import java.util.*

object SerializeColor : NettySerializer<Color>() {
    override fun deserialize(buf: ByteBuf, existing: Color?, syncing: Boolean): Color {
        return Color(buf.readInt())
    }

    override fun serialize(buf: ByteBuf, value: Color, syncing: Boolean) {
        buf.writeInt(value.rgb)
    }
}

object SerializeUUID: NettySerializer<UUID>() {
    override fun deserialize(buf: ByteBuf, existing: UUID?, syncing: Boolean): UUID {
        return UUID(buf.readLong(), buf.readLong())
    }

    override fun serialize(buf: ByteBuf, value: UUID, syncing: Boolean) {
        buf.writeLong(value.mostSignificantBits)
        buf.writeLong(value.leastSignificantBits)
    }
}

object SerializeByteBuf: NettySerializer<ByteBuf>() {
    override fun deserialize(buf: ByteBuf, existing: ByteBuf?, syncing: Boolean): ByteBuf {
        val bytes = ByteArray(buf.readVarInt())
        buf.readBytes(bytes)
        return Unpooled.wrappedBuffer(bytes)
    }

    override fun serialize(buf: ByteBuf, value: ByteBuf, syncing: Boolean) {
        val bytes = ByteArray(value.readableBytes())
        value.readBytes(bytes)
        buf.writeVarInt(bytes.size)
        buf.writeBytes(bytes)
    }
}
