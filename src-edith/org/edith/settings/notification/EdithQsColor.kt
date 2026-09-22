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

/** Setting for the Edith Quick Settings color scheme. Defaults to enabled. */
object EdithQsColor {
    const val KEY = "edith_use_qs_color_scheme"

    /** Whether the Edith color scheme is enabled (default `true`). */
    fun read(context: Context): Boolean =
        Settings.Secure.getInt(context.contentResolver, KEY, 1) == 1

    fun write(context: Context, enabled: Boolean) {
        Settings.Secure.putInt(context.contentResolver, KEY, if (enabled) 1 else 0)
    }
}
