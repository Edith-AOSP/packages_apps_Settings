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

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.android.internal.logging.nano.MetricsProto.MetricsEvent
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment

/**
 * Sub-screen for the expandable lock screen media player: its master switch, the animated art mode,
 * and the animated art cache.
 */
class ExpandableMediaPlayerFragment : DashboardFragment() {

    private val expandableObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = updateMotionArtState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.setTitle(R.string.edith_lockscreen_media_expandable_title)
    }

    override fun onStart() {
        super.onStart()
        requireContext()
            .contentResolver
            .registerContentObserver(
                Settings.Secure.getUriFor(EdithLockscreenMedia.KEY_EXPANDABLE),
                false,
                expandableObserver,
            )
        updateMotionArtState()
    }

    override fun onStop() {
        requireContext().contentResolver.unregisterContentObserver(expandableObserver)
        super.onStop()
    }

    /** Animated art only exists in the expanded player, so its pills follow the master switch. */
    private fun updateMotionArtState() {
        val enabled = EdithLockscreenMedia.readExpandable(requireContext())
        findPreference<MotionArtModePreference>(EdithMotionArt.KEY)?.setContentEnabled(enabled)
    }

    override fun getPreferenceScreenResId(): Int = R.xml.edith_expandable_media_settings

    override fun getLogTag(): String = "ExpandableMediaPlayerFragment"

    override fun getMetricsCategory(): Int = MetricsEvent.EDITH
}
