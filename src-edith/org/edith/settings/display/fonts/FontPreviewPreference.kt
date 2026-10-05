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

package org.edith.settings.display.fonts

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.fonts.FontVariationAxis
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceViewHolder
import com.android.settings.R
import com.android.settings.spa.preference.ComposeGroupSectionPreference
import org.edith.settings.core.variables.Styles
import org.edith.settings.widget.WallpaperBlurView
import java.io.File

/**
 * Compose preference that hosts the whole font picker: a live preview of the selected font
 * (Lorem ipsum as a heading, a sub-heading and body text), a horizontally scrollable row of font
 * chips, a font-weight slider and an Apply button.
 */
class FontPreviewPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) :
    ComposeGroupSectionPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setContent { FontPickerContent() }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setPadding(0, 0, 0, 0)
    }
}

/**
 * Maps a font selection to a Compose [FontFamily] at an explicit weight.
 *
 * `packageName == null` is the default (Google Sans / `google-sans-flex`); otherwise the family is
 * read from the overlay's `config_bodyFontFamily`. The font is loaded **directly from its file**
 * (from the system font config) rather than via `Typeface.create(String)`: once a custom font is
 * active, the override system rewrites family resolution (e.g. `google-sans-flex` -> the active
 * font), which would make the preview of Google Sans show the wrong font.
 */
@Composable
private fun familyOf(context: Context, packageName: String?, weight: Int): FontFamily {
    val family = remember(packageName) { familyNameOf(context, packageName) }
    val file = remember(family) { fontFileFor(family) }
    return remember(family, weight, file) { FontFamily(typefaceFor(family, file, weight)) }
}

/** The system font family name for a selection (default family, or an overlay's family). */
private fun familyNameOf(context: Context, packageName: String?): String {
    if (packageName == null) return DEFAULT_FAMILY
    return try {
        val res = context.packageManager.getResourcesForApplication(packageName)
        val resId = res.getIdentifier("config_bodyFontFamily", "string", packageName)
        if (resId != 0) res.getString(resId) else packageName
    } catch (e: Exception) {
        packageName
    }
}

/**
 * Resolves [family] to its font file by parsing the font XML directly.
 *
 * We don't use [SystemFonts]: in the app process it throws / returns nothing for the customization
 * families. Parsing `/product/etc/fonts_customization.xml` (then `/system/etc/fonts.xml`) is
 * reliable and lets us load the font file directly, bypassing the override system (which rewrites
 * `google-sans-flex` to the active custom font).
 */
private fun fontFileFor(family: String): File? {
    val parsed = customizationFontFileFor(family)
    Log.d(TAG, "fontFileFor($family) -> $parsed")
    return parsed
}

/** Parses `fonts_customization.xml` (and `fonts.xml`) for [family] and returns its first font file. */
private fun customizationFontFileFor(family: String): File? {
    val files =
        listOf(
            File("/product/etc/fonts_customization.xml"),
            File("/system/etc/fonts.xml"),
        )
    for (xml in files) {
        val parsed =
            runCatching { parseFontFamilyFile(xml, family) }
                .onFailure { Log.w(TAG, "parse ${xml.path} for $family failed", it) }
                .getOrNull()
        Log.d(TAG, "parse ${xml.path} for $family -> $parsed")
        if (parsed != null) return parsed
    }
    return null
}

/** Reads [xml], finds the `<family>`/`<family-list>` named [family], returns its first font file. */
private fun parseFontFamilyFile(xml: File, family: String): File? {
    if (!xml.exists()) return null
    val parser = Xml.newPullParser()
    // Keep the stream open for the whole parse (XmlPullParser reads lazily).
    xml.inputStream().use { stream ->
        parser.setInput(stream, null)
        var matchDepth = -1
        var depth = 0
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    depth++
                    val tag = parser.name
                    if (matchDepth == -1 &&
                        (tag == "family" || tag == "family-list") &&
                        parser.getAttributeValue(null, "name") == family
                    ) {
                        matchDepth = depth
                    } else if (matchDepth != -1 && tag == "font") {
                        val fileAttr = parser.getAttributeValue(null, "file")
                        if (!fileAttr.isNullOrBlank()) {
                            return resolveFontFile(fileAttr.trim())
                        }
                        // The font file is the first text node inside <font> (before any <axis>).
                        var fileName: String? = null
                        var inner = parser.next()
                        while (inner != XmlPullParser.START_TAG && inner != XmlPullParser.END_TAG) {
                            if (inner == XmlPullParser.TEXT) {
                                val t = parser.text?.trim()
                                if (!t.isNullOrEmpty()) {
                                    fileName = t
                                    break
                                }
                            }
                            inner = parser.next()
                        }
                        if (!fileName.isNullOrBlank()) {
                            return resolveFontFile(fileName.substringBefore(' ').substringBefore('\n'))
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (matchDepth != -1 && depth == matchDepth) matchDepth = -1
                    depth--
                }
            }
            event = parser.next()
        }
    }
    return null
}

/** Resolves a font file name against the font search dirs. */
private fun resolveFontFile(fileName: String): File? {
    if (fileName.startsWith("/")) {
        val f = File(fileName)
        return if (f.exists()) f else null
    }
    for (dir in FONT_DIRS) {
        val f = File(dir, fileName)
        if (f.exists()) return f
    }
    return null
}

private val FONT_DIRS =
    listOf(
        "/product/fonts/",
        "/system/fonts/",
        "/system_ext/fonts/",
    )

/** Builds a [Typeface] at [weight], preferring the exact [file]; else falls back to by-name. */
private fun typefaceFor(family: String, file: File?, weight: Int): Typeface {
    if (file != null) {
        runCatching {
                val builder = Typeface.Builder(file).setWeight(weight).setItalic(false)
                // Drive the variable axis explicitly so the weight always applies.
                builder.setFontVariationSettings(
                    arrayOf(FontVariationAxis("wght", weight.toFloat()))
                )
                return builder.build()
            }
            .onFailure { Log.w(TAG, "Builder failed for $family ($file)", it) }
    }
    Log.w(TAG, "No font file for $family, falling back to Typeface.create")
    val base =
        runCatching { Typeface.create(family, Typeface.NORMAL) }.getOrDefault(Typeface.DEFAULT)
    return runCatching { Typeface.create(base, weight, /* italic = */ false) }.getOrDefault(base)
}

private const val TAG = "FontPreviewPreference"

/** Platform default font family (Google Sans). */
private const val DEFAULT_FAMILY = "google-sans-flex"

@Composable
private fun FontPickerContent() {
    val context = LocalContext.current
    val manager = remember { FontOverlayManager(context) }
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    val scrim = Color(Styles.getSurface(context))

    val fonts = remember { manager.getAvailableFonts() }

    // Committed state (what is actually applied) and the staged (editable) values.
    val activeWeight =
        remember {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.FONT_WEIGHT_ADJUSTMENT,
                0,
            )
        }
    var activePackage by remember { mutableStateOf(manager.getActiveFontPackage()) }
    var activeWeightState by remember { mutableIntStateOf(activeWeight) }

    var staged by remember { mutableStateOf(activePackage) }
    var weight by remember { mutableFloatStateOf(activeWeight.toFloat()) }

    val isDirty = staged != activePackage || weight.toInt() != activeWeightState

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.Transparent)
            ) {
                // Frosted wallpaper background (same shell as the QS-style preview).
                AndroidView(
                    factory = { ctx -> WallpaperBlurView(ctx) },
                    modifier = Modifier.matchParentSize(),
                )
                Box(modifier = Modifier.matchParentSize().background(scrim.copy(alpha = 0.2f)))

                // Preview text: Lorem ipsum as heading / sub-heading / body, in the staged font.
                // The weight is pinned via an explicit wght variation so the slider previews the
                // exact weight regardless of the platform fontWeightAdjustment.
                val weightValue = previewWeight(weight)
                val family = familyOf(context, staged, weightValue)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = stringResource(R.string.edith_font_preview_heading),
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = family,
                        color = Color(Styles.getOnSurface(context)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.edith_font_preview_subheading),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = family,
                        color = Color(Styles.getOnSurface(context)),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.edith_font_preview_body),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = family,
                        color = Color(Styles.getOnSurface(context)),
                    )
                }
            }
        }

        // Font chips (horizontally scrollable).
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(fonts, key = { it.packageName ?: "default" }) { font ->
                FontChip(
                    label = font.label,
                    family = familyOf(context, font.packageName, 400),
                    selected = staged == font.packageName,
                    onClick = { staged = font.packageName },
                )
            }
        }

        // Font weight (live preview only; committed on Apply).
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.edith_font_weight_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(Styles.getTextColorPrimary(context)),
                )
                Text(
                    text = stringResource(R.string.edith_font_weight_reset),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(Styles.getColorAccent(context)),
                    modifier = Modifier.clickable { weight = activeWeightState.toFloat() },
                )
            }
            Slider(
                value = weight,
                onValueChange = { weight = it },
                valueRange = WEIGHT_MIN..WEIGHT_MAX,
                steps = WEIGHT_STEPS,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            )
        }

        // Apply (disabled while the font and weight match what is already applied).
        Button(
            enabled = isDirty,
            onClick = {
                if (manager.applyFont(staged)) {
                    Settings.Secure.putInt(
                        context.contentResolver,
                        Settings.Secure.FONT_WEIGHT_ADJUSTMENT,
                        weight.toInt(),
                    )
                    activePackage = staged
                    activeWeightState = weight.toInt()
                    restartSystemUi(context)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = stringResource(R.string.edith_font_apply))
        }

        // Info: a SystemUI restart is required for the change to take effect everywhere.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(Styles.getOnSurfaceVariant(context)),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.edith_font_restart_info),
                style = MaterialTheme.typography.bodySmall,
                color = Color(Styles.getOnSurfaceVariant(context)),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FontChip(
    label: String,
    family: FontFamily,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val accent = Color(Styles.getColorAccent(context))
    val border = if (selected) accent else Color(Styles.getSurfaceContainerHighest(context))
    val labelColor =
        if (selected) accent else Color(Styles.getTextColorPrimary(context))
    Column(
        modifier = Modifier.width(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier.size(width = 96.dp, height = 64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(Styles.getSurfaceContainer(context)))
                    .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(16.dp))
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = family,
                color = labelColor,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = family,
            color = labelColor,
            maxLines = 1,
        )
    }
}

/** The preview weight for a slider adjustment: base 400 + the delta, clamped to 100..1000. */
private fun previewWeight(adjustment: Float): Int =
    (BASE_WEIGHT + adjustment.toInt()).coerceIn(100, 1000)
/** Base preview weight before the adjustment (normal). */
private const val BASE_WEIGHT = 400

private const val WEIGHT_MIN = 0f
private const val WEIGHT_MAX = 300f

// Discrete stops between the endpoints (0, 50, 100, …, 300), matching the framework's
// fontWeightAdjustment deltas (0 = normal, 300 = bold).
private const val WEIGHT_STEPS = 5

/**
 * Restarts SystemUI so the new font is picked up everywhere.
 *
 * Triggers SystemUI's own `SysuiRestartReceiver` (exported, guarded by FORCE_STOP_PACKAGES which
 * Settings holds) so it kills and cleanly respawns itself; falls back to `forceStopPackage`.
 */
private fun restartSystemUi(context: Context) {
    val intent =
        Intent(SYSTEMUI_RESTART_ACTION)
            .setComponent(
                ComponentName(SYSTEMUI_PACKAGE, "$SYSTEMUI_PACKAGE.SysuiRestartReceiver")
            )
            .setData(Uri.parse("package://$SYSTEMUI_PACKAGE"))
    // The "no qualified user" warning for sendBroadcast from the system process is harmless.
    val sent = runCatching { context.sendBroadcast(intent) }.isSuccess
    if (!sent) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        runCatching { am.forceStopPackage(SYSTEMUI_PACKAGE) }
    }
}

private const val SYSTEMUI_PACKAGE = "com.android.systemui"
private const val SYSTEMUI_RESTART_ACTION = "com.android.systemui.action.RESTART"
