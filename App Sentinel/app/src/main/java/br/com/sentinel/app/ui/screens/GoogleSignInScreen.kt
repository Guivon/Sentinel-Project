package br.com.sentinel.app.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.BuildConfig
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.data.AvatarStore
import br.com.sentinel.app.services.GoogleSignInHelper
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTextLight
import br.com.sentinel.app.ui.theme.SentinelYellow

@Composable
fun SentinelGoogleSignInScreen(consentAt: String, onBack: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    var attempt by remember { mutableIntStateOf(0) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var errorDetail by remember { mutableStateOf<String?>(null) }
    val notConfigured = stringResource(R.string.google_not_configured)
    val cancelled = stringResource(R.string.google_cancelled)

    BackHandler { onBack() }

    LaunchedEffect(attempt) {
        errorText = null
        errorDetail = null
        if (!GoogleSignInHelper.isConfigured()) {
            errorText = notConfigured
            return@LaunchedEffect
        }
        val nonce = GoogleSignInHelper.newNonce()
        GoogleSignInHelper.requestIdToken(context, nonce)
            .mapCatching { token -> AuthRepository.signInWithGoogle(token, nonce).getOrThrow() }
            .onSuccess {
                AuthRepository.saveConsent(consentAt)
                AvatarStore.syncFromAccount(context)
                onSuccess()
            }
            .onFailure {
                Log.e("SentinelGoogle", "Google sign-in failed", it)
                errorText = AuthRepository.googleErrorMessage(it) ?: cancelled
                if (BuildConfig.DEBUG) errorDetail = "${it.javaClass.simpleName}: ${it.message}"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val current = errorText
        if (current == null) {
            CircularProgressIndicator(color = SentinelLightBlueCard, modifier = Modifier.size(40.dp))
            Text(
                stringResource(R.string.google_waiting),
                color = SentinelLightBlueCard,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Text(
                current,
                color = SentinelRiskRed,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            errorDetail?.let {
                Text(
                    it,
                    color = SentinelLightBlueCard,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Button(
                onClick = { attempt += 1 },
                colors = ButtonDefaults.buttonColors(containerColor = SentinelLightBlueCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth().height(50.dp)
            ) {
                Text(
                    stringResource(R.string.google_retry),
                    color = SentinelTextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.voltar), color = SentinelLightBlueCard)
            }
        }
    }
}
