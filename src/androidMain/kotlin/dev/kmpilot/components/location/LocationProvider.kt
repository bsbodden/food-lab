package dev.kmpilot.components.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import dev.kmpilot.components.map.LatLng
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Android `actual` — reads the device GPS via the platform `LocationManager` (no Google Play Services dep, so it
 * works on every Android). The app must hold a location permission + seed [LocationContext]; without either,
 * [current] returns null and [updates] completes empty (never throws). `@SuppressLint("MissingPermission")` — the
 * permission is the app's responsibility (documented), not this adapter's.
 */
actual class LocationProvider actual constructor() {

    private fun manager(): LocationManager? =
        LocationContext.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    @SuppressLint("MissingPermission")
    actual suspend fun current(): LatLng? {
        val m = manager() ?: return null
        val provider = m.getProviders(true).firstOrNull() ?: return null
        m.getLastKnownLocation(provider)?.let { return LatLng(it.latitude, it.longitude) }
        return suspendCancellableCoroutine { cont ->
            val listener = oneShot { loc -> if (cont.isActive) cont.resume(LatLng(loc.latitude, loc.longitude)) }
            runCatching { m.requestSingleUpdate(provider, listener, Looper.getMainLooper()) }
                .onFailure { if (cont.isActive) cont.resume(null) }
            cont.invokeOnCancellation { runCatching { m.removeUpdates(listener) } }
        }
    }

    @SuppressLint("MissingPermission")
    actual fun updates(): Flow<LatLng> = callbackFlow {
        val m = manager() ?: run { close(); return@callbackFlow }
        val provider = m.getProviders(true).firstOrNull() ?: run { close(); return@callbackFlow }
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) { trySend(LatLng(loc.latitude, loc.longitude)) }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        runCatching { m.requestLocationUpdates(provider, 2000L, 5f, listener, Looper.getMainLooper()) }
            .onFailure { close() }
        awaitClose { runCatching { m.removeUpdates(listener) } }
    }

    // minSdk 24: LocationListener has the (now-deprecated) status callbacks as abstract members, so a SAM lambda
    // won't compile — wrap a single-fix callback in a full object that removes itself after the first location.
    private fun oneShot(onLoc: (Location) -> Unit) = object : LocationListener {
        override fun onLocationChanged(loc: Location) = onLoc(loc)
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}
