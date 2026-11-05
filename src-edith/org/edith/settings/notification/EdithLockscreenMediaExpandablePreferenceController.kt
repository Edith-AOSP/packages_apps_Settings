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
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.TogglePreferenceController
import com.android.settings.slices.Sliceable

/**
 * Preference controller for the "expandable media player" switch, used both on the lock screen
 * page and on the expandable player's own page. Both write [EdithLockscreenMedia.KEY_EXPANDABLE],
 * so the two rows always show the same value.
 */
class EdithLockscreenMediaExpandablePreferenceController(context: Context) :
    TogglePreferenceController(context, KEY) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        screen.findPreference<Preference>(KEY)?.isPersistent = false
    }

    override fun isChecked(): Boolean = EdithLockscreenMedia.readExpandable(mContext)

    override fun setChecked(isChecked: Boolean): Boolean {
        EdithLockscreenMedia.writeExpandable(mContext, isChecked)
        return true
    }

    override fun getSliceHighlightMenuRes(): Int = Sliceable.NO_RES

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = EdithLockscreenMedia.KEY_EXPANDABLE
    }
}
