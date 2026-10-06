import com.kurbat.albionradar.isDebugBuild
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.buildConfig)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(projects.core.capture)
            implementation(projects.core.decode)
            implementation(projects.core.settings)
            implementation(projects.feature.settings)
            implementation(projects.feature.overlay)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutinesCore)

            implementation(libs.koin.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

buildConfig {
    packageName("com.kurbat.albionradar")

    sourceSets.named("jvmMain") {
        buildConfigField("boolean", "IS_DEBUG_BUILD", project.isDebugBuild)
    }
}

compose.desktop {
    application {
        mainClass = "com.kurbat.albionradar.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "com.kurbat.albionradar"
            packageVersion = "1.0.0"
        }
    }
}
