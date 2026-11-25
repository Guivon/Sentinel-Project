package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.icons.AccountBalance
import br.com.sentinel.app.ui.icons.Campaign
import br.com.sentinel.app.ui.icons.ChevronLeft
import br.com.sentinel.app.ui.icons.GppMaybe
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelTextMuted
import br.com.sentinel.app.ui.theme.SentinelTextSecondary
import br.com.sentinel.app.ui.theme.SentinelYellow
import java.time.Duration
import java.time.Instant

private const val HOURS_PER_DAY = 24L
private const val YESTERDAY_LIMIT_HOURS = 48L
private const val PANEL_CORNER_DP = 32

private class SystemUpdate(
    val icon: ImageVector,
    val onBlue: Boolean,
    val title: Int,
    val description: Int,
    val publishedAt: Instant
)

private val systemUpdates = listOf(
    SystemUpdate(
        Icons.Default.GppMaybe,
        true,
        R.string.notif_security_title,
        R.string.notif_security_desc,
        Instant.parse("2026-11-24T14:00:00Z")
    ),
    SystemUpdate(
        Icons.Default.Info,
        false,
        R.string.notif_update_title,
        R.string.notif_update_features_desc,
        Instant.parse("2026-11-24T10:00:00Z")
    ),
    SystemUpdate(
        Icons.Default.Campaign,
        true,
        R.string.notif_tip_title,
        R.string.notif_tip_desc,
        Instant.parse("2026-11-23T12:00:00Z")
    ),
    SystemUpdate(
        Icons.Default.AccountBalance,
        false,
        R.string.notif_bank_title,
        R.string.notif_bank_desc,
        Instant.parse("2026-11-21T12:00:00Z")
    ),
    SystemUpdate(
        Icons.Default.Info,
        true,
        R.string.notif_update_title,
        R.string.notif_update_access_desc,
        Instant.parse("2026-11-20T12:00:00Z")
    )
)

@Composable
fun SentinelNotificationsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
    ) {
        NotificationsHeader(onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(topStart = PANEL_CORNER_DP.dp, topEnd = PANEL_CORNER_DP.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            systemUpdates.forEach { UpdateRow(it) }
        }
    }
}

@Composable
private fun NotificationsHeader(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = stringResource(R.string.voltar),
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .offset(x = (-6).dp)
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.notificacoes),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.notif_subtitle), color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun UpdateRow(update: SystemUpdate) {
    val circleColor = if (update.onBlue) SentinelBlueBg else SentinelYellow
    val iconColor = if (update.onBlue) SentinelYellow else SentinelDarkBlueButton
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = update.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(update.title),
                color = SentinelDarkBlueButton,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = stringResource(update.description), color = SentinelTextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = SentinelDivider)
            Text(
                text = ageLabel(update.publishedAt),
                color = SentinelTextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ageLabel(publishedAt: Instant): String {
    val hours = Duration.between(publishedAt, Instant.now()).toHours().coerceAtLeast(0)
    return when {
        hours < 1 -> stringResource(R.string.notif_now)
        hours < HOURS_PER_DAY -> stringResource(R.string.notif_hours_ago, hours.toInt())
        hours < YESTERDAY_LIMIT_HOURS -> stringResource(R.string.notif_yesterday)
        else -> stringResource(R.string.notif_days_ago, (hours / HOURS_PER_DAY).toInt())
    }
}
