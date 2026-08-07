package com.android.settings.edith.dashboard

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settingslib.RestrictedTopLevelPreference
import com.android.settingslib.spa.framework.theme.SettingsTheme

class RestrictedHomepageSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : RestrictedTopLevelPreference(context, attrs, defStyleAttr, defStyleRes) {

    private var _iconVisible by mutableStateOf(true)
    private var _iconPaddingStart by mutableIntStateOf(-1)
    private var _textPaddingStart by mutableIntStateOf(-1)
    private var _checked by mutableStateOf(false)

    init {
        layoutResource = R.layout.preference_compose
        isSelectable = true
    }

    fun setIconVisible(visible: Boolean) {
        _iconVisible = visible
    }

    fun setIconPaddingStart(paddingStart: Int) {
        _iconPaddingStart = paddingStart
    }

    fun setTextPaddingStart(paddingStart: Int) {
        _textPaddingStart = paddingStart
    }

    @Suppress("DEPRECATION")
    override fun performClick() {
        if (!getRestrictedPreferenceHelper().performClick()) {
            val newValue = !_checked
            if (callChangeListener(newValue)) {
                _checked = newValue
                val mode = if (newValue) 1 else 0
                Settings.Global.putInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    mode,
                )
                val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                intent.putExtra("state", newValue)
                context.sendBroadcastAsUser(intent, UserHandle.ALL)
                notifyChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        _checked = Settings.Global.getInt(
            context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

        (holder.itemView as ComposeView).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindow,
            )
            setContent {
                SettingsTheme {
                    EdithHomepageSwitchContent(
                        icon = icon,
                        title = title,
                        summary = summary,
                        iconVisible = _iconVisible,
                        iconPaddingStartPx = _iconPaddingStart,
                        textPaddingStartPx = _textPaddingStart,
                        checked = _checked,
                    )
                }
            }
        }
    }
}
