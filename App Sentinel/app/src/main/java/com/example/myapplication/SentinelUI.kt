package com.example.myapplication

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val SentinelYellow = Color(0xFFEFF300)
val SentinelBlueBg = Color(0xFF4862FF)
val SentinelLightBlueCard = Color(0xFF3857F5)
val SentinelDarkBlueButton = Color(0xFF0F26B0)
val SentinelTextDark = Color(0xFF111111)
val SentinelTextLight = Color(0xFFFFFFFF)
val SentinelRiskRed = Color(0xFFE61F1F)
val SentinelTeal = Color(0xFF04E9CA)

@Preview(showBackground = true, name = "App Principal")
@Composable
fun SentinelAppPreview() {
    MyApplicationTheme {
        SentinelApp()
    }
}

val LocalScale = compositionLocalOf { 1f }

@Composable
fun ResponsiveScale(
    referenceWidthDp: Float = 411f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scale = (screenWidthDp / referenceWidthDp).coerceIn(0.8f, 1.3f)

    CompositionLocalProvider(
        LocalScale provides scale,
        LocalDensity provides Density(
            density = density.density * scale,
            fontScale = density.fontScale
        )
    ) {
        content()
    }
}

enum class SentinelScreen {
    SPLASH, ONBOARDING_PERMISSIONS, WELCOME, HOME, ANALYSIS_RESULT, PROFILE, HISTORY, LEARN, SECURITY, PrivacyPolicy
}

@Composable
fun SentinelApp() {
    var currentScreen by remember { mutableStateOf(SentinelScreen.SPLASH) }
    var resultadoAtual by remember { mutableStateOf<ScamDetector.Resultado?>(null) }
    var textoAtual by remember { mutableStateOf("") }
    var analisandoIA by remember { mutableStateOf(false) }
    var iaIndisponivel by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val onboardingFeito = isOnboardingConcluido(context)
        if (!onboardingFeito) {
            currentScreen = SentinelScreen.ONBOARDING_PERMISSIONS
            return@LaunchedEffect
        }

        currentScreen = if (AuthRepository.estaLogado()) SentinelScreen.HOME else SentinelScreen.WELCOME
    }

    ResponsiveScale {
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    SentinelScreen.SPLASH -> SentinelSplashScreen(
                        onContinue = { currentScreen = SentinelScreen.WELCOME }
                    )
                    SentinelScreen.WELCOME -> SentinelAuthFlow(
                        onLoginSuccess = { currentScreen = SentinelScreen.HOME }
                    )
                    SentinelScreen.ONBOARDING_PERMISSIONS -> SentinelOnboardingPermissionsScreen(
                        onContinuar = { currentScreen = SentinelScreen.WELCOME }
                    )
                    SentinelScreen.HOME -> SentinelHomeScreen(
                        onAnalyzeClick = { texto ->
                            textoAtual = texto
                            val resultado = ScamDetector.analisar(texto)
                            SentinelState.registrarAnalise()
                            resultadoAtual = resultado
                            analisandoIA = true
                            iaIndisponivel = false
                            currentScreen = SentinelScreen.ANALYSIS_RESULT
                            escopo.launch {
                                val resposta = SentinelApi.analisar(
                                    "Análise manual", texto, scoreApp = resultado.score
                                )
                                val resultadoFinal = if (resposta != null) {
                                    ScamDetector.Resultado(
                                        nivel = resposta.nivelApp(),
                                        score = resposta.pontuacao,
                                        motivos = resposta.motivos
                                    )
                                } else {
                                    iaIndisponivel = true
                                    resultado
                                }
                                resultadoAtual = resultadoFinal
                                analisandoIA = false
                                val statusHistorico = when {
                                    resultadoFinal.nivel == NivelRisco.SEGURO -> StatusAlerta.DESCARTADO
                                    resposta != null -> StatusAlerta.CONFIRMADO_IA
                                    else -> StatusAlerta.CONFIRMADO_LOCAL
                                }
                                SentinelState.registrar(
                                    "Análise manual", texto, resultadoFinal.motivos,
                                    statusHistorico, resultadoFinal.nivel
                                )
                            }
                        },
                        onHistoryClick = { currentScreen = SentinelScreen.HISTORY },
                        onLearnClick = { currentScreen = SentinelScreen.LEARN },
                        onNavigate = { currentScreen = it }
                    )
                    SentinelScreen.ANALYSIS_RESULT -> SentinelAnalysisResultScreen(
                        resultado = resultadoAtual,
                        textoOriginal = textoAtual,
                        analisandoIA = analisandoIA,
                        iaIndisponivel = iaIndisponivel,
                        onBack = { currentScreen = SentinelScreen.HOME }
                    )

                    SentinelScreen.PROFILE -> SentinelProfileScreen(
                        onLogout = {
                            escopo.launch {
                                AuthRepository.sair()
                                currentScreen = SentinelScreen.WELCOME
                            }
                        },
                        onBack = { currentScreen = SentinelScreen.HOME },
                        onNavigate = { currentScreen = it }
                    )
                    SentinelScreen.PrivacyPolicy -> SentinelPrivacyPolicyScreen(
                        onBack = { currentScreen = SentinelScreen.PROFILE }
                    )
                    SentinelScreen.HISTORY -> SentinelHistoryScreen(
                        alertas = SentinelState.alertas,
                        onBack = { currentScreen = SentinelScreen.HOME }
                    )
                    SentinelScreen.LEARN -> SentinelLearnScreen(
                        onBack = { currentScreen = SentinelScreen.HOME }
                    )
                    SentinelScreen.SECURITY -> SentinelSecurityScreen(
                        onBack = { currentScreen = SentinelScreen.HOME },
                        onAnalyzeClick = { currentScreen = SentinelScreen.HOME },
                        onNavigate = { currentScreen = it }
                    )
                }
            }
        }
    }
}


@Composable
fun SentinelLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.sentinel_logo),
        contentDescription = "Logo do Sentinel",
        modifier = modifier.size(180.dp)
    )
}


@Preview(showBackground = true, name = "Tela Splash")
@Composable
fun SentinelSplashScreenPreview() {
    SentinelSplashScreen(onContinue = {})
}

@Composable
fun SentinelSplashScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
            .clickable { onContinue() },
        contentAlignment = Alignment.Center
    ) {
        SentinelLogo(modifier = Modifier.size(180.dp))
    }
}

@Composable
fun SentinelOnboardingPermissionsScreen(onContinuar: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var notificacoesAtivo by remember { mutableStateOf(PermissoesHelper.isNotificationListenerEnabled(context)) }
    var chamadasAtivo by remember { mutableStateOf(PermissoesHelper.isCallScreeningRoleHeld(context)) }
    var emailConectado by remember { mutableStateOf(GmailHelper.emailConectado(context)) }

    val notificationListenerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { notificacoesAtivo = PermissoesHelper.isNotificationListenerEnabled(context) }

    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { chamadasAtivo = PermissoesHelper.isCallScreeningRoleHeld(context) }

    val contaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
        if (email != null) {
            GmailHelper.salvarEmailConectado(context, email)
            emailConectado = email
            GmailScheduler.agendar(context)
        }
    }

    var telaAtivo by remember { mutableStateOf(PermissoesHelper.isAccessibilityServiceEnabled(context)) }

    val telaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { telaAtivo = PermissoesHelper.isAccessibilityServiceEnabled(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Configure sua proteção", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Ative as permissões para o Sentinel te proteger de verdade.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = if (notificacoesAtivo) SentinelTeal else SentinelRiskRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (notificacoesAtivo) "Notificações ativas" else "Leitura de notificações desativada",
                    color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Text("Necessário para identificar golpes no WhatsApp.", color = Color.DarkGray, fontSize = 12.sp)
            }
            if (!notificacoesAtivo) {
                TextButton(onClick = {
                    notificationListenerLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) {
                    Text("Ativar", color = SentinelLightBlueCard, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = if (chamadasAtivo) SentinelTeal else SentinelRiskRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (chamadasAtivo) "Proteção de ligações ativa" else "Proteção contra ligações desativada",
                    color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Text("Identifica números suspeitos em chamadas.", color = Color.DarkGray, fontSize = 12.sp)
            }
            if (!chamadasAtivo) {
                TextButton(onClick = {
                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                    if (roleManager != null) {
                        callScreeningLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    }
                }) {
                    Text("Ativar", color = SentinelLightBlueCard, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gmail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = if (emailConectado != null) SentinelTeal else SentinelRiskRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = emailConectado ?: "Gmail não conectado",
                    color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Text("Identifica golpes recebidos por e-mail.", color = Color.DarkGray, fontSize = 12.sp)
            }
            if (emailConectado == null) {
                TextButton(onClick = {
                    contaLauncher.launch(
                        android.accounts.AccountManager.newChooseAccountIntent(
                            null, null, arrayOf("com.google"), null, null, null, null
                        )
                    )
                }) {
                    Text("Conectar", color = SentinelLightBlueCard, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = if (telaAtivo) SentinelTeal else SentinelRiskRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (telaAtivo) "Leitura de tela ativa" else "Leitura de tela desativada",
                    color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Text("Analisa a tela no aparelho para achar golpes em qualquer app.", color = Color.DarkGray, fontSize = 12.sp)
            }
            if (!telaAtivo) {
                TextButton(onClick = {
                    telaLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Text("Ativar", color = SentinelLightBlueCard, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    marcarOnboardingConcluido(context)
                    onContinuar()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SentinelYellow),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Continuar", color = SentinelBlueBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(
            onClick = {
                scope.launch {
                    marcarOnboardingConcluido(context)
                    onContinuar()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pular por agora", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true, name = "Tela de Boas-vindas")
@Composable
fun SentinelWelcomeScreenPreview() {
    MyApplicationTheme {
        SentinelAuthFlow(onLoginSuccess = {})
    }
}

enum class AuthScreen { Welcome, Login, Register }

@Composable
fun SentinelAuthFlow(onLoginSuccess: () -> Unit) {
    var screen by remember { mutableStateOf(AuthScreen.Welcome) }

    when (screen) {
        AuthScreen.Welcome -> SentinelWelcomeScreen(
            onEntrar = { screen = AuthScreen.Login },
            onRegistrar = { screen = AuthScreen.Register },
            onGoogle = { /* TODO: login com Google */ },
            onBB = onLoginSuccess
        )
        AuthScreen.Login -> SentinelLoginScreen(
            onBack = { screen = AuthScreen.Welcome },
            onRegistrar = { screen = AuthScreen.Register },
            onSuccess = onLoginSuccess
        )
        AuthScreen.Register -> SentinelRegisterScreen(
            onBack = { screen = AuthScreen.Welcome },
            onLogin = { screen = AuthScreen.Login },
            onSuccess = onLoginSuccess
        )
    }
}

@Composable
fun SentinelWelcomeScreen(
    onEntrar: () -> Unit,
    onRegistrar: () -> Unit,
    onGoogle: () -> Unit,
    onBB: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
    ) {
        val configuration = LocalConfiguration.current
        val screenHeight = (configuration.screenHeightDp / LocalScale.current).dp

        SentinelCornerRibbon(
            modifier = Modifier
                .size(115.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = (-160).dp)
        )

        SentinelCornerRibbonTop(
            modifier = Modifier
                .size(115.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer(scaleX = -1f)
                .offset(x = (-20).dp, y = screenHeight * 0.35f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            SentinelLogo(modifier = Modifier.size(260.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-50).dp)
            ) {
                Text(
                    text = "Bem-vindo ao Sentinel",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentinelLightBlueCard,
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Para continuar, acesse sua conta do Banco do Brasil.",
                    fontSize = 24.sp,
                    color = SentinelLightBlueCard.copy(alpha = 0.8f),
                    textAlign = TextAlign.Left,
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-70).dp)
            ) {
                Button(
                    onClick = onEntrar,
                    colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Entrar",
                        color = SentinelTextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Não tem cadastro? ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Registre-se agora")
                        }
                    },
                    color = SentinelLightBlueCard,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onRegistrar() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onGoogle,
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SentinelLightBlueCard.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Entrar com Google",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Button(
                        onClick = onBB,
                        colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.group53),
                            contentDescription = "Entrar com Banco do Brasil",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                    color = SentinelDarkBlueButton.copy(alpha = 0.2f)
                )
                Text(
                    text = "O Sentinel é um método de segurança exclusivo para clientes\ndo Banco do Brasil.",
                    fontWeight = FontWeight.Bold,
                    color = SentinelLightBlueCard.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = SentinelLightBlueCard,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun sentinelFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = SentinelLightBlueCard,
    unfocusedBorderColor = SentinelLightBlueCard.copy(alpha = 0.5f),
    cursorColor = SentinelLightBlueCard,
    focusedTextColor = SentinelLightBlueCard,
    unfocusedTextColor = SentinelLightBlueCard
)

@Composable
private fun SentinelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(SentinelLightBlueCard, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = sentinelFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SentinelLoginScreen(
    onBack: () -> Unit,
    onRegistrar: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = SentinelLightBlueCard)
        }

        SentinelLogo(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally))

        Text("Entrar", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SentinelLightBlueCard)

        SentinelTextField(email, { email = it }, "E-mail", KeyboardType.Email)
        SentinelTextField(senha, { senha = it }, "Senha", KeyboardType.Password, isPassword = true)

        erro?.let {
            Text(it, color = SentinelRiskRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    erro = null
                    AuthRepository.entrar(email.trim(), senha)
                        .onSuccess { onSuccess() }
                        .onFailure { erro = AuthRepository.mensagemErro(it) }
                    isLoading = false
                }
            },
            enabled = email.isNotBlank() && senha.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SentinelTextLight,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Entrar", color = SentinelTextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = buildAnnotatedString {
                append("Não tem cadastro? ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Registre-se agora") }
            },
            color = SentinelLightBlueCard,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onRegistrar() }
        )
    }
}

@Composable
fun SentinelRegisterScreen(
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSuccess: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = SentinelLightBlueCard)
        }

        SentinelLogo(modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))

        Text("Criar conta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SentinelLightBlueCard)

        SentinelTextField(nome, { nome = it }, "Nome completo")
        SentinelTextField(email, { email = it }, "E-mail", KeyboardType.Email)
        SentinelTextField(senha, { senha = it }, "Senha", KeyboardType.Password, isPassword = true)
        SentinelTextField(
            confirmar, { confirmar = it }, "Confirmar senha",
            KeyboardType.Password, isPassword = true,
            isError = confirmar.isNotEmpty() && confirmar != senha
        )

        erro?.let {
            Text(it, color = SentinelRiskRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    erro = null
                    AuthRepository.cadastrar(nome.trim(), email.trim(), senha)
                        .onSuccess { onSuccess() }
                        .onFailure { erro = AuthRepository.mensagemErro(it) }
                    isLoading = false
                }
            },
            enabled = nome.isNotBlank() && email.isNotBlank() && senha.length >= 6
                    && senha == confirmar && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = SentinelTextLight,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Criar conta", color = SentinelTextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = buildAnnotatedString {
                append("Já tem cadastro? ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Entrar") }
            },
            color = SentinelLightBlueCard,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onLogin() }
        )
    }
}


// =====================================================================
// FITAS DECORATIVAS
// =====================================================================

@Composable
fun SentinelCornerRibbon(
    modifier: Modifier = Modifier,
    color: Color = SentinelLightBlueCard,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val p1x = w * 0.65f; val p1y = h * 0.02f
        val p2x = w * 0.65f; val p2y = h * 0.38f
        val p3x = w * 0.0f;  val p3y = h * 0.90f
        val p4x = w * 0.0f;  val p4y = h * 0.55f

        val topPath = Path().apply {
            moveTo(p1x, p1y)
            lineTo(p2x, p2y)
            lineTo(p3x, p3y)
            lineTo(p4x, p4y)
            close()
        }
        drawPath(path = topPath, color = color)

        val trianglePath = Path().apply {
            moveTo(x = w * 0.40f, y = h * 0.65f)
            lineTo(x = w * 0.65f, y = h * 0.45f)
            lineTo(x = w * 0.65f, y = h * 0.85f)
            close()
        }
        drawPath(path = trianglePath, color = color)
    }
}

@Composable
fun SentinelCornerRibbonTop(
    modifier: Modifier = Modifier,
    color: Color = SentinelLightBlueCard
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val p1x = w * 0.80f; val p1y = h * 0.45f
        val p2x = w * 0.80f; val p2y = h * 0.10f
        val p3x = w * 0.0f;  val p3y = h * 0.55f
        val p4x = w * 0.0f;  val p4y = h * 0.95f

        val topPath = Path().apply {
            moveTo(p1x, p1y)
            lineTo(p2x, p2y)
            lineTo(p3x, p3y)
            lineTo(p4x, p4y)
            close()
        }
        drawPath(path = topPath, color = color)

        val trianglePath = Path().apply {
            moveTo(x = w * 0.50f, y = h * 0.20f)
            lineTo(x = w * 0.10f, y = h * 0.0f)
            lineTo(x = w * 0.10f, y = h * 0.40f)
            close()
        }
        drawPath(path = trianglePath, color = color)
    }
}


@Preview(showBackground = true, name = "Barra de Navegação")
@Composable
fun SentinelBottomNavigationBarPreview() {
    SentinelBottomNavigationBar(
        currentScreen = SentinelScreen.HOME,
        onNavigate = {}
    )
}

@Composable
fun SentinelBottomNavigationBar(
    currentScreen: SentinelScreen,
    onNavigate: (SentinelScreen) -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = SentinelBlueBg,
        selectedTextColor = SentinelBlueBg,
        unselectedIconColor = SentinelBlueBg,
        unselectedTextColor = SentinelBlueBg,
        indicatorColor = Color.Transparent
    )

    val homeSelected = currentScreen == SentinelScreen.HOME || currentScreen == SentinelScreen.ANALYSIS_RESULT
    val securitySelected = currentScreen == SentinelScreen.SECURITY
    val profileSelected = currentScreen == SentinelScreen.PROFILE

    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = homeSelected,
            onClick = { onNavigate(SentinelScreen.HOME) },
            icon = {
                Icon(
                    if (homeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Início"
                )
            },
            label = { Text("Início", fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = securitySelected,
            onClick = { onNavigate(SentinelScreen.SECURITY) },
            icon = {
                Icon(
                    if (securitySelected) Icons.Filled.Shield else Icons.Outlined.Shield,
                    contentDescription = "Segurança"
                )
            },
            label = { Text("Segurança", fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = profileSelected,
            onClick = { onNavigate(SentinelScreen.PROFILE) },
            icon = {
                Icon(
                    if (profileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil", fontSize = 11.sp) },
            colors = itemColors
        )
    }
}



@Preview(showBackground = true, name = "Tela Inicial (Home)")
@Composable
fun SentinelHomeScreenPreview() {
    SentinelHomeScreen(
        onAnalyzeClick = {},
        onHistoryClick = {},
        onLearnClick = {}
    )
}

@Composable
fun SentinelHomeScreen(
    onAnalyzeClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onLearnClick: () -> Unit,
    onTipsClick: () -> Unit = {},
    onNavigate: (SentinelScreen) -> Unit = {}
) {
    var message by remember { mutableStateOf("") }

    var nomeUsuario by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        nomeUsuario = AuthRepository.nomeAtual()?.trim()?.split(" ")?.firstOrNull() ?: ""
    }

    Scaffold(
        bottomBar = {
            SentinelBottomNavigationBar(
                currentScreen = SentinelScreen.HOME,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SentinelBlueBg)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 85.dp, y = 90.dp)
                    .size(220.dp)
                    .rotate(20f)
                    .border(1.dp, SentinelYellow, RoundedCornerShape(36.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 110.dp, y = 110.dp)
                    .size(220.dp)
                    .rotate(20f)
                    .background(SentinelYellow, RoundedCornerShape(36.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-150).dp, y = 90.dp)
                    .size(360.dp)
                    .rotate(-55f)
                    .border(1.dp, SentinelTeal, RoundedCornerShape(56.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-130).dp, y = 90.dp)
                    .size(330.dp)
                    .rotate(-55f)
                    .background(Color(0xFF1DE9B6), RoundedCornerShape(48.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sentinel_logo),
                        contentDescription = "Sentinel",
                        colorFilter = ColorFilter.tint(SentinelYellow),
                        modifier = Modifier.size(44.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificações",
                        tint = SentinelYellow,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (nomeUsuario.isNotBlank()) "Olá, $nomeUsuario!" else "Olá!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = SentinelTextLight
                )
                Text(
                    text = buildAnnotatedString {
                        append("Conte com a IA para\nte ")
                        withStyle(SpanStyle(color = SentinelYellow, fontWeight = FontWeight.Bold)) {
                            append("proteger")
                        }
                        append(" de\ngolpes.")
                    },
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    color = SentinelTextLight,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(SentinelRiskRed, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.vector),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Recebeu uma mensagem suspeita?",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cole o texto do SMS ou do e-mail e deixe a IA analisar para você.",
                                color = SentinelLightBlueCard,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = message,
                            onValueChange = { if (it.length <= 4000) message = it },
                            placeholder = { Text("Cole sua mensagem aqui.", color = Color.LightGray) },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFDDDDDD),
                                focusedBorderColor = SentinelLightBlueCard
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Text(
                            text = "${message.length}/4000",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { if (message.isNotBlank()) onAnalyzeClick(message) },
                        colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SentinelYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisar mensagem", color = SentinelTextLight, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SentinelQuickCard(
                        title = "Histórico",
                        subtitle = "Veja seu histórico",
                        icon = Icons.Default.History,
                        iconBackground = Color(0xFF9FA8F0),
                        onClick = onHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                    SentinelQuickCard(
                        title = "Aprender",
                        subtitle = "Veja suas análises",
                        icon = Icons.Default.MenuBook,
                        iconBackground = Color(0xFF1DE9B6),
                        onClick = onLearnClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFD9DE), RoundedCornerShape(16.dp))
                        .clickable { onTipsClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = SentinelRiskRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dicas de segurança",
                            color = SentinelRiskRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Desconfie de mensagens com urgência, promessas e links suspeitos.",
                            color = SentinelRiskRed,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SentinelRiskRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SentinelQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SentinelDarkBlueButton)
        }
        Spacer(modifier = Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = SentinelTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SentinelLightBlueCard, fontSize = 11.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SentinelLightBlueCard
        )
    }
}


@Preview(showBackground = true, name = "Resultado da Análise")
@Composable
fun SentinelAnalysisResultScreenPreview() {
    MyApplicationTheme {
        SentinelAnalysisResultScreen(
            resultado = ScamDetector.Resultado(
                nivel = NivelRisco.SUSPEITO,
                score = 90,
                motivos = listOf("Clique no link", "Confirme seus dados")
            ),
            textoOriginal = "Clique aqui e confirme seus dados agora",
            onBack = {}
        )
    }
}

@Composable
fun SentinelAnalysisResultScreen(
    resultado: ScamDetector.Resultado?,
    textoOriginal: String,
    onBack: () -> Unit,
    analisandoIA: Boolean = false,
    iaIndisponivel: Boolean = false
) {
    val nivel = resultado?.nivel ?: NivelRisco.SEGURO
    val score = (resultado?.score ?: 0).coerceIn(0, 100)
    val motivos = resultado?.motivos ?: emptyList()

    val labelRisco: String
    val corRisco: Color
    val tituloAlerta: String
    val descAlerta: String

    if (analisandoIA) {
        labelRisco = "ANALISANDO"; corRisco = Color(0xFF8A94FF)
        tituloAlerta = "Analisando..."; descAlerta = "Estamos conferindo a mensagem com a IA."
    } else {
        when (nivel) {
            NivelRisco.SUSPEITO -> {
                labelRisco = "ALTO RISCO"; corRisco = SentinelRiskRed
                tituloAlerta = "Atenção!"; descAlerta = "Essa mensagem pode ser golpe."
            }
            NivelRisco.DUVIDOSO -> {
                labelRisco = "SUSPEITO"; corRisco = Color(0xFFE0A800)
                tituloAlerta = "Fique atento"; descAlerta = "Essa mensagem tem sinais duvidosos."
            }
            NivelRisco.SEGURO -> {
                labelRisco = "BAIXO RISCO"; corRisco = SentinelTeal
                if (motivos.isEmpty()) {
                    tituloAlerta = "Tudo certo"; descAlerta = "Não identificamos sinais claros de golpe."
                } else {
                    tituloAlerta = "Sem sinais claros"; descAlerta = "Há pontos de atenção, mas nada conclusivo."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Resultado da análise",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Nossa IA analisou a mensagem que você enviou.",
            color = Color.White,
            fontSize = 18.sp
        )

        if (analisandoIA) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Conferindo com a IA. A primeira análise pode demorar um pouco.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        } else if (iaIndisponivel) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Não foi possível consultar a IA agora. Resultado baseado só na análise do aparelho.",
                color = SentinelYellow,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(corRisco, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tituloAlerta,
                        color = corRisco,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = descAlerta,
                        color = Color(0xFF444444),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE3E3E3))
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SentinelRiskGauge(score = score, corPrincipal = corRisco)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SCORE DE RISCO",
                        color = Color(0xFF777777),
                        fontSize = 12.sp
                    )
                    Text(
                        text = labelRisco,
                        color = corRisco,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            analisandoIA -> "Aguarde alguns segundos."
                            motivos.isEmpty() -> "Não identificamos padrões suspeitos nesta mensagem."
                            nivel == NivelRisco.SEGURO -> "Encontramos alguns pontos de atenção, mas nada conclusivo."
                            else -> "Nossa IA identificou sinais de fraude nesta mensagem."
                        },
                        color = Color(0xFF333333),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (motivos.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "O que identificamos?",
                    color = SentinelTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    motivos.forEach { motivo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE3E3E3), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(corRisco.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = corRisco,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = motivo,
                                color = SentinelTextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (textoOriginal.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Mensagem analisada", color = SentinelTextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(textoOriginal, color = Color(0xFF444444), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SentinelRiskGauge(score: Int, corPrincipal: Color = SentinelRiskRed, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            drawArc(
                color = corPrincipal.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = corPrincipal,
                startAngle = -90f,
                sweepAngle = 360f * score / 100f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                color = corPrincipal,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/100",
                color = Color(0xFF333333),
                fontSize = 14.sp
            )
        }
    }
}


@Preview(showBackground = true, name = "Perfil do Usuário")
@Composable
fun SentinelProfileScreenPreview() {
    MyApplicationTheme {
        SentinelProfileScreen(onLogout = {})
    }
}

private data class ProfileOption(
    val title: String,
    val icon: ImageVector
)

@Composable
fun SentinelProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    onNavigate: (SentinelScreen) -> Unit = {}
) {
    val context = LocalContext.current
    var acessibilidadeExpandida by remember { mutableStateOf(false) }
    var privacidadeExpandida by remember { mutableStateOf(false) }
    var notificacoesExpandida by remember { mutableStateOf(false) }
    var sobreExpandido by remember { mutableStateOf(false) }
    var compartilhamentoDados by remember { mutableStateOf(true) }
    var alertasUrgentes by remember { mutableStateOf(true) }

    var nomeUsuario by remember { mutableStateOf("") }
    var emailUsuario by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        nomeUsuario = AuthRepository.nomeAtual() ?: ""
        emailUsuario = AuthRepository.emailAtual() ?: ""
    }

    var notificacoesAtivo by remember { mutableStateOf(PermissoesHelper.isNotificationListenerEnabled(context)) }
    var chamadasAtivo by remember { mutableStateOf(PermissoesHelper.isCallScreeningRoleHeld(context)) }
    var emailConectado by remember { mutableStateOf(GmailHelper.emailConectado(context)) }

    val notificationListenerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        notificacoesAtivo = PermissoesHelper.isNotificationListenerEnabled(context)
    }

    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        chamadasAtivo = PermissoesHelper.isCallScreeningRoleHeld(context)
    }

    val contaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
        if (email != null) {
            GmailHelper.salvarEmailConectado(context, email)
            emailConectado = email
            GmailScheduler.agendar(context)
        }
    }

    var telaAtivo by remember { mutableStateOf(PermissoesHelper.isAccessibilityServiceEnabled(context)) }
    val escopoPerfil = rememberCoroutineScope()

    val telaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { telaAtivo = PermissoesHelper.isAccessibilityServiceEnabled(context) }

    LaunchedEffect(Unit) {
        notificacoesAtivo = PermissoesHelper.isNotificationListenerEnabled(context)
        chamadasAtivo = PermissoesHelper.isCallScreeningRoleHeld(context)
        telaAtivo = PermissoesHelper.isAccessibilityServiceEnabled(context)
    }

    val outrasOpcoes = listOf(
        ProfileOption("Privacidade", Icons.Default.Lock),
        ProfileOption("Notificações", Icons.Default.Notifications),
        ProfileOption("Sobre o Sentinel", Icons.Default.Info)
    )

    Scaffold(
        bottomBar = {
            SentinelBottomNavigationBar(
                currentScreen = SentinelScreen.PROFILE,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SentinelBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onBack() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Perfil",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .border(3.dp, SentinelYellow, CircleShape)
                        .padding(5.dp)
                        .background(Color(0xFFBDBDBD), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = nomeUsuario.ifBlank { "Usuário" },
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = emailUsuario,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${SentinelState.totalAnalisadas}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Mensagens scaneadas", color = Color.White, fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color.White.copy(alpha = 0.5f))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${SentinelState.golpesEvitados}",
                        color = SentinelYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Golpes evitados", color = Color.White, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Configurações",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // ACESSIBILIDADE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { acessibilidadeExpandida = !acessibilidadeExpandida }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = null,
                        tint = SentinelDarkBlueButton,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Acessibilidade",
                        color = SentinelDarkBlueButton,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (acessibilidadeExpandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SentinelDarkBlueButton
                    )
                }

                if (acessibilidadeExpandida) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        SentinelPermissaoItem(
                            icone = Icons.Outlined.Notifications,
                            ativo = notificacoesAtivo,
                            titulo = if (notificacoesAtivo) "Leitura de notificações ativa" else "Leitura de notificações desativada",
                            descricao = "Necessário para identificar golpes no WhatsApp.",
                            onAtivar = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                notificationListenerLauncher.launch(intent)
                            },
                            onDesativar = {
                                notificationListenerLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SentinelPermissaoItem(
                            icone = Icons.Default.Call,
                            ativo = chamadasAtivo,
                            titulo = if (chamadasAtivo) "Proteção contra ligações ativa" else "Proteção contra ligações desativada",
                            descricao = "Identifica números suspeitos em chamadas recebidas.",
                            onAtivar = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                                    if (roleManager != null) {
                                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                        callScreeningLauncher.launch(intent)
                                    }
                                }
                            },
                            onDesativar = {
                                callScreeningLauncher.launch(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SentinelPermissaoItem(
                            icone = Icons.Default.Email,
                            ativo = emailConectado != null,
                            titulo = emailConectado ?: "Gmail não conectado",
                            descricao = "Identifica golpes recebidos por e-mail.",
                            onAtivar = {
                                val intent = android.accounts.AccountManager.newChooseAccountIntent(
                                    null, null, arrayOf("com.google"), null, null, null, null
                                )
                                contaLauncher.launch(intent)
                            },
                            textoBotao = "Conectar"
                        )
                        if (emailConectado != null) {
                            TextButton(
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context, "Verificando e-mail...", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    WorkManager.getInstance(context)
                                        .enqueue(OneTimeWorkRequestBuilder<GmailCheckWorker>().build())
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    "Verificar e-mail agora",
                                    color = SentinelLightBlueCard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SentinelPermissaoItem(
                            icone = Icons.Default.Visibility,
                            ativo = telaAtivo,
                            titulo = if (telaAtivo) "Leitura de tela ativa" else "Leitura de tela desativada",
                            descricao = "Analisa o texto da tela no aparelho para achar golpes em qualquer app.",
                            onAtivar = {
                                telaLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            },
                            onDesativar = {
                                if (SentinelAccessibilityService.desligar()) {
                                    escopoPerfil.launch {
                                        delay(600)
                                        telaAtivo = PermissoesHelper.isAccessibilityServiceEnabled(context)
                                    }
                                } else {
                                    telaLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE3E3E3))

                // 1. PRIVACIDADE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { privacidadeExpandida = !privacidadeExpandida }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SentinelDarkBlueButton,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Privacidade",
                        color = SentinelDarkBlueButton,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (privacidadeExpandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SentinelDarkBlueButton
                    )
                }

                if (privacidadeExpandida) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch funcional
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Compartilhamento de dados",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ajuda a aprimorar a detecção de forma anónima.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = compartilhamentoDados,
                                onCheckedChange = { compartilhamentoDados = it }
                            )
                        }

                        // Botão que abre a nova tela de LGPD
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .clickable { onNavigate(SentinelScreen.PrivacyPolicy) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Política de Privacidade e LGPD",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Veja como os seus dados são tratados e protegidos.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = SentinelDarkBlueButton,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Botão que abre as permissões de privacidade nativas do Android
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Settings.ACTION_PRIVACY_SETTINGS)
                                    context.startActivity(intent)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permissões de privacidade do sistema",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Gerir câmara, microfone e acessos concedidos.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = SentinelDarkBlueButton,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE3E3E3))

                // 2. NOTIFICAÇÕES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notificacoesExpandida = !notificacoesExpandida }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SentinelDarkBlueButton, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Notificações", color = SentinelDarkBlueButton, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(if (notificacoesExpandida) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SentinelDarkBlueButton)
                }
                if (notificacoesExpandida) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Switch para ligar/desligar alertas urgentes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Alertas urgentes",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Avisar assim que um golpe for detectado.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = alertasUrgentes,
                                onCheckedChange = { alertasUrgentes = it }
                            )
                        }

                        // Botão funcional que abre as definições de notificações do Android
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .clickable {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gerir canais de notificações",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Configurar sons, pop-ups e permissões no sistema.",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = SentinelDarkBlueButton,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE3E3E3))

                // 3. SOBRE O SENTINEL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sobreExpandido = !sobreExpandido }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SentinelDarkBlueButton, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Sobre o Sentinel", color = SentinelDarkBlueButton, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(if (sobreExpandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = SentinelDarkBlueButton)
                }
                if (sobreExpandido) {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Versão", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Sentinel v1.0.0", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = SentinelRiskRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sair da conta",
                    color = SentinelRiskRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SentinelPermissaoItem(
    icone: ImageVector,
    ativo: Boolean,
    titulo: String,
    descricao: String,
    onAtivar: () -> Unit,
    textoBotao: String = "Ativar",
    onDesativar: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = if (ativo) SentinelTeal else SentinelRiskRed,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(descricao, color = Color.Gray, fontSize = 10.sp)
        }
        if (!ativo) {
            TextButton(onClick = onAtivar, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(textoBotao, color = SentinelLightBlueCard, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (onDesativar != null) {
            TextButton(onClick = onDesativar, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("Desativar", color = SentinelRiskRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun SentinelHistoryScreen(alertas: List<Alerta>, onBack: () -> Unit = {}) {
    var query by remember { mutableStateOf("") }

    val filtrados = alertas.filter {
        it.trecho.contains(query, ignoreCase = true) || it.app.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Histórico",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar análises antigas...", color = Color(0xFF666666), fontSize = 14.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF444444))
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (filtrados.isEmpty()) {
            Text(
                text = "Nenhum alerta encontrado ainda.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filtrados.forEach { alerta ->
                SentinelHistoryCardReal(alerta)
            }
        }
    }
}

@Composable
private fun SentinelHistoryCardReal(alerta: Alerta) {
    val (label, corFundo, corTexto, icone) = when (alerta.status) {
        StatusAlerta.CONFIRMADO_LOCAL, StatusAlerta.CONFIRMADO_IA ->
            if (alerta.nivel == NivelRisco.DUVIDOSO)
                listOf("SUSPEITO", Color(0xFFE6C800), Color(0xFF4A3F00), Icons.Outlined.Warning)
            else
                listOf("ALTO RISCO", SentinelRiskRed, Color.White, Icons.Outlined.Warning)
        StatusAlerta.ANALISANDO ->
            listOf("ANALISANDO", Color(0xFFE6C800), Color(0xFF4A3F00), Icons.Outlined.Info)
        StatusAlerta.OFICIAL ->
            listOf("OFICIAL", SentinelTeal, Color(0xFF0B3B36), Icons.Outlined.Info)
        StatusAlerta.DESCARTADO ->
            listOf("SEGURO", SentinelTeal, Color(0xFF0B3B36), Icons.Outlined.Info)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${alerta.app} • ${alerta.horario}",
                color = Color(0xFF555555),
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .background(corFundo as Color, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icone as ImageVector,
                    contentDescription = null,
                    tint = corTexto as Color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label as String,
                    color = corTexto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = alerta.trecho,
            color = SentinelTextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (alerta.motivos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = alerta.motivos.joinToString(", "),
                color = Color(0xFF777777),
                fontSize = 11.sp
            )
        }
    }
}


private data class LearnTopic(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val comoFunciona: String,
    val sinaisDeAlerta: List<String>,
    val comoSeProteger: List<String>
)

private val topicosAprender = listOf(
    LearnTopic(
        title = "Golpe do Falso Boleto",
        description = "Como conferir os dados do beneficiário antes de pagar qualquer documento.",
        icon = Icons.Outlined.Cancel,
        iconTint = Color.White,
        iconBackground = SentinelRiskRed,
        comoFunciona = "O golpista envia um boleto falso, muitas vezes parecido com uma cobrança real que você esperava (conta de luz, internet, compra online). O código de barras e os dados bancários apontam para a conta do golpista, não da empresa verdadeira.",
        sinaisDeAlerta = listOf(
            "Boleto chegou por e-mail ou WhatsApp sem você ter pedido",
            "Valor ou vencimento diferentes do que você esperava",
            "Nome do beneficiário não bate com a empresa conhecida",
            "Pressão para pagar rapidamente, com ameaça de juros ou corte de serviço"
        ),
        comoSeProteger = listOf(
            "Confira o nome do beneficiário antes de pagar",
            "Compare o código de barras com o do aplicativo oficial da empresa",
            "Em caso de dúvida, ligue para a empresa usando um número buscado por você mesmo, não o que veio na mensagem",
            "Evite pagar boletos recebidos fora dos canais oficiais"
        )
    ),
    LearnTopic(
        title = "Clonagem de WhatsApp",
        description = "Ative a confirmação em duas etapas e nunca passe o código SMS a terceiros.",
        icon = Icons.Default.Fingerprint,
        iconTint = Color(0xFF0B3B36),
        iconBackground = SentinelTeal,
        comoFunciona = "O golpista descobre seu número e tenta instalar seu WhatsApp em outro aparelho. Para isso, ele precisa do código de verificação de 6 dígitos que o WhatsApp manda por SMS. Se conseguir o código, assume sua conta e passa a falar com seus contatos se passando por você, geralmente pedindo dinheiro emprestado ou Pix urgente.",
        sinaisDeAlerta = listOf(
            "Você recebe um SMS com código de verificação do WhatsApp sem ter pedido",
            "Alguém pede esse código por telefone ou mensagem, alegando ser suporte técnico",
            "Seu WhatsApp desconecta sozinho sem motivo aparente"
        ),
        comoSeProteger = listOf(
            "Nunca compartilhe o código de verificação com ninguém, nem com \"suporte do WhatsApp\"",
            "Ative a confirmação em duas etapas em Configurações > Conta > Confirmação em duas etapas",
            "Se seu WhatsApp for clonado, avise seus contatos imediatamente por outro meio"
        )
    ),
    LearnTopic(
        title = "Phishing por E-mail",
        description = "Aprenda a identificar remetentes falsos e links de bancos maliciosos.",
        icon = Icons.Outlined.Cancel,
        iconTint = SentinelLightBlueCard,
        iconBackground = Color(0xFFE8EEFF),
        comoFunciona = "Você recebe um e-mail parecido com o de um banco, loja ou órgão público, pedindo para clicar num link e confirmar dados. O link leva a uma página falsa, idêntica à verdadeira, que captura a senha e outros dados digitados.",
        sinaisDeAlerta = listOf(
            "E-mail do remetente tem domínio estranho (ex: @banco-seguro123.com em vez do domínio oficial)",
            "Erros de português ou formatação estranha",
            "Pede para clicar em link e inserir senha ou dados do cartão",
            "Cria urgência: \"sua conta será bloqueada em 24h\""
        ),
        comoSeProteger = listOf(
            "Nunca clique em links de e-mail para acessar sua conta bancária; digite o endereço direto no navegador",
            "Confira o domínio completo do remetente, não só o nome exibido",
            "Bancos nunca pedem senha completa por e-mail",
            "Em caso de dúvida, entre em contato pelo site ou app oficial"
        )
    ),
    LearnTopic(
        title = "Falso Emprego / Tarefas",
        description = "Cuidado com promessas de dinheiro fácil trabalhando poucos minutos na internet.",
        icon = Icons.Outlined.Warning,
        iconTint = Color(0xFF5A4A00),
        iconBackground = Color(0xFFE6C800),
        comoFunciona = "Você recebe uma oferta de emprego fácil, geralmente por WhatsApp ou redes sociais, prometendo pagamento alto por tarefas simples (curtir vídeos, avaliar produtos). No início pagam pequenos valores para gerar confiança, depois pedem um \"depósito\" para liberar tarefas maiores ou retirar o saldo acumulado — que nunca é pago.",
        sinaisDeAlerta = listOf(
            "Promessa de ganho alto por tarefa simples e rápida",
            "Pedem depósito ou pagamento antecipado para \"liberar\" o trabalho",
            "Contato começou por mensagem não solicitada",
            "Pressão para recrutar mais pessoas para o esquema"
        ),
        comoSeProteger = listOf(
            "Desconfie de qualquer emprego que peça pagamento adiantado",
            "Empresas sérias não cobram para você trabalhar",
            "Pesquise o nome da empresa antes de aceitar qualquer proposta",
            "Nunca deposite dinheiro para \"desbloquear\" ganhos"
        )
    )
)

@Composable
fun SentinelLearnScreen(onBack: () -> Unit) {
    var topicoSelecionado by remember { mutableStateOf<LearnTopic?>(null) }

    if (topicoSelecionado != null) {
        SentinelLearnDetailScreen(
            topico = topicoSelecionado!!,
            onBack = { topicoSelecionado = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aprender e Proteger",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = SentinelYellow,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Seu nível de proteção: Alto",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Você completou 2 de 4 guias essenciais.",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            topicosAprender.forEach { topic ->
                SentinelLearnCard(topic, onClick = { topicoSelecionado = topic })
            }
        }
    }
}

@Composable
private fun SentinelLearnCard(topic: LearnTopic, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(topic.iconBackground, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = topic.icon,
                contentDescription = null,
                tint = topic.iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.title,
                color = SentinelTextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = topic.description,
                color = Color(0xFF555555),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ler agora",
                    color = SentinelLightBlueCard,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SentinelLightBlueCard,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SentinelLearnDetailScreen(topico: LearnTopic, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .clickable { onBack() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(topico.iconBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = topico.icon,
                    contentDescription = null,
                    tint = topico.iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = topico.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Como funciona",
                color = SentinelTextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = topico.comoFunciona,
                color = Color(0xFF444444),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = SentinelRiskRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sinais de alerta",
                    color = SentinelRiskRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            topico.sinaisDeAlerta.forEach { sinal ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", color = SentinelRiskRed, fontWeight = FontWeight.Bold)
                    Text(sinal, color = Color(0xFF444444), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SentinelTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Como se proteger",
                    color = Color(0xFF0B6458),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            topico.comoSeProteger.forEach { dica ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✓ ", color = Color(0xFF0B6458), fontWeight = FontWeight.Bold)
                    Text(dica, color = Color(0xFF444444), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "Status de Segurança")
@Composable
fun SentinelSecurityScreenPreview() {
    MyApplicationTheme {
        SentinelSecurityScreen(
            onBack = {},
            onAnalyzeClick = {}
        )
    }
}

@Composable
fun SentinelSecurityScreen(
    onBack: () -> Unit = {},
    onAnalyzeClick: () -> Unit = {},
    onNavigate: (SentinelScreen) -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            SentinelBottomNavigationBar(
                currentScreen = SentinelScreen.SECURITY,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SentinelBlueBg)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sua segurança",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Status do seu dispositivo",
                color = SentinelYellow,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF00E5C3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF0B6458),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Proteção Ativada",
                        color = Color.DarkGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seu app está atualizado e pronto para farejar links perigosos.",
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SentinelReportCard(modifier = Modifier.weight(1f))
                SentinelReportCard(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Alertas Recentes na sua Região",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            SentinelAlertItem(
                title = "Falso Pix por Telefone",
                subtitle = "Há 1 hora • São Paulo - SP"
            )
            Spacer(modifier = Modifier.height(10.dp))
            SentinelAlertItem(
                title = "Golpe da Falsa Central do BB",
                subtitle = "Ontem • Geral"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SentinelYellow, RoundedCornerShape(16.dp))
                    .clickable { onAnalyzeClick() }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SentinelBlueBg
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Fazer análise de mensagem",
                    color = SentinelBlueBg,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SentinelBlueBg
                )
            }
        }
    }
}

@Composable
fun SentinelReportCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = SentinelRiskRed,
            modifier = Modifier.size(30.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Registrar Golpe",
            color = Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ajude outros usuários relatando fraudes.",
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SentinelAlertItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = SentinelYellow,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentinelPrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidade e LGPD", fontWeight = FontWeight.Bold, color = SentinelDarkBlueButton) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SentinelDarkBlueButton
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tratamento de Dados Pessoais",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SentinelDarkBlueButton
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Em conformidade com a LGPD (Lei Geral de Proteção de Dados), o Sentinel opera sob os princípios de transparência, finalidade e minimização de dados.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Leitura de Notificações", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "O conteúdo de notificações de mensagens e apps bancários é processado em tempo real no próprio aparelho para detecção de fraudes. Nenhuma mensagem pessoal é enviada ou armazenada externamente.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Identificação de Chamadas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Apenas o número telefônico recebido é comparado com bancos de dados de fraudes e spam. O aplicativo não grava nem ouve chamadas telefônicas.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Seus Direitos (LGPD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Você tem total autonomia para ativar ou revogar permissões a qualquer momento pelas configurações do sistema ou do aplicativo.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}