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
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController

/**
 * Preference controller for the Quick Settings style entry.
 *
 * Adds a plain preference at the very top of the Notifications screen. Its title is "Quick
 * Settings" and its summary shows the currently selected style (AOSP or EdithUI). Tapping it opens
 * [QuickSettingsStyleFragment], where the style can be previewed and changed.
 *
 * The choice is stored in [Settings.Secure] under [KEY], which SystemUI observes live.
 */
class EdithQsStylePreferenceController(context: Context) :
    BasePreferenceController(context, KEY) {

    private var preference: Preference? = null

    private val observer =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                updateSummary()
            }
        }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val pref =
            Preference(mContext).apply {
                key = preferenceKey
                title = mContext.getString(R.string.edith_qs_style_title)
                // Place it directly on the screen, above every category (categories start at 0).
                order = -1
                isIconSpaceReserved = false
                fragment = QuickSettingsStyleFragment::class.java.name
            }
        preference = pref
        screen.addPreference(pref)

        mContext.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(KEY),
            false,
            observer,
        )
        updateSummary()
    }

    private fun updateSummary() {
        val isEdith = EdithQsStyle.read(mContext) == EdithQsStyle.VALUE_EDITHUI
        preference?.summary =
            mContext.getString(
                if (isEdith) R.string.edith_qs_style_edith else R.string.edith_qs_style_aosp
            )
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = EdithQsStyle.KEY
    }
}
