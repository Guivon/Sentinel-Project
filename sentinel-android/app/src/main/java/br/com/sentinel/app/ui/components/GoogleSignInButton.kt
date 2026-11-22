package br.com.sentinel.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard

@Composable
fun GoogleSignInButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SentinelLightBlueCard.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().height(50.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_google_g),
            contentDescription = stringResource(R.string.google_continue),
            modifier = Modifier.size(24.dp)
        )
    }
}
