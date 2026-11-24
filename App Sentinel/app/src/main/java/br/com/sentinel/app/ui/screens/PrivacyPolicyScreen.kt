package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelTextBody
import br.com.sentinel.app.ui.theme.SentinelTextDark
import br.com.sentinel.app.ui.theme.SentinelTextMuted
import br.com.sentinel.app.ui.theme.SentinelYellow

@Composable
fun SentinelPrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LgpdHeader(onBack)
        PrinciplesCard()
        LegalBasisCard()
        WhatIsCard()
        ConsentCard()
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SentinelYellow),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.privacy_understood),
                color = SentinelDarkBlueButton,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LgpdHeader(onBack: () -> Unit) {
    Column {
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
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.privacy_rights_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.lgpd_subtitle), color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun LgpdCard(icon: ImageVector, title: Int, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SentinelBlueBg,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(title),
                color = SentinelTextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun BulletRow(text: Int) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(text = "•", color = SentinelBlueBg, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(text), color = SentinelTextBody, fontSize = 13.sp)
    }
}

private class LgpdPrinciple(val title: Int, val description: Int)

private val lgpdPrinciples = listOf(
    LgpdPrinciple(R.string.lgpd_p_purpose, R.string.lgpd_p_purpose_desc),
    LgpdPrinciple(R.string.lgpd_p_adequacy, R.string.lgpd_p_adequacy_desc),
    LgpdPrinciple(R.string.lgpd_p_necessity, R.string.lgpd_p_necessity_desc),
    LgpdPrinciple(R.string.lgpd_p_access, R.string.lgpd_p_access_desc),
    LgpdPrinciple(R.string.lgpd_p_quality, R.string.lgpd_p_quality_desc),
    LgpdPrinciple(R.string.lgpd_p_transparency, R.string.lgpd_p_transparency_desc),
    LgpdPrinciple(R.string.lgpd_p_security, R.string.lgpd_p_security_desc),
    LgpdPrinciple(R.string.lgpd_p_prevention, R.string.lgpd_p_prevention_desc),
    LgpdPrinciple(R.string.lgpd_p_nondiscrimination, R.string.lgpd_p_nondiscrimination_desc),
    LgpdPrinciple(R.string.lgpd_p_accountability, R.string.lgpd_p_accountability_desc)
)

@Composable
private fun WhatIsCard() {
    LgpdCard(Icons.Default.Info, R.string.lgpd_what_title) {
        Text(text = stringResource(R.string.lgpd_what_desc), color = SentinelTextBody, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        BulletRow(R.string.lgpd_how_messages)
        BulletRow(R.string.lgpd_how_history)
        BulletRow(R.string.lgpd_how_account)
        BulletRow(R.string.lgpd_how_control)
    }
}

@Composable
private fun PrinciplesCard() {
    LgpdCard(Icons.Default.Gavel, R.string.lgpd_principles_title) {
        lgpdPrinciples.forEachIndexed { index, principle ->
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = stringResource(principle.title),
                    color = SentinelTextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(principle.description),
                    color = SentinelTextMuted,
                    fontSize = 13.sp
                )
            }
            if (index < lgpdPrinciples.lastIndex) HorizontalDivider(color = SentinelDivider)
        }
    }
}

@Composable
private fun LegalBasisCard() {
    LgpdCard(Icons.Default.VerifiedUser, R.string.lgpd_basis_title) {
        Text(text = stringResource(R.string.lgpd_basis_intro), color = SentinelTextBody, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        BulletRow(R.string.lgpd_basis_consent)
        BulletRow(R.string.lgpd_basis_law)
        BulletRow(R.string.lgpd_basis_contract)
        BulletRow(R.string.lgpd_basis_process)
        BulletRow(R.string.lgpd_basis_life)
        BulletRow(R.string.lgpd_basis_health)
        BulletRow(R.string.lgpd_basis_legitimate)
        BulletRow(R.string.lgpd_basis_credit)
    }
}

@Composable
private fun ConsentCard() {
    LgpdCard(Icons.Default.Lock, R.string.lgpd_consent_title) {
        Text(text = stringResource(R.string.lgpd_consent_desc), color = SentinelTextBody, fontSize = 13.sp)
    }
}
