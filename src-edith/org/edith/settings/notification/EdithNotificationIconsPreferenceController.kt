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
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import org.edith.settings.preference.EdithDropdownOption
import org.edith.settings.preference.EdithDropdownPreference

/**
 * Adds the status-bar "Notification icons" dropdown to Notifications > General.
 *
 * Values: `0` = icons, `1` = number, `2` = none. The value is written to [Settings.Secure] and read
 * live by SystemUI.
 */
class EdithNotificationIconsPreferenceController(context: Context) :
    BasePreferenceController(context, KEY) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val preference =
            EdithDropdownPreference(mContext).apply {
                key = preferenceKey
                order = 0
                configure(
                    title = mContext.getString(R.string.edith_notification_icons_title),
                    options =
                        listOf(
                            EdithDropdownOption(
                                MODE_ICONS.toString(),
                                mContext.getString(R.string.edith_notification_icons_show),
                            ),
                            EdithDropdownOption(
                                MODE_NUMBER.toString(),
                                mContext.getString(R.string.edith_notification_icons_number),
                            ),
                            EdithDropdownOption(
                                MODE_NONE.toString(),
                                mContext.getString(R.string.edith_notification_icons_none),
                            ),
                        ),
                    selectedValue = read().toString(),
                    onSelected = { write(it.value.toInt()) },
                )
                observeSetting(KEY) { read().toString() }
            }

        val general = screen.findPreference(GENERAL_CATEGORY) as? PreferenceGroup
        if (general != null) {
            general.addPreference(preference)
        } else {
            screen.addPreference(preference)
        }
    }

    override fun getAvailabilityStatus(): Int = AVAILABLE

    private fun read(): Int =
        Settings.Secure.getInt(mContext.contentResolver, KEY, MODE_ICONS)

    private fun write(value: Int) {
        Settings.Secure.putInt(mContext.contentResolver, KEY, value)
    }

    companion object {
        const val KEY = "edith_status_bar_notification_style"

        private const val MODE_ICONS = 0
        private const val MODE_NUMBER = 1
        private const val MODE_NONE = 2

        private const val GENERAL_CATEGORY = "general_notifications_category"
    }
}
