package dev.kmpilot.components.location

import dev.kmpilot.components.map.LatLng
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * iOS `actual` — CoreLocation (`CLLocationManager`). A system framework, so there are NO extra Maven deps and no
 * Pods/SPM. The app must add `NSLocationWhenInUseUsageDescription` to its Info.plist; `requestWhenInUseAuthorization`
 * prompts on first use. Create/use a `CLLocationManager` on the MAIN thread (its delegate callbacks need a run loop).
 */
@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider actual constructor() {

    actual suspend fun current(): LatLng? = suspendCancellableCoroutine { cont ->
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val ll = (didUpdateLocations.lastOrNull() as? CLLocation)?.coordinate
                    ?.useContents { LatLng(latitude, longitude) }
                manager.stopUpdatingLocation()
                if (cont.isActive) cont.resume(ll)
            }
            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                if (cont.isActive) cont.resume(null)
            }
        }
        manager.delegate = delegate
        manager.requestWhenInUseAuthorization()
        manager.requestLocation()
        cont.invokeOnCancellation { manager.stopUpdatingLocation() }
    }

    actual fun updates(): Flow<LatLng> = callbackFlow {
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                (didUpdateLocations.lastOrNull() as? CLLocation)?.coordinate
                    ?.useContents { trySend(LatLng(latitude, longitude)) }
            }
        }
        manager.delegate = delegate
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
        awaitClose { manager.stopUpdatingLocation() }
    }
}
