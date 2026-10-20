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
import android.net.Uri

/**
 * Client for SystemUI's motion art cache provider.
 *
 * The cache sits in SystemUI's device-protected storage, which this app cannot read, so size and
 * clearing go through the provider (`com.android.systemui.motionart`) rather than the filesystem.
 */
object MotionArtCache {
    /** [bytes] on disk and the number of cached loops. */
    data class Stats(val bytes: Long, val files: Int)

    private val AUTHORITY = Uri.parse("content://com.android.systemui.motionart")

    private const val METHOD_STATS = "stats"
    private const val METHOD_CLEAR = "clear"
    private const val KEY_BYTES = "bytes"
    private const val KEY_FILES = "files"

    fun stats(context: Context): Stats? = call(context, METHOD_STATS)

    fun clear(context: Context): Stats? = call(context, METHOD_CLEAR)

    private fun call(context: Context, method: String): Stats? =
        try {
            val result = context.contentResolver.call(AUTHORITY, method, null, null) ?: return null
            Stats(result.getLong(KEY_BYTES), result.getInt(KEY_FILES))
        } catch (e: RuntimeException) {
            null
        }
}
