package br.com.sentinel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard
import br.com.sentinel.app.ui.theme.SentinelRiskRed
import br.com.sentinel.app.ui.theme.SentinelSurfaceMuted
import br.com.sentinel.app.ui.theme.SentinelTeal

@Composable
internal fun SentinelPermissionItem(
    iconImage: ImageVector,
    active: Boolean,
    heading: String,
    descriptionText: String,
    onEnable: () -> Unit,
    buttonText: String = stringResource(R.string.ativar),
    onDisable: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SentinelSurfaceMuted, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = iconImage,
            contentDescription = null,
            tint = if (active) SentinelTeal else SentinelRiskRed,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(heading, color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(descriptionText, color = Color.Gray, fontSize = 10.sp)
        }
        if (!active) {
            TextButton(onClick = onEnable, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(buttonText, color = SentinelLightBlueCard, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else if (onDisable != null) {
            TextButton(onClick = onDisable, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    stringResource(R.string.desativar),
                    color = SentinelRiskRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
