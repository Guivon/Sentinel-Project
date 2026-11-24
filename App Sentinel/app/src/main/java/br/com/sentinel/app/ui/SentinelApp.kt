package br.com.sentinel.app.ui

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AccessibilityStore
import br.com.sentinel.app.ui.screens.SentinelAccessibilityScreen
import br.com.sentinel.app.ui.screens.SentinelAnalysisResultScreen
import br.com.sentinel.app.ui.screens.SentinelAuthFlow
import br.com.sentinel.app.ui.screens.SentinelHelpScreen
import br.com.sentinel.app.ui.screens.SentinelHistoryScreen
import br.com.sentinel.app.ui.screens.SentinelHomeScreen
import br.com.sentinel.app.ui.screens.SentinelLearnScreen
import br.com.sentinel.app.ui.screens.SentinelNotificationsScreen
import br.com.sentinel.app.ui.screens.SentinelOnboardingPermissionsScreen
import br.com.sentinel.app.ui.screens.SentinelPrivacyPolicyScreen
import br.com.sentinel.app.ui.screens.SentinelPrivacyScreen
import br.com.sentinel.app.ui.screens.SentinelProfileScreen
import br.com.sentinel.app.ui.screens.SentinelSecurityScreen
import br.com.sentinel.app.ui.screens.SentinelSecurityTipsScreen
import br.com.sentinel.app.ui.screens.SentinelSplashScreen
import br.com.sentinel.app.ui.theme.SentinelTheme
import kotlinx.coroutines.flow.map
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
    PRIVACY,
    NOTIFICATIONS,
    HELP,
    PRIVACY_POLICY,
    ACCESSIBILITY,
    SECURITY_TIPS
}

private fun screenTitle(route: String?): Int? = when (route) {
    SentinelScreen.HOME.name -> R.string.tela_inicial
    SentinelScreen.PROFILE.name -> R.string.perfil
    SentinelScreen.HISTORY.name -> R.string.historico
    SentinelScreen.LEARN.name -> R.string.aprender_e_proteger
    SentinelScreen.SECURITY.name -> R.string.sua_seguranca
    SentinelScreen.SECURITY_TIPS.name -> R.string.dicas_de_seguranca
    SentinelScreen.PRIVACY.name -> R.string.privacidade
    SentinelScreen.NOTIFICATIONS.name -> R.string.notificacoes
    SentinelScreen.HELP.name -> R.string.central_de_ajuda
    SentinelScreen.PRIVACY_POLICY.name -> R.string.privacidade_e_lgpd
    SentinelScreen.ACCESSIBILITY.name -> R.string.acessibilidade
    else -> null
}

private const val NAV_TRANSITION_MS = 350
private const val NAV_SLIDE_DIVISOR = 10

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
    val context = LocalContext.current
    remember(context) { AccessibilityStore.init(context) }
    val accessibility by AccessibilityStore.state.collectAsState()
    LaunchedEffect(accessibility.readAloud) {
        if (accessibility.readAloud) SentinelSpeaker.init(context) else SentinelSpeaker.shutdown()
    }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * accessibility.fontScale)
    ) {
        SentinelAppContent()
    }
}

@Composable
private fun SentinelAppContent() {
    val analysis: AnalysisViewModel = viewModel()
    val session: SessionViewModel = viewModel()
    val analysisState by analysis.state.collectAsState()
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val readAloud by AccessibilityStore.state.map { it.readAloud }.collectAsState(initial = false)
    val reduceMotion by AccessibilityStore.state.map { it.reduceAnimations }.collectAsState(initial = false)
    val transitionMs = if (reduceMotion) 0 else NAV_TRANSITION_MS
    LaunchedEffect(currentRoute, readAloud) {
        val title = screenTitle(currentRoute)
        if (readAloud && title != null) SentinelSpeaker.speak(context.getString(title))
    }

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
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            fadeIn(tween(transitionMs)) + slideInHorizontally(tween(transitionMs)) { it / NAV_SLIDE_DIVISOR }
        },
        exitTransition = { fadeOut(tween(transitionMs)) },
        popEnterTransition = { fadeIn(tween(transitionMs)) },
        popExitTransition = {
            fadeOut(tween(transitionMs)) +
                slideOutHorizontally(tween(transitionMs)) { it / NAV_SLIDE_DIVISOR }
        }
    ) {
        SentinelScreen.entries.forEach { screen ->
            composable(screen.name) {
                SentinelScreenContent(
                    screen = screen,
                    analysis = analysis,
                    session = session,
                    onNavigate = { navController.go(it) },
                    onPop = { navController.popBackStack() }
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
    onNavigate: (SentinelScreen) -> Unit,
    onPop: () -> Unit
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
        SentinelScreen.PRIVACY -> SentinelPrivacyScreen(
            onBack = { onNavigate(SentinelScreen.PROFILE) },
            onOpenPolicy = { onNavigate(SentinelScreen.PRIVACY_POLICY) }
        )
        SentinelScreen.HELP -> SentinelHelpScreen(
            onBack = { onNavigate(SentinelScreen.PROFILE) }
        )
        SentinelScreen.NOTIFICATIONS -> SentinelNotificationsScreen(
            onBack = onPop
        )
        SentinelScreen.PRIVACY_POLICY -> SentinelPrivacyPolicyScreen(
            onBack = { onNavigate(SentinelScreen.PRIVACY) }
        )
        SentinelScreen.ACCESSIBILITY -> SentinelAccessibilityScreen(
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
