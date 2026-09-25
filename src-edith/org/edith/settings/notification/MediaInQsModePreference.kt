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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposeGroupSectionPreference
import org.edith.settings.core.variables.Styles

/**
 * Compose preference presenting a three-option (Always / Dynamic / Disabled) selector for where
 * the compact media player appears in Quick Settings. Mirrors [QsStylePreference]'s pill style.
 */
class MediaInQsModePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) :
    ComposeGroupSectionPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { MediaInQsModeContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setPadding(0, 0, 0, 0)
    }
}

@Composable
private fun MediaInQsModeContent() {
    val context = LocalContext.current
    var value by remember { mutableStateOf(EdithMediaInQs.read(context)) }

    fun select(v: Int) {
        if (v == value) return
        EdithMediaInQs.write(context, v)
        value = v
    }

    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(cornerRadius)),
        color = Color(Styles.getSurfaceBright(context)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = str(R.string.edith_media_in_qs_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color(Styles.getTextColorPrimary(context)),
            )
            Box(modifier = Modifier.height(4.dp))
            Text(
                text =
                    str(
                        when (value) {
                            EdithMediaInQs.VALUE_ALWAYS -> R.string.edith_media_in_qs_summary_always
                            EdithMediaInQs.VALUE_DISABLED ->
                                R.string.edith_media_in_qs_summary_disabled
                            else -> R.string.edith_media_in_qs_summary_dynamic
                        }
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = Color(Styles.getOnSurfaceVariant(context)),
            )
            Box(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MediaInQsOption(
                    modifier = Modifier.weight(1f),
                    text = str(R.string.edith_media_in_qs_always),
                    selected = value == EdithMediaInQs.VALUE_ALWAYS,
                    onClick = { select(EdithMediaInQs.VALUE_ALWAYS) },
                )
                MediaInQsOption(
                    modifier = Modifier.weight(1f),
                    text = str(R.string.edith_media_in_qs_dynamic),
                    selected = value == EdithMediaInQs.VALUE_DYNAMIC,
                    onClick = { select(EdithMediaInQs.VALUE_DYNAMIC) },
                )
                MediaInQsOption(
                    modifier = Modifier.weight(1f),
                    text = str(R.string.edith_media_in_qs_disabled),
                    selected = value == EdithMediaInQs.VALUE_DISABLED,
                    onClick = { select(EdithMediaInQs.VALUE_DISABLED) },
                )
            }
        }
    }

    LaunchedSettingObserver(EdithMediaInQs.KEY) { value = EdithMediaInQs.read(context) }
}

@Composable
private fun MediaInQsOption(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val bg =
        if (selected) Color(context.getColor(android.R.color.system_accent1_100))
        else Styles.getSurfaceVariant(context).let { Color(it) }
    val fg =
        if (selected) Color(context.getColor(android.R.color.system_accent1_900))
        else Color(Styles.getTextColorPrimary(context))

    Box(
        modifier =
            modifier
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(bg)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
