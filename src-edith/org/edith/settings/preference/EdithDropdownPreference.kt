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

package org.edith.settings.preference

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.android.settings.spa.preference.ComposePreference

/** One entry in an [EdithDropdownPreference]. */
data class EdithDropdownOption(val value: String, val label: String)

/**
 * A reusable, inline Material 3 dropdown rendered as a Settings card.
 *
 * The row shows the title in the accent colour, the selected value underneath and a double chevron
 * on the end. Tapping anywhere on the row opens the option list directly below it; the selected
 * option carries a check. The preference owns no setting storage: [configure] supplies the current
 * value and a callback, and [setSelectedValue] lets the caller push external changes back in.
 *
 * When [observeSetting] is used, the preference also listens to that `Settings.Secure` key while it
 * is attached and refreshes the value. This keeps a screen in sync without every caller writing its
 * own `ContentObserver`.
 */
class EdithDropdownPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) :
    ComposePreference(context, attrs, defStyleAttr, defStyleRes) {

    private val state = EdithDropdownState()
    private var settingObserver: ContentObserver? = null
    private var attached = false

    init {
        setContent { EdithDropdownContent(state) }
    }

    fun configure(
        title: String,
        options: List<EdithDropdownOption>,
        selectedValue: String,
        onSelected: (EdithDropdownOption) -> Unit,
    ) {
        require(options.isNotEmpty()) { "A dropdown needs at least one option" }
        state.title = title
        state.options = options
        state.selectedValue =
            if (options.any { it.value == selectedValue }) selectedValue else options.first().value
        state.onSelected = onSelected
    }

    /** Updates the row, for example after the setting changed elsewhere. */
    fun setSelectedValue(value: String) {
        if (state.options.any { it.value == value } && state.selectedValue != value) {
            state.selectedValue = value
        }
    }

    /** Greys the row out and ignores taps without removing it from the screen. */
    fun setContentEnabled(enabled: Boolean) {
        state.enabled = enabled
    }

    /**
     * Watches [key] while the preference is attached. [read] is called on every change and should
     * return the new string value.
     */
    fun observeSetting(key: String, read: () -> String) {
        settingObserver?.let { context.contentResolver.unregisterContentObserver(it) }
        settingObserver =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    setSelectedValue(read())
                }
            }
        state.settingKey = key
        state.readSetting = read
        if (attached) {
            registerSettingObserver()
        }
    }

    override fun onAttached() {
        super.onAttached()
        attached = true
        registerSettingObserver()
    }

    override fun onDetached() {
        attached = false
        unregisterSettingObserver()
        super.onDetached()
    }

    private fun registerSettingObserver() {
        val key = state.settingKey ?: return
        if (settingObserver == null) return
        // Re-registering an already registered observer is a no-op for the same instance, but the
        // preference can be detached and re-attached, so always unregister first.
        unregisterSettingObserver()
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(key),
            false,
            settingObserver!!,
        )
        state.readSetting?.invoke()?.let { setSelectedValue(it) }
    }

    private fun unregisterSettingObserver() {
        settingObserver?.let { context.contentResolver.unregisterContentObserver(it) }
    }
}

private class EdithDropdownState {
    var title by mutableStateOf("")
    var options by mutableStateOf<List<EdithDropdownOption>>(emptyList())
    var selectedValue by mutableStateOf<String?>(null)
    var enabled by mutableStateOf(true)
    var onSelected: ((EdithDropdownOption) -> Unit)? = null
    var settingKey: String? = null
    var readSetting: (() -> String)? = null
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EdithDropdownContent(state: EdithDropdownState) {
    var expanded by remember { mutableStateOf(false) }
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val selectedIndex =
        state.options.indexOfFirst { it.value == state.selectedValue }.coerceAtLeast(0)
    val selectedOption = state.options.getOrNull(selectedIndex)
    val selectedLabel = selectedOption?.label ?: ""

    fun select(option: EdithDropdownOption) {
        state.selectedValue = option.value
        state.onSelected?.invoke(option)
        expanded = false
    }

    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clickable(enabled = state.enabled) { expanded = true }
                    .onSizeChanged { rowWidthPx = it.width }
                    .semantics {
                        role = Role.Button
                        contentDescription = "${state.title}, $selectedLabel"
                    }
                    .alpha(if (state.enabled) 1f else 0.38f)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Filled.UnfoldMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }

        val density = LocalDensity.current
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val rowWidth = with(density) { rowWidthPx.toDp() }
        val menuWidth =
            if (rowWidthPx > 0) {
                minOf(rowWidth, 260.dp)
            } else {
                260.dp
            }
        // The reference anchors the menu to the row's end, not its start.
        val menuOffsetX =
            if (isRtl || rowWidthPx == 0 || rowWidth <= menuWidth) {
                0.dp
            } else {
                rowWidth - menuWidth
            }

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(menuOffsetX, 0.dp),
            properties = PopupProperties(focusable = true),
        ) {
            val reveal = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                reveal.animateTo(1f, tween(durationMillis = 220, easing = FastOutSlowInEasing))
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier =
                    Modifier.width(menuWidth).graphicsLayer {
                        val revealScale = 0.86f + 0.14f * reveal.value
                        scaleX = revealScale
                        scaleY = revealScale
                        alpha = reveal.value
                        transformOrigin = TransformOrigin(1f, 0f)
                    },
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    state.options.forEach { option ->
                        val isSelected = option.value == state.selectedValue
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight =
                                        if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            onClick = { select(option) },
                            modifier = Modifier.semantics { selected = isSelected },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
