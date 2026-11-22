package br.com.sentinel.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.sentinel.app.ui.screens.SentinelAnalysisResultScreen
import br.com.sentinel.app.ui.screens.SentinelAuthFlow
import br.com.sentinel.app.ui.screens.SentinelHistoryScreen
import br.com.sentinel.app.ui.screens.SentinelHomeScreen
import br.com.sentinel.app.ui.screens.SentinelLearnScreen
import br.com.sentinel.app.ui.screens.SentinelOnboardingPermissionsScreen
import br.com.sentinel.app.ui.screens.SentinelPrivacyPolicyScreen
import br.com.sentinel.app.ui.screens.SentinelProfileScreen
import br.com.sentinel.app.ui.screens.SentinelSecurityScreen
import br.com.sentinel.app.ui.screens.SentinelSecurityTipsScreen
import br.com.sentinel.app.ui.screens.SentinelSplashScreen
import br.com.sentinel.app.ui.theme.SentinelTheme
import kotlinx.coroutines.launch

@Preview(showBackground = true, name = "App Principal")
@Composable
fun SentinelAppPreview() {
    SentinelTheme {
        SentinelApp()
    }
}

enum class SentinelScreen {
    SPLASH,
    ONBOARDING_PERMISSIONS,
    WELCOME,
    HOME,
    ANALYSIS_RESULT,
    PROFILE,
    HISTORY,
    LEARN,
    SECURITY,
    PRIVACY_POLICY,
    SECURITY_TIPS
}

private val ROOT_SCREENS = setOf(
    SentinelScreen.WELCOME,
    SentinelScreen.ONBOARDING_PERMISSIONS,
    SentinelScreen.HOME
)

private fun NavController.go(target: SentinelScreen) {
    if (popBackStack(target.name, false)) return
    navigate(target.name) {
        launchSingleTop = true
        if (target in ROOT_SCREENS) {
            popUpTo(graph.id) { inclusive = true }
        }
    }
}

@Composable
fun SentinelApp() {
    val analysis: AnalysisViewModel = viewModel()
    val session: SessionViewModel = viewModel()
    val analysisState by analysis.state.collectAsState()
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(currentRoute, analysisState.result) {
        if (currentRoute == SentinelScreen.ANALYSIS_RESULT.name && analysisState.result == null) {
            navController.go(SentinelScreen.HOME)
        }
    }

    LaunchedEffect(analysisState.abortMessage) {
        analysisState.abortMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            navController.go(SentinelScreen.HOME)
            analysis.consumeAbortMessage()
        }
    }

    NavHost(
        navController = navController,
        startDestination = SentinelScreen.SPLASH.name,
        modifier = Modifier.fillMaxSize()
    ) {
        SentinelScreen.entries.forEach { screen ->
            composable(screen.name) {
                SentinelScreenContent(
                    screen = screen,
                    analysis = analysis,
                    session = session,
                    onNavigate = { navController.go(it) }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (navController.currentDestination?.route != SentinelScreen.SPLASH.name) return@LaunchedEffect
        navController.go(session.startDestination(context))
    }
}

@Composable
private fun SentinelScreenContent(
    screen: SentinelScreen,
    analysis: AnalysisViewModel,
    session: SessionViewModel,
    onNavigate: (SentinelScreen) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val analysisState by analysis.state.collectAsState()
    val alertsViewModel: AlertsViewModel = viewModel()
    val alerts by alertsViewModel.alerts.collectAsState()

    when (screen) {
        SentinelScreen.SPLASH -> SentinelSplashScreen(
            onContinue = { onNavigate(SentinelScreen.WELCOME) }
        )
        SentinelScreen.WELCOME -> SentinelAuthFlow(
            onLoginSuccess = {
                scope.launch { onNavigate(session.destinationAfterLogin(context)) }
            }
        )
        SentinelScreen.ONBOARDING_PERMISSIONS -> SentinelOnboardingPermissionsScreen(
            onContinue = { onNavigate(SentinelScreen.HOME) }
        )
        SentinelScreen.HOME -> SentinelHomeScreen(
            onAnalyzeClick = { text, file ->
                analysis.analyze(context, text, file)
                onNavigate(SentinelScreen.ANALYSIS_RESULT)
            },
            onHistoryClick = { onNavigate(SentinelScreen.HISTORY) },
            onLearnClick = { onNavigate(SentinelScreen.LEARN) },
            onTipsClick = { onNavigate(SentinelScreen.SECURITY_TIPS) },
            onNavigate = onNavigate
        )
        SentinelScreen.ANALYSIS_RESULT -> SentinelAnalysisResultScreen(
            result = analysisState.result,
            originalContent = analysisState.content,
            analyzingAi = analysisState.analyzingAi,
            aiUnavailable = analysisState.aiUnavailable,
            onBack = { onNavigate(SentinelScreen.HOME) }
        )
        SentinelScreen.PROFILE -> SentinelProfileScreen(
            onLogout = {
                scope.launch {
                    session.signOut()
                    onNavigate(SentinelScreen.WELCOME)
                }
            },
            onBack = { onNavigate(SentinelScreen.HOME) },
            onNavigate = onNavigate
        )
        SentinelScreen.PRIVACY_POLICY -> SentinelPrivacyPolicyScreen(
            onBack = { onNavigate(SentinelScreen.PROFILE) }
        )
        SentinelScreen.HISTORY -> SentinelHistoryScreen(
            alerts = alerts,
            onBack = { onNavigate(SentinelScreen.HOME) }
        )
        SentinelScreen.LEARN -> SentinelLearnScreen(
            onBack = { onNavigate(SentinelScreen.HOME) }
        )
        SentinelScreen.SECURITY_TIPS -> SentinelSecurityTipsScreen(
            onBack = { onNavigate(SentinelScreen.HOME) }
        )
        SentinelScreen.SECURITY -> SentinelSecurityScreen(
            onBack = { onNavigate(SentinelScreen.HOME) },
            onAnalyzeClick = { onNavigate(SentinelScreen.HOME) },
            onHistoryClick = { onNavigate(SentinelScreen.HISTORY) },
            onNavigate = onNavigate
        )
    }
}
