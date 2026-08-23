package org.edith.settings.deviceinfo

import android.content.Context
import android.util.AttributeSet
import com.android.settings.R
import com.android.settings.widget.ValidatedEditTextPreference
import com.android.settingslib.widget.GroupSectionDividerMixin
import com.android.settingslib.widget.NormalPaddingMixin

class DeviceNameCardPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.editTextPreferenceStyle,
    defStyleRes: Int = 0,
) : ValidatedEditTextPreference(context, attrs, defStyleAttr, defStyleRes),
    GroupSectionDividerMixin,
    NormalPaddingMixin {

    init {
        layoutResource = R.layout.edith_device_name_card
    }
}
