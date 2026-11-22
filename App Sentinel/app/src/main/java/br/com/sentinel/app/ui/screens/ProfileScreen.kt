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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.ui.AlertsViewModel
import br.com.sentinel.app.ui.ProtectionPermissions
import br.com.sentinel.app.ui.SentinelScreen
import br.com.sentinel.app.ui.components.SentinelBottomNavigationBar
import br.com.sentinel.app.ui.rememberProtectionPermissions
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelTheme
import br.com.sentinel.app.ui.theme.SentinelYellow

@Preview(showBackground = true, name = "Perfil do Usuário")
@Composable
fun SentinelProfileScreenPreview() {
    SentinelTheme {
        SentinelProfileScreen(onLogout = {})
    }
}

private enum class ProfileSection { ACCESSIBILITY, PRIVACY, NOTIFICATIONS, ABOUT }

@Composable
fun SentinelProfileScreen(onLogout: () -> Unit, onBack: () -> Unit = {}, onNavigate: (SentinelScreen) -> Unit = {}) {
    var expanded by remember { mutableStateOf(emptySet<ProfileSection>()) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    val permissions = rememberProtectionPermissions()

    LaunchedEffect(Unit) {
        userName = AuthRepository.currentName() ?: ""
        userEmail = AuthRepository.currentEmail() ?: ""
    }

    fun toggle(section: ProfileSection) {
        expanded = if (section in expanded) expanded - section else expanded + section
    }

    Scaffold(
        bottomBar = {
            SentinelBottomNavigationBar(
                currentScreen = SentinelScreen.PROFILE,
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
                text = stringResource(R.string.perfil),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))
            ProfileHeader(userName = userName, userEmail = userEmail)

            Spacer(modifier = Modifier.height(20.dp))
            ProfileStats()

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.configuracoes),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
            ProfileSettingsCard(
                expanded = expanded,
                onToggle = { toggle(it) },
                permissions = permissions,
                onOpenPolicy = { onNavigate(SentinelScreen.PRIVACY_POLICY) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            LogoutButton(onLogout)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileStats() {
    val alertsViewModel: AlertsViewModel = viewModel()
    val totalAnalyzed by alertsViewModel.totalAnalyzed.collectAsState()
    val scamsAvoided by alertsViewModel.scamsAvoided.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileStat(
            value = totalAnalyzed,
            label = stringResource(R.string.mensagens_scaneadas),
            valueColor = Color.White,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )
        ProfileStat(
            value = scamsAvoided,
            label = stringResource(R.string.golpes_evitados),
            valueColor = SentinelYellow,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStat(value: Int, label: String, valueColor: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = SentinelRiskRed
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.sair_da_conta),
            color = SentinelRiskRed,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileSettingsCard(
    expanded: Set<ProfileSection>,
    onToggle: (ProfileSection) -> Unit,
    permissions: ProtectionPermissions,
    onOpenPolicy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        ProfileAccordion(
            icon = Icons.Default.Accessibility,
            title = stringResource(R.string.acessibilidade),
            expanded = ProfileSection.ACCESSIBILITY in expanded,
            onToggle = { onToggle(ProfileSection.ACCESSIBILITY) }
        ) {
            AccessibilitySection(permissions)
        }
        HorizontalDivider(color = SentinelDivider)

        ProfileAccordion(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.privacidade),
            expanded = ProfileSection.PRIVACY in expanded,
            onToggle = { onToggle(ProfileSection.PRIVACY) }
        ) {
            PrivacySection(onOpenPolicy = onOpenPolicy)
        }
        HorizontalDivider(color = SentinelDivider)

        ProfileAccordion(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.notificacoes),
            expanded = ProfileSection.NOTIFICATIONS in expanded,
            onToggle = { onToggle(ProfileSection.NOTIFICATIONS) }
        ) {
            NotificationsSection()
        }
        HorizontalDivider(color = SentinelDivider)

        ProfileAccordion(
            icon = Icons.Default.Info,
            title = stringResource(R.string.sobre_o_sentinel),
            expanded = ProfileSection.ABOUT in expanded,
            onToggle = { onToggle(ProfileSection.ABOUT) }
        ) {
            AboutSection()
        }
    }
}
