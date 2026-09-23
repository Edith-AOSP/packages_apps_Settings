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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposeGroupSectionPreference
import org.edith.settings.core.variables.Styles

/**
 * Compose preference for tuning the Edith QS tiles.
 *
 * Three tabs:
 *  - **QS colors** — the main QS grid tile colors (accent3/tertiary catalog).
 *  - **Quick Actions colors** — the 2x2 Quick Actions tile colors (role/primary catalog).
 *  - **Quick Actions shape** — the dual-state (dual-target) tile's outer + inner box corner radii.
 *
 * Each color slot stores a swatch *tag* (not a resolved color) so SystemUI re-resolves it against
 * the current theme.
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

private enum class TunerTab(val titleRes: Int) {
    QsColors(R.string.edith_tile_color_tab_qs),
    QuickActionsColors(R.string.edith_tile_color_tab_qa_colors),
    QuickActionsShape(R.string.edith_tile_color_tab_qa_shape),
}

@Composable
private fun EdithTileColorTunerContent() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    var selectedTab by remember { mutableIntStateOf(0) }

    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(cornerRadius)),
        color = Color(Styles.getSurfaceBright(context)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            val tabs = remember { TunerTab.values() }
            // A pill selector styled like the QS style selector (see QsStylePreference.Pill): it
            // reads naturally inside the rounded card, unlike a full-width tab row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                tabs.forEachIndexed { index, tab ->
                    TunerPill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(tab.titleRes),
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                    )
                }
            }
            Box(modifier = Modifier.height(16.dp))

            when (tabs[selectedTab]) {
                TunerTab.QsColors -> ColorsTab(scope = ColorScope.QS)
                TunerTab.QuickActionsColors -> ColorsTab(scope = ColorScope.QuickActions)
                TunerTab.QuickActionsShape -> ShapeTab()
            }
        }
    }
}

/** A selectable pill, styled identically to the QS style selector pill. */
@Composable
private fun TunerPill(modifier: Modifier, text: String, selected: Boolean, onClick: () -> Unit) {
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
        Text(text = text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

private enum class ColorScope {
    QS,
    QuickActions,
}

/** Which swatch catalog a colors tab uses. */
private fun swatchResolver(scope: ColorScope): (Context, String?) -> Int? =
    when (scope) {
        ColorScope.QS -> { context, tag -> EdithTileSwatches.resolve(context, tag) }
        ColorScope.QuickActions -> { context, tag -> QuickActionsTileSwatches.resolve(context, tag) }
    }

private fun swatchTags(scope: ColorScope): List<String> =
    when (scope) {
        ColorScope.QS -> EdithTileSwatches.pickableTags()
        ColorScope.QuickActions -> QuickActionsTileSwatches.pickableTags()
    }

@Composable
private fun ColorsTab(scope: ColorScope) {
    val context = LocalContext.current
    val keys =
        when (scope) {
            ColorScope.QS ->
                listOf(
                    EdithTileColor.KEY_ACTIVE_BG,
                    EdithTileColor.KEY_ACTIVE_FG,
                    EdithTileColor.KEY_INACTIVE_BG,
                    EdithTileColor.KEY_INACTIVE_FG,
                )
            ColorScope.QuickActions ->
                listOf(
                    EdithTileColor.QA_KEY_ACTIVE_BG,
                    EdithTileColor.QA_KEY_ACTIVE_FG,
                    EdithTileColor.QA_KEY_INACTIVE_BG,
                    EdithTileColor.QA_KEY_INACTIVE_FG,
                )
        }
    val alphaKey =
        when (scope) {
            ColorScope.QS -> EdithTileColor.KEY_INACTIVE_ALPHA
            ColorScope.QuickActions -> EdithTileColor.QA_KEY_INACTIVE_ALPHA
        }
    val resolve = swatchResolver(scope)
    val tags = remember(scope) { swatchTags(scope) }

    var activeBg by remember { mutableStateOf(EdithTileColor.readTag(context, keys[0])) }
    var activeFg by remember { mutableStateOf(EdithTileColor.readTag(context, keys[1])) }
    var inactiveBg by remember { mutableStateOf(EdithTileColor.readTag(context, keys[2])) }
    var inactiveFg by remember { mutableStateOf(EdithTileColor.readTag(context, keys[3])) }
    var inactiveAlpha by remember {
        mutableFloatStateOf(
            EdithTileColor.readInt(context, alphaKey).let {
                if (it == EdithTileColor.UNSET_INT) 54f else it.toFloat()
            }
        )
    }

    fun sync() {
        activeBg = EdithTileColor.readTag(context, keys[0])
        activeFg = EdithTileColor.readTag(context, keys[1])
        inactiveBg = EdithTileColor.readTag(context, keys[2])
        inactiveFg = EdithTileColor.readTag(context, keys[3])
        inactiveAlpha =
            EdithTileColor.readInt(context, alphaKey).let {
                if (it == EdithTileColor.UNSET_INT) 54f else it.toFloat()
            }
    }

    for (key in keys + alphaKey) {
        LaunchedSettingObserver(key) { sync() }
    }

    val defaultActiveBg = Color(context.getColor(com.android.internal.R.color.materialColorTertiaryDim))
    val defaultActiveFg = Color(context.getColor(com.android.internal.R.color.materialColorOnTertiary))
    val defaultInactiveBg = Color(context.getColor(com.android.internal.R.color.materialColorOnTertiary))
    val defaultInactiveFg = Color(context.getColor(com.android.internal.R.color.materialColorTertiary))

    fun colorOf(tag: String, default: Color): Color = resolve(context, tag)?.let { Color(it) } ?: default

    // --- Preview ---
    Text(
        text = stringResource(R.string.edith_tile_color_preview),
        style = MaterialTheme.typography.titleMedium,
        color = Color(Styles.getTextColorPrimary(context)),
    )
    Box(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        PreviewTile(
            label = stringResource(R.string.edith_tile_color_preview_active),
            subLabel = "bg ${nameOf(activeBg)} / fg ${nameOf(activeFg)}",
            fill = colorOf(activeBg, defaultActiveBg).copy(alpha = 1f),
            content = colorOf(activeFg, defaultActiveFg),
            shape = RoundedCornerShape(16.dp),
        )
        PreviewTile(
            label = stringResource(R.string.edith_tile_color_preview_inactive),
            subLabel = "bg ${nameOf(inactiveBg)} / fg ${nameOf(inactiveFg)}",
            fill = colorOf(inactiveBg, defaultInactiveBg).copy(alpha = inactiveAlpha / 100f),
            content = colorOf(inactiveFg, defaultInactiveFg),
            shape = CircleShape,
        )
    }

    // --- Slots ---
    val slotTitles =
        listOf(
            stringResource(R.string.edith_tile_color_slot_active_bg),
            stringResource(R.string.edith_tile_color_slot_active_fg),
            stringResource(R.string.edith_tile_color_slot_inactive_bg),
            stringResource(R.string.edith_tile_color_slot_inactive_fg),
        )
    val slots = remember(keys, slotTitles) { keys.mapIndexed { i, k -> Slot(k, slotTitles[i]) } }
    for (slot in slots) {
        Box(modifier = Modifier.height(16.dp))
        val current =
            when (slot.key) {
                keys[0] -> activeBg
                keys[1] -> activeFg
                keys[2] -> inactiveBg
                else -> inactiveFg
            }
        Text(
            text = "${slot.title}: ${nameOf(current)}",
            style = MaterialTheme.typography.titleSmall,
            color = Color(Styles.getTextColorPrimary(context)),
        )
        Box(modifier = Modifier.height(6.dp))
        SwatchGrid(
            tags = tags,
            resolve = resolve,
            selected = current,
            onPick = { tag -> EdithTileColor.writeTag(context, slot.key, tag) },
        )
    }

    // --- Alpha ---
    Box(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.edith_tile_color_inactive_alpha, inactiveAlpha.toInt()),
        style = MaterialTheme.typography.titleSmall,
        color = Color(Styles.getTextColorPrimary(context)),
    )
    Slider(
        value = inactiveAlpha,
        valueRange = 0f..100f,
        onValueChange = { inactiveAlpha = it },
        onValueChangeFinished = {
            EdithTileColor.writeInt(context, alphaKey, inactiveAlpha.toInt())
        },
    )

    // --- Reset ---
    Box(modifier = Modifier.height(8.dp))
    TextButton(onClick = { EdithTileColor.reset(context) }) {
        Text(stringResource(R.string.edith_tile_color_reset))
    }
}

@Composable
private fun ShapeTab() {
    val context = LocalContext.current
    val defaultOuter = 16f // Edith dual-state outer box (matches EdithDualTargetOuterCornerRadius)
    val defaultInner = 10f // Edith dual-state inner box (matches EdithDualTargetInnerCornerRadius)
    var outer by remember {
        mutableFloatStateOf(
            EdithTileColor.readInt(context, EdithTileColor.QA_KEY_SHAPE_OUTER).let {
                if (it == EdithTileColor.UNSET_INT) defaultOuter else it.toFloat()
            }
        )
    }
    var inner by remember {
        mutableFloatStateOf(
            EdithTileColor.readInt(context, EdithTileColor.QA_KEY_SHAPE_INNER).let {
                if (it == EdithTileColor.UNSET_INT) defaultInner else it.toFloat()
            }
        )
    }

    fun sync() {
        outer =
            EdithTileColor.readInt(context, EdithTileColor.QA_KEY_SHAPE_OUTER).let {
                if (it == EdithTileColor.UNSET_INT) defaultOuter else it.toFloat()
            }
        inner =
            EdithTileColor.readInt(context, EdithTileColor.QA_KEY_SHAPE_INNER).let {
                if (it == EdithTileColor.UNSET_INT) defaultInner else it.toFloat()
            }
    }
    for (key in EdithTileColor.QA_SHAPE_KEYS) {
        LaunchedSettingObserver(key) { sync() }
    }

    Text(
        text = stringResource(R.string.edith_tile_shape_preview),
        style = MaterialTheme.typography.titleMedium,
        color = Color(Styles.getTextColorPrimary(context)),
    )
    Box(modifier = Modifier.height(8.dp))
    DualStatePreview(outerRadius = outer, innerRadius = inner)
    Box(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.edith_tile_shape_outer, outer.toInt()),
        style = MaterialTheme.typography.titleSmall,
        color = Color(Styles.getTextColorPrimary(context)),
    )
    Slider(
        value = outer,
        valueRange = 0f..50f,
        onValueChange = { outer = it },
        onValueChangeFinished = {
            EdithTileColor.writeInt(context, EdithTileColor.QA_KEY_SHAPE_OUTER, outer.toInt())
        },
    )

    Box(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.edith_tile_shape_inner, inner.toInt()),
        style = MaterialTheme.typography.titleSmall,
        color = Color(Styles.getTextColorPrimary(context)),
    )
    Slider(
        value = inner,
        valueRange = 0f..50f,
        onValueChange = { inner = it },
        onValueChangeFinished = {
            EdithTileColor.writeInt(context, EdithTileColor.QA_KEY_SHAPE_INNER, inner.toInt())
        },
    )

    Box(modifier = Modifier.height(8.dp))
    TextButton(onClick = {
        EdithTileColor.writeInt(context, EdithTileColor.QA_KEY_SHAPE_OUTER, EdithTileColor.UNSET_INT)
        EdithTileColor.writeInt(context, EdithTileColor.QA_KEY_SHAPE_INNER, EdithTileColor.UNSET_INT)
    }) {
        Text(stringResource(R.string.edith_tile_color_reset))
    }
}

/** Preview of the dual-state (dual-target) tile: an outer box with an inner toggle-target box. */
@Composable
private fun DualStatePreview(outerRadius: Float, innerRadius: Float) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(outerRadius.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier.padding(start = 8.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(innerRadius.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
        )
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

/** A swatch grid, 6 per row, resolved through [resolve]. */
@Composable
private fun SwatchGrid(
    tags: List<String>,
    resolve: (Context, String?) -> Int?,
    selected: String,
    onPick: (String) -> Unit,
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.chunked(6).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (tag in rowItems) {
                    val argb = resolve(context, tag)
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
