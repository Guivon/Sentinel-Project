package br.com.sentinel.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import br.com.sentinel.app.ui.theme.SentinelLightBlueCard

@Composable
fun SentinelCornerRibbon(
    modifier: Modifier = Modifier,
    color: Color = SentinelLightBlueCard,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val p1x = width * 0.65f; val p1y = height * 0.02f
        val p2x = width * 0.65f; val p2y = height * 0.38f
        val p3x = width * 0.0f;  val p3y = height * 0.90f
        val p4x = width * 0.0f;  val p4y = height * 0.55f

        val topPath = Path().apply {
            moveTo(p1x, p1y)
            lineTo(p2x, p2y)
            lineTo(p3x, p3y)
            lineTo(p4x, p4y)
            close()
        }
        drawPath(path = topPath, color = color)

        val trianglePath = Path().apply {
            moveTo(x = width * 0.40f, y = height * 0.65f)
            lineTo(x = width * 0.65f, y = height * 0.45f)
            lineTo(x = width * 0.65f, y = height * 0.85f)
            close()
        }
        drawPath(path = trianglePath, color = color)
    }
}

@Composable
fun SentinelCornerRibbonTop(
    modifier: Modifier = Modifier,
    color: Color = SentinelLightBlueCard
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val p1x = width * 0.80f; val p1y = height * 0.45f
        val p2x = width * 0.80f; val p2y = height * 0.10f
        val p3x = width * 0.0f;  val p3y = height * 0.55f
        val p4x = width * 0.0f;  val p4y = height * 0.95f

        val topPath = Path().apply {
            moveTo(p1x, p1y)
            lineTo(p2x, p2y)
            lineTo(p3x, p3y)
            lineTo(p4x, p4y)
            close()
        }
        drawPath(path = topPath, color = color)

        val trianglePath = Path().apply {
            moveTo(x = width * 0.50f, y = height * 0.20f)
            lineTo(x = width * 0.10f, y = height * 0.0f)
            lineTo(x = width * 0.10f, y = height * 0.40f)
            close()
        }
        drawPath(path = trianglePath, color = color)
    }
}
