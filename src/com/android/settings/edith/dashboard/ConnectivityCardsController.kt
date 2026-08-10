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
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.SubSettingLauncher
import com.android.settingslib.core.AbstractPreferenceController
import com.android.settingslib.spa.framework.compose.rememberDrawablePainter
import com.android.settingslib.spa.framework.theme.SettingsTheme
import com.android.settingslib.widget.LayoutPreference

class ConnectivityCardsController(context: Context) : AbstractPreferenceController(context) {

    private var adapter: BluetoothAdapter? = null
    private var wifiManager: WifiManager? = null
    private var a2dpProfile: BluetoothProfile? = null
    private val handler = Handler(Looper.getMainLooper())

    private var wifiSsid by mutableStateOf<String?>(null)
    private var deviceName by mutableStateOf<String?>(null)

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = proxy
                handler.post { updateDeviceState() }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfile = null
                handler.post { updateDeviceState() }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    handler.post { updateDeviceState() }
                }
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    handler.post { updateWifiState() }
                }
            }
        }
    }

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)

        val cards = screen.findPreference<LayoutPreference>(KEY) ?: return

        wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val btManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        adapter = btManager?.adapter

        if (adapter != null) {
            adapter?.getProfileProxy(mContext, profileListener, BluetoothProfile.A2DP)
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(mContext, receiver, filter,
            ContextCompat.RECEIVER_NOT_EXPORTED)

        updateWifiState()
        updateDeviceState()

        val composeView = cards.findViewById<ComposeView>(R.id.connectivity_compose)
        composeView?.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindow,
        )
        composeView?.setContent {
            SettingsTheme {
                ConnectivityCards(
                    wifiSsid = wifiSsid,
                    deviceName = deviceName,
                    onNetworkClick = {
                        SubSettingLauncher(mContext)
                            .setDestination("com.android.settings.network.NetworkDashboardFragment")
                            .setSourceMetricsCategory(SettingsEnums.SETTINGS_NETWORK_CATEGORY)
                            .launch()
                    },
                    onDevicesClick = {
                        SubSettingLauncher(mContext)
                            .setDestination("com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                            .setSourceMetricsCategory(SettingsEnums.SETTINGS_NETWORK_CATEGORY)
                            .launch()
                    },
                )
            }
        }
    }

    private fun updateWifiState() {
        wifiSsid = getConnectedWifiSsid()
    }

    private fun updateDeviceState() {
        deviceName = getConnectedDeviceName()
    }

    private fun getConnectedWifiSsid(): String? {
        val wifi = wifiManager ?: return null
        if (!wifi.isWifiEnabled) return null
        val info = wifi.connectionInfo ?: return null
        val ssid = info.ssid ?: return null
        if (ssid == "<unknown ssid>" || ssid.isEmpty()) return null
        return ssid.replace("\"", "")
    }

    private fun getConnectedDeviceName(): String? {
        val proxy = a2dpProfile ?: return null
        val devices = proxy.connectedDevices ?: return null
        if (devices.isEmpty()) return null
        return devices.firstOrNull()?.name ?: devices.firstOrNull()?.address
    }

    override fun isAvailable(): Boolean = true

    override fun getPreferenceKey(): String = KEY

    companion object {
        private const val KEY = "homepage_connectivity_cards"
    }
}

@Composable
private fun ConnectivityCards(
    wifiSsid: String?,
    deviceName: String?,
    onNetworkClick: () -> Unit,
    onDevicesClick: () -> Unit,
) {
    val cornerRadius = dimensionResource(R.dimen.settingslib_preference_corner_radius)
    val iconSize = dimensionResource(R.dimen.dashboard_tile_image_size)
    val shape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConnectivityCard(
            modifier = Modifier.weight(1f),
            icon = rememberDrawablePainter(
                LocalContext.current.getDrawable(R.drawable.ic_dashboard_network)
            ),
            title = stringResource(R.string.network_dashboard_title),
            summary = wifiSsid ?: stringResource(R.string.not_connected),
            isActive = wifiSsid != null,
            shape = shape,
            iconSize = iconSize,
            onClick = onNetworkClick,
        )

        ConnectivityCard(
            modifier = Modifier.weight(1f),
            icon = rememberDrawablePainter(
                LocalContext.current.getDrawable(R.drawable.ic_dashboard_devices)
            ),
            title = stringResource(R.string.connected_devices_dashboard_title),
            summary = deviceName ?: stringResource(R.string.not_connected),
            isActive = deviceName != null,
            shape = shape,
            iconSize = iconSize,
            onClick = onDevicesClick,
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
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
