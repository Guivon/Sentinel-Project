package br.com.sentinel.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import br.com.sentinel.app.data.AccessibilityStore
import br.com.sentinel.app.data.ColorBlindMode

data class SentinelPalette(
    val blueBg: Color,
    val lightBlueCard: Color,
    val darkBlue: Color,
    val yellow: Color,
    val teal: Color,
    val tealDark: Color,
    val tealDarker: Color,
    val aqua: Color,
    val mint: Color,
    val riskRed: Color,
    val riskPink: Color,
    val periwinkle: Color,
    val periwinkleStrong: Color,
    val paleBlue: Color,
    val paleBlueAlt: Color
)

object SentinelPalettes {
    val standard = SentinelPalette(
        blueBg = Color(0xFF4862FF),
        lightBlueCard = Color(0xFF3857F5),
        darkBlue = Color(0xFF0F26B0),
        yellow = Color(0xFFEFF300),
        teal = Color(0xFF04E9CA),
        tealDark = Color(0xFF0B6458),
        tealDarker = Color(0xFF0B3B36),
        aqua = Color(0xFF00E5C3),
        mint = Color(0xFF1DE9B6),
        riskRed = Color(0xFFE61F1F),
        riskPink = Color(0xFFFFD9DE),
        periwinkle = Color(0xFF9FA8F0),
        periwinkleStrong = Color(0xFF8A94FF),
        paleBlue = Color(0xFFEFF1FF),
        paleBlueAlt = Color(0xFFE8EEFF)
    )

    val deuteranopia = SentinelPalette(
        blueBg = Color(0xFF0068FB),
        lightBlueCard = Color(0xFF005FE0),
        darkBlue = Color(0xFF0048C4),
        yellow = Color(0xFFFFF746),
        teal = Color(0xFF1A73E8),
        tealDark = Color(0xFF0A4C99),
        tealDarker = Color(0xFF062B5C),
        aqua = Color(0xFFFFF746),
        mint = Color(0xFFFFF746),
        riskRed = Color(0xFF8D7E27),
        riskPink = Color(0xFFE3E2DE),
        periwinkle = Color(0xFF79A4FD),
        periwinkleStrong = Color(0xFF99C0F7),
        paleBlue = Color(0xFFDFECFE),
        paleBlueAlt = Color(0xFFD9E7FD)
    )

    val protanopia = SentinelPalette(
        blueBg = Color(0xFF007AFF),
        lightBlueCard = Color(0xFF0070E6),
        darkBlue = Color(0xFF0052BD),
        yellow = Color(0xFFFFF100),
        teal = Color(0xFF0C8CE9),
        tealDark = Color(0xFF0A4C99),
        tealDarker = Color(0xFF062B5C),
        aqua = Color(0xFFFFF100),
        mint = Color(0xFFFFF100),
        riskRed = Color(0xFF5C532F),
        riskPink = Color(0xFFDBDCDF),
        periwinkle = Color(0xFF81ADFF),
        periwinkleStrong = Color(0xFF99C7F9),
        paleBlue = Color(0xFFEAF4FF),
        paleBlueAlt = Color(0xFFDCEBFF)
    )

    val tritanopia = SentinelPalette(
        blueBg = Color(0xFF0088A6),
        lightBlueCard = Color(0xFF007E99),
        darkBlue = Color(0xFF00657D),
        yellow = Color(0xFFFFEBD8),
        teal = Color(0xFF04E9CA),
        tealDark = Color(0xFF0B6458),
        tealDarker = Color(0xFF0B3B36),
        aqua = Color(0xFF00EDE0),
        mint = Color(0xFF00EDE0),
        riskRed = Color(0xFFF6001D),
        riskPink = Color(0xFFFFD4D9),
        periwinkle = Color(0xFF71B5C4),
        periwinkleStrong = Color(0xFF99CCD7),
        paleBlue = Color(0xFFE7F4F6),
        paleBlueAlt = Color(0xFFDFF0F4)
    )

    private val highContrastPalettes = ColorBlindMode.entries.associateWith { forMode(it).withHighContrast() }

    private fun forMode(mode: ColorBlindMode): SentinelPalette = when (mode) {
        ColorBlindMode.STANDARD -> standard
        ColorBlindMode.DEUTERANOPIA -> deuteranopia
        ColorBlindMode.PROTANOPIA -> protanopia
        ColorBlindMode.TRITANOPIA -> tritanopia
    }

    val current: SentinelPalette
        get() {
            val mode = AccessibilityStore.paletteMode
            return if (AccessibilityStore.highContrastActive) {
                highContrastPalettes.getValue(mode)
            } else {
                forMode(mode)
            }
        }
}

private const val DARKEN_BACKGROUND = 0.4f
private const val DARKEN_CARD = 0.45f
private const val DARKEN_BUTTON = 0.35f
private const val DARKEN_ALERT = 0.3f
private const val DARKEN_TEAL = 0.25f
private const val LIGHTEN_TEXT = 0.6f

private fun SentinelPalette.withHighContrast() = copy(
    blueBg = lerp(blueBg, Color.Black, DARKEN_BACKGROUND),
    lightBlueCard = lerp(lightBlueCard, Color.Black, DARKEN_CARD),
    darkBlue = lerp(darkBlue, Color.Black, DARKEN_BUTTON),
    riskRed = lerp(riskRed, Color.Black, DARKEN_ALERT),
    tealDark = lerp(tealDark, Color.Black, DARKEN_TEAL),
    periwinkleStrong = lerp(periwinkleStrong, Color.White, LIGHTEN_TEXT)
)
