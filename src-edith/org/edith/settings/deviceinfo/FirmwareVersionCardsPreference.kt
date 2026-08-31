/*
 * Copyright (C) 2026 The Android Open Source Project
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

package org.edith.settings.deviceinfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.UserHandle
import android.os.UserManager
import android.util.AttributeSet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.settings.R
import com.android.settings.Utils
import com.android.settings.spa.preference.ComposePreference
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.RestrictedLockUtils
import com.android.settingslib.RestrictedLockUtilsInternal
import com.android.settingslib.spa.framework.compose.rememberDrawablePainter
import com.android.settingslib.widget.GroupSectionDividerMixin
import com.android.settings.utils.getLocale
import org.edith.settings.widget.EdithCard

/**
 * A horizontal pair of [EdithCard]s showing the Android version and the security patch level.
 */
class FirmwareVersionCardsPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ComposePreference(context, attrs, defStyleAttr, defStyleRes), GroupSectionDividerMixin {

    init {
        isSelectable = false
        setContent { FirmwareVersionCardsContent() }
    }
}

@Composable
private fun FirmwareVersionCardsContent() {
    val context = LocalContext.current
    val hits = remember { LongArray(ACTIVITY_TRIGGER_COUNT) }

    val onAndroidVersionClick = click@{
        if (Utils.isMonkeyRunning()) return@click

        // Remove oldest hit and check whether there are 3 clicks within 500ms.
        for (index in 1 until ACTIVITY_TRIGGER_COUNT) hits[index - 1] = hits[index]
        hits[ACTIVITY_TRIGGER_COUNT - 1] = SystemClock.uptimeMillis()
        if (hits[ACTIVITY_TRIGGER_COUNT - 1] - hits[0] > DELAY_TIMER_MILLIS) return@click

        val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
        if (userManager?.hasUserRestriction(UserManager.DISALLOW_FUN) != true) {
            // Not restricted; launch the platform logo easter egg.
            context.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .setClassName("android", com.android.internal.app.PlatLogoActivity::class.java.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return@click
        }

        // Sorry, no fun for you!
        val myUserId = UserHandle.myUserId()
        val enforcedAdmin =
            RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
                context,
                UserManager.DISALLOW_FUN,
                myUserId,
            ) ?: return@click
        val disallowedBySystem =
            RestrictedLockUtilsInternal.hasBaseUserRestriction(
                context,
                UserManager.DISALLOW_FUN,
                myUserId,
            )
        if (!disallowedBySystem) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, enforcedAdmin)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EdithCard(
            modifier = Modifier.weight(1f),
            icon = rememberDrawablePainter(context.getDrawable(R.drawable.ic_android_vd_theme_24)),
            title = context.getString(R.string.firmware_version),
            summary = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY,
            isActive = false,
            titleStyle = MaterialTheme.typography.titleMedium,
            onClick = onAndroidVersionClick,
        )

        EdithCard(
            modifier = Modifier.weight(1f),
            icon = rememberDrawablePainter(context.getDrawable(R.drawable.ic_safety_center_shield)),
            title = context.getString(R.string.edith_security_patch),
            summary = DeviceInfoUtils.getSecurityPatch(context.getLocale()) ?: "",
            isActive = false,
            titleStyle = MaterialTheme.typography.titleMedium,
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://source.android.com/docs/security/bulletin/"))
                )
            },
        )
    }
}

private const val DELAY_TIMER_MILLIS = 500L
private const val ACTIVITY_TRIGGER_COUNT = 3
