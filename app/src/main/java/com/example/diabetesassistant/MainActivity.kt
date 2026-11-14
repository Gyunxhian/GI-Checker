package com.example.diabetesassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diabetesassistant.ui.screens.AppStateViewModel
import com.example.diabetesassistant.ui.theme.DiabetesAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appStateViewModel: AppStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by appStateViewModel.appThemeMode.collectAsStateWithLifecycle()
            DiabetesAssistantTheme(themeMode = themeMode) {
                DiabetesAssistantApp(appStateViewModel = appStateViewModel)
            }
        }
    }
}