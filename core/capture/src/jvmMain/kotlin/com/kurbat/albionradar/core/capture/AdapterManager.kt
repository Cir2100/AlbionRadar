package com.kurbat.albionradar.core.capture

import com.kurbat.albionradar.core.settings.persistence.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.Pcaps
import java.net.Inet4Address

internal class AdapterManager(private val settingsRepository: SettingsRepository, coroutineScope: CoroutineScope) {

    private val _selectedAdapter: MutableStateFlow<NetworkInterface?> = MutableStateFlow(null)
    val selectedAdapter: StateFlow<NetworkInterface?>
        get() = _selectedAdapter.asStateFlow()

    private val _adapters: MutableStateFlow<List<NetworkInterface>> = MutableStateFlow(emptyList())
    val adapters: StateFlow<List<NetworkInterface>>
        get() = _adapters.asStateFlow()

    init {
        initAdapters()
        coroutineScope.launch {
            settingsRepository.updatesFlow
                .map { it.adapter.lastUsedIP }
                .collect { ip ->
                    _selectedAdapter.value = ip?.let(::findDeviceByIP)
                }
        }
    }

    private fun findDeviceByIP(ip: String): NetworkInterface? = adapters.value.firstOrNull {
        it.address == ip
    }

    private fun initAdapters() {
        _adapters.value = Pcaps.findAllDevs().mapNotNull(::firstIPv4Interface)
    }

    private fun firstIPv4Interface(device: PcapNetworkInterface): NetworkInterface? {
        device.addresses.forEach { addr ->
            val ip = addr.address
            if (ip is Inet4Address) {
                return NetworkInterface(
                    name = device.description ?: device.name,
                    address = ip.hostAddress,
                    device = device.name,
                    description = device.description ?: ""
                )
            }
        }
        return null
    }
}
