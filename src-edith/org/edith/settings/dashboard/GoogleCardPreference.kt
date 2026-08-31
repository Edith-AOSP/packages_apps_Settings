package org.edith.settings.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settingslib.RestrictedTopLevelPreference
import org.edith.settings.core.variables.Styles

class GoogleCardPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : RestrictedTopLevelPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        layoutResource = R.layout.edith_google_card
        isSelectable = true
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val cornerRadius = context.resources
            .getDimensionPixelSize(R.dimen.settingslib_preference_corner_radius).toFloat()

        val card = holder.findViewById(R.id.google_card) as View
        card.clipToOutline = true
        card.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }

        val scrimColor = Styles.getCardScrimColor(context)
        val scrim = holder.findViewById(R.id.google_scrim) as View
        scrim.setBackgroundColor(scrimColor)
        scrim.alpha = 0.85f

        val textColor = Styles.getAccentTextColor(context)

        val logo = holder.findViewById(R.id.google_logo) as ImageView

        val titleView = holder.findViewById(R.id.google_title) as TextView
        titleView.text = title ?: context.getString(R.string.gms_enabled_title)
        titleView.setTextColor(textColor)

        val summaryView = holder.findViewById(R.id.google_summary) as TextView
        summaryView.text = summary ?: context.getString(R.string.google_settings_summary)
        summaryView.setTextColor(textColor)

        val avatar = holder.findViewById(R.id.google_avatar) as ImageView
        if (icon != null) {
            avatar.setImageDrawable(icon)
        }

        val radii = floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
            cornerRadius, cornerRadius, cornerRadius, cornerRadius)
        card.foreground = RippleDrawable(
            android.content.res.ColorStateList.valueOf(Color.TRANSPARENT),
            ColorDrawable(Color.TRANSPARENT),
            ShapeDrawable(RoundRectShape(radii, null, null)).apply {
                paint.color = Color.WHITE
            },
        )
    }
}
