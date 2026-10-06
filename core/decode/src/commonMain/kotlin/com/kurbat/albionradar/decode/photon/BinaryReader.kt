package com.kurbat.albionradar.decode.photon

class BinaryReader(private val data: ByteArray, private var position: Int = 0) {
    val hasRemaining: Boolean
        get() = position < data.size

    val remaining: Int
        get() = data.size - position

    fun readUInt8(): UByte {
        requireAvailable(1)
        return data[position++].toUByte()
    }

    fun readByte(): Byte {
        requireAvailable(1)
        return data[position++]
    }

    fun readUInt16BE(): UShort {
        requireAvailable(2)
        val value = ((data[position].toInt() and 0xFF) shl 8) or
            (data[position + 1].toInt() and 0xFF)
        position += 2
        return value.toUShort()
    }

    fun readUInt16LE(): UShort {
        requireAvailable(2)
        val value = (data[position].toInt() and 0xFF) or
            ((data[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return value.toUShort()
    }

    fun readInt16LE(): Short = readUInt16LE().toShort()

    fun readUInt32BE(): UInt {
        requireAvailable(4)
        val value = ((data[position].toLong() and 0xFF) shl 24) or
            ((data[position + 1].toLong() and 0xFF) shl 16) or
            ((data[position + 2].toLong() and 0xFF) shl 8) or
            (data[position + 3].toLong() and 0xFF)
        position += 4
        return value.toUInt()
    }

    fun readUInt32LE(): UInt {
        requireAvailable(4)
        val value = (data[position].toLong() and 0xFF) or
            ((data[position + 1].toLong() and 0xFF) shl 8) or
            ((data[position + 2].toLong() and 0xFF) shl 16) or
            ((data[position + 3].toLong() and 0xFF) shl 24)
        position += 4
        return value.toUInt()
    }

    fun readUInt64BE(): ULong {
        requireAvailable(8)
        var value = 0UL
        for (i in 0 until 8) {
            value = (value shl 8) or (data[position + i].toULong() and 0xFFUL)
        }
        position += 8
        return value
    }

    fun readUInt64LE(): ULong {
        requireAvailable(8)
        var value = 0UL
        for (i in 0 until 8) {
            value = value or ((data[position + i].toULong() and 0xFFUL) shl (i * 8))
        }
        position += 8
        return value
    }

    fun readFloat32BE(): Float {
        val bits = readUInt32BE().toInt()
        return Float.fromBits(bits)
    }

    fun readFloat32LE(): Float {
        return Float.fromBits(readUInt32LE().toInt())
    }

    fun readFloat64BE(): Double {
        val bits = readUInt64BE().toLong()
        return Double.fromBits(bits)
    }

    fun readFloat64LE(): Double = Double.fromBits(readUInt64LE().toLong())

    fun readBytes(length: Int): ByteArray {
        requireAvailable(length)
        val bytes = data.copyOfRange(position, position + length)
        position += length
        return bytes
    }

    fun skip(count: Int) {
        requireAvailable(count)
        position += count
    }

    fun peek(): Byte {
        if (position >=
            data.size
        ) {
            throw IndexOutOfBoundsException("Cannot peek at position $position")
        }
        return data[position]
    }

    fun createSubReader(length: Int): BinaryReader {
        val subData = readBytes(length)
        return BinaryReader(subData)
    }

    private fun requireAvailable(count: Int) {
        if (count < 0 || count > remaining) {
            throw IndexOutOfBoundsException(
                "Cannot read $count bytes at position $position (remaining=$remaining)"
            )
        }
    }
}
