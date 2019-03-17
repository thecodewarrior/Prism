package com.teamwizardry.prism.formats.network

import com.teamwizardry.mirror.type.TypeMirror
import com.teamwizardry.prism.PrismException
import com.teamwizardry.prism.Serializer
import java.nio.ByteBuffer

abstract class NetworkSerializer<T: Any>: Serializer<T>() {
    protected abstract fun deserialize(buf: ByteBuffer, existing: T?, syncing: Boolean): T
    protected abstract fun serialize(buf: ByteBuffer, value: T, syncing: Boolean)

    fun read(buf: ByteBuffer, existing: T?, syncing: Boolean): T {
        try {
            return deserialize(buf, existing, syncing)
        } catch (e: Throwable) {
            throw PrismException("Error deserializing $type", e)
        }
    }

    fun write(buf: ByteBuffer, value: T, syncing: Boolean) {
        try {
            return serialize(buf, value, syncing)
        } catch (e: Throwable) {
            throw PrismException("Error serializing $type", e)
        }
    }


    fun ByteBuffer.writeString(value: String) =// ByteBufferUtils.writeUTF8String(this, value)
    fun ByteBuffer.readString(): String = ByteBufferUtils.readUTF8String(this)

    fun ByteBuffer.writeStack(value: ItemStack) = ByteBufferUtils.writeItemStack(this, value)
    fun ByteBuffer.readStack(): ItemStack = ByteBufferUtils.readItemStack(this)

    fun ByteBuffer.writeTag(value: NBTTagCompound) = ByteBufferUtils.writeTag(this, value)
    fun ByteBuffer.readTag(): NBTTagCompound = ByteBufferUtils.readTag(this) ?: NBTTagCompound()

    fun ByteBuffer.writeVarInt(value: Int) {
        var input = value
        while (input and -128 != 0) {
            this.writeByte(input and 127 or 128)
            input = input ushr 7
        }

        this.writeByte(input)
    }

    fun ByteBuffer.readVarInt(): Int {
        var i = 0
        var j = 0

        while (true) {
            val b0 = this.readByte()
            i = i or (b0.toInt() and 127 shl j++ * 7)

            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }

            if (b0.toInt() and 128 != 128) {
                break
            }
        }

        return i
    }

    fun ByteBuffer.writeVarLong(value: Long) {
        var input = value
        while (input and -128L != 0L) {
            this.writeByte((input and 127L).toInt() or 128)
            input = value ushr 7
        }

        this.writeByte(value.toInt())
    }

    fun ByteBuffer.readVarLong(): Long {
        var i = 0L
        var j = 0

        while (true) {
            val b0 = this.readByte()
            i = i or ((b0.toInt() and 127).toLong() shl j++ * 7)

            if (j > 10) {
                throw RuntimeException("VarLong too big")
            }

            if (b0.toInt() and 128 != 128) {
                break
            }
        }

        return i
    }

    fun ByteBuffer.writeBooleanArray(value: BooleanArray) {
        val len = value.size
        this.writeVarInt(len)

        val toReturn = ByteArray((len + 7) / 8) // +7 to round up
        for (entry in toReturn.indices) {
            (0..7)
                .filter { entry * 8 + it < len && value[entry * 8 + it] }
                .forEach { toReturn[entry] = (toReturn[entry].toInt() or (128 shr it)).toByte() }
        }
        this.writeBytes(toReturn)
    }

    fun ByteBuffer.readBooleanArray(tryReadInto: BooleanArray? = null): BooleanArray {
        val len = this.readVarInt()
        val bytes = ByteArray((len + 7) / 8)
        this.readBytes(bytes)

        val toReturn = if (tryReadInto != null && tryReadInto.size == len) tryReadInto else BooleanArray(len)
        for (entry in bytes.indices) {
            for (bit in 0..7) {
                val bitThing = bytes[entry].toInt() and (128 shr bit)
                if (entry * 8 + bit < len && bitThing != 0) {
                    toReturn[entry * 8 + bit] = true
                }
            }
        }
        return toReturn
    }

    fun ByteBuffer.writeNullSignature() {
        writeBoolean(true)
    }

    fun ByteBuffer.writeNonnullSignature() {
        writeBoolean(false)
    }

    fun ByteBuffer.hasNullSignature(): Boolean = readBoolean()
}