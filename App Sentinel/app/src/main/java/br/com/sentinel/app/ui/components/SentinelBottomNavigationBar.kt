package br.com.sentinel.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.SentinelScreen
import br.com.sentinel.app.ui.icons.Shield
import br.com.sentinel.app.ui.theme.SentinelBlueBg

private const val LINE_HEIGHT_DP = 4
private const val LINE_WIDTH_FRACTION = 0.6f
private const val LINE_ANIMATION_MS = 250

@Composable
private fun Modifier.selectionLine(selected: Boolean): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(LINE_ANIMATION_MS),
        label = "nav_selection_line"
    )
    return drawBehind {
        val lineHeight = LINE_HEIGHT_DP.dp.toPx()
        val lineWidth = size.width * LINE_WIDTH_FRACTION * progress
        drawRoundRect(
            color = SentinelBlueBg,
            topLeft = Offset((size.width - lineWidth) / 2, 0f),
            size = Size(lineWidth, lineHeight),
            cornerRadius = CornerRadius(lineHeight / 2)
        )
    }
}

@Preview(showBackground = true, name = "Barra de Navegação")
@Composable
fun SentinelBottomNavigationBarPreview() {
    SentinelBottomNavigationBar(
        currentScreen = SentinelScreen.HOME,
        onNavigate = {}
    )
}

@Composable
fun SentinelBottomNavigationBar(currentScreen: SentinelScreen, onNavigate: (SentinelScreen) -> Unit) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = SentinelBlueBg,
        selectedTextColor = SentinelBlueBg,
        unselectedIconColor = SentinelBlueBg,
        unselectedTextColor = SentinelBlueBg,
        indicatorColor = Color.Transparent
    )

    val homeSelected = currentScreen == SentinelScreen.HOME || currentScreen == SentinelScreen.ANALYSIS_RESULT
    val securitySelected = currentScreen == SentinelScreen.SECURITY
    val profileSelected = currentScreen == SentinelScreen.PROFILE

    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = homeSelected,
            modifier = Modifier.selectionLine(homeSelected),
            onClick = { onNavigate(SentinelScreen.HOME) },
            icon = {
                Icon(
                    if (homeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.inicio)
                )
            },
            label = { Text(stringResource(R.string.inicio), fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = securitySelected,
            modifier = Modifier.selectionLine(securitySelected),
            onClick = { onNavigate(SentinelScreen.SECURITY) },
            icon = {
                Icon(
                    if (securitySelected) Icons.Filled.Shield else Icons.Outlined.Shield,
                    contentDescription = stringResource(R.string.seguranca)
                )
            },
            label = { Text(stringResource(R.string.seguranca), fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = profileSelected,
            modifier = Modifier.selectionLine(profileSelected),
            onClick = { onNavigate(SentinelScreen.PROFILE) },
            icon = {
                Icon(
                    if (profileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.perfil)
                )
            },
            label = { Text(stringResource(R.string.perfil), fontSize = 11.sp) },
            colors = itemColors
        )
    }
}
