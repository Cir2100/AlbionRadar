package com.kurbat.albionradar.core.settings.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kurbat.albionradar.core.settings.AppConfig
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
) : SettingsRepository {
    private val configKey = stringPreferencesKey("app_config")

    override val updatesFlow = dataStore.data.map(::mapPreferences)

    override suspend fun load(): AppConfig = (updatesFlow.firstOrNull() ?: AppConfig.default()).withMigration()

    override suspend fun save(config: AppConfig) {
        dataStore.edit { preferences ->
            preferences[configKey] = json.encodeToString(config)
        }
    }

    // todo not use json
    private fun mapPreferences(preferences: Preferences): AppConfig = preferences[configKey]?.let { jsonString ->
        try {
            json.decodeFromString<AppConfig>(jsonString).withMigration()
        } catch (_: Exception) {
            AppConfig.default()
        }
    } ?: AppConfig.default()

    private fun AppConfig.withMigration(): AppConfig = copy(filter = filter.withMigration())
}
