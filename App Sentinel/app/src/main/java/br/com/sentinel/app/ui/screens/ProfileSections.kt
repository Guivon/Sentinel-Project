package br.com.sentinel.app.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.sentinel.app.BuildConfig
import br.com.sentinel.app.R
import br.com.sentinel.app.services.GmailCheckWorker
import br.com.sentinel.app.ui.ProtectionPermissions
import br.com.sentinel.app.ui.components.SentinelPermissionItem
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelSurfaceMuted

@Composable
internal fun ProfileAccordion(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SentinelDarkBlueButton,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = SentinelDarkBlueButton,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = SentinelDarkBlueButton
        )
    }
    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelSurfaceMuted, RoundedCornerShape(12.dp))
            .then(clickModifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color.Gray, fontSize = 10.sp)
        }
        trailing()
    }
}

@Composable
private fun ChevronTrailing() {
    Icon(
        imageVector = Icons.Default.KeyboardArrowRight,
        contentDescription = null,
        tint = SentinelDarkBlueButton,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
internal fun AccessibilitySection(permissions: ProtectionPermissions) {
    val context = LocalContext.current

    SentinelPermissionItem(
        iconImage = Icons.Outlined.Notifications,
        active = permissions.notificationsEnabled,
        heading = if (permissions.notificationsEnabled) {
            stringResource(R.string.leitura_de_notificacoes_ativa)
        } else {
            stringResource(R.string.leitura_de_notificacoes_desativada)
        },
        descriptionText = stringResource(R.string.necessario_para_identificar_golpes_no),
        onEnable = permissions.requestNotificationAccess,
        onDisable = permissions.openNotificationSettings
    )
    SentinelPermissionItem(
        iconImage = Icons.Default.Email,
        active = permissions.connectedEmail != null,
        heading = permissions.connectedEmail ?: stringResource(R.string.gmail_nao_conectado),
        descriptionText = stringResource(R.string.identifica_golpes_recebidos_por_e_mail),
        onEnable = permissions.connectGmail,
        buttonText = stringResource(R.string.conectar)
    )
    if (permissions.connectedEmail != null) {
        TextButton(
            onClick = {
                Toast.makeText(context, R.string.checking_email, Toast.LENGTH_SHORT).show()
                WorkManager.getInstance(context)
                    .enqueue(OneTimeWorkRequestBuilder<GmailCheckWorker>().build())
            },
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                stringResource(R.string.verificar_e_mail_agora),
                color = SentinelLightBlueCard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    SentinelPermissionItem(
        iconImage = Icons.Default.Visibility,
        active = permissions.screenActive,
        heading = if (permissions.screenActive) {
            stringResource(R.string.leitura_de_tela_ativa)
        } else {
            stringResource(R.string.leitura_de_tela_desativada)
        },
        descriptionText = stringResource(R.string.analisa_o_texto_da_tela_no_aparelho_pa),
        onEnable = permissions.requestScreenReading,
        onDisable = permissions.turnOffScreenReading
    )
    SentinelPermissionItem(
        iconImage = Icons.Default.Sms,
        active = permissions.smsActive,
        heading = if (permissions.smsActive) {
            stringResource(R.string.sms_protection_active)
        } else {
            stringResource(R.string.sms_protection_inactive)
        },
        descriptionText = stringResource(R.string.sms_protection_description),
        onEnable = permissions.requestSms,
        onDisable = permissions.openSmsSettings
    )
    SentinelPermissionItem(
        iconImage = Icons.Default.Phone,
        active = permissions.callsActive,
        heading = if (permissions.callsActive) {
            stringResource(R.string.calls_protection_active)
        } else {
            stringResource(R.string.calls_protection_inactive)
        },
        descriptionText = stringResource(R.string.calls_protection_description),
        onEnable = permissions.requestCallProtection,
        onDisable = permissions.openCallSettings
    )
}

@Composable
internal fun PrivacySection(onOpenPolicy: () -> Unit) {
    val context = LocalContext.current
    ProfileInfoRow(
        title = stringResource(R.string.politica_de_privacidade_e_lgpd),
        description = stringResource(R.string.veja_como_os_seus_dados_sao_tratados_e),
        onClick = onOpenPolicy
    ) {
        ChevronTrailing()
    }
    ProfileInfoRow(
        title = stringResource(R.string.permissoes_de_privacidade_do_sistema),
        description = stringResource(R.string.gerir_camara_microfone_e_acessos_conce),
        onClick = { context.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS)) }
    ) {
        ChevronTrailing()
    }
}

@Composable
internal fun NotificationsSection() {
    val context = LocalContext.current
    ProfileInfoRow(
        title = stringResource(R.string.gerir_canais_de_notificacoes),
        description = stringResource(R.string.configurar_sons_pop_ups_e_permissoes_n),
        onClick = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        }
    ) {
        ChevronTrailing()
    }
}

@Composable
internal fun AboutSection() {
    ProfileInfoRow(
        title = stringResource(R.string.versao),
        description = stringResource(R.string.app_version_label, BuildConfig.VERSION_NAME)
    )
}
