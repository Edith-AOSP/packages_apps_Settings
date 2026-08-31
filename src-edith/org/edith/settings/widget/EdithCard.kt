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

package org.edith.settings.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import org.edith.settings.core.variables.Styles
import org.edith.settings.core.variables.toComposeColor

/**
 * A reusable dashboard-style card with an icon tile, a title and a summary.
 *
 * The card switches between an "active" (primary tint) and "inactive" (neutral) visual state via
 * [isActive], and exposes an [onClick] callback.
 */
@Composable
fun EdithCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    summary: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(
        dimensionResource(R.dimen.settingslib_preference_corner_radius)
    ),
    iconSize: Dp = dimensionResource(R.dimen.dashboard_tile_image_size),
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                Styles.getPrimary(context).toComposeColor()
            } else {
                Styles.getSurfaceBright(context).toComposeColor()
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .background(
                        color = if (isActive) {
                            Styles.getSurfaceContainer(context).toComposeColor()
                        } else {
                            Styles.getPrimary(context).toComposeColor()
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(
                        dimensionResource(R.dimen.dashboard_tile_foreground_image_size)
                    ),
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(
                        if (isActive) {
                            Styles.getOnSurface(context).toComposeColor()
                        } else {
                            Styles.getOnPrimary(context).toComposeColor()
                        }
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isActive) {
                    Styles.getOnPrimary(context).toComposeColor()
                } else {
                    Styles.getOnSurface(context).toComposeColor()
                },
            )

            Text(
                text = summary,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 13.sp,
                color = if (isActive) {
                    Styles.getOnPrimary(context).toComposeColor()
                } else {
                    Styles.getOnSurfaceVariant(context).toComposeColor()
                },
            )
        }
    }
}
