package com.kurbat.albionradar.core.capture

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object PacketCapturerStub : PacketCapturer {

    private val _stats = MutableStateFlow(CaptureStats())

    override val stats: StateFlow<CaptureStats>
        get() = _stats.asStateFlow()
    private val _packets = MutableStateFlow(Packet(ByteArrayContainer(ByteArray(0))))
    override val packets: Flow<Packet>
        get() = _packets.asStateFlow()

    override fun start() {}

    override fun stop() {}
}
