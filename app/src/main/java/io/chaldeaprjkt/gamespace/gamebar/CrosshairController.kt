/*
 * Copyright (C) 2025-2026 RisingOS Android Project
 * Adapted from AxionOS GameSpace CrosshairController
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.gamebar

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.data.AppSettings

/**
 * Controls the crosshair overlay shown on top of game sessions.
 * Supports 8 visual styles, custom color tinting, size, and opacity.
 */
class CrosshairController(
    private val context: Context,
    private val wm: WindowManager,
    private val appSettings: AppSettings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var crosshairView: ImageView? = null
    private var isAdded = false

    // Maps style index (1–8) to drawable resource
    private val styleResMap = mapOf(
        1 to R.drawable.crosshair_style_1,
        2 to R.drawable.crosshair_style_2,
        3 to R.drawable.crosshair_style_3,
        4 to R.drawable.crosshair_style_4,
        5 to R.drawable.crosshair_style_5,
        6 to R.drawable.crosshair_style_6,
        7 to R.drawable.crosshair_style_7,
        8 to R.drawable.crosshair_style_8
    )

    /** Call when session starts or settings change to show/hide/update the crosshair. */
    fun updateState() {
        handler.post {
            if (appSettings.crosshairEnabled) {
                if (isAdded) {
                    updateCrosshairView()
                } else {
                    show()
                }
            } else {
                hide()
            }
        }
    }

    private fun show() {
        val density = context.resources.displayMetrics.density
        val sizePx = (appSettings.crosshairSize * density).toInt()

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        crosshairView = ImageView(context).also { iv ->
            updateCrosshairDrawable(iv)
        }

        try {
            wm.addView(crosshairView, params)
            isAdded = true
        } catch (e: Exception) {
            e.printStackTrace()
            crosshairView = null
        }
    }

    private fun hide() {
        if (!isAdded || crosshairView == null) return
        try {
            wm.removeView(crosshairView)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            crosshairView = null
            isAdded = false
        }
    }

    private fun updateCrosshairView() {
        crosshairView?.let { updateCrosshairDrawable(it) }
    }

    private fun updateCrosshairDrawable(view: ImageView) {
        val styleRes = styleResMap[appSettings.crosshairStyle] ?: R.drawable.crosshair_style_1
        val opacity  = appSettings.crosshairOpacity / 100f
        val color    = appSettings.crosshairColor

        view.setImageResource(styleRes)
        view.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        view.alpha = opacity
    }

    /** Called when the game session ends — removes the crosshair cleanly. */
    fun destroy() {
        handler.post { hide() }
    }
}
