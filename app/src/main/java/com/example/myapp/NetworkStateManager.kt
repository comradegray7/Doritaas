package com.example.myapp

// NetworkManager.kt
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SignalCellularNodata
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myapp.ui.theme.LocalWindowSizeConstant
import com.example.myapp.view.components.CustomIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkStatus
 *
 * Enum class defining the various states of network connectivity.
 * Used to track and respond to changes in network availability.
 */
enum class NetworkStatus {
    Available,
    Unavailable,
    Losing,
    Lost
}

/**
 * NetworkState
 *
 * Data class representing the current state of network connectivity.
 * Contains comprehensive information about connection status, internet availability,
 * and the type of network connection being used.
 *
 * @property status The current network status
 * @property isConnected Whether the device is connected to a network
 * @property hasInternet Whether the network connection has validated internet access
 * @property connectionType The type of network connection (WiFi, Cellular, etc.)
 */
data class NetworkState(
    val status: NetworkStatus = NetworkStatus.Unavailable,
    val isConnected: Boolean = false,
    val hasInternet: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE
)

/**
 * ConnectionType
 *
 * Enum class defining the different types of network connections available.
 * Used to identify the transport mechanism for network connectivity.
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    NONE
}

@Singleton
/**
 * NetworkManager
 *
 * Singleton class responsible for monitoring and managing network connectivity state.
 * Provides real-time updates about network availability, connection type, and internet access.
 * Uses Android's ConnectivityManager to track network changes and expose them as a StateFlow.
 *
 * @property context Application context used to access system services
 */
class NetworkManager @Inject constructor(
    context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _networkStatus = MutableStateFlow(NetworkStatus.Unavailable)

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        startNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NetworkManager", "Network available")
                updateNetworkState(NetworkStatus.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                Log.d("NetworkManager", "Network losing (${maxMsToLive}ms)")
                updateNetworkState(NetworkStatus.Losing)
            }

            override fun onLost(network: Network) {
                Log.d("NetworkManager", "Network lost")
                updateNetworkState(NetworkStatus.Lost)
            }

            override fun onUnavailable() {
                Log.d("NetworkManager", "Network unavailable")
                updateNetworkState(NetworkStatus.Unavailable)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                Log.d(
                    "NetworkManager",
                    "Capabilities changed - Internet: $hasInternet, Validated: $hasValidated"
                )

                if (hasInternet && hasValidated) {
                    updateNetworkState(NetworkStatus.Available)
                }
            }
        }

        networkCallback?.let {
            connectivityManager.registerNetworkCallback(networkRequest, it)
        }

        // Check initial state
        updateNetworkState(getCurrentNetworkStatus())
    }

    private fun updateNetworkState(status: NetworkStatus) {
        _networkStatus.value = status

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        val isConnected = activeNetwork != null && capabilities != null
        val hasInternet =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val connectionType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> ConnectionType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> ConnectionType.ETHERNET
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> ConnectionType.VPN
            else -> ConnectionType.NONE
        }

        _networkState.value = NetworkState(
            status = status,
            isConnected = isConnected,
            hasInternet = hasInternet,
            connectionType = connectionType
        )
    }

    private fun getCurrentNetworkStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
    }

    /**
     * stopNetworkMonitoring
     *
     */
    fun stopNetworkMonitoring() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkCallback = null
    }

}

@Composable
fun rememberNetworkState(
    networkManager: NetworkManager
): NetworkState {
    val networkState by networkManager.networkState.collectAsState()
    return networkState
}

/**
 * Network Status Banner - shows at top of screen
 */
@Composable
fun NetworkStatusBanner(
    networkState: NetworkState
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    AnimatedVisibility(
        visible = !networkState.hasInternet,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = windowSizeClass.networkCardPadding,
            colors = CardDefaults.cardColors(
                containerColor = when (networkState.status) {
                    NetworkStatus.Lost, NetworkStatus.Unavailable ->
                        MaterialTheme.colorScheme.errorContainer

                    NetworkStatus.Losing ->
                        MaterialTheme.colorScheme.tertiaryContainer

                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(windowSizeClass.baseNormalVerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(windowSizeClass.normalVerticalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIcon(
                    icon = when (networkState.status) {
                        NetworkStatus.Lost, NetworkStatus.Unavailable -> Icons.Filled.CloudOff
                        NetworkStatus.Losing -> Icons.Filled.SignalCellularNodata
                        else -> Icons.Filled.Wifi
                    },
                    contentDescription = "Network Status",
                    tint = when (networkState.status) {
                        NetworkStatus.Lost, NetworkStatus.Unavailable ->
                            MaterialTheme.colorScheme.onErrorContainer

                        NetworkStatus.Losing ->
                            MaterialTheme.colorScheme.onTertiaryContainer

                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Text(
                    text = when (networkState.status) {
                        NetworkStatus.Lost, NetworkStatus.Unavailable ->
                            "No internet connection"

                        NetworkStatus.Losing ->
                            "Poor connection - some features may not work"

                        else -> "Connected"
                    },
                    style = windowSizeClass.bodyTextStyle,
                    color = when (networkState.status) {
                        NetworkStatus.Lost, NetworkStatus.Unavailable ->
                            MaterialTheme.colorScheme.onErrorContainer

                        NetworkStatus.Losing ->
                            MaterialTheme.colorScheme.onTertiaryContainer

                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Inline Network Indicator - small chip/badge
 */
@Composable
fun NetworkIndicator(
    networkState: NetworkState,
    modifier: Modifier = Modifier,
    showWhenConnected: Boolean = false
) {
    val windowSizeClass = LocalWindowSizeConstant.current

    if (!networkState.hasInternet || showWhenConnected) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(windowSizeClass.smallVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomIcon(
                icon = when (networkState.status) {
                    NetworkStatus.Available -> Icons.Filled.Wifi
                    NetworkStatus.Losing -> Icons.Filled.SignalCellularNodata
                    else -> Icons.Filled.CloudOff
                },
                contentDescription = "Network",
                tint = when (networkState.status) {
                    NetworkStatus.Available -> MaterialTheme.colorScheme.primary
                    NetworkStatus.Losing -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            if (!networkState.hasInternet) {
                Text(
                    text = stringResource(R.string.offline),
                    style = windowSizeClass.labelTextStyle,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}