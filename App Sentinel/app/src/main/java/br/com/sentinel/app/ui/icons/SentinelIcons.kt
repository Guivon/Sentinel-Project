package br.com.sentinel.app.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val ICON_SIZE = 24f

private fun buildIcon(name: String, autoMirror: Boolean, vararg pathData: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = ICON_SIZE.dp,
        defaultHeight = ICON_SIZE.dp,
        viewportWidth = ICON_SIZE,
        viewportHeight = ICON_SIZE,
        autoMirror = autoMirror
    )
    pathData.forEach { builder.addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    return builder.build()
}

private val filledAccessibility: ImageVector by lazy {
    buildIcon(
        "Accessibility",
        false,
        "M12 2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm9 7h-6v13h-2v-6h-2v6H9V9H3V7h18v2z"
    )
}

private val filledAccountBalance: ImageVector by lazy {
    buildIcon(
        "AccountBalance",
        false,
        "M4 10h3v7H4zm6.5 0h3v7h-3zM2 19h20v3H2zm15-9h3v7h-3zm-5-9L2 6v2h20V6z"
    )
}

private val filledAdminPanelSettings: ImageVector by lazy {
    buildIcon(
        "AdminPanelSettings",
        false,
        "M17 11c.34 0 .67.04 1 .09V6.27L10.5 3 3 6.27v4.91c0 4.54 3.2 8.79 7.5 9.82.55-.13 " +
            "1.08-.32 1.6-.55-.69-.98-1.1-2.17-1.1-3.45 0-3.31 2.69-6 6-6z",
        "M17 13c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 1.38c.62 0 1.12.51 " +
            "1.12 1.12s-.51 1.12-1.12 1.12-1.12-.51-1.12-1.12.5-1.12 1.12-1.12zm0 5.37c-.93 " +
            "0-1.74-.46-2.24-1.17.05-.72 1.51-1.08 2.24-1.08s2.19.36 2.24 1.08c-.5.71-1.31 " +
            "1.17-2.24 1.17z"
    )
}

private val filledAttachFile: ImageVector by lazy {
    buildIcon(
        "AttachFile",
        false,
        "M16.5 6v11.5c0 2.21-1.79 4-4 4s-4-1.79-4-4V5a2.5 2.5 0 0 1 5 0v10.5c0 .55-.45 1-1 " +
            "1s-1-.45-1-1V6H10v9.5a2.5 2.5 0 0 0 5 0V5c0-2.21-1.79-4-4-4S7 2.79 7 5v12.5c0 3.04 " +
            "2.46 5.5 5.5 5.5s5.5-2.46 5.5-5.5V6h-1.5z"
    )
}

private val filledCampaign: ImageVector by lazy {
    buildIcon(
        "Campaign",
        false,
        "M18 11v2h4v-2h-4zm-2 6.61c.96.71 2.21 1.65 3.2 2.39.4-.53.8-1.07 " +
            "1.2-1.6-.99-.74-2.24-1.68-3.2-2.4-.4.54-.8 1.08-1.2 1.61zM20.4 " +
            "5.6c-.4-.53-.8-1.07-1.2-1.6-.99.74-2.24 1.68-3.2 2.4.4.53.8 1.07 1.2 1.6.96-.72 " +
            "2.21-1.65 3.2-2.4zM4 9c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h1v4h2v-4h1l5 3V6L8 9H4zm11.5 " +
            "3c0-1.33-.58-2.53-1.5-3.35v6.69c.92-.81 1.5-2.01 1.5-3.34z"
    )
}

private val filledCardGiftcard: ImageVector by lazy {
    buildIcon(
        "CardGiftcard",
        false,
        "M20 6h-2.18c.11-.31.18-.65.18-1a2.996 2.996 0 0 0-5.5-1.65l-.5.67-.5-.68C10.96 2.54 " +
            "10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 " +
            "1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 " +
            "1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 " +
            "15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 11 8.76l1-1.36 1 1.36L15.38 12 17 10.83 " +
            "14.92 8H20v6z"
    )
}

private val filledChevronLeft: ImageVector by lazy {
    buildIcon(
        "ChevronLeft",
        false,
        "M15.41 7.41 14 6l-6 6 6 6 1.41-1.41L10.83 12z"
    )
}

private val filledChevronRight: ImageVector by lazy {
    buildIcon(
        "ChevronRight",
        false,
        "M10 6 8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"
    )
}

private val filledDownload: ImageVector by lazy {
    buildIcon(
        "Download",
        false,
        "M5 20h14v-2H5v2zM19 9h-4V3H9v6H5l7 7 7-7z"
    )
}

private val filledError: ImageVector by lazy {
    buildIcon(
        "Error",
        false,
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 " +
            "15h-2v-2h2v2zm0-4h-2V7h2v6z"
    )
}

private val filledEventBusy: ImageVector by lazy {
    buildIcon(
        "EventBusy",
        false,
        "m9.31 17 2.44-2.44L14.19 17l1.06-1.06-2.44-2.44 2.44-2.44L14.19 10l-2.44 2.44L9.31 " +
            "10l-1.06 1.06 2.44 2.44-2.44 2.44L9.31 17zM19 3h-1V1h-2v2H8V1H6v2H5c-1.11 " +
            "0-1.99.9-1.99 2L3 19a2 2 0 0 0 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 " +
            "16H5V8h14v11z"
    )
}

private val filledExpandLess: ImageVector by lazy {
    buildIcon(
        "ExpandLess",
        false,
        "m12 8-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"
    )
}

private val filledExpandMore: ImageVector by lazy {
    buildIcon(
        "ExpandMore",
        false,
        "M16.59 8.59 12 13.17 7.41 8.59 6 10l6 6 6-6z"
    )
}

private val filledFingerprint: ImageVector by lazy {
    buildIcon(
        "Fingerprint",
        false,
        "M17.81 4.47c-.08 0-.16-.02-.23-.06C15.66 3.42 14 3 12.01 3c-1.98 0-3.86.47-5.57 " +
            "1.41-.24.13-.54.04-.68-.2a.506.506 0 0 1 .2-.68C7.82 2.52 9.86 2 12.01 2c2.13 0 " +
            "3.99.47 6.03 1.52.25.13.34.43.21.67a.49.49 0 0 1-.44.28zM3.5 9.72a.499.499 0 0 " +
            "1-.41-.79c.99-1.4 2.25-2.5 3.75-3.27C9.98 4.04 14 4.03 17.15 5.65c1.5.77 2.76 1.86 " +
            "3.75 3.25a.5.5 0 0 1-.12.7c-.23.16-.54.11-.7-.12a9.388 9.388 0 0 " +
            "0-3.39-2.94c-2.87-1.47-6.54-1.47-9.4.01-1.36.7-2.5 1.7-3.4 " +
            "2.96-.08.14-.23.21-.39.21zm6.25 12.07a.47.47 0 0 " +
            "1-.35-.15c-.87-.87-1.34-1.43-2.01-2.64-.69-1.23-1.05-2.73-1.05-4.34 0-2.97 2.54-5.39 " +
            "5.66-5.39s5.66 2.42 5.66 5.39c0 " +
            ".28-.22.5-.5.5s-.5-.22-.5-.5c0-2.42-2.09-4.39-4.66-4.39-2.57 0-4.66 1.97-4.66 4.39 0 " +
            "1.44.32 2.77.93 3.85.64 1.15 1.08 1.64 1.85 2.42.19.2.19.51 0 " +
            ".71-.11.1-.24.15-.37.15zm7.17-1.85c-1.19 " +
            "0-2.24-.3-3.1-.89-1.49-1.01-2.38-2.65-2.38-4.39 0-.28.22-.5.5-.5s.5.22.5.5c0 1.41.72 " +
            "2.74 1.94 3.56.71.48 1.54.71 2.54.71.24 0 .64-.03 " +
            "1.04-.1.27-.05.53.13.58.41.05.27-.13.53-.41.58-.57.11-1.07.12-1.21.12zM14.91 22c-.04 " +
            "0-.09-.01-.13-.02-1.59-.44-2.63-1.03-3.72-2.1a7.297 7.297 0 0 1-2.17-5.22c0-1.62 " +
            "1.38-2.94 3.08-2.94 1.7 0 3.08 1.32 3.08 2.94 0 1.07.93 1.94 2.08 1.94s2.08-.87 " +
            "2.08-1.94c0-3.77-3.25-6.83-7.25-6.83-2.84 0-5.44 1.58-6.61 4.03-.39.81-.59 1.76-.59 " +
            "2.8 0 .78.07 2.01.67 3.61.1.26-.03.55-.29.64-.26.1-.55-.04-.64-.29a11.14 11.14 0 0 " +
            "1-.73-3.96c0-1.2.23-2.29.68-3.24 1.33-2.79 4.28-4.6 7.51-4.6 4.55 0 8.25 3.51 8.25 " +
            "7.83 0 1.62-1.38 2.94-3.08 " +
            "2.94s-3.08-1.32-3.08-2.94c0-1.07-.93-1.94-2.08-1.94s-2.08.87-2.08 1.94c0 1.71.66 " +
            "3.31 1.87 4.51.95.94 1.86 1.46 3.27 1.85.27.07.42.35.35.61-.05.23-.26.38-.47.38z"
    )
}

private val filledGavel: ImageVector by lazy {
    buildIcon(
        "Gavel",
        false,
        "m5.25 8.069 2.83-2.827 14.134 14.15-2.83 2.827zm4.236-4.242L12.314.998l5.657 " +
            "5.656-2.828 2.83zM.999 12.315l2.828-2.829 5.657 5.657-2.828 2.828zM1 21h12v2H1z"
    )
}

private val filledGppMaybe: ImageVector by lazy {
    buildIcon(
        "GppMaybe",
        false,
        "M12 2 4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm1 " +
            "14h-2v-2h2v2zm0-4h-2V7h2v5z"
    )
}

private val filledHistory: ImageVector by lazy {
    buildIcon(
        "History",
        false,
        "M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 " +
            "7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 " +
            "5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"
    )
}

private val filledLink: ImageVector by lazy {
    buildIcon(
        "Link",
        false,
        "M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 " +
            "0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 " +
            "3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z"
    )
}

private val filledMenuBook: ImageVector by lazy {
    buildIcon(
        "MenuBook",
        false,
        "M21 5c-1.11-.35-2.33-.5-3.5-.5-1.95 0-4.05.4-5.5 1.5-1.45-1.1-3.55-1.5-5.5-1.5S2.45 " +
            "4.9 1 6v14.65c0 .25.25.5.5.5.1 0 .15-.05.25-.05C3.1 20.45 5.05 20 6.5 20c1.95 0 " +
            "4.05.4 5.5 1.5 1.35-.85 3.8-1.5 5.5-1.5 1.65 0 3.35.3 4.75 1.05.1.05.15.05.25.05.25 " +
            "0 .5-.25.5-.5V6c-.6-.45-1.25-.75-2-1zm0 13.5c-1.1-.35-2.3-.5-3.5-.5-1.7 " +
            "0-4.15.65-5.5 1.5V8c1.35-.85 3.8-1.5 5.5-1.5 1.2 0 2.4.15 3.5.5v11.5z",
        "M17.5 10.5c.88 0 1.73.09 2.5.26V9.24c-.79-.15-1.64-.24-2.5-.24-1.7 " +
            "0-3.24.29-4.5.83v1.66c1.13-.64 2.7-.99 4.5-.99zM13 12.49v1.66c1.13-.64 2.7-.99 " +
            "4.5-.99.88 0 1.73.09 2.5.26V11.9c-.79-.15-1.64-.24-2.5-.24-1.7 0-3.24.3-4.5.83zm4.5 " +
            "1.84c-1.7 0-3.24.29-4.5.83v1.66c1.13-.64 2.7-.99 4.5-.99.88 0 1.73.09 " +
            "2.5.26v-1.52c-.79-.16-1.64-.24-2.5-.24z"
    )
}

private val filledPalette: ImageVector by lazy {
    buildIcon(
        "Palette",
        false,
        "M12 2C6.49 2 2 6.49 2 12s4.49 10 10 10a2.5 2.5 0 0 0 " +
            "2.5-2.5c0-.61-.23-1.2-.64-1.67a.528.528 0 0 1-.13-.33c0-.28.22-.5.5-.5H16c3.31 0 " +
            "6-2.69 6-6 0-4.96-4.49-9-10-9zm5.5 11c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 " +
            "1.5 1.5-.67 1.5-1.5 1.5zm-3-4c-.83 0-1.5-.67-1.5-1.5S13.67 6 14.5 6s1.5.67 1.5 " +
            "1.5S15.33 9 14.5 9zM5 11.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5S7.33 13 6.5 13 5 " +
            "12.33 5 11.5zm6-4c0 .83-.67 1.5-1.5 1.5S8 8.33 8 7.5 8.67 6 9.5 6s1.5.67 1.5 1.5z"
    )
}

private val filledPayments: ImageVector by lazy {
    buildIcon(
        "Payments",
        false,
        "M19 14V6c0-1.1-.9-2-2-2H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h14c1.1 0 2-.9 " +
            "2-2zm-9-1c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm13-6v11c0 1.1-.9 2-2 " +
            "2H4v-2h17V7h2z"
    )
}

private val filledPersonOff: ImageVector by lazy {
    buildIcon(
        "PersonOff",
        false,
        "M8.65 5.82a3.999 3.999 0 1 1 5.53 5.53L8.65 5.82zM20 " +
            "17.17c-.02-1.1-.63-2.11-1.61-2.62-.54-.28-1.13-.54-1.77-.76L20 17.17zm1.19 4.02L2.81 " +
            "2.81 1.39 4.22l8.89 8.89c-1.81.23-3.39.79-4.67 1.45A2.97 2.97 0 0 0 4 " +
            "17.22V20h13.17l2.61 2.61 1.41-1.42z"
    )
}

private val filledPhotoCamera: ImageVector by lazy {
    buildIcon(
        "PhotoCamera",
        false,
        "M8.8 12a3.2 3.2 0 1 0 6.4 0a3.2 3.2 0 1 0 -6.4 0z",
        "M9 2 7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 " +
            "2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 " +
            "5-2.24 5-5 5z"
    )
}

private val filledRemoveRedEye: ImageVector by lazy {
    buildIcon(
        "RemoveRedEye",
        false,
        "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 " +
            "11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 " +
            "5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
    )
}

private val filledSchedule: ImageVector by lazy {
    buildIcon(
        "Schedule",
        false,
        "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 " +
            "2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z",
        "M12.5 7H11v6l5.25 3.15.75-1.23-4.5-2.67z"
    )
}

private val filledSecurity: ImageVector by lazy {
    buildIcon(
        "Security",
        false,
        "M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 " +
            "4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"
    )
}

private val filledShield: ImageVector by lazy {
    buildIcon(
        "Shield",
        false,
        "M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"
    )
}

private val filledSmartphone: ImageVector by lazy {
    buildIcon(
        "Smartphone",
        false,
        "M17 1.01 7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 " +
            "2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"
    )
}

private val filledSms: ImageVector by lazy {
    buildIcon(
        "Sms",
        false,
        "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM9 " +
            "11H7V9h2v2zm4 0h-2V9h2v2zm4 0h-2V9h2v2z"
    )
}

private val filledTouchApp: ImageVector by lazy {
    buildIcon(
        "TouchApp",
        false,
        "M9 11.24V7.5a2.5 2.5 0 0 1 5 0v3.74c1.21-.81 2-2.18 2-3.74C16 5.01 13.99 3 11.5 3S7 " +
            "5.01 7 7.5c0 1.56.79 2.93 2 3.74zm9.84 " +
            "4.63-4.54-2.26c-.17-.07-.35-.11-.54-.11H13v-6c0-.83-.67-1.5-1.5-1.5S10 6.67 10 " +
            "7.5v10.74c-3.6-.76-3.54-.75-3.67-.75-.31 0-.59.13-.79.33l-.79.8 4.94 " +
            "4.94c.27.27.65.44 1.06.44h6.79c.75 0 1.33-.55 " +
            "1.44-1.28l.75-5.27c.01-.07.02-.14.02-.2 0-.62-.38-1.16-.91-1.38z"
    )
}

private val filledVerifiedUser: ImageVector by lazy {
    buildIcon(
        "VerifiedUser",
        false,
        "M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16-4-4 " +
            "1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"
    )
}

private val filledVisibility: ImageVector by lazy {
    buildIcon(
        "Visibility",
        false,
        "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 " +
            "11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 " +
            "5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
    )
}

private val filledVisibilityOff: ImageVector by lazy {
    buildIcon(
        "VisibilityOff",
        false,
        "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 " +
            "3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 " +
            "12 7zM2 4.27l2.28 2.28.46.46A11.804 11.804 0 0 0 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 " +
            "3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 " +
            "1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 " +
            "1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78 3.15 " +
            "3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"
    )
}

private val filledVolumeUp: ImageVector by lazy {
    buildIcon(
        "VolumeUp",
        false,
        "M3 9v6h4l5 5V4L7 9H3zm13.5 3A4.5 4.5 0 0 0 14 7.97v8.05c1.48-.73 2.5-2.25 " +
            "2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 " +
            "7-8.77s-2.99-7.86-7-8.77z"
    )
}

private val filledVpnKey: ImageVector by lazy {
    buildIcon(
        "VpnKey",
        false,
        "M12.65 10A5.99 5.99 0 0 0 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6a5.99 5.99 0 0 0 " +
            "5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"
    )
}

private val outlinedCancel: ImageVector by lazy {
    buildIcon(
        "Cancel",
        false,
        "M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm0 18c-4.41 " +
            "0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm3.59-13L12 10.59 8.41 7 7 8.41 10.59 " +
            "12 7 15.59 8.41 17 12 13.41 15.59 17 17 15.59 13.41 12 17 8.41z"
    )
}

private val outlinedHistory: ImageVector by lazy {
    buildIcon(
        "History",
        false,
        "M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 " +
            "7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 " +
            "5v5l4.25 2.52.77-1.28-3.52-2.09V8z"
    )
}

private val outlinedShield: ImageVector by lazy {
    buildIcon(
        "Shield",
        false,
        "M12 2 4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3zm6 9.09c0 " +
            "4-2.55 7.7-6 8.83-3.45-1.13-6-4.82-6-8.83v-4.7l6-2.25 6 2.25v4.7z"
    )
}

private val outlinedStarBorder: ImageVector by lazy {
    buildIcon(
        "StarBorder",
        false,
        "m22 9.24-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 " +
            "21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 " +
            "4.38.38-3.32 2.88 1 4.28L12 15.4z"
    )
}

private val autoHelpOutline: ImageVector by lazy {
    buildIcon(
        "HelpOutline",
        true,
        "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 " +
            "2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 " +
            "4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"
    )
}

private val autoLogout: ImageVector by lazy {
    buildIcon(
        "Logout",
        true,
        "m17 7-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 " +
            "2v14c0 1.1.9 2 2 2h8v-2H4V5z"
    )
}

val Icons.Filled.Accessibility: ImageVector get() = filledAccessibility
val Icons.Filled.AccountBalance: ImageVector get() = filledAccountBalance
val Icons.Filled.AdminPanelSettings: ImageVector get() = filledAdminPanelSettings
val Icons.Filled.AttachFile: ImageVector get() = filledAttachFile
val Icons.Filled.Campaign: ImageVector get() = filledCampaign
val Icons.Filled.CardGiftcard: ImageVector get() = filledCardGiftcard
val Icons.Filled.ChevronLeft: ImageVector get() = filledChevronLeft
val Icons.Filled.ChevronRight: ImageVector get() = filledChevronRight
val Icons.Filled.Download: ImageVector get() = filledDownload
val Icons.Filled.Error: ImageVector get() = filledError
val Icons.Filled.EventBusy: ImageVector get() = filledEventBusy
val Icons.Filled.ExpandLess: ImageVector get() = filledExpandLess
val Icons.Filled.ExpandMore: ImageVector get() = filledExpandMore
val Icons.Filled.Fingerprint: ImageVector get() = filledFingerprint
val Icons.Filled.Gavel: ImageVector get() = filledGavel
val Icons.Filled.GppMaybe: ImageVector get() = filledGppMaybe
val Icons.Filled.History: ImageVector get() = filledHistory
val Icons.Filled.Link: ImageVector get() = filledLink
val Icons.Filled.MenuBook: ImageVector get() = filledMenuBook
val Icons.Filled.Palette: ImageVector get() = filledPalette
val Icons.Filled.Payments: ImageVector get() = filledPayments
val Icons.Filled.PersonOff: ImageVector get() = filledPersonOff
val Icons.Filled.PhotoCamera: ImageVector get() = filledPhotoCamera
val Icons.Filled.RemoveRedEye: ImageVector get() = filledRemoveRedEye
val Icons.Filled.Schedule: ImageVector get() = filledSchedule
val Icons.Filled.Security: ImageVector get() = filledSecurity
val Icons.Filled.Shield: ImageVector get() = filledShield
val Icons.Filled.Smartphone: ImageVector get() = filledSmartphone
val Icons.Filled.Sms: ImageVector get() = filledSms
val Icons.Filled.TouchApp: ImageVector get() = filledTouchApp
val Icons.Filled.VerifiedUser: ImageVector get() = filledVerifiedUser
val Icons.Filled.Visibility: ImageVector get() = filledVisibility
val Icons.Filled.VisibilityOff: ImageVector get() = filledVisibilityOff
val Icons.Filled.VolumeUp: ImageVector get() = filledVolumeUp
val Icons.Filled.VpnKey: ImageVector get() = filledVpnKey
val Icons.Outlined.Cancel: ImageVector get() = outlinedCancel
val Icons.Outlined.History: ImageVector get() = outlinedHistory
val Icons.Outlined.Shield: ImageVector get() = outlinedShield
val Icons.Outlined.StarBorder: ImageVector get() = outlinedStarBorder
val Icons.AutoMirrored.Filled.HelpOutline: ImageVector get() = autoHelpOutline
val Icons.AutoMirrored.Filled.Logout: ImageVector get() = autoLogout
