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
import androidx.annotation.ColorInt

/**
 * Catalog of the color swatches the (debug) Edith tile color tuner can pick from.
 *
 * Must stay in sync with SystemUI's `EdithTileSwatches` (same tags / same resources).
 */
object EdithTileSwatches {
    val ACCENT3_TONES = listOf(0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

    val ROLE_TAGS = listOf(
        "tertiary",
        "tertiaryDim",
        "tertiaryContainer",
        "onTertiary",
        "onTertiaryContainer",
    )

    /** All swatch tags, in display order. */
    val ALL_TAGS: List<String> = ACCENT3_TONES.map { "a3_$it" } + ROLE_TAGS

    /**
     * The tags shown as pickable swatches in the tuner: only the accent3 (tertiary) tone ramp. The
     * semantic role tags are intentionally omitted because they duplicate the ramp tones (e.g.
     * `tertiaryContainer` / `tertiaryDim`), which showed up as duplicate swatches.
     */
    fun pickableTags(): List<String> = ACCENT3_TONES.map { "a3_$it" }

    /** Resolves the [tag] to its color for the given [context], or `null` when [EdithTileColor.UNSET]/unknown. */
    @ColorInt
    fun resolve(context: Context, tag: String?): Int? {
        if (tag.isNullOrEmpty()) return null
        val resId =
            when {
                tag.startsWith("a3_") ->
                    when (tag.removePrefix("a3_").toIntOrNull()) {
                        0 -> android.R.color.system_accent3_0
                        10 -> android.R.color.system_accent3_10
                        50 -> android.R.color.system_accent3_50
                        100 -> android.R.color.system_accent3_100
                        200 -> android.R.color.system_accent3_200
                        300 -> android.R.color.system_accent3_300
                        400 -> android.R.color.system_accent3_400
                        500 -> android.R.color.system_accent3_500
                        600 -> android.R.color.system_accent3_600
                        700 -> android.R.color.system_accent3_700
                        800 -> android.R.color.system_accent3_800
                        900 -> android.R.color.system_accent3_900
                        1000 -> android.R.color.system_accent3_1000
                        else -> null
                    }
                tag == "tertiary" -> com.android.internal.R.color.materialColorTertiary
                tag == "tertiaryDim" -> com.android.internal.R.color.materialColorTertiaryDim
                tag == "tertiaryContainer" ->
                    com.android.internal.R.color.materialColorTertiaryContainer
                tag == "onTertiary" -> com.android.internal.R.color.materialColorOnTertiary
                tag == "onTertiaryContainer" ->
                    com.android.internal.R.color.materialColorOnTertiaryContainer
                else -> null
            }
        return resId?.let { context.getColor(it) }
    }
}
