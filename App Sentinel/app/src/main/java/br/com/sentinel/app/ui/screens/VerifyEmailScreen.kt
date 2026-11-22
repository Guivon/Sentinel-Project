package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.ui.components.SentinelLogo
import br.com.sentinel.app.ui.components.SentinelTextField
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CODE_MIN_LENGTH = 6
private const val CODE_MAX_LENGTH = 8
private const val RESEND_COOLDOWN_SECONDS = 60
private const val SECOND_MS = 1000L

@Composable
fun SentinelVerifyEmailScreen(email: String, onBack: () -> Unit, onVerified: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var infoText by remember { mutableStateOf<String?>(null) }
    var cooldown by remember { mutableIntStateOf(RESEND_COOLDOWN_SECONDS) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(SECOND_MS)
            cooldown -= 1
        }
    }

    val resentMessage = stringResource(R.string.verify_code_resent)

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
            stringResource(R.string.verify_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SentinelLightBlueCard
        )
        Text(
            stringResource(R.string.verify_description, email),
            color = SentinelLightBlueCard,
            fontSize = 14.sp
        )

        SentinelTextField(
            code,
            { value -> code = value.filter { it.isDigit() }.take(CODE_MAX_LENGTH) },
            stringResource(R.string.verify_code_label),
            KeyboardType.NumberPassword
        )

        errorText?.let { Text(it, color = SentinelRiskRed, fontSize = 13.sp) }
        infoText?.let { Text(it, color = SentinelLightBlueCard, fontSize = 13.sp) }

        VerifyConfirmButton(
            enabled = code.length >= CODE_MIN_LENGTH && !isLoading,
            loading = isLoading
        ) {
            scope.launch {
                isLoading = true
                errorText = null
                AuthRepository.verifySignupCode(email, code)
                    .onSuccess { onVerified() }
                    .onFailure { errorText = AuthRepository.errorMessage(it) }
                isLoading = false
            }
        }

        VerifyResendButton(
            cooldown = cooldown,
            enabled = cooldown == 0 && !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            scope.launch {
                errorText = null
                infoText = null
                AuthRepository.resendSignupCode(email)
                    .onSuccess {
                        infoText = resentMessage
                        cooldown = RESEND_COOLDOWN_SECONDS
                    }
                    .onFailure { errorText = AuthRepository.errorMessage(it) }
            }
        }

        Text(
            stringResource(R.string.verify_spam_hint),
            color = SentinelLightBlueCard,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun VerifyConfirmButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = SentinelTextLight,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                stringResource(R.string.verify_button),
                color = SentinelTextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VerifyResendButton(cooldown: Int, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(
            if (cooldown > 0) {
                stringResource(R.string.verify_resend_wait, cooldown)
            } else {
                stringResource(R.string.verify_resend)
            },
            color = SentinelLightBlueCard,
            fontWeight = FontWeight.Bold
        )
    }
}
