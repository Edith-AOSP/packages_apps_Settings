/*
 * Copyright (C) 2026 The Android Open Source Project
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

package org.edith.settings.deviceinfo

import android.content.Context
import android.os.SystemProperties
import android.util.AttributeSet
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.android.settings.R
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.widget.GroupSectionDividerMixin
import org.edith.settings.core.variables.Styles
import org.edith.settings.core.variables.toComposeColor

/**
 * A card showing the Edith brand: the Edith logo, an "E D I T H" title in header typography, and a
 * rounded pill badge displaying the `ro.edith.build.type` value.
 */
class SoftwareInformationCardPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes), GroupSectionDividerMixin {

    init {
        isSelectable = false
        setContent { SoftwareInformationCardContent() }
    }
}

@Composable
private fun SoftwareInformationCardContent() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)

    val shape = RoundedCornerShape(cornerRadius)

    // Bright brand-tinted scrim so it doesn't darken the waves underneath.
    val scrimColor = Color(Styles.getScrimColor(context))

    // On-primary color, used for the EDITH / version / codename text.
    val titleColor = Color(Styles.getOnPrimary(context))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            .height(160.dp)
            .clip(shape),
    ) {
        // Gradient waves animation filling the whole card background. The view is made taller
        // than the card and top-aligned so that CENTER_CROP (which anchors to the center) crops
        // the empty bottom of the 1080x500 canvas and keeps the wave shapes (drawn near the top
        // of the canvas) in view.
        AndroidView(
            factory = { ctx ->
                LottieAnimationView(ctx).apply {
                    setAnimation(R.raw.edith_waves_lottie)
                    repeatCount = LottieDrawable.INFINITE
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    applyWaveGradient(
                        this,
                        targetColor = Styles.getWaveColor(ctx),
                    )
                    playAnimation()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(320.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = 0.45f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_edith_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )

                BuildTypeBadge(
                    buildType = SystemProperties.get(EDITH_BUILD_TYPE_PROPERTY, ""),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = context.getString(R.string.edith_software_information_brand),
                modifier = Modifier.offset(x = (-1.5).dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = SystemProperties.get(EDITH_VERSION_PROPERTY, ""),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = SystemProperties.get(EDITH_VERSION_CODENAME_PROPERTY, ""),
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Tints the original authored gradient of each wave layer toward the Monet [targetColor] while
 * preserving the original multi-stop light-to-dark shading. The original gradients are
 * near-monochrome (white -> teal -> deep teal), so each stop's luminance encodes its depth in the
 * ramp; we re-apply that luminance to the target hue.
 *
 * The tint is applied as a static per-layer array with the exact stop count of the original
 * gradient (5 stops for `.wave0`..`.wave5`, 3 stops for `.wave6`), so colors and positions always
 * match and the animation cannot crash.
 */
private fun applyWaveGradient(
    view: LottieAnimationView,
    targetColor: Int,
) {
    val targetR = (targetColor shr 16 and 0xFF) / 255f
    val targetG = (targetColor shr 8 and 0xFF) / 255f
    val targetB = (targetColor and 0xFF) / 255f

    // Normalized luminance of the original 5-stop ramp (layers 0..5).
    val luminance5 = floatArrayOf(0.935f, 0.831f, 0.737f, 0.668f, 0.598f)
    // Normalized luminance of the original 3-stop ramp (layer 6).
    val luminance3 = floatArrayOf(1.0f, 0.869f, 0.737f)

    fun tint(luminances: FloatArray): Array<Int> =
        Array(luminances.size) { i ->
            val lum = luminances[i]
            val nr = (targetR * lum * 255f).toInt().coerceIn(0, 255)
            val ng = (targetG * lum * 255f).toInt().coerceIn(0, 255)
            val nb = (targetB * lum * 255f).toInt().coerceIn(0, 255)
            (0xFF shl 24) or (nr shl 16) or (ng shl 8) or nb
        }

    for (i in 0 until 6) {
        view.addValueCallback(
            KeyPath("**", ".wave$i", "**"),
            LottieProperty.GRADIENT_COLOR,
            LottieValueCallback(tint(luminance5)),
        )
    }
    view.addValueCallback(
        KeyPath("**", ".wave6", "**"),
        LottieProperty.GRADIENT_COLOR,
        LottieValueCallback(tint(luminance3)),
    )
}

@Composable
private fun BuildTypeBadge(buildType: String) {
    if (buildType.isEmpty()) {
        return
    }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Styles.getPrimary(context).toComposeColor())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildType,
            style = MaterialTheme.typography.labelMedium,
            color = Styles.getOnPrimary(context).toComposeColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val EDITH_BUILD_TYPE_PROPERTY = "ro.edith.build.type"
private const val EDITH_VERSION_PROPERTY = "ro.edith.version"
private const val EDITH_VERSION_CODENAME_PROPERTY = "ro.edith.version_codename"
