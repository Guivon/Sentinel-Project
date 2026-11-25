package br.com.sentinel.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.icons.AccountBalance
import br.com.sentinel.app.ui.icons.CardGiftcard
import br.com.sentinel.app.ui.icons.ChevronLeft
import br.com.sentinel.app.ui.icons.Download
import br.com.sentinel.app.ui.icons.Link
import br.com.sentinel.app.ui.icons.Payments
import br.com.sentinel.app.ui.icons.Schedule
import br.com.sentinel.app.ui.icons.Security
import br.com.sentinel.app.ui.icons.VpnKey
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskPink
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelYellow

private class SecurityTip(val icon: ImageVector, @StringRes val title: Int, @StringRes val body: Int)

private val securityTips = listOf(
    SecurityTip(Icons.Default.Schedule, R.string.tip_urgency_title, R.string.tip_urgency_body),
    SecurityTip(Icons.Default.VpnKey, R.string.tip_codes_title, R.string.tip_codes_body),
    SecurityTip(Icons.Default.AccountBalance, R.string.tip_bank_title, R.string.tip_bank_body),
    SecurityTip(Icons.Default.Link, R.string.tip_links_title, R.string.tip_links_body),
    SecurityTip(Icons.Default.Payments, R.string.tip_pix_title, R.string.tip_pix_body),
    SecurityTip(Icons.Default.Security, R.string.tip_two_step_title, R.string.tip_two_step_body),
    SecurityTip(Icons.Default.Download, R.string.tip_apps_title, R.string.tip_apps_body),
    SecurityTip(Icons.Default.CardGiftcard, R.string.tip_offers_title, R.string.tip_offers_body),
    SecurityTip(Icons.Default.Call, R.string.tip_official_title, R.string.tip_official_body),
    SecurityTip(Icons.Default.Lock, R.string.tip_passwords_title, R.string.tip_passwords_body)
)

@Composable
fun SentinelSecurityTipsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
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
            text = stringResource(R.string.tips_title),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.tips_subtitle),
            color = SentinelYellow,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            securityTips.forEach { tip -> SecurityTipCard(tip) }
            ReportedScamFooter()
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SecurityTipCard(tip: SecurityTip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SentinelYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tip.icon,
                contentDescription = null,
                tint = SentinelBlueBg,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(tip.title),
                color = SentinelLightBlueCard,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(tip.body),
                color = Color.DarkGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ReportedScamFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelRiskPink, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = SentinelRiskRed,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.tips_footer_title),
                color = SentinelRiskRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tips_footer_body),
                color = SentinelRiskRed,
                fontSize = 13.sp
            )
        }
    }
}
