package dev.kmpilot.components.media

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image as SkiaImage

/**
 * KMPilot component — **AsyncImage** (wasm target).
 *
 * Loads a remote image and renders it (cover-cropped); shows [fallback] until ready, or if it fails. Fetches
 * the bytes in the browser and decodes them with Skia. CORS-open hosts only (TheMealDB photos qualify). On
 * Android/iOS the same surface is backed by Coil/AVFoundation; this tiny wasm `actual` is the idiomatic
 * Kotlin/Wasm path. Decoded bitmaps are cached by URL.
 */
@Composable
actual fun AsyncImage(url: String, modifier: Modifier, fallback: @Composable () -> Unit) {
    var bmp by remember(url) { mutableStateOf(ImageCache.peek(url)) }
    LaunchedEffect(url) { if (bmp == null) bmp = ImageCache.load(url) }
    val b = bmp
    if (b != null) Image(b, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    else fallback()
}

private object ImageCache {
    private val cache = HashMap<String, ImageBitmap>()   // successes only (a null entry can't be told from "absent")
    private val failed = HashSet<String>()               // decoded-but-undecodable bytes → never re-decode

    fun peek(url: String): ImageBitmap? = cache[url]

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun load(url: String): ImageBitmap? {
        cache[url]?.let { return it }
        if (url in failed) return null                                // known-bad bytes: don't re-fetch/re-decode
        startFetch(url)
        repeat(400) {
            val r = readFetch(url)
            if (r.isNotEmpty()) {
                val bmp = runCatching { SkiaImage.makeFromEncoded(Base64.decode(r)).toComposeImageBitmap() }.getOrNull()
                if (bmp != null) cache[url] = bmp else failed.add(url) // bad bytes are permanent; fetch/CORS errors are not
                return bmp
            }
            delay(50)
        }
        return null                                                   // timeout (or a transient fetch error) → retried on next mount
    }
}

/** Warm the cache for a set of images (call at startup) so cards show real photos immediately. */
actual fun warmImages(scope: CoroutineScope, urls: List<String>) {
    urls.distinct().forEach { url -> scope.launch { ImageCache.load(url) } }
}

// Fetch in the browser, stash as base64 keyed by url; Kotlin polls. Avoids Promise / typed-array interop, and
// each js(...) is a function's sole statement (Kotlin/Wasm rule). The js can reference the `url` parameter.
private fun startFetch(url: String) {
    js("if(!(globalThis.__img && globalThis.__img[url])){ (globalThis.__img=globalThis.__img||{})[url]='__P'; fetch(url).then(function(r){ if(!r.ok) throw 0; return r.arrayBuffer(); }).then(function(buf){ var a=new Uint8Array(buf); var s=''; var c=0x8000; for(var i=0;i<a.length;i+=c){ s+=String.fromCharCode.apply(null,a.subarray(i,i+c)); } globalThis.__img[url]=btoa(s); }).catch(function(){ delete globalThis.__img[url]; }); }")
}

private fun readFetch(url: String): String = js("((globalThis.__img && globalThis.__img[url] && globalThis.__img[url] !== '__P') ? globalThis.__img[url] : '')")
