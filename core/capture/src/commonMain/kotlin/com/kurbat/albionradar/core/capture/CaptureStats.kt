package com.kurbat.albionradar.core.capture

data class CaptureStats(
    val packetsReceived: Long = 0,
    val bytesReceived: Long = 0,
    val packetsDropped: Long = 0,
    val isActive: Boolean = false,
    val packetsPerSecond: Int = 0,
    val bytesPerSecond: Long = 0
)
