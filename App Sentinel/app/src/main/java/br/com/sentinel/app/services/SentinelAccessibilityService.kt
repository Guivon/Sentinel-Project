package br.com.sentinel.app.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import br.com.sentinel.app.data.AlertStatus
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.domain.AutoAnalysis
import br.com.sentinel.app.domain.BehaviorMonitor
import br.com.sentinel.app.domain.RiskLevel
import br.com.sentinel.app.domain.ScamDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SentinelAccessibilityService : AccessibilityService() {

    private val analysisThread = HandlerThread("sentinel-tela").apply { start() }
    private val handler = Handler(analysisThread.looper)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var pending = false
    private val process = Runnable {
        pending = false
        analyzeScreen()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName != this.packageName) trackBehavior(event)
        if (ignore(packageName)) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            handler.post(process)
            return
        }
        if (!pending) {
            pending = true
            handler.postDelayed(process, DELAY_MS)
        }
    }

    private fun trackBehavior(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> BehaviorMonitor.onTyping()
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> BehaviorMonitor.onWindowChange()
            else -> return
        }
        val report = BehaviorMonitor.check() ?: return
        serviceScope.launch { AutoAnalysis.processBehavior(applicationContext, report) }
    }

    override fun onInterrupt() {
        handler.removeCallbacks(process)
    }

    override fun onDestroy() {
        handler.removeCallbacks(process)
        serviceScope.cancel()
        analysisThread.quitSafely()
        instance = null
        super.onDestroy()
    }

    private fun ignore(eventPackage: String) = eventPackage == packageName || eventPackage !in MONITORED_PACKAGES

    @Suppress("DEPRECATION")
    private fun analyzeScreen() {
        val root = rootInActiveWindow ?: return
        try {
            val packageName = root.packageName?.toString() ?: return
            if (ignore(packageName)) return

            val blocks = mutableListOf<String>()
            collect(root, blocks, intArrayOf(0))

            val suspiciousItems = mutableListOf<Pair<String, ScamDetector.DetectionResult>>()
            var analyzed = false
            for (block in blocks) {
                if (!SentinelState.repeated(block)) analyzed = true
                val result = ScamDetector.analyze(block)
                if (result.level != RiskLevel.SAFE && result.score >= MIN_SCREEN_SCORE) {
                    suspiciousItems.add(block to result)
                }
            }
            if (analyzed) SentinelState.recordAnalysis()
            BehaviorMonitor.onScreenText(blocks)

            val name = appName(packageName)
            suspiciousItems
                .sortedByDescending { it.second.score }
                .take(MAX_ALERTS_PER_SCREEN)
                .forEach { (block, result) ->
                    if (!SentinelState.canAlert("tela", block)) return@forEach
                    SentinelState.record(
                        "Tela • $name",
                        block,
                        result.reasons,
                        AlertStatus.CONFIRMED_LOCAL,
                        result.level
                    )
                    NotificationHelper.sendNotification(this, "$name (tela)", result.reasons, block)
                }
        } finally {
            root.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private fun collect(no: AccessibilityNodeInfo?, output: MutableList<String>, counter: IntArray) {
        if (no == null || counter[0] >= MAX_NODES || output.size >= MAX_BLOCKS) return
        counter[0]++
        if (!no.isVisibleToUser || no.isPassword) return

        if (!no.isEditable) {
            val content = no.text?.toString()?.trim()
            if (!content.isNullOrEmpty() && content.length >= MIN_SIZE) {
                output.add(content.take(MAX_SIZE))
            }
        }

        for (index in 0 until no.childCount) {
            val child = no.getChild(index) ?: continue
            collect(child, output, counter)
            child.recycle()
        }
    }

    private fun appName(packageName: String): String {
        KNOWN_NAMES[packageName]?.let { return it }
        return try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("Sentinel", "Unknown package $packageName: ${e.message}")
            packageName
        }
    }

    companion object {
        @Volatile
        private var instance: SentinelAccessibilityService? = null

        fun turnOff(): Boolean {
            val service = instance ?: return false
            service.disableSelf()
            return true
        }

        private const val DELAY_MS = 250L
        private const val MAX_NODES = 600
        private const val MAX_BLOCKS = 40
        private const val MIN_SIZE = 25
        private const val MAX_SIZE = 600
        private const val MAX_ALERTS_PER_SCREEN = 3
        private const val MIN_SCREEN_SCORE = 30

        private val KNOWN_NAMES = mapOf(
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "org.telegram.messenger" to "Telegram",
            "com.google.android.apps.messaging" to "Mensagens",
            "com.samsung.android.messaging" to "Mensagens",
            "com.google.android.gm" to "Gmail",
            "com.instagram.android" to "Instagram",
            "com.facebook.katana" to "Facebook",
            "com.facebook.orca" to "Messenger",
            "com.android.chrome" to "Chrome"
        )

        private val MONITORED_PACKAGES = KNOWN_NAMES.keys + setOf(
            "com.android.mms",
            "com.microsoft.emmx",
            "org.mozilla.firefox",
            "com.sec.android.app.sbrowser",
            "com.opera.browser"
        )
    }
}
