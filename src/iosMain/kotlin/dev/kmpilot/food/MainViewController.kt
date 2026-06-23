package dev.kmpilot.food

import androidx.compose.ui.window.ComposeUIViewController
import dev.kmpilot.food.ui.App
import dev.kmpilot.food.ui.buildRoot
import platform.UIKit.UIViewController

/** iOS entrypoint — the iosApp Xcode project hosts this in a SwiftUI UIViewControllerRepresentable. */
fun MainViewController(): UIViewController = ComposeUIViewController { App(buildRoot()) }
