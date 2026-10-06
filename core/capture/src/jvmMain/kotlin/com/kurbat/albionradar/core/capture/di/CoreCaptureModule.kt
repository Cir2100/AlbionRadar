package com.kurbat.albionradar.core.capture.di

import com.kurbat.albionradar.core.capture.AdapterManager
import com.kurbat.albionradar.core.capture.JvmMainBuildConfig
import com.kurbat.albionradar.core.capture.PacketCapturer
import com.kurbat.albionradar.core.capture.PacketCapturerStub
import com.kurbat.albionradar.core.capture.PcapCapturer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

actual val coreCaptureModule = module {

    single {
        AdapterManager(
            settingsRepository = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO)
        )
    }

    single<PacketCapturer> {
        if (JvmMainBuildConfig.IS_USE_STUB_CAPTURER) {
            PacketCapturerStub
        } else {
            PcapCapturer(
                adapterManager = get(),
                coroutineScope = CoroutineScope(Dispatchers.IO)
            )
        }
    }
}
