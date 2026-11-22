package br.com.sentinel.app.ui

import androidx.lifecycle.ViewModel
import br.com.sentinel.app.data.Alert
import br.com.sentinel.app.data.SentinelState
import kotlinx.coroutines.flow.StateFlow

class AlertsViewModel : ViewModel() {
    val alerts: StateFlow<List<Alert>> = SentinelState.alerts
    val totalAnalyzed: StateFlow<Int> = SentinelState.totalAnalyzed
    val scamsAvoided: StateFlow<Int> = SentinelState.scamsAvoided
}
