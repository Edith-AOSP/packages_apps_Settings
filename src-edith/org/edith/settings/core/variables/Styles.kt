package org.edith.settings.core.variables

import android.content.Context
import android.util.TypedValue

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

import com.android.settingslib.widget.theme.R as ThemeR

/**
 * Centralized color and style resolution for the Edith Settings UI.
 *
 * All Material roles are resolved from the settingslib theme resources (which follow the
 * light/dark theme and dynamic color), so callers don't need inline {@code TypedValue} or
 * {@code context.getColor(...)} boilerplate.
 */
object Styles {

    @ColorInt
    fun getColorAccent(context: Context): Int =
        resolveAttribute(context, android.R.attr.colorAccent)

    @ColorInt
    fun getTextColorPrimary(context: Context): Int =
        resolveAttribute(context, android.R.attr.textColorPrimary)

    @ColorInt
    fun getTextColorSecondary(context: Context): Int =
        resolveAttribute(context, android.R.attr.textColorSecondary)

    @ColorInt
    fun getCardContentBackgroundColor(context: Context): Int =
        resolveAttribute(context, android.R.attr.colorBackground)

    @ColorInt
    fun getContentBackgroundColor(context: Context): Int =
        resolveAttribute(context, android.R.attr.colorBackground)

    @ColorInt
    fun getPrimary(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorPrimary)

    @ColorInt
    fun getOnPrimary(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorOnPrimary)

    @ColorInt
    fun getSecondary(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSecondary)

    @ColorInt
    fun getTertiaryContainer(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorTertiaryContainer)

    @ColorInt
    fun getOnTertiaryContainer(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorOnTertiaryContainer)

    @ColorInt
    fun getSurface(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSurface)

    @ColorInt
    fun getSurfaceBright(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSurfaceBright)

    @ColorInt
    fun getSurfaceContainer(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSurfaceContainer)

    @ColorInt
    fun getSurfaceContainerHighest(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSurfaceContainerHighest)

    @ColorInt
    fun getSurfaceVariant(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSurfaceVariant)

    @ColorInt
    fun getOnSurface(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorOnSurface)

    @ColorInt
    fun getOnSurfaceVariant(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorOnSurfaceVariant)

    /** Bright brand-tinted scrim used to overlay the software information card. */
    @ColorInt
    fun getScrimColor(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorPrimaryInverse)

    /** Wave tint color for the software information card. */
    @ColorInt
    fun getWaveColor(context: Context): Int =
        context.getColor(ThemeR.color.settingslib_materialColorSecondary)

    /** Card scrim color (dark neutral in night mode, light neutral in day mode). */
    @ColorInt
    fun getCardScrimColor(context: Context): Int =
        if (isNightMode(context)) {
            context.getColor(android.R.color.system_neutral2_800)
        } else {
            context.getColor(android.R.color.system_neutral1_100)
        }

    /** Accent text color (light tint in night mode, dark tint in day mode). */
    @ColorInt
    fun getAccentTextColor(context: Context): Int =
        if (isNightMode(context)) {
            context.getColor(android.R.color.system_accent1_50)
        } else {
            context.getColor(android.R.color.system_accent1_800)
        }

    fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode
            and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    @ColorInt
    private fun resolveAttribute(context: Context, attr: Int, @ColorInt fallback: Int): Int {
        val outValue = TypedValue()
        return if (context.theme.resolveAttribute(attr, outValue, true)) {
            outValue.data
        } else {
            fallback
        }
    }

    @ColorInt
    private fun resolveAttribute(context: Context, attr: Int): Int =
        resolveAttribute(context, attr, getSurface(context))
}

/** Converts an ARGB color int to a Compose [Color]. */
fun Int.toComposeColor(): Color = Color(this)
