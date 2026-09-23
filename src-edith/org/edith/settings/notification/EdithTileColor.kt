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
import android.provider.Settings

/**
 * Settings for the Edith QS tile color tuner.
 *
 * Each color slot stores a swatch *tag* (a stable string) in [Settings.Secure], or [UNSET] when not
 * overridden. SystemUI resolves the tag against the current theme, so the override follows palette
 * changes (wallpaper / ThemePicker).
 */
object EdithTileColor {
    /** Sentinel meaning "this color slot is not overridden". */
    const val UNSET = ""

    /** Sentinel meaning "this numeric slot is not overridden". */
    const val UNSET_INT = -1

    const val KEY_ACTIVE_BG = "edith_qs_tile_color_active_bg"
    const val KEY_ACTIVE_FG = "edith_qs_tile_color_active_fg"
    const val KEY_INACTIVE_BG = "edith_qs_tile_color_inactive_bg"
    const val KEY_INACTIVE_FG = "edith_qs_tile_color_inactive_fg"

    /** Inactive background alpha, as a percentage 0-100, or [UNSET_INT]. */
    const val KEY_INACTIVE_ALPHA = "edith_qs_tile_color_inactive_alpha"

    // --- Quick Actions scope -------------------------------------------------

    const val QA_KEY_ACTIVE_BG = "edith_qa_tile_color_active_bg"
    const val QA_KEY_ACTIVE_FG = "edith_qa_tile_color_active_fg"
    const val QA_KEY_INACTIVE_BG = "edith_qa_tile_color_inactive_bg"
    const val QA_KEY_INACTIVE_FG = "edith_qa_tile_color_inactive_fg"

    /** Quick Actions inactive background alpha, as a percentage 0-100, or [UNSET_INT]. */
    const val QA_KEY_INACTIVE_ALPHA = "edith_qa_tile_color_inactive_alpha"

    /** Dual-target outer box corner radius (dp), or [UNSET_INT]. */
    const val QA_KEY_SHAPE_OUTER = "edith_qa_tile_shape_outer"

    /** Dual-target inner toggle-target box corner radius (dp), or [UNSET_INT]. */
    const val QA_KEY_SHAPE_INNER = "edith_qa_tile_shape_inner"

    /** Reads the swatch tag for a color slot (or [UNSET]). */
    fun readTag(context: Context, key: String): String =
        Settings.Secure.getString(context.contentResolver, key) ?: UNSET

    fun writeTag(context: Context, key: String, tag: String) {
        Settings.Secure.putString(context.contentResolver, key, tag)
    }

    fun readInt(context: Context, key: String): Int =
        Settings.Secure.getInt(context.contentResolver, key, UNSET_INT)

    fun writeInt(context: Context, key: String, value: Int) {
        Settings.Secure.putInt(context.contentResolver, key, value)
    }

    /** Clears all overrides, falling back to the role defaults. */
    fun reset(context: Context) {
        for (key in COLOR_KEYS) {
            writeTag(context, key, UNSET)
        }
        writeInt(context, KEY_INACTIVE_ALPHA, UNSET_INT)
        for (key in QA_COLOR_KEYS) {
            writeTag(context, key, UNSET)
        }
        writeInt(context, QA_KEY_INACTIVE_ALPHA, UNSET_INT)
        for (key in QA_SHAPE_KEYS) {
            writeInt(context, key, UNSET_INT)
        }
    }

    val COLOR_KEYS = listOf(KEY_ACTIVE_BG, KEY_ACTIVE_FG, KEY_INACTIVE_BG, KEY_INACTIVE_FG)
    val ALL_KEYS = COLOR_KEYS + KEY_INACTIVE_ALPHA

    val QA_COLOR_KEYS =
        listOf(QA_KEY_ACTIVE_BG, QA_KEY_ACTIVE_FG, QA_KEY_INACTIVE_BG, QA_KEY_INACTIVE_FG)
    val QA_SHAPE_KEYS = listOf(QA_KEY_SHAPE_OUTER, QA_KEY_SHAPE_INNER)
    val QA_ALL_KEYS = QA_COLOR_KEYS + QA_KEY_INACTIVE_ALPHA + QA_SHAPE_KEYS
}
