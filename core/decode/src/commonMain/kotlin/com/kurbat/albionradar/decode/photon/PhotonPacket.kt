package com.kurbat.albionradar.decode.photon

data class PhotonPacket(
    val peerId: UShort,
    val flags: UByte,
    val commandCount: UByte,
    val timestamp: UInt,
    val challenge: UInt,
    val commands: List<PhotonCommand>,
    val isEncrypted: Boolean = false
)

data class PhotonCommand(
    val commandType: UByte,
    val channelId: UByte,
    val commandFlags: UByte,
    val commandLength: UInt,
    val sequenceNumber: UInt,
    val messageType: UByte?,
    val payload: ByteArray?
)
