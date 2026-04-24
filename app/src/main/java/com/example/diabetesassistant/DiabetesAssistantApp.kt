package com.example.diabetesassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.diabetesassistant.navigation.AppNavHost
import com.example.diabetesassistant.navigation.MainDestination
import com.example.diabetesassistant.ui.screens.AuthOnboardingScreen
import com.example.diabetesassistant.ui.theme.DiabetesAssistantTheme

@Composable
fun DiabetesAssistantApp() {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("diabetes_assistant_auth", android.content.Context.MODE_PRIVATE)
    }
    var signedIn by remember { mutableStateOf(preferences.getBoolean("signed_in", false)) }

    if (!signedIn) {
        AuthOnboardingScreen(
            onFinished = {
                preferences.edit().putBoolean("signed_in", true).apply()
                signedIn = true
            },
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = MainDestination.fromRoute(currentRoute)
    val showBottomBar = MainDestination.items.any { it.route == currentRoute }

    Scaffold(
        containerColor = Color(0xFFF3F7F4),
        bottomBar = {
            if (showBottomBar) {
                SplitBottomNavigationBar(
                    currentDestination = currentDestination,
                    navController = navController,
                )
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onSignOut = {
                preferences.edit().clear().apply()
                signedIn = false
            },
        )
    }
}

@Composable
private fun SplitBottomNavigationBar(
    currentDestination: MainDestination,
    navController: NavHostController,
) {
    val regularDestinations = listOf(
        MainDestination.Home,
        MainDestination.Archive,
        MainDestination.Life,
    )

    fun navigate(destination: MainDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars)
            .padding(horizontal = 18.dp)
            .padding(top = 10.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(3f)
                .height(68.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color(0xFF0B6F36).copy(alpha = 0.12f),
                    spotColor = Color(0xFF0B6F36).copy(alpha = 0.12f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.82f),
                            Color.White.copy(alpha = 0.50f),
                            Color(0xFFA8D4B9).copy(alpha = 0.28f),
                        ),
                        start = Offset.Zero,
                        end = Offset(520f, 160f),
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.86f), RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            regularDestinations.forEach { destination ->
                BottomNavPillItem(
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = { navigate(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        CameraNavButton(
            selected = currentDestination == MainDestination.Camera,
            onClick = { navigate(MainDestination.Camera) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BottomNavPillItem(
    destination: MainDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) Color(0xFF007A35) else Color(0xFF7D8781)
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) Color.White.copy(alpha = 0.50f) else Color.Transparent,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.titleRes),
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = stringResource(destination.titleRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CameraNavButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonColor = if (selected) Color(0xFF0B6F36) else Color(0xFF43FF82)
    val iconColor = if (selected) Color.White else Color(0xFF073D20)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(66.dp)
                .shadow(
                    elevation = 22.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF0B6F36).copy(alpha = 0.32f),
                    spotColor = Color(0xFF0B6F36).copy(alpha = 0.32f),
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.Transparent,
                        ),
                        start = Offset.Zero,
                        end = Offset(80f, 80f),
                    ),
                    CircleShape,
                )
                .border(3.dp, Color.White.copy(alpha = 0.92f), CircleShape),
            shape = CircleShape,
            color = buttonColor,
            contentColor = iconColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = MainDestination.Camera.icon,
                    contentDescription = stringResource(MainDestination.Camera.titleRes),
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(MainDestination.Camera.titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF007A35),
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiabetesAssistantAppPreview() {
    DiabetesAssistantTheme {
        DiabetesAssistantApp()
    }
}
