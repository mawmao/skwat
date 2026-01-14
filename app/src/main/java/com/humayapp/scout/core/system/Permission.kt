package com.humayapp.scout.core.system

import androidx.annotation.DrawableRes
import com.humayapp.scout.core.ui.theme.ScoutIcons

data class Permission(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
    val permissions: List<String>
)

object ScoutPermissions {
    val Location = Permission(
        icon = ScoutIcons.LocationOn,
        title = "Allow Location Access",
        description = "The app needs it to get GPS coordinates",
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val Camera = Permission(
        icon = ScoutIcons.MobileCamera,
        title = "Allow Camera Access",
        description = "The app needs it to take photos or scan QR codes",
        permissions = listOf(
            android.Manifest.permission.CAMERA
        )
    )
}