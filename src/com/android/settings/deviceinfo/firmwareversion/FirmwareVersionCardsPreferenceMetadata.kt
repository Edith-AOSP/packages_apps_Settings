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
 * Metadata for the horizontal Android version + security patch card pair on the firmware version
 * screen.
 */
class FirmwareVersionCardsPreferenceMetadata :
    PreferenceMetadata,
    PreferenceBinding {

    override val key: String
        get() = "firmware_version_cards"

    override val purpose: Int
        get() = R.string.firmware_version

    override val title: Int
        get() = 0

    override val summary: Int
        get() = 0

    override val indexable: Boolean
        get() = false

    override val sensitivityLevel: Int
        get() = SensitivityLevel.NO_SENSITIVITY

    override fun createWidget(context: Context): Preference =
        org.edith.settings.deviceinfo.FirmwareVersionCardsPreference(context)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isSelectable = false
    }
}
