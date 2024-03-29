// adapted from https://github.com/m-derakhshan/DarkMode

package com.example.readr

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView

class ScreenshotState internal constructor() {
    private val _bitmap = mutableStateOf<Bitmap?>(null)
    val bitmap: State<Bitmap?> = _bitmap

    fun setBitmap(bitmap: Bitmap?) {
        _bitmap.value = bitmap
    }

    internal var callback: (() -> Unit)? = null
    fun capture() {
        callback?.invoke()
    }
}


fun View.screenshot(
    bounds: Rect,
    bitmapCallback: (Bitmap?) -> Unit
) {
    try {
        val bitmap = Bitmap.createBitmap(
            bounds.width.toInt(),
            bounds.height.toInt(),
            Bitmap.Config.ARGB_8888,
        )
        PixelCopy.request(
            (this.context as Activity).window,
            android.graphics.Rect(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            ),
            bitmap,
            {
                when (it) {
                    PixelCopy.SUCCESS -> {
                        bitmapCallback.invoke(bitmap)
                    }

                    else -> {
                        bitmapCallback.invoke(null)
                    }
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        bitmapCallback.invoke(null)
    }
}

@Composable
fun rememberScreenshotState() = remember {
    ScreenshotState()
}


@Composable
fun ScreenshotScope(
    modifier: Modifier = Modifier,
    screenshotState: ScreenshotState,
    content: @Composable () -> Unit,
) {
    val view: View = LocalView.current

    var composableBounds by remember { mutableStateOf<Rect?>(null) }

    DisposableEffect(Unit) {
        screenshotState.callback = {
            composableBounds?.let { bounds ->
                if (bounds.width == 0f || bounds.height == 0f) return@let
                view.screenshot(bounds) { resultBitmap: Bitmap? ->
                    screenshotState.setBitmap(resultBitmap)
                }
            }
        }

        onDispose {
            screenshotState.bitmap.value?.apply {
                if (!isRecycled) {
                    recycle()
                }
            }
            screenshotState.setBitmap(null)
            screenshotState.callback = null
        }
    }

    Box(modifier = modifier
        .onGloballyPositioned {
            composableBounds = it.boundsInWindow()
        }
    ) {
        content()
    }
}


