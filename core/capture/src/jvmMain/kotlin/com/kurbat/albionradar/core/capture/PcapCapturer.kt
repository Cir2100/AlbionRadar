package com.kurbat.albionradar.core.capture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pcap4j.core.BpfProgram
import org.pcap4j.core.NotOpenException
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.UdpPacket
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import org.pcap4j.packet.Packet as PcapPacket

private const val ALBION_PORT = 5056
private const val SNAP_LEN = 65536
private const val PROMISCUOUS = false
private const val READ_TIMEOUT_MS = 10
private const val STATS_UPDATE_INTERVAL_MS = 1000L

internal class PcapCapturer(private val adapterManager: AdapterManager, private val coroutineScope: CoroutineScope) :
    PacketCapturer {
    private val bytesReceived = AtomicLong(0)
    private val packetsReceived = AtomicLong(0)
    private val packetsDropped = AtomicLong(0)

    private var pcapHandle: PcapHandle? = null
    private var captureJob: Job? = null

    private val _packets = MutableSharedFlow<Packet>()
    override val packets: Flow<Packet>
        get() = _packets.asSharedFlow()

    private val _stats = MutableStateFlow(CaptureStats())
    override val stats: StateFlow<CaptureStats>
        get() = _stats.asStateFlow()

    override fun start() {
        _stats.value = _stats.value.copy(isActive = true)

        coroutineScope.launch { startStatsUpdater() }

        coroutineScope.launch {
            adapterManager.selectedAdapter.collect { networkInterface ->
                captureJob?.cancelAndJoin()
                stopHandle()
                pcapHandle = openDevice(networkInterface)
                captureJob = handle()
            }
        }
    }

    private fun handle() = coroutineScope.launch {
        val handle = pcapHandle ?: return@launch
        try {
            while (coroutineScope.isActive) {
                val packet = try {
                    handle.nextPacketEx
                } catch (_: TimeoutException) {
                    continue
                } catch (_: NotOpenException) {
                    break
                }
                processPacket(packet)
            }
        } finally {
            stop()
        }
    }

    override fun stop() {
        _stats.value = _stats.value.copy(isActive = false)
        captureJob?.cancel()
        stopHandle()
        coroutineScope.cancel()
    }

    private fun stopHandle() {
        pcapHandle?.let { if (it.isOpen) it.close() }
    }

    private fun openDevice(networkInterface: NetworkInterface?): PcapHandle? {
        val deviceName = networkInterface?.device ?: return null
        val nif = Pcaps.getDevByName(deviceName)
            ?: throw IllegalArgumentException("Device not found: $deviceName")

        val mode = if (PROMISCUOUS) {
            PcapNetworkInterface.PromiscuousMode.PROMISCUOUS
        } else {
            PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS
        }

        val handle = nif.openLive(SNAP_LEN, mode, READ_TIMEOUT_MS)

        val filter = "udp and (dst port $ALBION_PORT or src port $ALBION_PORT)"
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE)

        return handle
    }

    // todo remove mb. Only isConnected
    private suspend fun startStatsUpdater() {
        var lastPackets = 0L
        var lastBytes = 0L

        while (coroutineScope.isActive) {
            delay(STATS_UPDATE_INTERVAL_MS)

            val currentPackets = packetsReceived.get()
            val currentBytes = bytesReceived.get()
            val currentDropped = packetsDropped.get()

            val pps = ((currentPackets - lastPackets) * 1000 / STATS_UPDATE_INTERVAL_MS).toInt()
            val bps = ((currentBytes - lastBytes) * 1000 / STATS_UPDATE_INTERVAL_MS)

            _stats.value = CaptureStats(
                packetsReceived = currentPackets,
                bytesReceived = currentBytes,
                packetsDropped = currentDropped,
                isActive = true,
                packetsPerSecond = pps,
                bytesPerSecond = bps
            )

            lastPackets = currentPackets
            lastBytes = currentBytes
        }
    }

    private suspend fun processPacket(packet: PcapPacket?) {
        if (packet == null) return

        val udpPacket = packet.get(UdpPacket::class.java) ?: return
        val payload = udpPacket.payload?.rawData

        if (payload == null || payload.isEmpty()) return

        packetsReceived.incrementAndGet()
        bytesReceived.addAndGet(payload.size.toLong())

        _packets.emit(Packet(ByteArrayContainer(payload)))
    }
}
