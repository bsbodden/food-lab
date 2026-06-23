import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// Morsel — a food-delivery app (On-demand/Delivery archetype): browse → menu → cart → a LIVE MAP TRACK screen
// fed by emulated courier telemetry. Built TEST-FIRST with the full production structure: shared commonMain
// domain/logic/statecharts (money math + order/delivery state) AND the shared Compose UI, a jvm test harness
// for the acceptance criteria, and the REAL mobile targets (Android + iOS) plus the wasmJs in-browser emulator.
// The map is a Canvas/MapLibre render; telemetry arrives over the emulator's sensor bridge.
plugins {
    kotlin("multiplatform") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    kotlin("plugin.compose") version "2.4.0"
    id("org.jetbrains.compose") version "1.11.1"
    id("com.android.application") version "8.13.2" // last AGP 8.x — still supports single-module KMP (AGP 9 dropped it)
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
    androidTarget() // the Android target → APK (buildable on this Linux box with the SDK)
    // iOS targets — the REAL mobile target. Declaring them is fine on Linux; only the compile/link needs macOS.
    // Skip iosX64 (MapLibre Compose has no iosX64 artifact; modern sims are arm64).
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "FoodApp"; isStatic = true }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("com.arkivanov.decompose:decompose:3.5.0")
                implementation("io.github.nsk90:kstatemachine:0.38.1")
                implementation("io.github.nsk90:kstatemachine-coroutines:0.38.1")
                // the shared Compose UI now lives in commonMain (Android/iOS/wasm) — NOT wasm-only
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation("com.arkivanov.decompose:extensions-compose:3.5.0")
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
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.10.1")
                // Coil3 — real remote-image loading on Android. Uses Coil's BUNDLED OkHttp network layer
                // (coil-network-okhttp) rather than the coil-network-ktor3 path: ktor-client-core 3.2.0 ships
                // methods with spaces in their names, which D8 rejects on this minSdk (needs DEX format 040),
                // breaking dexing of the APK. coil-network-okhttp auto-registers the same network fetcher and
                // pulls no Ktor, so it dexes cleanly and produces identical real-image behavior. (iOS keeps the
                // coil-network-ktor3 + ktor-client-darwin path — Kotlin/Native has no dexing step.)
                implementation("io.coil-kt.coil3:coil-compose:3.2.0")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")
            }
        }
        // iosMain is the intermediate source set created by the default hierarchy template; it does not exist
        // yet when this sourceSets {} block body runs, so configure it via the live `all {}` hook which fires
        // when the template materializes it (eager `by getting` / `named()` are too early here).
        all {
            if (name == "iosMain") {
                dependencies {
                    // Coil3 — real remote-image loading on iOS. coil-network-ktor3 + the Darwin Ktor
                    // engine AUTO-register the network fetcher; no manual ImageLoader setup needed.
                    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
                    implementation("io.coil-kt.coil3:coil-network-ktor3:3.2.0")
                    implementation("io.ktor:ktor-client-darwin:3.2.0")
                }
            }
        }
    }
}

android {
    namespace = "dev.kmpilot.food"
    compileSdk = 35
    defaultConfig {
        applicationId = "dev.kmpilot.food"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
