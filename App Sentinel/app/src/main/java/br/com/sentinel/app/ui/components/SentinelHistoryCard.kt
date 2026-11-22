package br.com.sentinel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.Alert
import br.com.sentinel.app.data.AlertStatus
import br.com.sentinel.app.domain.RiskLevel
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTeal
import br.com.sentinel.app.ui.theme.SentinelTealDarker
import br.com.sentinel.app.ui.theme.SentinelTextDark
import br.com.sentinel.app.ui.theme.SentinelTextMuted
import br.com.sentinel.app.ui.theme.SentinelTextSecondary
import br.com.sentinel.app.ui.theme.SentinelWarningDark
import br.com.sentinel.app.ui.theme.SentinelWarningYellow

@Composable
internal fun SentinelHistoryAlertCard(alert: Alert) {
    val (label, backgroundColor, textColor, iconImage) = when (alert.status) {
        AlertStatus.CONFIRMED_LOCAL, AlertStatus.CONFIRMED_AI ->
            if (alert.level == RiskLevel.DOUBTFUL) {
                listOf(
                    stringResource(R.string.suspeito),
                    SentinelWarningYellow,
                    SentinelWarningDark,
                    Icons.Outlined.Warning
                )
            } else {
                listOf(stringResource(R.string.alto_risco), SentinelRiskRed, Color.White, Icons.Outlined.Warning)
            }
        AlertStatus.ANALYZING ->
            listOf(stringResource(R.string.analisando), SentinelWarningYellow, SentinelWarningDark, Icons.Outlined.Info)
        AlertStatus.OFFICIAL ->
            listOf(stringResource(R.string.diz_ser_oficial), SentinelTeal, SentinelTealDarker, Icons.Outlined.Info)
        AlertStatus.DISMISSED ->
            listOf(stringResource(R.string.seguro), SentinelTeal, SentinelTealDarker, Icons.Outlined.Info)
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
                text = "${alert.app} • ${alert.timeLabel}",
                color = SentinelTextSecondary,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .background(backgroundColor as Color, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconImage as ImageVector,
                    contentDescription = null,
                    tint = textColor as Color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label as String,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = alert.snippet,
            color = SentinelTextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (alert.reasons.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = alert.reasons.joinToString(", "),
                color = SentinelTextMuted,
                fontSize = 11.sp
            )
        }
    }
}
