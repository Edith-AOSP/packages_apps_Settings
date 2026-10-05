/*
 * Copyright (C) 2024-2026 Lunaris AOSP
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

package org.edith.settings.display.fonts

import android.content.Context
import android.content.om.OverlayManager
import android.content.res.Resources
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * Enumerates and applies the system font overlays (`android.theme.customization.font`).
 *
 * A font is an RRO overlay package that targets `android`, declares the font customization
 * category, and exposes `config_bodyFontFamily` (+ the medium/headline variants). Selecting one
 * enables it exclusively in its category and writes the four families to
 * [PROP_OVERLAY_FONTS] (`body:bodyMedium:headline:headlineMedium`), which the framework's
 * `FontController`/`Typeface` read at runtime.
 *
 * The default font (Google Sans, `google-sans-flex`) has no overlay; it clears the property.
 */
class FontOverlayManager(context: Context) {

    private val mContext = context.applicationContext
    private val mOverlayManager: OverlayManager? =
        mContext.getSystemService(OverlayManager::class.java)

    /** An available font: the overlay package (null = the default) and its display label. */
    data class Font(val packageName: String?, val label: String)

    /** All selectable fonts, with the default (Google Sans) first. */
    fun getAvailableFonts(): List<Font> {
        val fonts = mutableListOf(Font(null, DEFAULT_LABEL))
        val manager = mOverlayManager ?: return fonts
        try {
            manager
                .getOverlayInfosForTarget(TARGET_PACKAGE, UserHandle.SYSTEM)
                .asSequence()
                .filter { it.category == CATEGORY_FONT }
                .map { it.packageName }
                .distinct()
                .sorted()
                // Hide overlays that map to the platform default family (redundant with the
                // synthesised "Google Sans" default chip) and the stock Noto Serif Source overlay.
                .filterNot { bodyFamilyOf(it) == DEFAULT_FAMILY || it in EXCLUDED_OVERLAYS }
                .forEach { pkg -> fonts.add(Font(pkg, labelFor(pkg))) }
        } catch (e: Exception) {
            Log.e(TAG, "getAvailableFonts failed", e)
        }
        return fonts
    }

    /** The body font family declared by an overlay package, or null if it can't be read. */
    private fun bodyFamilyOf(overlayPackage: String): String? {
        return try {
            val res = mContext.packageManager.getResourcesForApplication(overlayPackage)
            val resId = res.getIdentifier(CONFIG_BODY, "string", overlayPackage)
            if (resId == 0) null else res.getString(resId)
        } catch (e: Exception) {
            null
        }
    }

    /** The currently active font overlay package, or null when the default is active. */
    fun getActiveFontPackage(): String? {
        val raw =
            Settings.Secure.getStringForUser(
                mContext.contentResolver,
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                UserHandle.USER_CURRENT,
            ) ?: return null
        if (raw.isEmpty()) return null
        return try {
            val pkg = JSONObject(raw).optString(CATEGORY_FONT, null)
            if (pkg.isNullOrEmpty() || "android" == pkg) null else pkg
        } catch (e: JSONException) {
            Log.e(TAG, "getActiveFontPackage: JSON parse error", e)
            null
        }
    }

    /** The label of the active font (or the default label). */
    fun getActiveFontLabel(): String {
        val pkg = getActiveFontPackage() ?: return DEFAULT_LABEL
        return labelFor(pkg)
    }

    /** Enables [packageName] (or the default when null) and writes the font property. */
    fun applyFont(packageName: String?): Boolean {
        val isDefault = packageName == null || DEFAULT_OVERLAY_PACKAGE == packageName
        val manager = mOverlayManager ?: return false
        return try {
            if (isDefault) {
                val current = getActiveFontPackage()
                if (current != null) {
                    manager.setEnabled(current, false, UserHandle.SYSTEM)
                }
                SystemProperties.set(PROP_OVERLAY_FONTS, "")
                writeFontSetting(null)
            } else {
                manager.setEnabledExclusiveInCategory(packageName, UserHandle.SYSTEM)
                writeOverlayFontProp(packageName)
                writeFontSetting(packageName)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "applyFont failed for: $packageName", e)
            false
        }
    }

    /**
     * Records (or clears) the font category in [Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES].
     *
     * The system's `ThemeSettingsManager` only tracks the color/theme-style categories, so a font
     * overlay enabled directly through `OverlayManager` is not reflected there. Writing the key here
     * keeps [getActiveFontPackage] (and the Settings summary) accurate.
     */
    private fun writeFontSetting(packageName: String?) {
        try {
            val current =
                Settings.Secure.getStringForUser(
                    mContext.contentResolver,
                    Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                    UserHandle.USER_CURRENT,
                )
            val json = if (current.isNullOrEmpty()) JSONObject() else JSONObject(current)
            if (packageName == null) {
                json.remove(CATEGORY_FONT)
            } else {
                json.put(CATEGORY_FONT, packageName)
            }
            Settings.Secure.putStringForUser(
                mContext.contentResolver,
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                json.toString(),
                UserHandle.USER_CURRENT,
            )
        } catch (e: Exception) {
            Log.w(TAG, "writeFontSetting failed for $packageName", e)
        }
    }

    /** Resets to the default font. */
    fun resetToDefault(): Boolean = applyFont(null)

    private fun writeOverlayFontProp(overlayPackage: String) {
        try {
            val res: Resources = mContext.packageManager.getResourcesForApplication(overlayPackage)
            val body = readResString(res, overlayPackage, CONFIG_BODY)
            val bodyMedium = readResString(res, overlayPackage, CONFIG_BODY_MEDIUM)
            val headline = readResString(res, overlayPackage, CONFIG_HEADLINE)
            val headlineMedium = readResString(res, overlayPackage, CONFIG_HEADLINE_MEDIUM)
            val value = "$body:$bodyMedium:$headline:$headlineMedium"
            SystemProperties.set(PROP_OVERLAY_FONTS, value)
            Log.d(TAG, "$PROP_OVERLAY_FONTS=$value")
        } catch (e: Exception) {
            Log.w(TAG, "writeOverlayFontProp failed for $overlayPackage", e)
        }
    }

    private fun readResString(res: Resources, overlayPackage: String, configName: String): String {
        val resId = res.getIdentifier(configName, "string", overlayPackage)
        if (resId == 0) {
            Log.w(TAG, "$overlayPackage missing '$configName', using package name")
            return overlayPackage
        }
        return res.getString(resId)
    }

    private fun labelFor(pkg: String): String {
        val pm = mContext.packageManager
        return try {
            pm.getApplicationInfo(pkg, 0).loadLabel(pm).toString()
        } catch (e: Exception) {
            pkg
        }
    }

    companion object {
        private const val TAG = "FontOverlayManager"

        /** RRO category for the system font overlays. */
        const val CATEGORY_FONT = "android.theme.customization.font"

        /** Package the font overlays target. */
        private const val TARGET_PACKAGE = "android"

        /** Sentinel for the default (no overlay) font. */
        private const val DEFAULT_OVERLAY_PACKAGE = "android"

        /** Property read by the framework's `FontController`. */
        const val PROP_OVERLAY_FONTS = "persist.sys.edith_overlay_fonts"

        /** Label shown for the default font (Google Sans, the platform default). */
        const val DEFAULT_LABEL = "Google Sans"

        /** Body family of the platform default font; overlays mapping to it are hidden. */
        private const val DEFAULT_FAMILY = "google-sans-flex"

        /** Font overlays that are not offered in the picker. */
        private val EXCLUDED_OVERLAYS = setOf("com.android.theme.font.notoserifsource")

        private const val CONFIG_BODY = "config_bodyFontFamily"
        private const val CONFIG_BODY_MEDIUM = "config_bodyFontFamilyMedium"
        private const val CONFIG_HEADLINE = "config_headlineFontFamily"
        private const val CONFIG_HEADLINE_MEDIUM = "config_headlineFontFamilyMedium"
    }
}
