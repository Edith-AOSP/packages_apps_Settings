package org.edith.settings.core.variables

import android.content.Context
import android.util.TypedValue

object Styles {
    fun getColorAccent(context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
        return tv.data
    }

    fun getTextColorPrimary(context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textColorPrimary, tv, true)
        return tv.data
    }

    fun getCardContentBackgroundColor(context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
        return tv.data
    }

    fun getContentBackgroundColor(context: Context): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
        return tv.data
    }

    fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode
            and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
