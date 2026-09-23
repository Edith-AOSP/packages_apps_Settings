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

import android.os.Bundle
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment

/**
 * Sub-screen for tuning the Edith QS tile colors and the dual-state tile shape. Reached by
 * long-pressing the Quick Settings preview while the EdithUI style is selected.
 */
class EdithQsTileColorFragment : DashboardFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.edith_tile_color_screen_title)
    }

    override fun getPreferenceScreenResId(): Int = R.xml.edith_qs_tile_color_settings

    override fun getLogTag(): String = "EdithQsTileColorFragment"

    override fun getMetricsCategory(): Int = MetricsEvent.EDITH
}
