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
import com.android.settings.core.BasePreferenceController

/**
 * Controller for the (debug) Edith tile color tuner entry.
 *
 * The entry is only available when [EdithTileColor.TUNER_PROP] is set; otherwise it is hidden.
 */
class EdithTileColorEntryPreferenceController(context: Context, preferenceKey: String) :
    BasePreferenceController(context, preferenceKey) {

    /** Context-only constructor used when the controller is declared via `settings:controller`. */
    constructor(context: Context) : this(context, KEY)

    override fun getAvailabilityStatus(): Int =
        if (EdithTileColor.isTunerEnabled()) AVAILABLE else CONDITIONALLY_UNAVAILABLE

    companion object {
        const val KEY = "edith_tile_color_tuner_entry"
    }
}
