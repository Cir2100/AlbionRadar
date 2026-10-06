package com.kurbat.albionradar.core.settings.di

import com.kurbat.albionradar.core.settings.persistence.DataStoreSettingsRepository
import com.kurbat.albionradar.core.settings.persistence.SettingsRepository
import com.kurbat.albionradar.core.settings.persistence.createDataStore
import org.koin.dsl.module
import java.io.File

val coreSettingsModule = module {
    single<SettingsRepository> {
        val appDir = System.getProperty("user.home") + File.separator + ".albionradar"
        DataStoreSettingsRepository(
            dataStore = createDataStore("/Users/cir2100/Desktop/albionRadar") // todo path
        )
    }
}
