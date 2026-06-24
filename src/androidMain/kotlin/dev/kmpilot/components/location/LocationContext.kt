package dev.kmpilot.components.location

import android.content.Context

/**
 * Holds the application [Context] that [LocationProvider] needs to reach the system `LocationManager`.
 * Seed it once from your Activity/Application before using a [LocationProvider]:
 * `LocationContext.applicationContext = applicationContext` (e.g. in `MainActivity.onCreate`).
 */
object LocationContext {
    var applicationContext: Context? = null
}
