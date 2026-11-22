package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.ui.components.GoogleSignInButton
import br.com.sentinel.app.ui.components.SentinelCornerRibbon
import br.com.sentinel.app.ui.components.SentinelCornerRibbonTop
import br.com.sentinel.app.ui.components.SentinelLogo
import br.com.sentinel.app.ui.components.SentinelTextField
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelTheme
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.launch

@Preview(showBackground = true, name = "Tela de Boas-vindas")
@Composable
fun SentinelWelcomeScreenPreview() {
    SentinelTheme {
        SentinelAuthFlow(onLoginSuccess = {})
    }
}

enum class AuthScreen { Welcome, Login, Consent, Register, Verify, GoogleConsent, GoogleSignIn }

@Composable
fun SentinelAuthFlow(onLoginSuccess: () -> Unit) {
    var screen by remember { mutableStateOf(AuthScreen.Welcome) }
    var consentAt by rememberSaveable { mutableStateOf("") }
    var pendingEmail by rememberSaveable { mutableStateOf("") }
    val onGoogleClick: () -> Unit = { screen = AuthScreen.GoogleConsent }

    when (screen) {
        AuthScreen.Welcome -> SentinelWelcomeScreen(
            onSignIn = { screen = AuthScreen.Login },
            onRegister = { screen = AuthScreen.Consent },
            onGoogleClick = onGoogleClick
        )
        AuthScreen.Login -> SentinelLoginScreen(
            onBack = { screen = AuthScreen.Welcome },
            onRegister = { screen = AuthScreen.Consent },
            onSuccess = onLoginSuccess,
            onGoogleClick = onGoogleClick
        )
        AuthScreen.Consent -> SentinelConsentScreen(
            onBack = { screen = AuthScreen.Welcome },
            onAccept = { acceptedAt ->
                consentAt = acceptedAt
                screen = AuthScreen.Register
            }
        )
        AuthScreen.Register -> SentinelRegisterScreen(
            consentAt = consentAt,
            onBack = { screen = AuthScreen.Consent },
            onLogin = { screen = AuthScreen.Login },
            onSuccess = onLoginSuccess,
            onGoogleClick = onGoogleClick,
            onNeedsConfirmation = { email ->
                pendingEmail = email
                screen = AuthScreen.Verify
            }
        )
        AuthScreen.Verify -> SentinelVerifyEmailScreen(
            email = pendingEmail,
            onBack = { screen = AuthScreen.Register },
            onVerified = onLoginSuccess
        )
        AuthScreen.GoogleConsent -> SentinelConsentScreen(
            onBack = { screen = AuthScreen.Welcome },
            onAccept = { acceptedAt ->
                consentAt = acceptedAt
                screen = AuthScreen.GoogleSignIn
            }
        )
        AuthScreen.GoogleSignIn -> SentinelGoogleSignInScreen(
            consentAt = consentAt,
            onBack = { screen = AuthScreen.Welcome },
            onSuccess = onLoginSuccess
        )
    }
}

@Composable
fun SentinelWelcomeScreen(onSignIn: () -> Unit, onRegister: () -> Unit, onGoogleClick: () -> Unit = {}) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
    ) {
        WelcomeRibbons(screenWidth = maxWidth, screenHeight = maxHeight)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SentinelLogo(modifier = Modifier.fillMaxSize())
            }
            WelcomeTexts()
            Spacer(modifier = Modifier.height(20.dp))
            WelcomeActions(onSignIn, onRegister, onGoogleClick)
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeFooter()
        }
    }
}

@Composable
private fun BoxScope.WelcomeRibbons(screenWidth: Dp, screenHeight: Dp) {
    val ribbonSize = screenWidth * 0.28f

    SentinelCornerRibbon(
        modifier = Modifier
            .size(ribbonSize)
            .align(Alignment.BottomStart)
            .offset(x = -(screenWidth * 0.05f), y = -(screenHeight * 0.2f))
    )

    SentinelCornerRibbonTop(
        modifier = Modifier
            .size(ribbonSize)
            .align(Alignment.TopEnd)
            .graphicsLayer(scaleX = -1f)
            .offset(x = -(screenWidth * 0.05f), y = screenHeight * 0.33f)
    )
}

@Composable
private fun WelcomeTexts() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.bem_vindo_ao_sentinel),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelLightBlueCard,
            textAlign = TextAlign.Left
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.para_continuar_acesse_sua_conta_do_ban),
            fontSize = 24.sp,
            color = SentinelLightBlueCard.copy(alpha = 0.8f),
            textAlign = TextAlign.Left,
            modifier = Modifier.padding(horizontal = 1.dp)
        )
    }
}

@Composable
private fun WelcomeActions(onSignIn: () -> Unit, onRegister: () -> Unit, onGoogleClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSignIn,
                colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(
                    stringResource(R.string.entrar),
                    color = SentinelTextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            GoogleSignInButton(onClick = onGoogleClick, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.nao_tem_cadastro))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.registre_se_agora))
                }
            },
            color = SentinelLightBlueCard,
            fontSize = 15.sp,
            modifier = Modifier.clickable { onRegister() }
        )
    }
}

@Composable
private fun WelcomeFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = SentinelDarkBlueButton.copy(alpha = 0.2f)
        )
        Text(
            text = stringResource(R.string.o_sentinel_e_um_metodo_de_seguranca_ex),
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

@Composable
fun SentinelLoginScreen(onBack: () -> Unit, onRegister: () -> Unit, onSuccess: () -> Unit, onGoogleClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.voltar),
                tint = SentinelLightBlueCard
            )
        }

        SentinelLogo(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally))

        Text(
            stringResource(R.string.entrar),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelLightBlueCard
        )

        SentinelTextField(email, { email = it }, stringResource(R.string.e_mail), KeyboardType.Email)
        SentinelTextField(
            password,
            { password = it },
            stringResource(R.string.senha),
            KeyboardType.Password,
            isPassword = true
        )

        errorText?.let {
            Text(it, color = SentinelRiskRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorText = null
                    AuthRepository.signIn(email.trim(), password)
                        .onSuccess { onSuccess() }
                        .onFailure { errorText = AuthRepository.errorMessage(it) }
                    isLoading = false
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
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
                Text(
                    stringResource(R.string.entrar),
                    color = SentinelTextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        GoogleDivider()
        GoogleSignInButton(onClick = onGoogleClick)

        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.nao_tem_cadastro))
                withStyle(
                    SpanStyle(fontWeight = FontWeight.Bold)
                ) { append(stringResource(R.string.registre_se_agora)) }
            },
            color = SentinelLightBlueCard,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onRegister() }
        )
    }
}

@Composable
fun SentinelRegisterScreen(
    consentAt: String,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSuccess: () -> Unit,
    onGoogleClick: () -> Unit,
    onNeedsConfirmation: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.voltar),
                tint = SentinelLightBlueCard
            )
        }

        SentinelLogo(modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))

        Text(
            stringResource(R.string.criar_conta),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelLightBlueCard
        )

        SentinelTextField(name, { name = it }, stringResource(R.string.nome_completo))
        SentinelTextField(email, { email = it }, stringResource(R.string.e_mail), KeyboardType.Email)
        SentinelTextField(
            password,
            { password = it },
            stringResource(R.string.senha),
            KeyboardType.Password,
            isPassword = true
        )
        SentinelTextField(
            confirm,
            { confirm = it },
            stringResource(R.string.confirmar_senha),
            KeyboardType.Password,
            isPassword = true,
            isError = confirm.isNotEmpty() && confirm != password
        )

        errorText?.let {
            Text(it, color = SentinelRiskRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorText = null
                    AuthRepository.signUp(name.trim(), email.trim(), password, consentAt)
                        .onSuccess { loggedIn ->
                            if (loggedIn) onSuccess() else onNeedsConfirmation(email.trim())
                        }
                        .onFailure { errorText = AuthRepository.errorMessage(it) }
                    isLoading = false
                }
            },
            enabled = name.isNotBlank() &&
                email.isNotBlank() &&
                password.length >= 6 &&
                password == confirm &&
                !isLoading,
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
                Text(
                    stringResource(R.string.criar_conta),
                    color = SentinelTextLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        GoogleDivider()
        GoogleSignInButton(onClick = onGoogleClick)

        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.ja_tem_cadastro))
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(R.string.entrar)) }
            },
            color = SentinelLightBlueCard,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onLogin() }
        )
    }
}

@Composable
private fun GoogleDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = SentinelLightBlueCard.copy(alpha = 0.4f))
        Text(stringResource(R.string.google_or), color = SentinelLightBlueCard, fontSize = 13.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = SentinelLightBlueCard.copy(alpha = 0.4f))
    }
}
