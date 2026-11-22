package br.com.sentinel.app.services

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import br.com.sentinel.app.domain.CallAnalyzer

class SentinelCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())

        val incoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        if (!incoming) return

        val number = callDetails.handle?.schemeSpecificPart
        val verificationFailed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED
        CallAnalyzer.screen(this, number, verificationFailed)
    }
}
