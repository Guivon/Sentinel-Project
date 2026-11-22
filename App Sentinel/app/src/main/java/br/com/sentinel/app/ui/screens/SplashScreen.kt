package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.sentinel.app.ui.components.SentinelLogo
import br.com.sentinel.app.ui.theme.SentinelYellow

@Preview(showBackground = true, name = "Tela Splash")
@Composable
fun SentinelSplashScreenPreview() {
    SentinelSplashScreen(onContinue = {})
}

@Composable
fun SentinelSplashScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelYellow)
            .clickable { onContinue() },
        contentAlignment = Alignment.Center
    ) {
        SentinelLogo(modifier = Modifier.size(180.dp))
    }
}
