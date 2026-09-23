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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposeGroupSectionPreference
import org.edith.settings.core.variables.Styles

/**
 * Debug Compose preference for tuning the Edith QS tertiary tile colors.
 *
 * Shows a live preview (active squircle + inactive circle) plus a swatch grid per color slot. Each
 * slot stores a swatch *tag* (not a resolved color) so SystemUI re-resolves it against the current
 * theme. Only shown when [EdithTileColor.TUNER_PROP] is set.
 */
class EdithTileColorTunerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) :
    ComposeGroupSectionPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { EdithTileColorTunerContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setPadding(0, 0, 0, 0)
    }
}

/** A color slot the tuner can edit. */
private class Slot(val key: String, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EdithTileColorTunerContent() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)

    val slots =
        remember {
            listOf(
                Slot(EdithTileColor.KEY_ACTIVE_BG, "Active background"),
                Slot(EdithTileColor.KEY_ACTIVE_FG, "Active glyph"),
                Slot(EdithTileColor.KEY_INACTIVE_BG, "Inactive background"),
                Slot(EdithTileColor.KEY_INACTIVE_FG, "Inactive glyph"),
            )
        }

    var activeBg by remember { mutableStateOf(EdithTileColor.readTag(context, EdithTileColor.KEY_ACTIVE_BG)) }
    var activeFg by remember { mutableStateOf(EdithTileColor.readTag(context, EdithTileColor.KEY_ACTIVE_FG)) }
    var inactiveBg by remember { mutableStateOf(EdithTileColor.readTag(context, EdithTileColor.KEY_INACTIVE_BG)) }
    var inactiveFg by remember { mutableStateOf(EdithTileColor.readTag(context, EdithTileColor.KEY_INACTIVE_FG)) }
    var inactiveAlpha by remember {
        mutableFloatStateOf(
            EdithTileColor.readInt(context, EdithTileColor.KEY_INACTIVE_ALPHA).let {
                if (it == EdithTileColor.UNSET_INT) 54f else it.toFloat()
            }
        )
    }

    fun sync() {
        activeBg = EdithTileColor.readTag(context, EdithTileColor.KEY_ACTIVE_BG)
        activeFg = EdithTileColor.readTag(context, EdithTileColor.KEY_ACTIVE_FG)
        inactiveBg = EdithTileColor.readTag(context, EdithTileColor.KEY_INACTIVE_BG)
        inactiveFg = EdithTileColor.readTag(context, EdithTileColor.KEY_INACTIVE_FG)
        inactiveAlpha =
            EdithTileColor.readInt(context, EdithTileColor.KEY_INACTIVE_ALPHA).let {
                if (it == EdithTileColor.UNSET_INT) 54f else it.toFloat()
            }
    }

    for (key in EdithTileColor.ALL_KEYS) {
        LaunchedSettingObserver(key) { sync() }
    }

    // Defaults for the preview (role-based), used when a slot is unset.
    val defaultActiveBg = Color(context.getColor(com.android.internal.R.color.materialColorTertiaryDim))
    val defaultActiveFg = Color(context.getColor(com.android.internal.R.color.materialColorOnTertiary))
    val defaultInactiveBg = Color(context.getColor(com.android.internal.R.color.materialColorOnTertiary))
    val defaultInactiveFg = Color(context.getColor(com.android.internal.R.color.materialColorTertiary))

    fun colorOf(tag: String, default: Color): Color =
        EdithTileSwatches.resolve(context, tag)?.let { Color(it) } ?: default

    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(cornerRadius)),
        color = Color(Styles.getSurfaceBright(context)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // --- Preview ---
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                color = Color(Styles.getTextColorPrimary(context)),
            )
            Box(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewTile(
                    label = "active",
                    subLabel = "bg ${nameOf(activeBg)} / fg ${nameOf(activeFg)}",
                    fill = colorOf(activeBg, defaultActiveBg).copy(alpha = 1f),
                    content = colorOf(activeFg, defaultActiveFg),
                    shape = RoundedCornerShape(16.dp),
                )
                PreviewTile(
                    label = "inactive",
                    subLabel = "bg ${nameOf(inactiveBg)} / fg ${nameOf(inactiveFg)}",
                    fill = colorOf(inactiveBg, defaultInactiveBg).copy(alpha = inactiveAlpha / 100f),
                    content = colorOf(inactiveFg, defaultInactiveFg),
                    shape = CircleShape,
                )
            }

            // --- Slots ---
            for (slot in slots) {
                Box(modifier = Modifier.height(16.dp))
                val current =
                    when (slot.key) {
                        EdithTileColor.KEY_ACTIVE_BG -> activeBg
                        EdithTileColor.KEY_ACTIVE_FG -> activeFg
                        EdithTileColor.KEY_INACTIVE_BG -> inactiveBg
                        else -> inactiveFg
                    }
                Text(
                    text = "${slot.title}: ${nameOf(current)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(Styles.getTextColorPrimary(context)),
                )
                Box(modifier = Modifier.height(6.dp))
                SwatchGrid(
                    selected = current,
                    onPick = { tag -> EdithTileColor.writeTag(context, slot.key, tag) },
                )
            }

            // --- Alpha ---
            Box(modifier = Modifier.height(16.dp))
            Text(
                text = "inactive alpha: ${inactiveAlpha.toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                color = Color(Styles.getTextColorPrimary(context)),
            )
            Slider(
                value = inactiveAlpha,
                valueRange = 0f..100f,
                onValueChange = { inactiveAlpha = it },
                onValueChangeFinished = {
                    EdithTileColor.writeInt(
                        context,
                        EdithTileColor.KEY_INACTIVE_ALPHA,
                        inactiveAlpha.toInt(),
                    )
                },
            )

            // --- Reset ---
            Box(modifier = Modifier.height(8.dp))
            TextButton(onClick = { EdithTileColor.reset(context) }) {
                Text(stringResource(R.string.edith_tile_color_reset))
            }
        }
    }
}

/** Display name for a swatch tag ("default" when unset). */
private fun nameOf(tag: String): String = tag.ifEmpty { "default" }

@Composable
private fun PreviewTile(label: String, subLabel: String, fill: Color, content: Color, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(shape).background(fill),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(content))
        }
        Box(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = subLabel, style = MaterialTheme.typography.labelSmall)
    }
}

/** A swatch grid of the accent3 (tertiary) tone ramp, 6 per row. */
@Composable
private fun SwatchGrid(selected: String, onPick: (String) -> Unit) {
    val context = LocalContext.current
    val tags = remember { EdithTileSwatches.pickableTags() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.chunked(6).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (tag in rowItems) {
                    val argb = EdithTileSwatches.resolve(context, tag)
                    val isSelected = tag == selected && selected.isNotEmpty()
                    Box(
                        modifier =
                            Modifier.size(40.dp)
                                .clip(CircleShape)
                                .background(if (argb != null) Color(argb) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color =
                                        if (isSelected) {
                                            Color(Styles.getTextColorPrimary(context))
                                        } else {
                                            Color(Styles.getOnSurfaceVariant(context))
                                                .copy(alpha = 0.4f)
                                        },
                                    shape = CircleShape,
                                )
                                .clickable { onPick(tag) }
                    )
                }
            }
        }
    }
}
