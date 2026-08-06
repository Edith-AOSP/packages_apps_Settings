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

package com.android.settings.edith.dashboard

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settingslib.RestrictedTopLevelPreference
import com.android.settingslib.spa.framework.theme.SettingsTheme

/**
 * Homepage preference that supports device admin / user restriction.
 */
class RestrictedHomepagePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : RestrictedTopLevelPreference(context, attrs, defStyleAttr, defStyleRes) {

    private var _iconVisible by mutableStateOf(true)
    private var _iconPaddingStart by mutableIntStateOf(-1)
    private var _textPaddingStart by mutableIntStateOf(-1)

    init {
        layoutResource = R.layout.preference_compose
    }

    fun setIconVisible(visible: Boolean) {
        _iconVisible = visible
    }

    fun setIconPaddingStart(paddingStart: Int) {
        _iconPaddingStart = paddingStart
    }

    fun setTextPaddingStart(paddingStart: Int) {
        _textPaddingStart = paddingStart
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.itemView as ComposeView).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                SettingsTheme {
                    EdithHomepageContent(
                        icon = icon,
                        title = title,
                        iconVisible = _iconVisible,
                        iconPaddingStartPx = _iconPaddingStart,
                        textPaddingStartPx = _textPaddingStart,
                    )
                }
            }
        }
    }
}
