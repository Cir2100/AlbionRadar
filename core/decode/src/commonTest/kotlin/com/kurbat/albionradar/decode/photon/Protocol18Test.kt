package com.kurbat.albionradar.decode.photon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class Protocol18Test {
    private val protocol = Protocol18()

    @Test
    fun deserializesProtocol18Primitives() {
        assertEquals(0x1234.toShort(), protocol.deserialize(BinaryReader(bytes(0x34, 0x12)), TypeCodes.SHORT))
        assertEquals(1f, protocol.deserialize(BinaryReader(bytes(0x00, 0x00, 0x80, 0x3f)), TypeCodes.FLOAT))
        assertEquals("foo", protocol.deserialize(BinaryReader(bytes(0x03, 'f'.code, 'o'.code, 'o'.code)), TypeCodes.STRING))
        assertEquals(2, protocol.deserialize(BinaryReader(bytes(0x04)), TypeCodes.COMPRESSED_INT))
        assertEquals(-3, protocol.deserialize(BinaryReader(bytes(0x05)), TypeCodes.COMPRESSED_INT))
    }

    @Test
    fun routesGenericDispatchByParameter252() {
        val event = protocol.deserializeEventData(
            BinaryReader(
                bytes(
                    0x01, // generic wire dispatch
                    0x01, // parameter count
                    0xfc, TypeCodes.SHORT.toInt(), 0x7b, 0x00 // real event code 123
                )
            )
        )

        assertEquals(1, event.wireCode)
        assertEquals(123, event.code)
        assertEquals(123, event.getInt(252))
    }

    @Test
    fun extractsFiniteMoveCoordinatesFromLiveCapture() {
        val raw = bytes(
            3, 104, 88, 142, 195, 84, 154, 222, 8, 176, 230, 36, 195, 237, 215, 175, 67,
            166, 0, 0, 176, 63, 245, 26, 37, 195, 158, 196, 175, 67
        )
        val typedByteArray = TypeCodes.ARRAY.toInt() or TypeCodes.BYTE.toInt()
        val payload = bytes(3, 1, 1, typedByteArray, raw.size, *raw.toUnsignedInts())

        val event = protocol.deserializeEventData(BinaryReader(payload))

        assertEquals(3, event.code)
        assertEquals(-164.90112f, event.getFloat(4)!!, 0.001f)
        assertEquals(351.68692f, event.getFloat(5)!!, 0.001f)
        assertEquals(3, event.getInt(252))
    }

    @Test
    fun doesNotExposeNonFiniteMoveCoordinates() {
        val raw = ByteArray(17)
        putUInt32LE(raw, 9, Float.NaN.toBits().toUInt())
        putUInt32LE(raw, 13, Float.POSITIVE_INFINITY.toBits().toUInt())
        val typedByteArray = TypeCodes.ARRAY.toInt() or TypeCodes.BYTE.toInt()
        val payload = bytes(3, 1, 1, typedByteArray, raw.size, *raw.toUnsignedInts())

        val event = protocol.deserializeEventData(BinaryReader(payload))

        assertNull(event.getFloat(4))
        assertNull(event.getFloat(5))
        assertFalse(event.parameters.containsKey(4))
    }

    @Test
    fun readsRealOperationCodeAndMovePosition() {
        val positionBytes = ByteArray(8)
        putUInt32LE(positionBytes, 0, 10.5f.toBits().toUInt())
        putUInt32LE(positionBytes, 4, 20.25f.toBits().toUInt())
        val typedFloatArray = TypeCodes.ARRAY.toInt() or TypeCodes.FLOAT.toInt()
        val payload = bytes(
            1, // generic wire operation code
            2, // parameter count
            253, TypeCodes.SHORT.toInt(), 22, 0,
            1, typedFloatArray, 2, *positionBytes.toUnsignedInts()
        )

        val request = protocol.deserializeOperationRequest(BinaryReader(payload))

        assertEquals(22, request.code)
        assertEquals(listOf(10.5f, 20.25f), request.parameters[1])
    }

    private fun bytes(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }

    private fun ByteArray.toUnsignedInts(): IntArray = IntArray(size) { this[it].toUByte().toInt() }

    private fun putUInt32LE(target: ByteArray, offset: Int, value: UInt) {
        repeat(4) { index -> target[offset + index] = (value shr (index * 8)).toByte() }
    }
}
