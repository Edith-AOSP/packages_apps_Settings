/*
 * Copyright (C) 2026 xdroidOSS, NauzyxLabs
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

package org.edith.settings.display

import android.content.Context
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import com.android.settings.core.BasePreferenceController

class ThemeSwitchPreferenceController(context: Context) :
    BasePreferenceController(context, KEY) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val pref = ThemeSwitchPreference(mContext).apply {
            key = preferenceKey
            order = -1
            isIconSpaceReserved = false
        }
        val category = screen.findPreference<PreferenceGroup>("category_key_appearance")!!
        category.addPreference(pref)
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = "edith_dark_mode"
    }
}
