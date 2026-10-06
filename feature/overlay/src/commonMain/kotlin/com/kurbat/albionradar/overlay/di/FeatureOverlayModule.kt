package com.kurbat.albionradar.overlay.di

import com.kurbat.albionradar.overlay.presentation.OverlayViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureOverlayModule = module {
    viewModelOf(::OverlayViewModel)
}
