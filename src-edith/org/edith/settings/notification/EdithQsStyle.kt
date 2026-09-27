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

/** Shared setting for the Quick Settings style: 0 = AOSP, 1 = EdithUI (default). */
object EdithQsStyle {
    const val KEY = "edith_qs_style"
    const val VALUE_AOSP = 0
    const val VALUE_EDITHUI = 1

    fun read(context: Context): Int =
        Settings.Secure.getInt(context.contentResolver, KEY, VALUE_EDITHUI)

    fun write(context: Context, value: Int) {
        Settings.Secure.putInt(context.contentResolver, KEY, value)
    }
}
