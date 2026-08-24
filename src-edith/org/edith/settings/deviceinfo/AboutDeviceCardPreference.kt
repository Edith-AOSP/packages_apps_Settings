package org.edith.settings.deviceinfo

import android.content.Context
import android.os.Build
import android.os.SystemProperties
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.settings.R
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.widget.GroupSectionDividerMixin
import org.edith.settings.widget.WallpaperBlurView

class AboutDeviceCardPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes), GroupSectionDividerMixin {

    init {
        isSelectable = false
        setContent { AboutDeviceCardContent() }
    }
}

@Composable
private fun AboutDeviceCardContent() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    val smallRadius = 4.dp

    // Rounded top corners only, small radius on the bottom.
    val shape = RoundedCornerShape(
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomStart = smallRadius,
        bottomEnd = smallRadius,
    )

    val isNight = (context.resources.configuration.uiMode
        and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES

    val scrimColor = if (isNight) {
        Color(context.getColor(android.R.color.system_neutral2_800))
    } else {
        Color(context.getColor(android.R.color.system_neutral1_100))
    }

    val textColor = if (isNight) {
        Color(context.getColor(android.R.color.system_accent1_50))
    } else {
        Color(context.getColor(android.R.color.system_accent1_800))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            .height(160.dp)
            .clip(shape),
    ) {
        // Blurred wallpaper.
        AndroidView(
            factory = { ctx -> WallpaperBlurView(ctx) },
            modifier = Modifier.fillMaxSize(),
        )

        // Light scrim so the blurred wallpaper stays visible but text remains legible.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = 0.55f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.edith_ic_app_google),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = resolveTitle(context),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = resolveBrand(context),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = resolveSubSummary(),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }
    }
}

private fun resolveTitle(context: Context): String =
    context.getString(R.string.edith_about_device_title).ifEmpty { Build.MODEL }

private fun resolveBrand(context: Context): String {
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
