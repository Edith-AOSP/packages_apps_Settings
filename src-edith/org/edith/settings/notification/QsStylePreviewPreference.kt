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
import android.util.AttributeSet
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceViewHolder
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settings.spa.preference.ComposeGroupSectionPreference
import org.edith.settings.core.variables.Styles
import org.edith.settings.core.variables.toComposeColor
import org.edith.settings.widget.WallpaperBlurView

/**
 * Compose preference that shows a frosted, live preview of the Quick Settings panel, crossfading
 * between the EdithUI and AOSP layouts as the style setting changes.
 */
class QsStylePreviewPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) :
    ComposeGroupSectionPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { QsStylePreviewContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        // The Compose content owns all padding.
        holder.itemView.setPadding(0, 0, 0, 0)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QsStylePreviewContent() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    val scrim = Styles.getCardScrimColor(context).toComposeColor()

    var value by remember { mutableStateOf(EdithQsStyle.read(context)) }

    // Hidden entry point to the tile tuner: long-press the preview, but only while the EdithUI
    // style is selected.
    val openTuner = {
        if (value == EdithQsStyle.VALUE_EDITHUI) {
            SubSettingLauncher(context)
                .setDestination(EdithQsTileColorFragment::class.java.name)
                .setTitleRes(R.string.edith_tile_color_screen_title)
                .setSourceMetricsCategory(MetricsEvent.EDITH)
                .launch()
        }
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(330.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .combinedClickable(onClick = {}, onLongClick = openTuner)
    ) {
        // Frosted wallpaper background.
        AndroidView(factory = { ctx -> WallpaperBlurView(ctx) }, modifier = Modifier.fillMaxSize())

        // Scrim, matching the reference (alpha 0.2).
        Box(modifier = Modifier.fillMaxSize().background(scrim.copy(alpha = 0.2f)))

        Crossfade(targetState = value, label = "QsStylePreview") { v ->
            if (v == EdithQsStyle.VALUE_EDITHUI) EdithUiPanel() else AospPanel()
        }
    }

    // Keep the local state in sync with any external change to the setting.
    LaunchedSettingObserver(EdithQsStyle.KEY) { value = EdithQsStyle.read(context) }
}

@Composable
private fun EdithUiPanel() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        // Row 1: Internet (active, label + secondary) | Mobile data (inactive, chip).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Tile(
                modifier = Modifier.weight(1f),
                active = true,
                iconRes = R.drawable.edith_qs_wifi_full,
                label = str(R.string.edith_qs_style_internet),
                secondary = str(R.string.edith_qs_style_ssid),
                alignToChip = true,
            )
            Tile(
                modifier = Modifier.weight(1f),
                active = false,
                iconRes = R.drawable.edith_qs_mobile_data,
                label = str(R.string.edith_qs_style_mobile_data),
                chip = true,
            )
        }
        // Row 2: Bluetooth (inactive, chip) | Hotspot (inactive, plain icon).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Tile(
                modifier = Modifier.weight(1f),
                active = false,
                iconRes = R.drawable.edith_qs_bluetooth_on,
                label = str(R.string.edith_qs_style_bluetooth),
                chip = true,
            )
            Tile(
                modifier = Modifier.weight(1f),
                active = false,
                iconRes = R.drawable.edith_qs_hotspot,
                label = str(R.string.edith_qs_style_hotspot),
                alignToChip = true,
            )
        }
        // Row 3: brightness track.
        Row(modifier = Modifier.fillMaxWidth()) {
            SliderTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.edith_qs_brightness,
                fraction = 0.7f,
                active = true,
                iconSize = 18.dp,
            )
        }
        // Row 4: volume track.
        Row(modifier = Modifier.fillMaxWidth()) {
            SliderTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.edith_qs_volume,
                fraction = 0.4f,
                active = true,
                iconSize = 18.dp,
            )
        }
    }
}

@Composable
private fun AospPanel() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        // Brightness slider (first).
        Row(modifier = Modifier.fillMaxWidth()) {
            SliderTile(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.edith_qs_brightness,
                fraction = 0.7f,
                active = true,
                iconSize = 18.dp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Tile(
                modifier = Modifier.weight(1f),
                active = true,
                rect = true,
                iconRes = R.drawable.edith_qs_wifi_full,
                label = str(R.string.edith_qs_style_internet),
                secondary = str(R.string.edith_qs_style_ssid),
            )
            Tile(
                modifier = Modifier.weight(1f),
                active = false,
                rect = true,
                iconRes = R.drawable.edith_qs_bluetooth_on,
                label = str(R.string.edith_qs_style_bluetooth),
                chip = true,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Tile(
                modifier = Modifier.weight(1f),
                active = false,
                rect = true,
                iconRes = R.drawable.edith_qs_heads_up,
                label = str(R.string.edith_qs_style_heads_up),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Tile(
                    modifier = Modifier.weight(1f),
                    active = true,
                    rect = true,
                    iconRes = R.drawable.edith_qs_dnd,
                    label = null,
                )
                Tile(
                    modifier = Modifier.weight(1f),
                    active = false,
                    rect = true,
                    iconRes = R.drawable.edith_qs_wallet,
                    label = null,
                )
            }
        }
    }
}
