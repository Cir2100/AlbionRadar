package com.kurbat.albionradar.decode.photon

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotonDecoderTest {
    @Test
    fun decodesReliableAndUnreliableEvents() {
        val eventPayload = genericEventPayload(realCode = 123)

        val reliable = PhotonDecoder().decode(
            singleCommandPacket(CommandTypes.RELIABLE, bytes(0, MessageTypes.EVENT.toInt(), *eventPayload.toUnsignedInts()))
        )
        val unreliable = PhotonDecoder().decode(
            singleCommandPacket(
                CommandTypes.UNRELIABLE,
                bytes(0, 0, 0, 1, 0, MessageTypes.EVENT.toInt(), *eventPayload.toUnsignedInts())
            )
        )

        assertEquals(eventPayload.toList(), assertNotNull(reliable).commands.single().payload!!.toList())
        assertEquals(eventPayload.toList(), assertNotNull(unreliable).commands.single().payload!!.toList())
    }

    @Test
    fun reassemblesOutOfOrderFragmentsAndIgnoresDuplicates() {
        val eventPayload = genericEventPayload(realCode = 123)
        val reliablePayload = bytes(0, MessageTypes.EVENT.toInt(), *eventPayload.toUnsignedInts())
        val split = reliablePayload.size / 2
        val first = fragmentPacket(reliablePayload, 0, split, fragmentNumber = 0)
        val second = fragmentPacket(reliablePayload, split, reliablePayload.size, fragmentNumber = 1)
        val decoder = PhotonDecoder()

        assertTrue(decoder.decode(second)!!.commands.isEmpty())
        assertTrue(decoder.decode(second)!!.commands.isEmpty())
        val completed = decoder.decode(first)

        val command = assertNotNull(completed).commands.single()
        assertEquals(MessageTypes.EVENT, command.messageType)
        assertEquals(eventPayload.toList(), command.payload!!.toList())
    }

    @Test
    fun reportsMalformedPacketsButTreatsEncryptionSeparately() {
        var encrypted = false
        val decoder = PhotonDecoder(onEncrypted = { encrypted = true })

        assertNull(decoder.decode(bytes(1, 2, 3)))
        assertNotNull(decoder.lastError)

        val encryptedPacket = ByteArray(12).apply { this[2] = 1 }
        val parsed = decoder.decode(encryptedPacket)
        assertNotNull(parsed)
        assertTrue(parsed.isEncrypted)
        assertTrue(encrypted)
        assertNull(decoder.lastError)
    }

    @Test
    fun decoderAndProtocol18AgreeOnEffectiveEventCode() {
        val eventPayload = genericEventPayload(realCode = 123)
        val packet = singleCommandPacket(
            CommandTypes.RELIABLE,
            bytes(0, MessageTypes.EVENT.toInt(), *eventPayload.toUnsignedInts())
        )

        val command = PhotonDecoder().decode(packet)!!.commands.single()
        val event = Protocol18().deserializeEventData(BinaryReader(command.payload!!))

        assertEquals(123, event.code)
        assertEquals(1, event.wireCode)
        assertFalse(event.parameters.isEmpty())
    }

    private fun genericEventPayload(realCode: Int): ByteArray = bytes(
        1,
        1,
        252,
        TypeCodes.SHORT.toInt(),
        realCode and 0xff,
        (realCode ushr 8) and 0xff
    )

    private fun singleCommandPacket(commandType: UByte, commandPayload: ByteArray): ByteArray {
        val commandLength = 12 + commandPayload.size
        return ByteArray(12 + commandLength).apply {
            this[3] = 1
            this[12] = commandType.toByte()
            putUInt32BE(this, 16, commandLength.toUInt())
            commandPayload.copyInto(this, destinationOffset = 24)
        }
    }

    private fun fragmentPacket(
        reliablePayload: ByteArray,
        from: Int,
        to: Int,
        fragmentNumber: Int
    ): ByteArray {
        val fragment = reliablePayload.copyOfRange(from, to)
        val fragmentPayload = ByteArray(20 + fragment.size)
        putUInt32BE(fragmentPayload, 0, 100u)
        putUInt32BE(fragmentPayload, 4, 2u)
        putUInt32BE(fragmentPayload, 8, fragmentNumber.toUInt())
        putUInt32BE(fragmentPayload, 12, reliablePayload.size.toUInt())
        putUInt32BE(fragmentPayload, 16, from.toUInt())
        fragment.copyInto(fragmentPayload, destinationOffset = 20)
        return singleCommandPacket(CommandTypes.FRAGMENT, fragmentPayload)
    }

    private fun putUInt32BE(target: ByteArray, offset: Int, value: UInt) {
        repeat(4) { index -> target[offset + index] = (value shr ((3 - index) * 8)).toByte() }
    }

    private fun bytes(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }

    private fun ByteArray.toUnsignedInts(): IntArray = IntArray(size) { this[it].toUByte().toInt() }
}
