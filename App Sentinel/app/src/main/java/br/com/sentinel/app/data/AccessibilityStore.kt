package br.com.sentinel.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ColorBlindMode { STANDARD, DEUTERANOPIA, PROTANOPIA, TRITANOPIA }

data class AccessibilityState(
    val colorBlindMode: ColorBlindMode = ColorBlindMode.STANDARD,
    val highContrast: Boolean = false,
    val textSize: Float = DEFAULT_TEXT_SIZE,
    val readAloud: Boolean = false,
    val reduceAnimations: Boolean = false
) {
    val fontScale: Float get() = MIN_FONT_SCALE + FONT_SCALE_RANGE * textSize

    companion object {
        const val DEFAULT_TEXT_SIZE = 0.5f
        private const val MIN_FONT_SCALE = 0.8f
        private const val FONT_SCALE_RANGE = 0.4f
    }
}

object AccessibilityStore {
    private const val PREFS = "sentinel_accessibility"
    private const val KEY_MODE = "color_blind_mode"
    private const val KEY_CONTRAST = "high_contrast"
    private const val KEY_TEXT_SIZE = "text_size"
    private const val KEY_READ_ALOUD = "read_aloud"
    private const val KEY_REDUCE = "reduce_animations"

    private var prefs: SharedPreferences? = null
    private val mutableState = MutableStateFlow(AccessibilityState())
    val state: StateFlow<AccessibilityState> = mutableState.asStateFlow()

    var paletteMode: ColorBlindMode by mutableStateOf(ColorBlindMode.STANDARD)
        private set

    var highContrastActive: Boolean by mutableStateOf(false)
        private set

    fun init(context: Context) {
        if (prefs != null) return
        val stored = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = stored
        mutableState.value = AccessibilityState(
            colorBlindMode = ColorBlindMode.entries.firstOrNull { it.name == stored.getString(KEY_MODE, null) }
                ?: ColorBlindMode.STANDARD,
            highContrast = stored.getBoolean(KEY_CONTRAST, false),
            textSize = stored.getFloat(KEY_TEXT_SIZE, AccessibilityState.DEFAULT_TEXT_SIZE),
            readAloud = stored.getBoolean(KEY_READ_ALOUD, false),
            reduceAnimations = stored.getBoolean(KEY_REDUCE, false)
        )
        paletteMode = mutableState.value.colorBlindMode
        highContrastActive = mutableState.value.highContrast
    }

    fun update(transform: (AccessibilityState) -> AccessibilityState) {
        val next = transform(mutableState.value)
        mutableState.value = next
        paletteMode = next.colorBlindMode
        highContrastActive = next.highContrast
        prefs?.edit()
            ?.putString(KEY_MODE, next.colorBlindMode.name)
            ?.putBoolean(KEY_CONTRAST, next.highContrast)
            ?.putFloat(KEY_TEXT_SIZE, next.textSize)
            ?.putBoolean(KEY_READ_ALOUD, next.readAloud)
            ?.putBoolean(KEY_REDUCE, next.reduceAnimations)
            ?.apply()
    }
}
