package br.com.sentinel.app.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import br.com.sentinel.app.R
import br.com.sentinel.app.data.AlertStatus
import br.com.sentinel.app.data.SentinelState
import br.com.sentinel.app.services.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object CallAnalyzer {

    private const val TAG = "Sentinel"
    private const val BRAZIL_PREFIX = "+55"
    private const val TELEMARKETING_PREFIX = "0303"
    private const val NOTIFY_THRESHOLD = 30
    private const val SUSPICIOUS_THRESHOLD = 65
    private const val WEIGHT_HIDDEN = 35
    private const val WEIGHT_SPOOFED = 70
    private const val WEIGHT_INTERNATIONAL = 30
    private const val WEIGHT_TELEMARKETING = 20
    private const val WEIGHT_UNKNOWN = 10

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun screen(context: Context, number: String?, verificationFailed: Boolean) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                process(appContext, number, verificationFailed)
            } catch (e: Exception) {
                Log.e(TAG, "Call analysis failed: ${e.message}")
            }
        }
    }

    private fun process(context: Context, number: String?, verificationFailed: Boolean) {
        val hidden = number.isNullOrBlank()
        val institution = if (hidden) null else OfficialContacts.identify(number.orEmpty())
        val known = if (hidden) false else isInContacts(context, number.orEmpty())
        if (known == true) return

        val signs = collectSigns(context, number, institution, known, verificationFailed)

        SentinelState.recordAnalysis()
        val score = signs.sumOf { it.second }.coerceAtMost(100)
        val level = when {
            score >= SUSPICIOUS_THRESHOLD -> RiskLevel.SUSPICIOUS
            score >= NOTIFY_THRESHOLD -> RiskLevel.DOUBTFUL
            else -> RiskLevel.SAFE
        }
        val source = if (hidden) {
            context.getString(R.string.call_source_hidden)
        } else {
            context.getString(R.string.call_source, number)
        }
        val snippet = if (hidden) {
            context.getString(R.string.call_snippet_hidden)
        } else {
            context.getString(R.string.call_snippet, number)
        }

        if (level == RiskLevel.SAFE) {
            if (institution != null) {
                SentinelState.record(
                    source,
                    snippet,
                    listOf(context.getString(R.string.call_official_claim, institution)),
                    AlertStatus.OFFICIAL
                )
                NotificationHelper.sendOfficialNotification(context, source, institution)
            }
            return
        }

        val reasons = signs.map { it.first }
        SentinelState.record(source, snippet, reasons, AlertStatus.CONFIRMED_LOCAL, level)
        NotificationHelper.sendNotification(context, source, reasons, snippet)
    }

    private fun collectSigns(
        context: Context,
        number: String?,
        institution: String?,
        known: Boolean?,
        verificationFailed: Boolean
    ): List<Pair<String, Int>> {
        val signs = mutableListOf<Pair<String, Int>>()
        val hidden = number.isNullOrBlank()
        if (hidden) signs += context.getString(R.string.call_sign_hidden) to WEIGHT_HIDDEN
        if (verificationFailed) {
            val text = if (institution != null) {
                context.getString(R.string.call_sign_claims_official, institution)
            } else {
                context.getString(R.string.call_sign_spoofed)
            }
            signs += text to WEIGHT_SPOOFED
        }
        if (!hidden && institution == null) {
            val digits = number.orEmpty().trim()
            if (digits.startsWith("+") && !digits.startsWith(BRAZIL_PREFIX)) {
                signs += context.getString(R.string.call_sign_international) to WEIGHT_INTERNATIONAL
            }
            if (digits.filter { it.isDigit() }.startsWith(TELEMARKETING_PREFIX)) {
                signs += context.getString(R.string.call_sign_telemarketing) to WEIGHT_TELEMARKETING
            }
            if (known == false) signs += context.getString(R.string.call_sign_unknown) to WEIGHT_UNKNOWN
        }
        return signs
    }

    private fun isInContacts(context: Context, number: String): Boolean? {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null
            )?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Contact lookup failed: ${e.message}")
            null
        }
    }
}
