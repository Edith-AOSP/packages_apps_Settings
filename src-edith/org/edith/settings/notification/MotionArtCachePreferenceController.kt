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
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.core.lifecycle.LifecycleObserver
import com.android.settingslib.core.lifecycle.events.OnStart
import com.android.settingslib.utils.ThreadUtils

/**
 * Preference controller for the animated art cache row.
 *
 * Shows the cache's size and clears it after a confirmation. Both the size and the clear go
 * through [MotionArtCache], which talks to SystemUI's cache provider.
 */
class MotionArtCachePreferenceController(context: Context) :
    BasePreferenceController(context, KEY), LifecycleObserver, OnStart {

    private var preference: Preference? = null
    private var stats: MotionArtCache.Stats? = null

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference =
            screen.findPreference<Preference>(KEY)?.apply {
                setOnPreferenceClickListener {
                    showClearDialog()
                    true
                }
            }
    }

    override fun onStart() = refresh()

    override fun getAvailabilityStatus(): Int = AVAILABLE

    private fun refresh() {
        val preference = preference ?: return
        ThreadUtils.getBackgroundExecutor().execute {
            val stats = MotionArtCache.stats(mContext)
            ThreadUtils.postOnMainThread { bind(preference, stats) }
        }
    }

    private fun bind(preference: Preference, stats: MotionArtCache.Stats?) {
        this.stats = stats
        preference.summary = summary(stats)
        preference.isEnabled = stats != null && stats.files > 0
    }

    private fun summary(stats: MotionArtCache.Stats?): String =
        when {
            stats == null -> mContext.getString(R.string.edith_motion_art_cache_unavailable)
            stats.files == 0 -> mContext.getString(R.string.edith_motion_art_cache_empty)
            else ->
                mContext.getString(
                    R.string.edith_motion_art_cache_summary,
                    android.text.format.Formatter.formatFileSize(mContext, stats.bytes),
                )
        }

    private fun showClearDialog() {
        val stats = stats ?: return
        android.app.AlertDialog.Builder(mContext)
            .setTitle(R.string.edith_motion_art_cache_dialog_title)
            .setMessage(
                mContext.getString(
                    R.string.edith_motion_art_cache_dialog_message,
                    android.text.format.Formatter.formatFileSize(mContext, stats.bytes),
                )
            )
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.edith_motion_art_cache_clear) { _, _ -> clear() }
            .show()
    }

    private fun clear() {
        val preference = preference ?: return
        preference.isEnabled = false
        ThreadUtils.getBackgroundExecutor().execute {
            val stats = MotionArtCache.clear(mContext)
            ThreadUtils.postOnMainThread { bind(preference, stats) }
        }
    }

    companion object {
        const val KEY = "edith_motion_art_cache"
    }
}
