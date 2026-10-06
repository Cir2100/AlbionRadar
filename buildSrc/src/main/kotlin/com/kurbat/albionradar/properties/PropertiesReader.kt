package com.kurbat.albionradar.properties

import org.gradle.api.Project
import java.util.Properties

private lateinit var localProperties: Properties

fun Project.getLocalProperties(): Properties {
    if (::localProperties.isInitialized) return localProperties
    localProperties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            localProperties.load(stream)
        }
    }

    return localProperties
}

private fun Project.getProperty(key: String): Any? = getLocalProperties().getValue(key)

internal fun Project.getProperty(name: String, defaultValue: String): String =
    getProperty(name).asString() ?: defaultValue

internal fun Project.getProperty(name: String, defaultValue: Int): Int = getProperty(name).asInt() ?: defaultValue

internal fun Project.getProperty(name: String, defaultValue: Boolean): Boolean =
    getProperty(name).asBoolean() ?: defaultValue
