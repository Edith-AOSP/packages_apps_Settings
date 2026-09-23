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
import android.os.SystemProperties
import android.provider.Settings

/**
 * Settings for the (debug) Edith QS tile color tuner.
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

    /** System property gating the tuner; matches SystemUI's EdithQsColorInteractor.TUNER_PROP. */
    const val TUNER_PROP = "debug.enable_edith_tile_color"

    /** Whether the (debug) Edith tile color tuner is enabled. */
    fun isTunerEnabled(): Boolean = SystemProperties.getBoolean(TUNER_PROP, false)

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
    }

    val COLOR_KEYS = listOf(KEY_ACTIVE_BG, KEY_ACTIVE_FG, KEY_INACTIVE_BG, KEY_INACTIVE_FG)
    val ALL_KEYS = COLOR_KEYS + KEY_INACTIVE_ALPHA
}
