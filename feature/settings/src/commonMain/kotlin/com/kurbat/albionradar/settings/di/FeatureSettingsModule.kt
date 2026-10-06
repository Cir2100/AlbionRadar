package com.kurbat.albionradar.settings.di

import com.kurbat.albionradar.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSettingsModule = module {
    viewModelOf(::SettingsViewModel)
}
