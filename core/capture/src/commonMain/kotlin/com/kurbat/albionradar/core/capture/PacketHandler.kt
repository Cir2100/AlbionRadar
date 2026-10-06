package com.kurbat.albionradar.core.capture

fun interface PacketHandler {
    fun onPacket(payload: ByteArray)
}
