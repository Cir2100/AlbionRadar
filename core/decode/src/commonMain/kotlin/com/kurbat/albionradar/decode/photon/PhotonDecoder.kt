package com.kurbat.albionradar.decode.photon

class PhotonDecoder(
    private val onParseError: ((String) -> Unit)? = null,
    private val onEncrypted: (() -> Unit)? = null
) {
    private val pendingSegments = linkedMapOf<UInt, PendingSegment>()

    var lastError: String? = null
        private set

    fun decode(payload: ByteArray): PhotonPacket? {
        if (payload.size < PHOTON_HEADER_LENGTH) {
            return fail("Payload is shorter than the Photon header (${payload.size} bytes)")
        }

        return try {
            val packet = parsePacket(BinaryReader(payload))
            lastError = null
            packet
        } catch (error: Exception) {
            fail(error.message ?: "Unknown Photon parsing error")
        }
    }

    private fun parsePacket(reader: BinaryReader): PhotonPacket {
        val peerId = reader.readUInt16BE()
        val flags = reader.readUInt8()
        val commandCount = reader.readUInt8()
        val timestamp = reader.readUInt32BE()
        val challenge = reader.readUInt32BE()

        if (flags == ENCRYPTED_PACKET_FLAG) {
            onEncrypted?.invoke()
            return PhotonPacket(
                peerId = peerId,
                flags = flags,
                commandCount = commandCount,
                timestamp = timestamp,
                challenge = challenge,
                commands = emptyList(),
                isEncrypted = true
            )
        }

        val commands = buildList {
            repeat(commandCount.toInt()) {
                parseCommand(reader)?.let(::add)
            }
        }

        return PhotonPacket(
            peerId = peerId,
            flags = flags,
            commandCount = commandCount,
            timestamp = timestamp,
            challenge = challenge,
            commands = commands
        )
    }

    private fun parseCommand(reader: BinaryReader): PhotonCommand? {
        require(reader.remaining >= COMMAND_HEADER_LENGTH) {
            "Photon command header is truncated"
        }

        val commandType = reader.readUInt8()
        val channelId = reader.readUInt8()
        val commandFlags = reader.readUInt8()
        reader.skip(1)
        val commandLength = reader.readUInt32BE()
        val sequenceNumber = reader.readUInt32BE()
        val payloadLengthLong = commandLength.toLong() - COMMAND_HEADER_LENGTH

        require(payloadLengthLong in 0..reader.remaining.toLong()) {
            "Invalid Photon command length: $commandLength (remaining=${reader.remaining})"
        }

        val commandPayload = reader.readBytes(payloadLengthLong.toInt())
        val metadata = CommandMetadata(
            commandType = commandType,
            channelId = channelId,
            commandFlags = commandFlags,
            commandLength = commandLength,
            sequenceNumber = sequenceNumber
        )

        return when (commandType) {
            CommandTypes.RELIABLE -> parseReliable(metadata, commandPayload)
            CommandTypes.UNRELIABLE -> {
                require(commandPayload.size >= UNRELIABLE_PREFIX_LENGTH) {
                    "Unreliable Photon command is missing its sequence prefix"
                }
                parseReliable(metadata, commandPayload.copyOfRange(UNRELIABLE_PREFIX_LENGTH, commandPayload.size))
            }
            CommandTypes.FRAGMENT -> handleFragment(metadata, commandPayload)
            else -> metadata.toCommand(messageType = null, payload = null)
        }
    }

    private fun parseReliable(metadata: CommandMetadata, payload: ByteArray): PhotonCommand {
        require(payload.size >= RELIABLE_PREFIX_LENGTH) {
            "Reliable Photon command is missing signal/message bytes"
        }

        val messageType = payload[1].toUByte()
        if (messageType == MessageTypes.ENCRYPTED) {
            onEncrypted?.invoke()
            return metadata.toCommand(messageType = messageType, payload = null)
        }

        return metadata.toCommand(
            messageType = messageType,
            payload = payload.copyOfRange(RELIABLE_PREFIX_LENGTH, payload.size)
        )
    }

    private fun handleFragment(metadata: CommandMetadata, payload: ByteArray): PhotonCommand? {
        require(payload.size >= FRAGMENT_HEADER_LENGTH) {
            "Photon fragment header is truncated"
        }

        val reader = BinaryReader(payload)
        val startSequence = reader.readUInt32BE()
        reader.readUInt32BE() // fragment count; completion is tracked by received bytes
        reader.readUInt32BE() // fragment number
        val totalLength = reader.readUInt32BE().toLong()
        val fragmentOffset = reader.readUInt32BE().toLong()
        val fragment = reader.readBytes(reader.remaining)

        require(totalLength in 1..MAX_REASSEMBLED_LENGTH.toLong()) {
            "Invalid reassembled Photon payload length: $totalLength"
        }
        require(fragmentOffset >= 0 && fragmentOffset + fragment.size <= totalLength) {
            "Photon fragment exceeds its reassembled payload"
        }

        val expectedLength = totalLength.toInt()
        val segment = pendingSegments[startSequence]
            ?.takeIf { it.data.size == expectedLength }
            ?: createPendingSegment(startSequence, expectedLength)

        val offset = fragmentOffset.toInt()
        fragment.forEachIndexed { index, value ->
            val target = offset + index
            segment.data[target] = value
            if (!segment.received[target]) {
                segment.received[target] = true
                segment.receivedBytes++
            }
        }

        if (segment.receivedBytes < segment.data.size) {
            return null
        }

        pendingSegments.remove(startSequence)
        return parseReliable(metadata, segment.data)
    }

    private fun createPendingSegment(startSequence: UInt, totalLength: Int): PendingSegment {
        if (pendingSegments.size >= MAX_PENDING_SEGMENTS) {
            val oldest = pendingSegments.keys.firstOrNull()
            if (oldest != null) pendingSegments.remove(oldest)
        }

        return PendingSegment(
            data = ByteArray(totalLength),
            received = BooleanArray(totalLength)
        ).also { pendingSegments[startSequence] = it }
    }

    private fun fail(reason: String): PhotonPacket? {
        lastError = reason
        onParseError?.invoke(reason)
        return null
    }

    private data class PendingSegment(
        val data: ByteArray,
        val received: BooleanArray,
        var receivedBytes: Int = 0
    )

    private data class CommandMetadata(
        val commandType: UByte,
        val channelId: UByte,
        val commandFlags: UByte,
        val commandLength: UInt,
        val sequenceNumber: UInt
    ) {
        fun toCommand(messageType: UByte?, payload: ByteArray?) = PhotonCommand(
            commandType = commandType,
            channelId = channelId,
            commandFlags = commandFlags,
            commandLength = commandLength,
            sequenceNumber = sequenceNumber,
            messageType = messageType,
            payload = payload
        )
    }

    private companion object {
        const val PHOTON_HEADER_LENGTH = 12
        const val COMMAND_HEADER_LENGTH = 12
        const val FRAGMENT_HEADER_LENGTH = 20
        const val UNRELIABLE_PREFIX_LENGTH = 4
        const val RELIABLE_PREFIX_LENGTH = 2
        const val MAX_PENDING_SEGMENTS = 64
        const val MAX_REASSEMBLED_LENGTH = 1_048_576
        val ENCRYPTED_PACKET_FLAG: UByte = 1u
    }
}
