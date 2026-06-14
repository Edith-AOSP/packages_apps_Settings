/*
 * Copyright (C) 2026 xdroidOSS, NauzyxLabs
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

package org.edith.settings.display

import android.content.Context
import android.database.ContentObserver
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.view.Display
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.display.BrightnessUtils
import org.edith.settings.core.variables.Styles
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
class BrightnessSliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { BrightnessSliderContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val itemView = holder.itemView
        itemView.background = null
        itemView.minimumHeight = 0
        itemView.setPadding(0, 0, 0, 0)
    }

    @Composable
    private fun BrightnessSliderContent() {
        val context = LocalContext.current
        val resolver = context.contentResolver
        val displayManager = remember {
            context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        }
        val maxBrightness = remember {
            context.resources.getInteger(
                com.android.internal.R.integer.config_screenBrightnessSettingMaximum
            )
        }

        fun readGammaBrightness(): Int {
            val linear = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0)
            return BrightnessUtils.convertLinearToGamma(linear, 0, maxBrightness)
        }

        fun writeGammaBrightness(gamma: Int) {
            val linear = BrightnessUtils.convertGammaToLinear(gamma, 0, maxBrightness)
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, linear)
        }

        var currentBrightness by remember {
            mutableIntStateOf(readGammaBrightness())
        }

        var isDragging by remember { mutableStateOf(false) }

        val animatedValue by animateFloatAsState(
            targetValue = currentBrightness.toFloat(),
            animationSpec = if (isDragging) snap() else tween(durationMillis = 300),
            label = "brightnessAnimation",
        )

        val percentage by remember(currentBrightness) {
            derivedStateOf {
                val pct = (currentBrightness * 100f / BrightnessUtils.GAMMA_SPACE_MAX).roundToInt()
                pct.coerceIn(0, 100)
            }
        }

        DisposableEffect(resolver, displayManager) {
            val handler = Handler(Looper.getMainLooper())
            val uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
            val observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    if (!isDragging) {
                        currentBrightness = readGammaBrightness()
                    }
                }
            }
            resolver.registerContentObserver(uri, false, observer)

            val displayListener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {}
                override fun onDisplayRemoved(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {
                    if (displayId == Display.DEFAULT_DISPLAY && !isDragging) {
                        currentBrightness = readGammaBrightness()
                    }
                }
            }
            displayManager.registerDisplayListener(displayListener, handler)

            onDispose {
                resolver.unregisterContentObserver(observer)
                displayManager.unregisterDisplayListener(displayListener)
            }
        }

        val sliderColors = SliderDefaults.colors().copy(
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            thumbColor = MaterialTheme.colorScheme.primary,
        )
        val interactionSource = remember { MutableInteractionSource() }
        val dragged by interactionSource.collectIsDraggedAsState()
        val thumbWidth by animateDpAsState(
            targetValue = if (dragged) 2.dp else 4.dp,
            animationSpec = tween(durationMillis = 150),
            label = "thumbWidth",
        )
        val thumbHeight by animateDpAsState(
            targetValue = if (dragged) 36.dp else 44.dp,
            animationSpec = tween(durationMillis = 150),
            label = "thumbHeight",
        )

        val titleColor = Styles.getTextColorPrimary(context)
        val summaryColor = Color(titleColor).copy(alpha = 0.6f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.brightness),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(titleColor),
                )
                Text(
                    text = "${percentage}%",
                    maxLines = 1,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = summaryColor,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = animatedValue,
                onValueChange = {
                    isDragging = true
                    val value = it.toInt().coerceIn(
                        BrightnessUtils.GAMMA_SPACE_MIN, BrightnessUtils.GAMMA_SPACE_MAX
                    )
                    currentBrightness = value
                    writeGammaBrightness(value)
                },
                onValueChangeFinished = {
                    isDragging = false
                    val value = currentBrightness.coerceIn(
                        BrightnessUtils.GAMMA_SPACE_MIN, BrightnessUtils.GAMMA_SPACE_MAX
                    )
                    writeGammaBrightness(value)
                },
                valueRange = BrightnessUtils.GAMMA_SPACE_MIN.toFloat()..BrightnessUtils.GAMMA_SPACE_MAX.toFloat(),
                colors = sliderColors,
                interactionSource = interactionSource,
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        colors = sliderColors,
                        thumbSize = DpSize(thumbWidth, thumbHeight),
                    )
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = sliderColors,
                        modifier = Modifier.height(28.dp),
                        trackCornerSize = 12.dp,
                        drawStopIndicator = null,
                        thumbTrackGapSize = 6.dp,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(36.dp),
            )
        }
    }
}
