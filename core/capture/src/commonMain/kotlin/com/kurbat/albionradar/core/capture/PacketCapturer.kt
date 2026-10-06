package com.kurbat.albionradar.core.capture

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PacketCapturer {
    val packets: Flow<Packet>
    val stats: StateFlow<CaptureStats>
    fun start()
    fun stop()
}
