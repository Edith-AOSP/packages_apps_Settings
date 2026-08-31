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

import android.app.settings.SettingsEnums
import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionSettings
import com.android.settings.spa.preference.ComposePreference
import org.edith.settings.core.variables.Styles
import org.edith.settings.core.variables.toComposeColor

/**
 * A single-line [ComposePreference] with the title on the left, the summary on the right
 * (space-between), a trailing chevron arrow, and a tertiary rounded background.
 */
class EdithVersionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        isSelectable = true
        setOnPreferenceClickListener {
            SubSettingLauncher(context)
                .setDestination(FirmwareVersionSettings::class.java.name)
                .setSourceMetricsCategory(SettingsEnums.DEVICEINFO)
                .launch()
            true
        }
        setContent { EdithVersionContent() }
    }

    @Composable
    private fun EdithVersionContent() {
        val context = LocalContext.current
        val tertiaryContainer = Styles.getTertiaryContainer(context).toComposeColor()
        val onTertiaryContainer = Styles.getOnTertiaryContainer(context).toComposeColor()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tertiaryContainer)
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title?.toString() ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = onTertiaryContainer,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val summary = summary
                if (!summary.isNullOrEmpty()) {
                    Text(
                        text = summary.toString(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onTertiaryContainer,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(onTertiaryContainer, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = tertiaryContainer,
                    )
                }
            }
        }
    }
}
