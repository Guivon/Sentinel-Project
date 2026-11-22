package br.com.sentinel.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.CommunityApi
import br.com.sentinel.app.ui.SentinelScreen
import br.com.sentinel.app.ui.components.SentinelBottomNavigationBar
import br.com.sentinel.app.ui.components.SentinelReportCard
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelTheme
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.launch

@Preview(showBackground = true, name = "Status de Segurança")
@Composable
fun SentinelSecurityScreenPreview() {
    SentinelTheme {
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
    onHistoryClick: () -> Unit = {},
    onNavigate: (SentinelScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var alertsState by remember { mutableStateOf<AlertsState>(AlertsState.Loading) }
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val alerts = CommunityApi.fetchAlerts()
        alertsState = if (alerts == null) AlertsState.Failed else AlertsState.Loaded(alerts)
    }

    if (showReportDialog) {
        ReportScamDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { description, region, done ->
                scope.launch {
                    val sent = CommunityApi.reportScam(description, region)
                    Toast.makeText(
                        context,
                        if (sent) R.string.report_sent else R.string.report_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    if (sent) showReportDialog = false
                    done()
                }
            }
        )
    }

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
                contentDescription = stringResource(R.string.voltar),
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.sua_seguranca),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.status_do_seu_dispositivo),
                color = SentinelYellow,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            ProtectionStatusCard()

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SentinelReportCard(
                    modifier = Modifier.weight(1f),
                    onClick = { showReportDialog = true }
                )
                SentinelReportCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.meu_historico),
                    description = stringResource(R.string.veja_as_mensagens_que_o_sentinel_ja_an),
                    icon = Icons.Outlined.History,
                    iconTint = SentinelBlueBg,
                    onClick = onHistoryClick
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.alertas_recentes_na_sua_regiao),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            RegionalAlertsList(alertsState)

            Spacer(modifier = Modifier.height(20.dp))
            AnalyzeMessageShortcut(onClick = onAnalyzeClick)
        }
    }
}
