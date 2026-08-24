package org.edith.settings.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.util.AttributeSet
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.settings.R
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.widget.GroupSectionDividerMixin

/**
 * A "Detailed Specs" card rendered as a 2-column x 3-row grid. Each cell has a
 * title / summary / subsummary. Summary and subsummary can be overridden by
 * device-specific overlay resources; empty overrides fall back to auto-detection.
 */
class DeviceSpecPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes), GroupSectionDividerMixin {

    init {
        isSelectable = false
        setContent { DeviceSpecContent() }
    }
}

@Composable
private fun DeviceSpecContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceBright,
                shape = RoundedCornerShape(
                    context.resources
                        .getDimensionPixelSize(R.dimen.settingslib_preference_corner_radius).dp,
                ),
            )
            .padding(16.dp),
    ) {
        SpecRow(
            cell1 = {
                SpecCell(
                    title = stringResource(R.string.edith_spec_cpu_title),
                    summary = resolve(context, R.string.edith_spec_cpu_summary) { detectBrand() },
                    subsummary = resolve(context, R.string.edith_spec_cpu_subsummary) { detectSoc() },
                )
            },
            cell2 = {
                SpecCell(
                    title = stringResource(R.string.edith_spec_mem_title),
                    summary = resolve(context, R.string.edith_spec_mem_summary) { detectMemSummary() },
                    subsummary = resolve(context, R.string.edith_spec_mem_subsummary) { detectMemSubsummary(context) },
                )
            },
        )

        SpecRow(
            cell1 = {
                SpecCell(
                    title = stringResource(R.string.edith_spec_gpu_title),
                    summary = resolve(context, R.string.edith_spec_gpu_summary) { detectGpuSummary() },
                    subsummary = resolve(context, R.string.edith_spec_gpu_subsummary) { detectGpuSubsummary() },
                )
            },
            cell2 = {
                SpecCell(
                    title = stringResource(R.string.edith_spec_display_title),
                    summary = resolve(context, R.string.edith_spec_display_summary) { detectResolution(context) },
                    subsummary = resolve(context, R.string.edith_spec_display_subsummary) { detectDisplaySubsummary() },
                )
            },
        )

        SpecRow(
            cell1 = {
                SpecCell(
                    title = stringResource(R.string.edith_spec_camera_title),
                    summary = stringResource(R.string.edith_spec_camera_rear_summary),
                    subsummary = resolve(context, R.string.edith_spec_camera_rear_subsummary) { detectCameraRear() },
                )
            },
            cell2 = {
                SpecCell(
                    title = null,
                    summary = stringResource(R.string.edith_spec_camera_front_summary),
                    subsummary = resolve(context, R.string.edith_spec_camera_front_subsummary) { detectCameraFront() },
                )
            },
        )
    }
}

@Composable
private fun SpecRow(
    cell1: @Composable () -> Unit,
    cell2: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) { cell1() }
        Column(modifier = Modifier.weight(1f)) { cell2() }
    }
}

@Composable
private fun SpecCell(
    title: String?,
    summary: String,
    subsummary: String,
) {
    if (title != null) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Text(
        text = summary,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = subsummary,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun resolve(context: Context, overrideRes: Int, detect: () -> String): String =
    context.getString(overrideRes).ifEmpty { detect() }

private fun detectBrand(): String =
    SystemProperties.get("ro.product.brand", "").ifEmpty { android.os.Build.BRAND }

private fun detectSoc(): String =
    SystemProperties.get("ro.soc.model", "")
        .ifEmpty { SystemProperties.get("ro.hardware.chipname", "") }
        .ifEmpty { SystemProperties.get("ro.board.platform", "") }

private fun detectRam(context: Context): String {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    return formatAdvertisedSize(memInfo.totalMem)
}

private fun detectStorage(): String {
    val stat = StatFs(Environment.getDataDirectory().path)
    return formatAdvertisedSize(stat.totalBytes)
}

/**
 * Rounds a byte count up to the nearest commonly-advertised size (in whole
 * binary GiB), so e.g. 3.8 GiB RAM reports as "4 GB" and 112 GiB storage
 * reports as "128 GB".
 */
private fun formatAdvertisedSize(bytes: Long): String {
    val gib = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    val advertised = ADVERTISED_GB.firstOrNull { gib <= it } ?: ADVERTISED_GB.last()
    return if (advertised >= 1024) {
        "${advertised / 1024} TB"
    } else {
        "$advertised GB"
    }
}

private fun detectMemSummary(): String =
    SystemProperties.get("ro.hardware.ram", "")
        .ifEmpty { SystemProperties.get("ro.boot.ddr_type", "") }

private fun detectMemSubsummary(context: Context): String =
    "${detectRam(context)} | ${detectStorage()}"

private fun detectGpuSummary(): String =
    SystemProperties.get("ro.hardware.egl", "")

private fun detectGpuSubsummary(): String =
    SystemProperties.get("ro.gpu.name", "")

private fun detectResolution(context: Context): String {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val bounds = wm.currentWindowMetrics.bounds
    return "${bounds.width()} × ${bounds.height()}"
}

private fun detectDisplaySubsummary(): String =
    SystemProperties.get("ro.display.type", "")

private fun detectCameraRear(): String =
    SystemProperties.get("ro.camera.rear", "")

private fun detectCameraFront(): String =
    SystemProperties.get("ro.camera.front", "")

private val ADVERTISED_GB = intArrayOf(
    1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 64, 128, 256, 512, 1024, 2048,
)
