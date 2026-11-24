package br.com.sentinel.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelTheme
import br.com.sentinel.app.ui.theme.SentinelYellow
import kotlinx.coroutines.delay

private const val STEP_DELAY_MS = 1100L
private const val SPINNER_PERIOD_MS = 1100
private const val SPINNER_SWEEP = 100f

@Preview(showBackground = true, name = "Análise em andamento")
@Composable
fun SentinelAnalysisProgressScreenPreview() {
    SentinelTheme {
        SentinelAnalysisProgressScreen()
    }
}

@Composable
fun SentinelAnalysisProgressScreen() {
    val steps = listOf(
        stringResource(R.string.progress_step_content),
        stringResource(R.string.progress_step_links),
        stringResource(R.string.progress_step_fraud),
        stringResource(R.string.progress_step_urgency)
    )
    var current by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (current < steps.size - 1) {
            delay(STEP_DELAY_MS)
            current += 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.sentinel_logo),
            contentDescription = stringResource(R.string.logo_do_sentinel),
            colorFilter = ColorFilter.tint(SentinelYellow),
            modifier = Modifier.width(150.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.progress_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.progress_description),
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        SpinnerRing()
        Spacer(modifier = Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            steps.forEachIndexed { index, label ->
                ProgressStep(label = label, completed = index < current, active = index == current)
            }
        }
    }
}

@Composable
private fun SpinnerRing() {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SPINNER_PERIOD_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_angle"
    )
    Canvas(modifier = Modifier.size(96.dp)) {
        val strokeWidth = 14.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        drawArc(
            color = SentinelYellow.copy(alpha = 0.35f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = SentinelYellow,
            startAngle = angle,
            sweepAngle = SPINNER_SWEEP,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ProgressStep(label: String, completed: Boolean, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val markerModifier = Modifier.size(34.dp)
        Box(
            modifier = when {
                completed -> markerModifier.background(SentinelYellow, CircleShape)
                active -> markerModifier.border(2.dp, SentinelYellow, CircleShape)
                else -> markerModifier.border(2.dp, SentinelYellow.copy(alpha = 0.35f), CircleShape)
            },
            contentAlignment = Alignment.Center
        ) {
            if (completed || active) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (completed) SentinelBlueBg else SentinelYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = if (completed || active) 1f else 0.6f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
