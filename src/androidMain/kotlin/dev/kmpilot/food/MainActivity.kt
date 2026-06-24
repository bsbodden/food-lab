package dev.kmpilot.food

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.kmpilot.components.location.LocationContext
import dev.kmpilot.food.ui.App
import dev.kmpilot.food.ui.buildRoot

/** Android entrypoint — hosts the shared App() in a ComponentActivity. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // seed the context the location/LocationProvider component needs, and ask for the GPS permission up front
        LocationContext.applicationContext = applicationContext
        runCatching { requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1) }
        setContent { App(buildRoot()) }
    }
}
