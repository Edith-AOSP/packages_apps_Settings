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

class BrightnessSliderPreferenceController(context: Context) :
    BasePreferenceController(context, KEY) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val pref = BrightnessSliderPreference(mContext).apply {
            key = preferenceKey
            order = -199
            isIconSpaceReserved = false
        }

        val category = screen.findPreference("category_brightness") as? PreferenceGroup
        category?.addPreference(pref)
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = "xdroid_brightness_slider"
    }
}
