package br.com.sentinel.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AuthRepository
import br.com.sentinel.app.ui.ATTACHMENT_MIME_TYPES
import br.com.sentinel.app.ui.AttachedFile
import br.com.sentinel.app.ui.MAX_ATTACHMENT_BYTES
import br.com.sentinel.app.ui.SentinelScreen
import br.com.sentinel.app.ui.components.SentinelBottomNavigationBar
import br.com.sentinel.app.ui.describeAttachment
import br.com.sentinel.app.ui.theme.SentinelBlueBg

@Preview(showBackground = true, name = "Tela Inicial (Home)")
@Composable
fun SentinelHomeScreenPreview() {
    SentinelHomeScreen(
        onAnalyzeClick = { _, _ -> },
        onHistoryClick = {},
        onLearnClick = {}
    )
}

@Composable
fun SentinelHomeScreen(
    onAnalyzeClick: (String, AttachedFile?) -> Unit,
    onHistoryClick: () -> Unit,
    onLearnClick: () -> Unit,
    onTipsClick: () -> Unit = {},
    onNavigate: (SentinelScreen) -> Unit = {}
) {
    var message by remember { mutableStateOf("") }
    var attachment by remember { mutableStateOf<AttachedFile?>(null) }
    var userName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val (name, size) = describeAttachment(context, uri)
            if (size > MAX_ATTACHMENT_BYTES) {
                Toast.makeText(context, R.string.attachment_too_large, Toast.LENGTH_LONG).show()
            } else {
                attachment = AttachedFile(uri, name)
            }
        }
    }

    LaunchedEffect(Unit) {
        userName = AuthRepository.currentName()?.trim()?.split(" ")?.firstOrNull() ?: ""
    }

    Scaffold(
        bottomBar = {
            SentinelBottomNavigationBar(
                currentScreen = SentinelScreen.HOME,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SentinelBlueBg)
        ) {
            HomeBackgroundShapes()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                HomeTopBar()
                Spacer(modifier = Modifier.height(24.dp))
                HomeGreeting(userName)
                Spacer(modifier = Modifier.height(24.dp))
                AnalysisInputCard(
                    message = message,
                    onMessageChange = { message = it },
                    attachment = attachment,
                    onPickAttachment = { picker.launch(ATTACHMENT_MIME_TYPES) },
                    onRemoveAttachment = { attachment = null },
                    onAnalyze = {
                        if (message.isNotBlank() || attachment != null) onAnalyzeClick(message, attachment)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                HomeQuickActions(onHistoryClick = onHistoryClick, onLearnClick = onLearnClick)
                Spacer(modifier = Modifier.height(12.dp))
                SecurityTipsCard(onClick = onTipsClick)
            }
        }
    }
}
