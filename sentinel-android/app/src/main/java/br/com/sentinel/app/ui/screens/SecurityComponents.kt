package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.CommunityApi
import br.com.sentinel.app.data.GmailHelper
import br.com.sentinel.app.services.PermissionsHelper
import br.com.sentinel.app.ui.components.SentinelAlertItem
import br.com.sentinel.app.ui.theme.SentinelAqua
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTealDark
import br.com.sentinel.app.ui.theme.SentinelYellow

private const val REPORT_MIN_LENGTH = 10
private const val REPORT_MAX_LENGTH = 1000
private const val REGION_MAX_LENGTH = 60
private const val PROTECTIONS_TOTAL = 5

internal sealed interface AlertsState {
    data object Loading : AlertsState
    data object Failed : AlertsState
    data class Loaded(val items: List<CommunityApi.RegionalAlert>) : AlertsState
}

@Composable
internal fun ProtectionStatusCard() {
    val context = LocalContext.current
    val notificationsOk = PermissionsHelper.isNotificationListenerEnabled(context)
    val screenOk = PermissionsHelper.isAccessibilityServiceEnabled(context)
    val smsOk = PermissionsHelper.isSmsPermissionGranted(context)
    val callsOk = PermissionsHelper.isCallScreeningRoleHeld(context)
    val gmailOk = GmailHelper.connectedEmail(context) != null
    val activeCount = listOf(notificationsOk, screenOk, smsOk, callsOk, gmailOk).count { it }

    val title = when (activeCount) {
        PROTECTIONS_TOTAL -> stringResource(R.string.protecao_ativada)
        0 -> stringResource(R.string.protecao_desativada)
        else -> stringResource(R.string.protecao_parcial)
    }
    val description = if (activeCount == PROTECTIONS_TOTAL) {
        stringResource(R.string.notificacoes_leitura_de_tela_e_sms_est)
    } else {
        stringResource(R.string.s_1_de_3_protecoes_ligadas_ajuste_no_seu, activeCount, PROTECTIONS_TOTAL)
    }
    val color = when (activeCount) {
        PROTECTIONS_TOTAL -> SentinelAqua
        0 -> SentinelRiskRed
        else -> SentinelYellow
    }

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
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SentinelTealDark,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.DarkGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color.DarkGray, fontSize = 13.sp)
        }
    }
}

@Composable
internal fun RegionalAlertsList(state: AlertsState) {
    when (state) {
        AlertsState.Loading -> AlertsMessage(
            stringResource(R.string.carregando_alertas),
            Color.White.copy(alpha = 0.8f)
        )
        AlertsState.Failed -> AlertsMessage(
            stringResource(R.string.nao_foi_possivel_carregar_os_alertas_a),
            SentinelYellow
        )
        is AlertsState.Loaded -> if (state.items.isEmpty()) {
            AlertsMessage(
                stringResource(R.string.nenhum_alerta_recente_por_enquanto),
                Color.White.copy(alpha = 0.8f)
            )
        } else {
            state.items.forEach { alert ->
                SentinelAlertItem(title = alert.title, subtitle = alert.subtitle())
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AlertsMessage(text: String, color: Color) {
    Text(text, color = color, fontSize = 13.sp)
}

@Composable
internal fun AnalyzeMessageShortcut(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelYellow, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SentinelBlueBg)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.fazer_analise_de_mensagem),
            color = SentinelBlueBg,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SentinelBlueBg)
    }
}

@Composable
internal fun ReportScamDialog(
    onDismiss: () -> Unit,
    onSubmit: (description: String, region: String, done: () -> Unit) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val canSend = description.trim().length >= REPORT_MIN_LENGTH && !sending

    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        title = { Text(stringResource(R.string.registrar_golpe)) },
        text = {
            Column {
                Text(stringResource(R.string.conte_o_que_aconteceu_seu_relato_ajuda), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= REPORT_MAX_LENGTH) description = it },
                    label = { Text(stringResource(R.string.o_que_aconteceu)) },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = region,
                    onValueChange = { if (it.length <= REGION_MAX_LENGTH) region = it },
                    label = { Text(stringResource(R.string.cidade_uf_opcional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSend,
                onClick = {
                    sending = true
                    onSubmit(description.trim(), region.trim()) { sending = false }
                }
            ) {
                Text(if (sending) stringResource(R.string.enviando) else stringResource(R.string.enviar))
            }
        },
        dismissButton = {
            TextButton(enabled = !sending, onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
