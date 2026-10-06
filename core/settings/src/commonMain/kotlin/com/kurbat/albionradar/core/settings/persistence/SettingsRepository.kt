package com.kurbat.albionradar.core.settings.persistence

import com.kurbat.albionradar.core.settings.AppConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val updatesFlow: Flow<AppConfig>
    suspend fun load(): AppConfig
    suspend fun save(config: AppConfig)
}
