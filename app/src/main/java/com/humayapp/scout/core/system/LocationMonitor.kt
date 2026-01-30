package com.humayapp.scout.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
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

interface LocationMonitor {
    val isEnabled: Flow<Boolean>
}

class LocationProviderMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(ScoutDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : LocationMonitor {

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override val isEnabled: Flow<Boolean> = callbackFlow {

        trySend(isLocationEnabled())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(isLocationEnabled())
                }
            }
        }

        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
        .flowOn(ioDispatcher)
        .conflate()
}
