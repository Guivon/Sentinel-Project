package br.com.sentinel.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.ui.theme.SentinelMockAction
import br.com.sentinel.app.ui.theme.SentinelMockBackground
import br.com.sentinel.app.ui.theme.SentinelRiskRed

enum class MockKind { LIST, TOGGLE, DIALOG }

class MockStep(
    @StringRes val caption: Int,
    @StringRes val title: Int,
    val rows: List<Int>,
    val highlight: Int,
    val kind: MockKind
)

private val ScreenBackground = SentinelMockBackground
private val RowBackground = Color.White

@Composable
fun TutorialMockScreen(step: MockStep, modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "tutorial_pulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "tutorial_pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp))
            .background(ScreenBackground, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Text(
                text = stringResource(step.title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (step.kind != MockKind.DIALOG) {
                step.rows.forEachIndexed { index, label ->
                    MockRow(
                        label = stringResource(label),
                        highlighted = index == step.highlight,
                        pulse = pulse,
                        showToggle = step.kind == MockKind.TOGGLE
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        if (step.kind == MockKind.DIALOG) {
            MockDialog(step = step, pulse = pulse)
        }
    }
}

@Composable
private fun MockRow(label: String, highlighted: Boolean, pulse: Float, showToggle: Boolean) {
    val borderModifier = if (highlighted) {
        Modifier.border(3.dp, SentinelRiskRed.copy(alpha = pulse), RoundedCornerShape(10.dp))
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .background(RowBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Black, modifier = Modifier.weight(1f))
        if (showToggle) {
            Switch(checked = false, onCheckedChange = null)
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
        if (highlighted) {
            Spacer(modifier = Modifier.width(6.dp))
            TapHint(pulse)
        }
    }
}

@Composable
private fun MockDialog(step: MockStep, pulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(step.rows.first()),
                fontSize = 13.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()) {
                step.rows.drop(1).forEachIndexed { offset, label ->
                    val highlighted = offset + 1 == step.highlight
                    val borderModifier = if (highlighted) {
                        Modifier.border(3.dp, SentinelRiskRed.copy(alpha = pulse), RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    }
                    Row(
                        modifier = Modifier.then(borderModifier).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(label),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentinelMockAction
                        )
                        if (highlighted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            TapHint(pulse)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
private fun TapHint(pulse: Float) {
    Icon(
        imageVector = Icons.Default.TouchApp,
        contentDescription = null,
        tint = SentinelRiskRed,
        modifier = Modifier.size(22.dp).alpha(pulse)
    )
}
