import com.kurbat.albionradar.isUseStubCapturer

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.buildConfig)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(projects.core.settings)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.koin.core)
        }

        jvmMain.dependencies {
            implementation(libs.pcap4j.core)
            implementation(libs.pcap4j.packetfactoryStatic)
            implementation(libs.slf4j.simple)
        }
    }
}

buildConfig {
    packageName("com.kurbat.albionradar.core.capture")

    sourceSets.named("jvmMain") {
        buildConfigField("boolean", "IS_USE_STUB_CAPTURER", project.isUseStubCapturer)
    }
}
