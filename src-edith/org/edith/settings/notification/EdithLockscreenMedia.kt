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
 * Settings for the lock screen media player: [KEY] (compact vs three-row) and [KEY_EXPANDABLE]
 * (whether tapping the card opens the expanded player), both stored in [Settings.Secure].
 */
object EdithLockscreenMedia {
    const val KEY = "edith_lockscreen_media_compact"
    const val VALUE_THREE_ROW = 0
    const val VALUE_COMPACT = 1

    /** Whether tapping the lock screen media card expands it into the large art player. */
    const val KEY_EXPANDABLE = "edith_lockscreen_media_expandable"

    fun read(context: Context): Int =
        Settings.Secure.getInt(context.contentResolver, KEY, VALUE_THREE_ROW)

    fun write(context: Context, value: Int) {
        Settings.Secure.putInt(context.contentResolver, KEY, value)
    }

    /**
     * Reads [KEY_EXPANDABLE] as a boolean. Stored as an int, but an older build may have written a
     * boolean, so "0"/"false" is off and anything else (or nothing) is on.
     */
    fun readExpandable(context: Context): Boolean =
        when (
            Settings.Secure.getString(context.contentResolver, KEY_EXPANDABLE)?.lowercase()
        ) {
            "0",
            "false" -> false
            else -> true
        }

    fun writeExpandable(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, KEY_EXPANDABLE, if (enabled) 1 else 0)
    }
}
