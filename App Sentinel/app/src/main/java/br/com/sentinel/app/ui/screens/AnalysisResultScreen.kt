package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AccessibilityStore
import br.com.sentinel.app.domain.RiskLevel
import br.com.sentinel.app.domain.ScamDetector
import br.com.sentinel.app.ui.SentinelSpeaker
import br.com.sentinel.app.ui.components.SentinelRiskGauge
import br.com.sentinel.app.ui.icons.ChevronLeft
import br.com.sentinel.app.ui.icons.Error
import br.com.sentinel.app.ui.icons.EventBusy
import br.com.sentinel.app.ui.icons.Link
import br.com.sentinel.app.ui.icons.Payments
import br.com.sentinel.app.ui.icons.PersonOff
import br.com.sentinel.app.ui.icons.Schedule
import br.com.sentinel.app.ui.theme.SentinelAmber
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelPeriwinkleStrong
import br.com.sentinel.app.ui.theme.SentinelRiskPink
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTeal
import br.com.sentinel.app.ui.theme.SentinelTextBody
import br.com.sentinel.app.ui.theme.SentinelTextDark
import br.com.sentinel.app.ui.theme.SentinelTextMuted
import br.com.sentinel.app.ui.theme.SentinelTextStrong
import br.com.sentinel.app.ui.theme.SentinelTheme
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.flow.map

@Preview(showBackground = true, name = "Resultado da Análise")
@Composable
fun SentinelAnalysisResultScreenPreview() {
    SentinelTheme {
        SentinelAnalysisResultScreen(
            result = ScamDetector.DetectionResult(
                level = RiskLevel.SUSPICIOUS,
                score = 90,
                reasons = listOf(stringResource(R.string.clique_no_link), stringResource(R.string.confirme_seus_dados))
            ),
            originalContent = stringResource(R.string.clique_aqui_e_confirme_seus_dados_agor),
            onBack = {}
        )
    }
}

private class RiskPresentation(
    val label: String,
    val color: Color,
    val title: String,
    val description: String,
    val summary: String
)

@Composable
private fun riskPresentation(level: RiskLevel, analyzingAi: Boolean, hasReasons: Boolean): RiskPresentation {
    if (analyzingAi) {
        return RiskPresentation(
            label = stringResource(R.string.analisando),
            color = SentinelPeriwinkleStrong,
            title = stringResource(R.string.analisando_2),
            description = stringResource(R.string.estamos_conferindo_a_mensagem_com_a_ia),
            summary = stringResource(R.string.aguarde_alguns_segundos)
        )
    }
    return when (level) {
        RiskLevel.SUSPICIOUS -> RiskPresentation(
            label = stringResource(R.string.alto_risco),
            color = SentinelRiskRed,
            title = stringResource(R.string.atencao),
            description = stringResource(R.string.essa_mensagem_pode_ser_golpe),
            summary = stringResource(R.string.nossa_ia_identificou_sinais_de_fraude)
        )
        RiskLevel.DOUBTFUL -> RiskPresentation(
            label = stringResource(R.string.suspeito),
            color = SentinelAmber,
            title = stringResource(R.string.fique_atento),
            description = stringResource(R.string.essa_mensagem_tem_sinais_duvidosos),
            summary = stringResource(R.string.nossa_ia_identificou_sinais_de_fraude)
        )
        RiskLevel.SAFE -> if (hasReasons) {
            RiskPresentation(
                label = stringResource(R.string.baixo_risco),
                color = SentinelTeal,
                title = stringResource(R.string.sem_sinais_claros),
                description = stringResource(R.string.ha_pontos_de_atencao_mas_nada_conclusi),
                summary = stringResource(R.string.encontramos_alguns_pontos_de_atencao_m)
            )
        } else {
            RiskPresentation(
                label = stringResource(R.string.baixo_risco),
                color = SentinelTeal,
                title = stringResource(R.string.tudo_certo),
                description = stringResource(R.string.nao_identificamos_sinais_claros_de_gol),
                summary = stringResource(R.string.nao_identificamos_padroes_suspeitos_ne)
            )
        }
    }
}

@Composable
private fun resultSpeech(risk: RiskPresentation, score: Int, reasons: List<String>): String {
    val parts = mutableListOf(
        risk.label,
        risk.title,
        risk.description,
        stringResource(R.string.result_speech_score, score)
    )
    if (reasons.isNotEmpty()) {
        parts.add(stringResource(R.string.result_speech_reasons))
        parts.addAll(reasons)
    }
    return parts.joinToString(". ")
}

@Composable
fun SentinelAnalysisResultScreen(
    result: ScamDetector.DetectionResult?,
    originalContent: String,
    onBack: () -> Unit,
    analyzingAi: Boolean = false,
    aiUnavailable: Boolean = false
) {
    val level = result?.level ?: RiskLevel.SAFE
    val score = (result?.score ?: 0).coerceIn(0, 100)
    val reasons = result?.reasons ?: emptyList()
    val risk = riskPresentation(level, analyzingAi, reasons.isNotEmpty())
    val readAloud by AccessibilityStore.state.map { it.readAloud }.collectAsState(initial = false)
    val spokenResult = if (analyzingAi) null else resultSpeech(risk, score, reasons)
    LaunchedEffect(spokenResult, readAloud) {
        if (readAloud && spokenResult != null) SentinelSpeaker.speak(spokenResult)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ResultHeader(onBack = onBack)

        if (analyzingAi) {
            Spacer(modifier = Modifier.height(12.dp))
            AiProgressBanner()
        } else if (aiUnavailable) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.nao_foi_possivel_consultar_a_ia_agora),
                color = SentinelYellow,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        RiskSummaryCard(risk = risk, score = score)

        Spacer(modifier = Modifier.height(16.dp))
        if (reasons.isNotEmpty()) {
            ReasonsCard(reasons = reasons, accent = risk.color)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (originalContent.isNotBlank()) {
            AnalyzedMessageCard(originalContent)
        }
    }
}

@Composable
private fun ResultHeader(onBack: () -> Unit) {
    Icon(
        imageVector = Icons.Default.ChevronLeft,
        contentDescription = stringResource(R.string.voltar),
        tint = Color.White,
        modifier = Modifier
            .size(36.dp)
            .clickable { onBack() }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.resultado_da_analise),
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = stringResource(R.string.nossa_ia_analisou_a_mensagem_que_voce),
        color = Color.White,
        fontSize = 18.sp
    )
}

@Composable
private fun AiProgressBanner() {
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
            text = stringResource(R.string.conferindo_com_a_ia_a_primeira_analise),
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun RiskSummaryCard(risk: RiskPresentation, score: Int) {
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
                    .background(risk.color, RoundedCornerShape(8.dp)),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = risk.title,
                    color = risk.color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = risk.description, color = SentinelTextBody, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = SentinelDivider)
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SentinelRiskGauge(score = score, mainColor = risk.color)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.score_de_risco),
                    color = SentinelTextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = risk.label,
                    color = risk.color,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = risk.summary, color = SentinelTextStrong, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ReasonsCard(reasons: List<String>, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.o_que_identificamos),
            color = SentinelTextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reasons.forEach { reason -> ReasonRow(reason = reason, accent = accent) }
        }
    }
}

private enum class ReasonKind(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int?,
    val keywords: List<String>
) {
    LINK(
        Icons.Default.Link,
        R.string.reason_title_link,
        R.string.reason_desc_link,
        listOf("link", "site", "domín")
    ),
    URGENCY(
        Icons.Default.Schedule,
        R.string.reason_title_urgency,
        R.string.reason_desc_urgency,
        listOf("urgên", "urgen", "prazo")
    ),
    DATA(
        Icons.Default.Lock,
        R.string.reason_title_data,
        R.string.reason_desc_data,
        listOf("dado", "senha", "sensív")
    ),
    WRITING(
        Icons.Default.Edit,
        R.string.reason_title_writing,
        R.string.reason_desc_writing,
        listOf("erro", "escrita", "gramát")
    ),
    PAYMENT(
        Icons.Default.Payments,
        R.string.reason_title_payment,
        null,
        listOf("débito", "cobrança", "pagamento", "pix", "boleto")
    ),
    SENDER(
        Icons.Default.PersonOff,
        R.string.reason_title_sender,
        null,
        listOf("número", "remetente", "contato", "oficial")
    ),
    DATE(
        Icons.Default.EventBusy,
        R.string.reason_title_date,
        null,
        listOf("data", "impossível", "inconsist")
    ),
    OTHER(Icons.Default.Error, R.string.reason_title_other, null, emptyList())
}

private fun reasonKind(reason: String): ReasonKind {
    val text = reason.lowercase()
    return ReasonKind.entries.firstOrNull { kind -> kind.keywords.any { text.contains(it) } } ?: ReasonKind.OTHER
}

@Composable
private fun ReasonRow(reason: String, accent: Color) {
    val kind = reasonKind(reason)
    val shape = RoundedCornerShape(12.dp)
    val background = if (accent == SentinelRiskRed) SentinelRiskPink else accent.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .border(1.dp, accent.copy(alpha = 0.25f), shape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accent, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = kind.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(kind.titleRes),
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = kind.descriptionRes?.let { stringResource(it) } ?: reason,
                color = SentinelTextDark,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AnalyzedMessageCard(content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.mensagem_analisada),
            color = SentinelTextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(content, color = SentinelTextBody, fontSize = 13.sp)
    }
}
