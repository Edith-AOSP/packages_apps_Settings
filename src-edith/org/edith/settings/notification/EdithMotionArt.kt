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
 * Shared setting for animated (video) album art on the expanded lock screen media player:
 *
 * - [VALUE_ON] (0, default): fetch and play the loop on any network.
 * - [VALUE_WIFI_ONLY] (1): fetch it only on unmetered networks; loops already cached keep playing.
 * - [VALUE_OFF] (2): keep the static cover.
 *
 * Older builds stored this as a boolean under [LEGACY_KEY]. That value is still read when [KEY] is
 * absent; a new key avoids reinterpreting a legacy `true` (`1`) as [VALUE_WIFI_ONLY].
 */
object EdithMotionArt {
    const val KEY = "edith_lockscreen_motion_art_mode"
    const val LEGACY_KEY = "edith_lockscreen_motion_art"

    const val VALUE_ON = 0
    const val VALUE_WIFI_ONLY = 1
    const val VALUE_OFF = 2

    fun read(context: Context): Int {
        val stored = Settings.Secure.getString(context.contentResolver, KEY)
        stored?.toIntOrNull()?.let { if (it in VALUE_ON..VALUE_OFF) return it }
        if (stored != null) return VALUE_ON
        return when (
            Settings.Secure.getString(context.contentResolver, LEGACY_KEY)?.lowercase()
        ) {
            "0",
            "false" -> VALUE_OFF
            else -> VALUE_ON
        }
    }

    fun write(context: Context, value: Int) {
        Settings.Secure.putInt(context.contentResolver, KEY, value)
    }
}
