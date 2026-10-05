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

package org.edith.settings.display.fonts

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController

/**
 * Preference controller for the Font entry in Display → Appearance.
 *
 * Adds a plain preference whose summary shows the currently selected font. Tapping it opens
 * [FontSettingsFragment], where a font can be previewed and applied.
 */
class FontPreferenceController(context: Context) : BasePreferenceController(context, KEY) {

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
                title = mContext.getString(R.string.edith_font_title)
                // Sit above the other Appearance entries (which default to order 0).
                order = -1
                isIconSpaceReserved = false
                fragment = FontSettingsFragment::class.java.name
            }
        preference = pref

        // Add into the Display → Appearance category, not the top-level screen.
        val category = screen.findPreference(CATEGORY_KEY_APPEARANCE) as? PreferenceGroup
        if (category != null) {
            category.addPreference(pref)
        } else {
            screen.addPreference(pref)
        }

        mContext.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES),
            false,
            observer,
        )
        updateSummary()
    }

    private fun updateSummary() {
        preference?.summary = FontOverlayManager(mContext).getActiveFontLabel()
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE

    companion object {
        const val KEY = "edith_font"
        private const val CATEGORY_KEY_APPEARANCE = "category_key_appearance"
    }
}
