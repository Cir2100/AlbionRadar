package com.kurbat.albionradar

import com.kurbat.albionradar.properties.getProperty
import org.gradle.api.Project

val Project.isUseStubCapturer: Boolean
    get() = getProperty("isUseStubCapturer", false)

val Project.isDebugBuild: Boolean
    get() = getProperty("isDebugBuild", false)
