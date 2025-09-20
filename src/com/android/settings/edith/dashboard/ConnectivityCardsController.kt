/*
 * Copyright (C) 2023 Halcyon Project
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

package com.android.settings.edith.dashboard

import android.app.settings.SettingsEnums
import android.content.Context
import android.widget.LinearLayout
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.widget.LayoutPreference

class ConnectivityCardsController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val cards = screen.findPreference<LayoutPreference>(KEY) ?: return

        val fragmentMap = mapOf(
            R.id.edith_network_settings to
                "com.android.settings.network.NetworkDashboardFragment",
            R.id.edith_devices_settings to
                "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment",
        )

        fragmentMap.forEach { (id, fragmentClass) ->
            cards.findViewById<LinearLayout>(id)?.setOnClickListener {
                SubSettingLauncher(mContext)
                    .setDestination(fragmentClass)
                    .setSourceMetricsCategory(SettingsEnums.SETTINGS_NETWORK_CATEGORY)
                    .launch()
            }
        }
    }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY

    companion object {
        private const val KEY = "homepage_connectivity_cards"
    }
}
