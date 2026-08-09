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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.spa.framework.compose.rememberDrawablePainter

class HomepagePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes) {

    private var _iconVisible by mutableStateOf(true)
    private var _iconPaddingStart by mutableIntStateOf(-1)
    private var _textPaddingStart by mutableIntStateOf(-1)

    init {
        isSelectable = true
        buildContent()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
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

    private fun buildContent() {
        setContent {
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

@Composable
internal fun EdithHomepageContent(
    icon: Drawable?,
    title: CharSequence?,
    iconVisible: Boolean,
    iconPaddingStartPx: Int,
    textPaddingStartPx: Int,
) {
    val density = LocalDensity.current
    val iconSize = dimensionResource(R.dimen.dashboard_tile_image_size)
    val iconPadding =
        if (iconPaddingStartPx >= 0) with(density) { iconPaddingStartPx.toDp() }
        else dimensionResource(R.dimen.edith_homepage_icon_start_padding)
    val textPadding =
        if (textPaddingStartPx >= 0) with(density) { textPaddingStartPx.toDp() }
        else dimensionResource(R.dimen.edith_homepage_text_start_padding)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconVisible && icon != null) {
            Box(
                modifier = Modifier
                    .padding(start = iconPadding, end = 10.dp)
                    .size(iconSize),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = rememberDrawablePainter(icon),
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = title?.toString() ?: "",
            modifier = Modifier
                .weight(1f)
                .padding(start = textPadding, top = 24.dp, bottom = 24.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal,
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun EdithHomepageSwitchContent(
    icon: Drawable?,
    title: CharSequence?,
    summary: CharSequence?,
    iconVisible: Boolean,
    iconPaddingStartPx: Int,
    textPaddingStartPx: Int,
    checked: Boolean = false,
    pillCornerRadius: Boolean = false,
    minHeightPx: Float = -1f,
) {
    val density = LocalDensity.current
    val iconSize = dimensionResource(R.dimen.dashboard_tile_image_size)
    val iconPadding =
        if (iconPaddingStartPx >= 0) with(density) { iconPaddingStartPx.toDp() }
        else dimensionResource(R.dimen.edith_homepage_icon_start_padding)
    val textPadding =
        if (textPaddingStartPx >= 0) with(density) { textPaddingStartPx.toDp() }
        else dimensionResource(R.dimen.edith_homepage_text_start_padding)
    val minHeight = if (minHeightPx > 0f) with(density) { minHeightPx.toDp() } else 72.dp

    val rowModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = minHeight)
    val clipModifier = if (pillCornerRadius) {
        Modifier.clip(RoundedCornerShape(50))
    } else {
        Modifier
    }

    Row(
        modifier = rowModifier.then(clipModifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconVisible && icon != null) {
            Box(
                modifier = Modifier
                    .padding(start = iconPadding, end = 10.dp)
                    .size(iconSize),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = rememberDrawablePainter(icon),
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = textPadding, top = 16.dp, bottom = 16.dp),
        ) {
            Text(
                text = title?.toString() ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal,
            )
            if (!summary.isNullOrEmpty()) {
                Text(
                    text = summary.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val switchColors = SwitchDefaults.colors()
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 0.dp),
            colors = switchColors,
            thumbContent = if (checked) {
                {
                    Icon(
                        painter = painterResource(
                            com.android.settingslib.widget.theme.R.drawable
                                .settingslib_expressive_icon_check),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                {
                    Icon(
                        painter = painterResource(
                            com.android.settingslib.widget.theme.R.drawable
                                .settingslib_expressive_icon_close),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.surface,
                    )
                }
            },
        )
    }
}
