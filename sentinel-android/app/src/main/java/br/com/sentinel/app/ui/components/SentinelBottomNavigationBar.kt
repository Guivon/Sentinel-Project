package br.com.sentinel.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.ui.SentinelScreen
import br.com.sentinel.app.ui.theme.SentinelBlueBg

@Preview(showBackground = true, name = "Barra de Navegação")
@Composable
fun SentinelBottomNavigationBarPreview() {
    SentinelBottomNavigationBar(
        currentScreen = SentinelScreen.HOME,
        onNavigate = {}
    )
}

@Composable
fun SentinelBottomNavigationBar(
    currentScreen: SentinelScreen,
    onNavigate: (SentinelScreen) -> Unit
) {
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
