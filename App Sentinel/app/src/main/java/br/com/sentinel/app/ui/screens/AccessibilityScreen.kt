package br.com.sentinel.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AccessibilityState
import br.com.sentinel.app.data.AccessibilityStore
import br.com.sentinel.app.data.ColorBlindMode
import br.com.sentinel.app.ui.SentinelSpeaker
import br.com.sentinel.app.ui.theme.SentinelBlueBg
import br.com.sentinel.app.ui.theme.SentinelBorderLight
import br.com.sentinel.app.ui.theme.SentinelDarkBlueButton
import br.com.sentinel.app.ui.theme.SentinelDivider
import br.com.sentinel.app.ui.theme.SentinelTextMuted
import br.com.sentinel.app.ui.theme.SentinelTextStrong

private val SwitchOffTrack = Color(0xFF8A8A8A)
private val TipCardColor = Color.White.copy(alpha = 0.3f)
private val ModeYellow = Color(0xFFF2D400)
private val ModePurple = Color(0xFFC58BFF)
private val ModeGreen = Color(0xFF5DB04A)
private val ModeOrange = Color(0xFFFF7A3D)

private data class ModeOption(val mode: ColorBlindMode, val label: Int, val color: Color)

private val modeOptions = listOf(
    ModeOption(ColorBlindMode.STANDARD, R.string.acc_mode_standard, ModeYellow),
    ModeOption(ColorBlindMode.DEUTERANOPIA, R.string.acc_mode_deuteranopia, ModePurple),
    ModeOption(ColorBlindMode.PROTANOPIA, R.string.acc_mode_protanopia, ModeGreen),
    ModeOption(ColorBlindMode.TRITANOPIA, R.string.acc_mode_tritanopia, ModeOrange)
)

@Composable
fun SentinelAccessibilityScreen(onBack: () -> Unit) {
    val state by AccessibilityStore.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SentinelBlueBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = stringResource(R.string.voltar),
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .offset(x = (-6).dp)
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.height(12.dp))
        AccessibilityHeader()
        Spacer(modifier = Modifier.height(20.dp))
        ColorBlindCard(state)
        Spacer(modifier = Modifier.height(16.dp))
        TextAndVisualCard(state)
        Spacer(modifier = Modifier.height(16.dp))
        AccessibilityTip()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AccessibilityHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Accessibility,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.acessibilidade),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.acc_subtitle),
        color = Color.White,
        fontSize = 14.sp
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
private fun CardTitle(icon: ImageVector, title: Int, description: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SentinelDarkBlueButton,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(title),
            color = SentinelTextStrong,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(text = stringResource(description), color = SentinelTextMuted, fontSize = 13.sp)
}

@Composable
private fun ColorBlindCard(state: AccessibilityState) {
    SettingsCard {
        CardTitle(Icons.Default.RemoveRedEye, R.string.acc_colorblind_title, R.string.acc_colorblind_desc)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            modeOptions.forEach { option ->
                ModeChoice(
                    option = option,
                    selected = state.colorBlindMode == option.mode,
                    onClick = { AccessibilityStore.update { it.copy(colorBlindMode = option.mode) } }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SettingRow(
            icon = Icons.Default.Palette,
            title = R.string.acc_high_contrast_title,
            description = R.string.acc_high_contrast_desc,
            checked = state.highContrast,
            onChange = { value -> AccessibilityStore.update { it.copy(highContrast = value) } }
        )
    }
}

@Composable
private fun ModeChoice(option: ModeOption, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.White, CircleShape)
                .border(
                    BorderStroke(3.dp, if (selected) SentinelDarkBlueButton else SentinelBorderLight),
                    CircleShape
                )
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(SentinelBlueBg))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(option.color))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(option.label),
            color = if (selected) SentinelDarkBlueButton else SentinelTextMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TextAndVisualCard(state: AccessibilityState) {
    val context = LocalContext.current
    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.acc_aa),
                color = SentinelDarkBlueButton,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.acc_text_title),
                color = SentinelTextStrong,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = stringResource(R.string.acc_text_desc), color = SentinelTextMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextSizeSlider(state.textSize)
        HorizontalLine()
        SettingRow(
            icon = Icons.Default.VolumeUp,
            title = R.string.acc_read_aloud_title,
            description = R.string.acc_read_aloud_desc,
            checked = state.readAloud,
            onChange = { value ->
                AccessibilityStore.update { it.copy(readAloud = value) }
                if (value) {
                    SentinelSpeaker.init(context)
                    SentinelSpeaker.speak(context.getString(R.string.acc_read_aloud_on))
                }
            }
        )
        HorizontalLine()
        SettingRow(
            icon = Icons.Default.Smartphone,
            title = R.string.acc_reduce_title,
            description = R.string.acc_reduce_desc,
            checked = state.reduceAnimations,
            onChange = { value -> AccessibilityStore.update { it.copy(reduceAnimations = value) } }
        )
    }
}

@Composable
private fun TextSizeSlider(value: Float) {
    var dragValue by remember(value) { mutableFloatStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("A", color = SentinelDarkBlueButton, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it },
            onValueChangeFinished = { AccessibilityStore.update { it.copy(textSize = dragValue) } },
            colors = SliderDefaults.colors(
                thumbColor = SentinelDarkBlueButton,
                activeTrackColor = SentinelDarkBlueButton,
                inactiveTrackColor = SwitchOffTrack
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        Text("A", color = SentinelDarkBlueButton, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HorizontalLine() {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(SentinelDivider)
    )
}

@Composable
private fun SettingRow(icon: ImageVector, title: Int, description: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SentinelDarkBlueButton,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(title),
                color = SentinelTextStrong,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(description), color = SentinelTextMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SentinelDarkBlueButton,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SwitchOffTrack,
                uncheckedBorderColor = SwitchOffTrack
            )
        )
    }
}

@Composable
private fun AccessibilityTip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TipCardColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.acc_tip_title),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(R.string.acc_tip_desc), color = Color.White, fontSize = 12.sp)
        }
    }
}
