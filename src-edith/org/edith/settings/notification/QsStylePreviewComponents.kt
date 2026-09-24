/*
 * Copyright (C) 2026 Edith AOSP
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

package org.edith.settings.notification

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import org.edith.settings.core.variables.Styles

// Preview tile colors, mirroring the real QS tile roles (see res/values/edith_colors.xml).
private fun activeFill(context: Context): Color = Color(context.getColor(R.color.edith_preview_tile_active))

private fun activeContent(context: Context): Color =
    Color(context.getColor(R.color.edith_preview_tile_active_content))

private fun inactiveFill(context: Context): Color =
    Color(context.getColor(R.color.edith_preview_tile_inactive))

private fun inactiveContent(context: Context): Color =
    Color(context.getColor(R.color.edith_preview_tile_inactive_content))

/** Inner-box fill for a dual-target tile, matching the real QS tile's leading icon box. */
private fun innerFill(context: Context, active: Boolean): Color =
    Color(
        context.getColor(
            if (active) R.color.edith_preview_tile_inner_active
            else R.color.edith_preview_tile_inner_inactive
        )
    )

@Composable
internal fun str(res: Int): String = stringResource(res)

@Composable
internal fun Icon24(res: Int, tint: Color) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier.size(24.dp),
    )
}

/**
 * A Quick Settings preview tile, mirroring the real tile: active = primary/onPrimary, inactive =
 * surface effect + onSurface. The outer shape follows the real shape for the current state
 * (AOSP: 24dp active / circle inactive; Edith: 16dp active / circle inactive). When [dualState] is
 * set the tile mirrors a dual-target tile: its leading icon sits in the inner box (rounded in the
 * Edith style, circular for AOSP), matching the real inner box. A [label] maps to a single/two-line
 * text.
 */
@Composable
internal fun Tile(
    modifier: Modifier,
    active: Boolean,
    iconRes: Int,
    label: String?,
    secondary: String? = null,
    rect: Boolean = false,
    dualState: Boolean = false,
    edithOuterRadius: Dp = 16.dp,
    edithInnerRadius: Dp = 10.dp,
) {
    val context = LocalContext.current
    // Edith dual-target tiles are always the squircle (16dp), matching the active state; other
    // tiles are rounded when active (AOSP 24dp / Edith 16dp) and a circle when inactive.
    val activeOuterRadius = if (rect) 24.dp else edithOuterRadius
    val shape =
        if (active || (dualState && !rect)) RoundedCornerShape(activeOuterRadius)
        else RoundedCornerShape(percent = 50)
    // Inner box (dual-target): Edith is 10dp rounded; Vanilla is a 20dp rounded rect when active
    // and near-circular when inactive.
    val innerShape =
        when {
            !dualState -> CircleShape
            !rect -> RoundedCornerShape(edithInnerRadius)
            active -> RoundedCornerShape(20.dp)
            else -> RoundedCornerShape(percent = 50)
        }
    // The inner box nearly fills the tile height (the 64dp tile minus even insets), so the
    // left/right/bottom insets around it are tight and even.
    val innerBoxSize = 52.dp
    val innerBoxStart = 6.dp
    // Whether the leading icon sits in an inner box (dual-target tile).
    val hasInnerBox = dualState
    // A dual-target tile keeps the surface-effect background in both states, with the accent only
    // on its inner box; a single tile fills with the accent when active.
    val fill = if (active && !dualState) activeFill(context) else inactiveFill(context)
    // Label/text color on the tile surface, and the icon color inside the inner box.
    val content = if (active && !hasInnerBox) activeContent(context) else inactiveContent(context)
    val innerIcon = if (active) activeContent(context) else inactiveContent(context)
    val innerColor = innerFill(context, active)

    // Icon-only tiles center their icon; labelled tiles place the icon at the leading edge.
    if (label == null) {
        Box(
            modifier = modifier.height(64.dp).clip(shape).background(fill),
            contentAlignment = Alignment.Center,
        ) {
            if (hasInnerBox) {
                Box(
                    modifier =
                        Modifier.size(innerBoxSize)
                            .clip(innerShape)
                            .background(innerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon24(iconRes, innerIcon)
                }
            } else {
                Icon24(iconRes, content)
            }
        }
        return
    }

    Row(
        modifier = modifier.height(64.dp).clip(shape).background(fill),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasInnerBox) {
            // Dual-target tile: inner box behind the leading icon (matches the real tile), sized to
            // nearly fill the tile height so the left/right/bottom insets are tight and even.
            Box(
                modifier =
                    Modifier.padding(start = innerBoxStart)
                        .size(innerBoxSize)
                        .clip(innerShape)
                        .background(innerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon24(iconRes, innerIcon)
            }
        } else {
            // Keep the label aligned with the boxed tiles.
            Box(
                modifier = Modifier.padding(start = innerBoxStart).size(innerBoxSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon24(iconRes, content)
            }
        }

        Column(
            modifier = Modifier.padding(start = 12.dp).padding(end = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = content,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                style =
                    TextStyle(
                        lineHeightStyle =
                            LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                            )
                    ),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            if (secondary != null) {
                Text(
                    text = secondary,
                    color = content.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    style =
                        TextStyle(
                            lineHeightStyle =
                                LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both,
                                )
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

/**
 * An M3 Expressive slider mock, matching the Quick Settings brightness/volume sliders.
 *
 * Mirrors the M3 slider spec: a rounded track (corner radius 12dp at 40dp tall) that is split
 * around a tall, thin vertical pill thumb (4dp wide, taller than the track) with a thumb-track gap,
 * and the icon drawn *inside* the track at the end. [fraction] is the fill amount; [active] selects
 * the accent-filled (brightness) vs neutral look.
 */
@Composable
internal fun SliderTile(
    modifier: Modifier,
    iconRes: Int,
    fraction: Float = 0.5f,
    active: Boolean = false,
    iconSize: Dp = 24.dp,
) {
    val context = LocalContext.current
    // Colors mirror BrightnessSliderPreference: SurfaceContainerHighest inactive track, Primary for
    // the active track and thumb (neutral when not active). The icon uses onSurface so it stays
    // readable on the track in both themes (matches the real slider's inactiveTickColor).
    val trackColor = Color(Styles.getSurfaceContainerHighest(context))
    val activeColor = if (active) Color(Styles.getPrimary(context)) else inactiveFill(context)
    val thumbColor = if (active) Color(Styles.getPrimary(context)) else Color.White
    val content = Color(Styles.getOnSurface(context))

    // Proportions follow the M3 slider (see BrightnessSliderPreference): 28dp track with a 12dp
    // corner, a 4dp x 44dp thumb, and a 6dp thumb-track gap.
    val trackHeight = 28.dp
    val trackCorner = 12.dp
    val thumbWidth = 4.dp
    val thumbHeight = 44.dp
    val gap = 6.dp
    val iconInset = 8.dp

    val density = LocalDensity.current
    val trackPx = with(density) { trackHeight.toPx() }
    val trackCornerPx = with(density) { trackCorner.toPx() }
    val thumbWPx = with(density) { thumbWidth.toPx() }
    val thumbPx = with(density) { thumbHeight.toPx() }
    val gapPx = with(density) { gap.toPx() }

    Box(modifier = modifier.height(thumbHeight)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackTop = (size.height - trackPx) / 2f
            val thumbLeft = ((size.width) * fraction.coerceIn(0f, 1f) - thumbWPx / 2f)
                .coerceIn(0f, size.width - thumbWPx)

            // Active track (rounded both ends).
            val activeWidth = (thumbLeft - gapPx).coerceAtLeast(0f)
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(0f, trackTop),
                    size = Size(activeWidth, trackPx),
                    cornerRadius = CornerRadius(trackCornerPx, trackCornerPx),
                )
            }
            // Inactive track (rounded both ends).
            val inactiveLeft = thumbLeft + thumbWPx + gapPx
            val inactiveWidth = (size.width - inactiveLeft).coerceAtLeast(0f)
            if (inactiveWidth > 0f) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(inactiveLeft, trackTop),
                    size = Size(inactiveWidth, trackPx),
                    cornerRadius = CornerRadius(trackCornerPx, trackCornerPx),
                )
            }
            // Vertical pill thumb, taller than the track, centered on the split.
            val thumbTop = (size.height - thumbPx) / 2f
            val thumbRadius = CornerRadius(thumbWPx / 2f, thumbWPx / 2f)
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(thumbLeft, thumbTop),
                size = Size(thumbWPx, thumbPx),
                cornerRadius = thumbRadius,
            )
        }
        // Icon, drawn inside the track at the end.
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = iconInset)) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(content),
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/** Re-runs [onChange] whenever the [Settings.Secure] [key] changes. */
@Composable
internal fun LaunchedSettingObserver(key: String, onChange: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(key) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    onChange()
                }
            }
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(key),
            false,
            observer,
        )
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
}
