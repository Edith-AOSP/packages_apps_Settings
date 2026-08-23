package org.edith.settings.deviceinfo

import android.content.Context
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import android.os.SystemProperties
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settingslib.widget.GroupSectionDividerMixin
import com.android.settingslib.widget.NormalPaddingMixin

class AboutDeviceCardPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle,
    defStyleRes: Int = 0,
) : Preference(context, attrs, defStyleAttr, defStyleRes),
    GroupSectionDividerMixin,
    NormalPaddingMixin {

    init {
        layoutResource = R.layout.edith_about_card
        isSelectable = false
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val resources = context.resources
        val cornerRadius = resources
            .getDimensionPixelSize(R.dimen.settingslib_preference_corner_radius).toFloat()
        val smallRadius = 4f * resources.displayMetrics.density

        val card = holder.findViewById(R.id.about_card) as View
        card.clipToOutline = true
        card.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val path = Path().apply {
                    addRoundRect(
                        RectF(0f, 0f, view.width.toFloat(), view.height.toFloat()),
                        floatArrayOf(
                            cornerRadius, cornerRadius,
                            cornerRadius, cornerRadius,
                            smallRadius, smallRadius,
                            smallRadius, smallRadius,
                        ),
                        Path.Direction.CW,
                    )
                }
                outline.setConvexPath(path)
            }
        }

        val isNight = (resources.configuration.uiMode
            and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Light scrim so the blurred wallpaper stays visible but text remains legible.
        val scrimColor = context.getColor(
            if (isNight) android.R.color.system_neutral2_800
            else android.R.color.system_neutral1_100
        )
        (holder.findViewById(R.id.about_scrim) as View).apply {
            setBackgroundColor(scrimColor)
            alpha = 0.55f
        }

        val textColor = context.getColor(
            if (isNight) android.R.color.system_accent1_50
            else android.R.color.system_accent1_800
        )

        (holder.findViewById(R.id.about_title) as TextView).apply {
            text = resolveTitle()
            setTextColor(textColor)
        }

        (holder.findViewById(R.id.about_summary) as TextView).apply {
            text = resolveBrand()
            setTextColor(textColor)
        }

        (holder.findViewById(R.id.about_sub_summary) as TextView).apply {
            text = resolveSubSummary()
            setTextColor(textColor)
        }
    }

    private fun resolveTitle(): String {
        val configured = context.getString(R.string.edith_about_device_title)
        return configured.ifEmpty { Build.MODEL }
    }

    private fun resolveBrand(): String {
        val configured = context.getString(R.string.edith_about_device_brand)
        val brand = if (configured.isNotEmpty()) configured
            else SystemProperties.get("ro.product.brand", "").ifEmpty { Build.BRAND }
        return context.getString(R.string.edith_about_device_by_brand, brand)
    }

    private fun resolveSubSummary(): String {
        val model = SystemProperties.get("ro.product.model", Build.MODEL)
        val edithDevice = SystemProperties.get("ro.edith.device", "")
        return if (edithDevice.isNotEmpty()) "$model | $edithDevice" else model
    }
}
