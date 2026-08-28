/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import androidx.preference.Preference
import com.android.settings.R
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.preference.PreferenceBinding

/**
 * Metadata for the software information hero card on the firmware version screen. It renders a
 * [org.edith.settings.deviceinfo.SoftwareInformationCardPreference] widget.
 */
class SoftwareInformationCardPreferenceMetadata :
    PreferenceMetadata,
    PreferenceBinding {

    override val key: String
        get() = "software_information_card"

    override val purpose: Int
        get() = R.string.edith_software_information_brand

    override val title: Int
        get() = 0

    override val summary: Int
        get() = 0

    override val indexable: Boolean
        get() = false

    override val sensitivityLevel: Int
        get() = SensitivityLevel.NO_SENSITIVITY

    override fun createWidget(context: Context): Preference =
        org.edith.settings.deviceinfo.SoftwareInformationCardPreference(context)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        // The card draws its own content; ensure it is not selectable.
        preference.isSelectable = false
    }
}
