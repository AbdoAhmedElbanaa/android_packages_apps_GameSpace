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
 * Supports 8 visual styles, custom color tinting, size, opacity, and positioning.
 */
class CrosshairController(
    private val context: Context,
    private val wm: WindowManager,
    private val appSettings: AppSettings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var crosshairView: ImageView? = null
    private var isAdded = false

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

    fun updateState() {
        handler.post {
            if (appSettings.crosshairEnabled) {
                showCrosshair()
            } else {
                hideCrosshair()
            }
        }
    }

    private fun showCrosshair() {
        val view = crosshairView ?: ImageView(context).also {
            crosshairView = it
        }

        val styleRes = styleResMap[appSettings.crosshairStyle] ?: R.drawable.crosshair_style_1
        view.setImageResource(styleRes)
        view.setColorFilter(appSettings.crosshairColor, PorterDuff.Mode.SRC_IN)
        view.alpha = appSettings.crosshairOpacity

        val density = context.resources.displayMetrics.density
        val sizePx = (appSettings.crosshairSize * density).toInt()

        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            width = sizePx
            height = sizePx
            gravity = Gravity.CENTER
            x = (appSettings.crosshairOffsetX * density).toInt()
            y = (appSettings.crosshairOffsetY * density).toInt()
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        try {
            if (!isAdded) {
                wm.addView(view, params)
                isAdded = true
            } else {
                wm.updateViewLayout(view, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideCrosshair() {
        handler.post {
            crosshairView?.let { view ->
                if (isAdded) {
                    try {
                        wm.removeViewImmediate(view)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    isAdded = false
                }
            }
            crosshairView = null
        }
    }

    fun destroy() {
        hideCrosshair()
    }
}
