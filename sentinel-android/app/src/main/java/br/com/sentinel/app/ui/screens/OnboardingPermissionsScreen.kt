package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.markOnboardingCompleted
import br.com.sentinel.app.ui.rememberProtectionPermissions
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTeal
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.launch

@Composable
fun SentinelOnboardingPermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissions = rememberProtectionPermissions()

    val finish: () -> Unit = {
        scope.launch {
            markOnboardingCompleted(context)
            onContinue()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        OnboardingHeader()
        Spacer(modifier = Modifier.height(24.dp))

        OnboardingPermissionCard(
            icon = Icons.Outlined.Notifications,
            active = permissions.notificationsEnabled,
            title = if (permissions.notificationsEnabled) {
                stringResource(R.string.notificacoes_ativas)
            } else {
                stringResource(R.string.leitura_de_notificacoes_desativada)
            },
            description = stringResource(R.string.necessario_para_identificar_golpes_no),
            actionLabel = stringResource(R.string.ativar),
            onAction = permissions.requestNotificationAccess
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingPermissionCard(
            icon = Icons.Default.Email,
            active = permissions.connectedEmail != null,
            title = permissions.connectedEmail ?: stringResource(R.string.gmail_nao_conectado),
            description = stringResource(R.string.identifica_golpes_recebidos_por_e_mail),
            actionLabel = stringResource(R.string.conectar),
            onAction = permissions.connectGmail
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingPermissionCard(
            icon = Icons.Default.Visibility,
            active = permissions.screenActive,
            title = if (permissions.screenActive) {
                stringResource(R.string.leitura_de_tela_ativa)
            } else {
                stringResource(R.string.leitura_de_tela_desativada)
            },
            description = stringResource(R.string.analisa_a_tela_no_aparelho_para_achar),
            actionLabel = stringResource(R.string.ativar),
            onAction = permissions.requestScreenReading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingPermissionCard(
            icon = Icons.Default.Sms,
            active = permissions.smsActive,
            title = if (permissions.smsActive) {
                stringResource(R.string.sms_protection_active)
            } else {
                stringResource(R.string.sms_protection_inactive)
            },
            description = stringResource(R.string.sms_protection_description),
            actionLabel = stringResource(R.string.ativar),
            onAction = permissions.requestSms
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnboardingPermissionCard(
            icon = Icons.Default.Phone,
            active = permissions.callsActive,
            title = if (permissions.callsActive) {
                stringResource(R.string.calls_protection_active)
            } else {
                stringResource(R.string.calls_protection_inactive)
            },
            description = stringResource(R.string.calls_protection_description),
            actionLabel = stringResource(R.string.ativar),
            onAction = permissions.requestCallProtection
        )

        Spacer(modifier = Modifier.height(32.dp))
        OnboardingActions(onContinue = finish, onSkip = finish)
    }
}

@Composable
private fun OnboardingHeader() {
    Text(
        stringResource(R.string.configure_sua_protecao),
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        stringResource(R.string.ative_as_permissoes_para_o_sentinel_te),
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 14.sp
    )
}

@Composable
private fun OnboardingPermissionCard(
    icon: ImageVector,
    active: Boolean,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) SentinelTeal else SentinelRiskRed,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color.DarkGray, fontSize = 12.sp)
        }
        if (!active) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = SentinelLightBlueCard, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OnboardingActions(onContinue: () -> Unit, onSkip: () -> Unit) {
    Button(
        onClick = onContinue,
        colors = ButtonDefaults.buttonColors(containerColor = SentinelYellow),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(
            stringResource(R.string.continuar),
            color = SentinelBlueBg,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }

    TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.pular_por_agora),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
    }
}
