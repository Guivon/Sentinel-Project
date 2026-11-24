package br.com.sentinel.app.ui.theme

import androidx.compose.ui.graphics.Color
import br.com.sentinel.app.data.AccessibilityStore

val SentinelYellow: Color
    get() = SentinelPalettes.current.yellow
val SentinelBlueBg: Color
    get() = SentinelPalettes.current.blueBg
val SentinelLightBlueCard: Color
    get() = SentinelPalettes.current.lightBlueCard
val SentinelDarkBlueButton: Color
    get() = SentinelPalettes.current.darkBlue
val SentinelTextDark = Color(0xFF111111)
val SentinelTextLight = Color(0xFFFFFFFF)
val SentinelRiskRed: Color
    get() = SentinelPalettes.current.riskRed
val SentinelTeal: Color
    get() = SentinelPalettes.current.teal

val SentinelSurfaceMuted = Color(0xFFF5F5F5)
val SentinelTextBody: Color
    get() = contrastColor(Color(0xFF444444), Color(0xFF1A1A1A))
val SentinelDivider: Color
    get() = contrastColor(Color(0xFFE3E3E3), Color(0xFF888888))
val SentinelWarningYellow = Color(0xFFE6C800)
val SentinelTealDark: Color
    get() = SentinelPalettes.current.tealDark
val SentinelTealDarker: Color
    get() = SentinelPalettes.current.tealDarker
val SentinelBackgroundLight = Color(0xFFF8F9FA)
val SentinelTextMuted: Color
    get() = contrastColor(Color(0xFF777777), Color(0xFF333333))
val SentinelTextSecondary: Color
    get() = contrastColor(Color(0xFF555555), Color(0xFF222222))
val SentinelWarningDark = Color(0xFF4A3F00)
val SentinelTextStrong: Color
    get() = contrastColor(Color(0xFF333333), Color(0xFF000000))
val SentinelMint: Color
    get() = SentinelPalettes.current.mint
val SentinelRiskPink: Color
    get() = SentinelPalettes.current.riskPink
val SentinelPaleBlue: Color
    get() = SentinelPalettes.current.paleBlue
val SentinelPaleBlueAlt: Color
    get() = SentinelPalettes.current.paleBlueAlt
val SentinelAmber = Color(0xFFE0A800)
val SentinelBorderLight: Color
    get() = contrastColor(Color(0xFFDDDDDD), Color(0xFF666666))
val SentinelDisabled: Color
    get() = contrastColor(Color(0xFFBDBDBD), Color(0xFF757575))
val SentinelPeriwinkle: Color
    get() = SentinelPalettes.current.periwinkle
val SentinelPeriwinkleStrong: Color
    get() = SentinelPalettes.current.periwinkleStrong
val SentinelTextHint: Color
    get() = contrastColor(Color(0xFF666666), Color(0xFF2B2B2B))
val SentinelWarningDarkest = Color(0xFF5A4A00)
val SentinelAqua: Color
    get() = SentinelPalettes.current.aqua
val SentinelMockBackground = Color(0xFFF2F2F2)
val SentinelMockAction = Color(0xFF1A56DB)

private fun contrastColor(normal: Color, strong: Color): Color =
    if (AccessibilityStore.highContrastActive) strong else normal
