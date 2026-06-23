import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// Morsel — a food-delivery app (On-demand/Delivery archetype): browse → menu → cart → a LIVE MAP TRACK screen
// fed by emulated courier telemetry. Built TEST-FIRST with the full production structure: shared commonMain
// domain/logic/statecharts (money math + order/delivery state), a jvm test harness for the acceptance criteria,
// and a wasmJs UI. The map is a Canvas render; telemetry arrives over the emulator's sensor bridge.
plugins {
    kotlin("multiplatform") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    kotlin("plugin.compose") version "2.4.0"
    id("org.jetbrains.compose") version "1.11.1"
}

repositories {
    google()
    mavenCentral()
}

configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-browser:0.3")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    jvm() // unit-test harness for the shared commonMain (the acceptance criteria run here)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("com.arkivanov.decompose:decompose:3.5.0")
                implementation("io.github.nsk90:kstatemachine:0.38.1")
                implementation("io.github.nsk90:kstatemachine-coroutines:0.38.1")
                // the MapView component is an expect @Composable in commonMain (one interface, per-platform actuals)
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation("com.arkivanov.decompose:extensions-compose:3.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            }
        }
        val jvmMain by getting {
            dependencies {
                // the Compose compiler plugin is module-wide; the jvm test target needs the runtime on its
                // classpath even though it has no @Composable (the UI is wasm-only).
                implementation(compose.runtime)
            }
        }
    }
}
