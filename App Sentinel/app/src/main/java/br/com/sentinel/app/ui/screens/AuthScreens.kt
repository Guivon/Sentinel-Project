package br.com.sentinel.app.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.ui.components.GoogleSignInButton
import br.com.sentinel.app.ui.components.SentinelCornerRibbon
import br.com.sentinel.app.ui.components.SentinelCornerRibbonTop
import br.com.sentinel.app.ui.components.SentinelLogo
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTextDark
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelTextMuted
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

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            fadeIn(tween(AUTH_TRANSITION_MS)) togetherWith fadeOut(tween(AUTH_TRANSITION_MS))
        },
        label = "auth_flow"
    ) { target ->
        when (target) {
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
}

@Composable
fun SentinelWelcomeScreen(onSignIn: () -> Unit, onRegister: () -> Unit, onGoogleClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
    ) {
        WelcomeRibbons()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            SentinelLogo(modifier = Modifier.size(260.dp))
            WelcomeTexts()
            WelcomeActions(onSignIn, onRegister, onGoogleClick)
            WelcomeFooter()
        }
    }
}

@Composable
private fun BoxScope.WelcomeRibbons() {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

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
}

@Composable
private fun WelcomeTexts() {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-50).dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-70).dp)
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
        modifier = Modifier.padding(bottom = 16.dp)
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

private const val AUTH_TRANSITION_MS = 350
private const val LOGIN_PREFS = "sentinel_login"
private const val REMEMBERED_EMAIL_KEY = "remembered_email"

@Composable
fun SentinelLoginScreen(onBack: () -> Unit, onRegister: () -> Unit, onSuccess: () -> Unit, onGoogleClick: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE) }
    val savedEmail = remember { prefs.getString(REMEMBERED_EMAIL_KEY, null) }
    var email by remember { mutableStateOf(savedEmail.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(savedEmail != null) }
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
                tint = SentinelBlueBg
            )
        }

        SentinelLogo(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally))

        Text(
            stringResource(R.string.login_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelBlueBg
        )

        LoginField(
            label = stringResource(R.string.login_email_label),
            placeholder = stringResource(R.string.login_email_placeholder),
            value = email,
            onValueChange = { email = it },
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        LoginField(
            label = stringResource(R.string.senha),
            placeholder = stringResource(R.string.login_password_placeholder),
            value = password,
            onValueChange = { password = it },
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rememberMe = !rememberMe },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = SentinelBlueBg,
                    uncheckedColor = SentinelBlueBg,
                    checkmarkColor = SentinelYellow
                )
            )
            Text(
                stringResource(R.string.login_remember_me),
                color = SentinelBlueBg,
                fontSize = 13.sp
            )
        }

        errorText?.let {
            Text(it, color = SentinelRiskRed, fontSize = 13.sp)
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorText = null
                    AuthRepository.signIn(email.trim(), password)
                        .onSuccess {
                            prefs.edit {
                                if (rememberMe) {
                                    putString(REMEMBERED_EMAIL_KEY, email.trim())
                                } else {
                                    remove(REMEMBERED_EMAIL_KEY)
                                }
                            }
                            onSuccess()
                        }
                        .onFailure { errorText = AuthRepository.errorMessage(it) }
                    isLoading = false
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = SentinelBlueBg,
                disabledContainerColor = SentinelBlueBg.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
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
            color = SentinelBlueBg,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onRegister() }
        )
    }
}

@Composable
private fun LoginField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = SentinelBlueBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = SentinelTextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = SentinelTextMuted) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = stringResource(
                                if (visible) R.string.login_hide_password else R.string.login_show_password
                            ),
                            tint = SentinelTextMuted
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword && !visible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = SentinelBlueBg,
                unfocusedBorderColor = Color.Transparent,
                errorContainerColor = Color.White,
                errorBorderColor = SentinelRiskRed,
                cursorColor = SentinelBlueBg,
                focusedTextColor = SentinelTextDark,
                unfocusedTextColor = SentinelTextDark
            ),
            modifier = Modifier.fillMaxWidth()
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
                tint = SentinelBlueBg
            )
        }

        SentinelLogo(modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally))

        Text(
            stringResource(R.string.criar_conta),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelBlueBg
        )

        LoginField(
            label = stringResource(R.string.nome_completo),
            placeholder = stringResource(R.string.register_name_placeholder),
            value = name,
            onValueChange = { name = it },
            leadingIcon = Icons.Default.Person,
            keyboardType = KeyboardType.Text
        )
        LoginField(
            label = stringResource(R.string.login_email_label),
            placeholder = stringResource(R.string.login_email_placeholder),
            value = email,
            onValueChange = { email = it },
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        LoginField(
            label = stringResource(R.string.senha),
            placeholder = stringResource(R.string.login_password_placeholder),
            value = password,
            onValueChange = { password = it },
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        LoginField(
            label = stringResource(R.string.confirmar_senha),
            placeholder = stringResource(R.string.register_confirm_placeholder),
            value = confirm,
            onValueChange = { confirm = it },
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
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
            colors = ButtonDefaults.buttonColors(
                containerColor = SentinelBlueBg,
                disabledContainerColor = SentinelBlueBg.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
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
            color = SentinelBlueBg,
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
