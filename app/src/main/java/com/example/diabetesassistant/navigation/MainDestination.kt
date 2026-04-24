package com.example.diabetesassistant.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.diabetesassistant.R

sealed class MainDestination(
    val route: String,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    data object Home : MainDestination("home", R.string.module_home, Icons.Filled.Home)

    data object Camera : MainDestination("camera", R.string.module_camera, Icons.Filled.CameraAlt)

    data object Archive : MainDestination("archive", R.string.module_archive, Icons.Filled.Archive)

    data object Life : MainDestination("life", R.string.module_life, Icons.Filled.Person)

    companion object {
        val items: List<MainDestination> = listOf(Home, Archive, Life, Camera)

        fun fromRoute(route: String?): MainDestination =
            items.find { it.route == route } ?: Home
    }
}
