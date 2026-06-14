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

import android.app.UiModeManager
import android.app.settings.SettingsEnums
import android.content.Context
import android.content.res.Configuration
import android.os.PowerManager
import android.util.AttributeSet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settings.display.darkmode.DarkModeSettingsFragment
import com.android.settings.spa.preference.ComposePreference
import org.edith.settings.core.variables.Styles

class ThemeSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { ThemeContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val itemView = holder.itemView
        itemView.background = null
        itemView.minimumHeight = 0
        itemView.setPadding(0, 0, 0, 0)
    }

    @Composable
    private fun ThemeContent() {
        val context = LocalContext.current
        val uiModeManager = remember {
            context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        }
        val accentColor = Styles.getColorAccent(context)
        val textColor = Styles.getTextColorPrimary(context)

        var isDark by remember {
            mutableStateOf(
                (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
            )
        }

        fun setDarkMode(enabled: Boolean) {
            uiModeManager.setNightModeActivated(enabled)
            isDark = enabled
        }

        val powerManager = remember {
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        }
        var isBatterySaver by remember {
            mutableStateOf(powerManager.isPowerSaveMode)
        }
        DisposableEffect(context) {
            val receiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                    isBatterySaver = powerManager.isPowerSaveMode
                }
            }
            val filter = android.content.IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            onDispose { context.unregisterReceiver(receiver) }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = context.getString(R.string.device_theme),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(textColor),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = context.getString(R.string.xd_theme_dark),
                    isSelected = isDark,
                    isDarkThemePreview = true,
                    accentColor = accentColor,
                    enabled = !isBatterySaver || isDark,
                    onClick = { if (!isBatterySaver || isDark) setDarkMode(true) }
                )
                ThemePreviewCard(
                    modifier = Modifier.weight(1f),
                    title = context.getString(R.string.xd_theme_light),
                    isSelected = !isDark,
                    isDarkThemePreview = false,
                    accentColor = accentColor,
                    enabled = !isBatterySaver || !isDark,
                    onClick = { if (!isBatterySaver || !isDark) setDarkMode(false) }
                )
            }
            if (isDark) {
                Spacer(modifier = Modifier.height(12.dp))
                AdditionalOptionsRow(context, accentColor, isBatterySaver, isDark)
            }
        }
    }

    @Composable
    private fun AdditionalOptionsRow(
        context: Context,
        accentColor: Int,
        isBatterySaver: Boolean,
        isDark: Boolean
    ) {
        val cardBg = Styles.getCardContentBackgroundColor(context)
        val arrowBg = Styles.getContentBackgroundColor(context)
        val textColor = Styles.getTextColorPrimary(context)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    SubSettingLauncher(context)
                        .setDestination(DarkModeSettingsFragment::class.java.name)
                        .setSourceMetricsCategory(SettingsEnums.DISPLAY)
                        .launch()
                },
            shape = RoundedCornerShape(12.dp),
            color = Color(cardBg),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = context.getString(R.string.dark_ui_mode),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(textColor),
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(arrowBg)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(accentColor),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                if (isBatterySaver) {
                    Text(
                        text = context.getString(
                            if (isDark) R.string.dark_ui_mode_disabled_summary_dark_theme_on
                            else R.string.dark_ui_mode_disabled_summary_dark_theme_off
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Clip,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(textColor).copy(alpha = 0.6f),
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, bottom = 16.dp
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    modifier: Modifier,
    title: String,
    isSelected: Boolean,
    isDarkThemePreview: Boolean,
    accentColor: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(accentColor)
        else Color.Gray.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) Color(accentColor).copy(alpha = 0.08f)
        else Color.Transparent
    val alpha = if (enabled) 1f else 0.4f

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer { this.alpha = alpha }
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenMockup(isDarkThemePreview)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = Color(Styles.getTextColorPrimary(LocalContext.current)),
            )
        }
    }
}

@Composable
private fun ScreenMockup(isDark: Boolean) {
    val context = LocalContext.current
    val screenCorner = 8.dp

    val screenBg = if (isDark) {
        Color(context.getColor(android.R.color.system_neutral1_900))
    } else {
        Color(context.getColor(android.R.color.system_neutral1_50))
    }
    fun resolveColor(attr: Int): Color {
        val tv = android.util.TypedValue()
        val config = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (isDark) Configuration.UI_MODE_NIGHT_YES
                else Configuration.UI_MODE_NIGHT_NO
        }
        context.createConfigurationContext(config).theme.resolveAttribute(attr, tv, true)
        return Color(tv.data)
    }
    val tilePrimary = resolveColor(android.R.attr.colorAccent)
    val tileInactive = if (isDark) {
        Color(context.getColor(android.R.color.system_neutral2_800))
    } else {
        Color(context.getColor(android.R.color.system_neutral1_100))
    }
    val mediaCardBg = if (isDark) {
        Color(context.getColor(android.R.color.system_tertiary_container_light))
    } else {
        Color(context.getColor(android.R.color.system_accent3_800))
    }
    val mediaCardOnBg = if (isDark) {
        Color(context.getColor(android.R.color.system_on_tertiary_container_light))
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(screenCorner))
            .background(screenBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Status bar — time/date (left) and battery (right)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Time / date
                    Box(
                        modifier = Modifier
                            .size(width = 28.dp, height = 4.dp)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.4f)
                                else Color.Black.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    // Battery
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 4.dp)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.4f)
                                else Color.Black.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // QS tiles — 2x2 grid
            val tileColors = listOf(tilePrimary, tileInactive, tileInactive, tileInactive)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                tileColors.chunked(2).forEachIndexed { index, rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                    if (index < tileColors.chunked(2).size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Media card — music player / bottom sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(top = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(mediaCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(mediaCardOnBg.copy(alpha = 0.6f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(mediaCardOnBg.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Handle — gesture pill
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 8.dp)
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.5f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
