/*
 * Copyright (C) 2023 Halcyon Project
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

package com.android.settings.edith.dashboard

import android.app.settings.SettingsEnums
import android.content.Context
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.bluetooth.Utils
import com.android.settings.core.SubSettingLauncher
import com.android.settings.network.InternetPreferenceRepository
import com.android.settingslib.bluetooth.BluetoothCallback
import com.android.settingslib.bluetooth.CachedBluetoothDevice
import com.android.settingslib.bluetooth.LocalBluetoothManager
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.spa.framework.compose.rememberDrawablePainter
import com.android.settingslib.spa.framework.theme.SettingsTheme
import com.android.settingslib.widget.LayoutPreference

class ConnectivityCardsController(context: Context) : AbstractPreferenceController(context) {

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val cards = screen.findPreference<LayoutPreference>(KEY) ?: return

        val composeView = cards.findViewById<ComposeView>(R.id.connectivity_compose)
        composeView?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow,
        )
        composeView?.setContent {
            SettingsTheme {
                ConnectivityCards()
            }
        }
    }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY

    companion object {
        private const val KEY = "homepage_connectivity_cards"
    }
}

@Composable
private fun ConnectivityCards() {
    val context = LocalContext.current
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    val iconSize = dimensionResource(R.dimen.dashboard_tile_image_size)
    val shape = RoundedCornerShape(cornerRadius)

    // ---------- Internet state ----------
    var internetSummary by remember { mutableStateOf("") }
    var internetConnected by remember { mutableStateOf(false) }
    val internetRepo = remember { InternetPreferenceRepository(context) }
    LaunchedEffect(internetRepo) {
        internetRepo.displayInfoFlow().collect { displayInfo ->
            internetSummary = displayInfo.summary
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            internetConnected = caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    // ---------- Bluetooth state ----------
    var devicesSummary by remember { mutableStateOf(context.getString(R.string.not_connected)) }
    var devicesConnected by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val btManager: LocalBluetoothManager? = Utils.getLocalBluetoothManager(context)
        val callback = object : BluetoothCallback {
            override fun onBluetoothStateChanged(bluetoothState: Int) { update() }
            override fun onConnectionStateChanged(
                device: CachedBluetoothDevice?, state: Int) { update() }
            override fun onAclConnectionStateChanged(
                device: CachedBluetoothDevice, state: Int) { update() }
            override fun onActiveDeviceChanged(
                device: CachedBluetoothDevice?, bluetoothProfile: Int) { update() }
            override fun onDeviceBondStateChanged(
                device: CachedBluetoothDevice, bondState: Int) { update() }
            override fun onProfileConnectionStateChanged(
                device: CachedBluetoothDevice, state: Int, bluetoothProfile: Int) { update() }

            fun update() {
                val manager = btManager ?: return
                val adapter = manager.bluetoothAdapter ?: return
                val connected = adapter.bondedDevices?.firstOrNull { it.isConnected }
                devicesConnected = connected != null
                devicesSummary = if (!adapter.isEnabled) {
                    context.getString(com.android.settingslib.R.string.bluetooth_disconnected)
                } else {
                    connected?.name ?: context.getString(R.string.not_connected)
                }
            }
        }
        btManager?.eventManager?.registerCallback(callback)
        callback.update()
        onDispose { btManager?.eventManager?.unregisterCallback(callback) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
            ConnectivityCard(
                modifier = Modifier.weight(1f),
                icon = rememberDrawablePainter(
                    context.getDrawable(R.drawable.ic_dashboard_network)
                ),
                title = stringResource(R.string.network_dashboard_title),
                summary = internetSummary.ifEmpty {
                    stringResource(R.string.not_connected)
                },
                isActive = internetConnected,
                shape = shape,
                iconSize = iconSize,
                onClick = {
                    SubSettingLauncher(context)
                        .setDestination("com.android.settings.network.NetworkDashboardFragment")
                        .setSourceMetricsCategory(SettingsEnums.SETTINGS_NETWORK_CATEGORY)
                        .launch()
                },
            )

            ConnectivityCard(
                modifier = Modifier.weight(1f),
                icon = rememberDrawablePainter(
                    context.getDrawable(R.drawable.ic_dashboard_devices)
                ),
                title = stringResource(R.string.connected_devices_dashboard_title),
                summary = devicesSummary,
                isActive = devicesConnected,
                shape = shape,
                iconSize = iconSize,
                onClick = {
                    SubSettingLauncher(context)
                        .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                        .setSourceMetricsCategory(SettingsEnums.SETTINGS_NETWORK_CATEGORY)
                        .launch()
                },
            )
        }
}

@Composable
private fun ConnectivityCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    summary: String,
    isActive: Boolean,
    shape: RoundedCornerShape,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceBright
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Inside,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(0.dp))

            Text(
                text = summary,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
