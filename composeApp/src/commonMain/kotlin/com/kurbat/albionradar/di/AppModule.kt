package com.kurbat.albionradar.di

import com.kurbat.albionradar.core.capture.di.coreCaptureModule
import com.kurbat.albionradar.core.settings.di.coreSettingsModule
import com.kurbat.albionradar.overlay.di.featureOverlayModule
import com.kurbat.albionradar.settings.di.featureSettingsModule
import org.koin.dsl.module

val appModule = module {
    includes(
        coreSettingsModule,
        coreCaptureModule,
        featureOverlayModule,
        featureSettingsModule
    )
}
