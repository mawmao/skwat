package com.humayapp.scout.core.system

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.humayapp.scout.core.common.dispatcher.Dispatcher
import com.humayapp.scout.core.common.dispatcher.ScoutDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {

            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks += network
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                networks -= network
                channel.trySend(networks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .flowOn(ioDispatcher)
        .conflate()

    @Suppress("DEPRECATION")
    private fun ConnectivityManager.isCurrentlyConnected() = activeNetwork
        ?.let(::getNetworkCapabilities)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}
