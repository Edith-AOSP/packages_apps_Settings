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
 * Shared setting controlling where the compact (two-row) media player is shown in Quick Settings:
 *
 * - [VALUE_ALWAYS] (0): compact media player in both QQS and the expanded QS panel.
 * - [VALUE_DYNAMIC] (1, default): compact media player only in QQS (the in-row behavior).
 * - [VALUE_DISABLED] (2): always use the three-row media player.
 */
object EdithMediaInQs {
    const val KEY = "edith_media_in_qs_mode"
    const val VALUE_ALWAYS = 0
    const val VALUE_DYNAMIC = 1
    const val VALUE_DISABLED = 2

    fun read(context: Context): Int =
        Settings.Secure.getInt(context.contentResolver, KEY, VALUE_DYNAMIC)

    fun write(context: Context, value: Int) {
        Settings.Secure.putInt(context.contentResolver, KEY, value)
    }
}
