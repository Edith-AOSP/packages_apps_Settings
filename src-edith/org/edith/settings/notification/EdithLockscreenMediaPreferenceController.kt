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
import com.android.settings.core.TogglePreferenceController
import com.android.settings.slices.Sliceable

/**
 * Preference controller for the "compact media player" switch on the lock screen sub-settings.
 *
 * Stored in [Settings.Secure] under [EdithLockscreenMedia.KEY] as an int (`0` = three-row,
 * `1` = compact two-row), which SystemUI observes live.
 */
class EdithLockscreenMediaPreferenceController(context: Context) :
    TogglePreferenceController(context, KEY) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        screen.findPreference<Preference>(KEY)?.isPersistent = false
    }

    override fun isChecked(): Boolean =
        EdithLockscreenMedia.read(mContext) == EdithLockscreenMedia.VALUE_COMPACT

    override fun setChecked(isChecked: Boolean): Boolean {
        EdithLockscreenMedia.write(
            mContext,
            if (isChecked) EdithLockscreenMedia.VALUE_COMPACT
            else EdithLockscreenMedia.VALUE_THREE_ROW,
        )
        return true
    }

    override fun getSliceHighlightMenuRes(): Int = Sliceable.NO_RES

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = EdithLockscreenMedia.KEY
    }
}
