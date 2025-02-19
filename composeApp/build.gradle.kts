import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            api("co.touchlab:kermit:2.0.4")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation("com.microsoft.onnxruntime:onnxruntime:1.20.0")
            implementation("com.microsoft.onnxruntime:onnxruntime_gpu:1.20.0")

            implementation("uk.co.caprica:vlcj:4.8.2")
            implementation("com.github.kwhat:jnativehook:2.2.2")
            implementation("net.java.dev.jna:jna:5.13.0")
            implementation("net.java.dev.jna:jna-platform:5.13.0")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.ogzkesk.marvel.test.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.ogzkesk.marvel.test"
            packageVersion = "1.0.0"
        }
    }
}

