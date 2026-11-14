package com.example.diabetesassistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.diabetesassistant.navigation.AppNavHost
import com.example.diabetesassistant.navigation.MainDestination
import com.example.diabetesassistant.ui.screens.AppStateViewModel
import com.example.diabetesassistant.ui.screens.AuthOnboardingMode
import com.example.diabetesassistant.ui.screens.AuthOnboardingScreen
import com.example.diabetesassistant.ui.theme.DiabetesAssistantTheme

@Composable
fun DiabetesAssistantApp(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
) {
    val signedIn by appStateViewModel.signedIn.collectAsStateWithLifecycle()
    val signedInUserId by appStateViewModel.signedInUserId.collectAsStateWithLifecycle()
    val onboardingCompleted by appStateViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val authState by appStateViewModel.authFormState.collectAsStateWithLifecycle()
    val onboardingSubmissionState by appStateViewModel.onboardingSubmissionState.collectAsStateWithLifecycle()
    val guardianDays by appStateViewModel.guardianDays.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        appStateViewModel.ensureSingleUserSignedIn()
    }

    if (!onboardingCompleted) {
        AuthOnboardingScreen(
            mode = AuthOnboardingMode.OnboardingOnly,
            authState = authState,
            onboardingSubmissionState = onboardingSubmissionState,
            onAuthModeChange = appStateViewModel::setRegisterMode,
            onNicknameChange = appStateViewModel::updateNickname,
            onAccountChange = appStateViewModel::updateAccount,
            onPasswordChange = appStateViewModel::updatePassword,
            onAuthSubmit = appStateViewModel::submitAuth,
            onCompleteOnboarding = appStateViewModel::completeOnboarding,
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = MainDestination.fromRoute(currentRoute)
    // 仅在三个顶级主 Tab 页面展示底部导航栏；进入二级子流程（如拍照测算、记录详情）时自动隐藏
    val mainTabRoutes = listOf(MainDestination.Home.route, MainDestination.Archive.route, MainDestination.Life.route)
    val showBottomBar = mainTabRoutes.contains(currentRoute)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            guardianDays = guardianDays,
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
        if (currentDestination == destination) return
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = destination != MainDestination.Home
            }
            launchSingleTop = true
            restoreState = destination != MainDestination.Home
        }
    }

    val isCameraVisible = currentDestination != MainDestination.Home

    // 弹性弹簧动画规范：中等弹性阻尼，生动自然不生硬
    val springSpecFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val springSpecDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val cameraWeight by animateFloatAsState(
        targetValue = if (isCameraVisible) 1.15f else 0.0001f,
        animationSpec = springSpecFloat,
        label = "cameraWeight",
    )
    val cameraScale by animateFloatAsState(
        targetValue = if (isCameraVisible) 1f else 0.001f,
        animationSpec = springSpecFloat,
        label = "cameraScale",
    )
    val cameraAlpha by animateFloatAsState(
        targetValue = if (isCameraVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "cameraAlpha",
    )
    val spacerWidth by animateDpAsState(
        targetValue = if (isCameraVisible) 12.dp else 0.dp,
        animationSpec = springSpecDp,
        label = "navSpacerWidth",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Transparent)
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars)
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 主导航胶囊：在首页占满整个导航空间；离开首页时，被弹性展开的相机键挤压压缩空间
        Row(
            modifier = Modifier
                .weight(3f)
                .height(68.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            regularDestinations.forEach { destination ->
                val isSelected = currentDestination == destination
                val itemWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.45f else 1.0f,
                    animationSpec = springSpecFloat,
                    label = "itemWeight_${destination.route}",
                )

                BottomNavPillItem(
                    destination = destination,
                    selected = isSelected,
                    onClick = { navigate(destination) },
                    modifier = Modifier.weight(itemWeight),
                )
            }
        }

        if (isCameraVisible || cameraWeight > 0.01f) {
            Spacer(modifier = Modifier.width(spacerWidth))

            Box(
                modifier = Modifier
                    .weight(cameraWeight)
                    .graphicsLayer {
                        scaleX = cameraScale
                        scaleY = cameraScale
                        alpha = cameraAlpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                CameraNavButton(
                    selected = currentDestination == MainDestination.Camera,
                    onClick = { navigate(MainDestination.Camera) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavPillItem(
    destination: MainDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBg = MaterialTheme.colorScheme.primaryContainer
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "pillContentColor",
    )
    val pillBgColor by animateColorAsState(
        targetValue = if (selected) selectedBg else Color.Transparent,
        animationSpec = tween(220),
        label = "pillBgColor",
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(22.dp),
        color = pillBgColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = stringResource(destination.titleRes),
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(destination.titleRes),
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                softWrap = false,
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
    val buttonColor = MaterialTheme.colorScheme.primary
    val iconColor = MaterialTheme.colorScheme.onPrimary

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(60.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            )
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
        shape = CircleShape,
        color = buttonColor,
        contentColor = iconColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = MainDestination.Camera.icon,
                contentDescription = "拍照测算",
                tint = iconColor,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiabetesAssistantAppPreview() {
    DiabetesAssistantTheme {
        DiabetesAssistantApp()
    }
}
