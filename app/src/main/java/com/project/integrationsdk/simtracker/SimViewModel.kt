package com.project.integrationsdk.simtracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.integrationsdk.simtracker.CleverTapManager
import com.project.integrationsdk.simtracker.SimClassification
import com.project.integrationsdk.simtracker.SimClassifier
import com.project.integrationsdk.simtracker.SimReader
import com.project.integrationsdk.simtracker.SimRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SimViewModel(application: Application) : AndroidViewModel(application) {

    private val _simClassifications = MutableLiveData<List<SimClassification>>()
    val simClassifications: LiveData<List<SimClassification>> = _simClassifications

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Stable session ID for this app launch
    private val sessionId: String = UUID.randomUUID().toString().take(8)

    /**
     * Main entry point called from MainActivity after permission is granted.
     * Reads SIMs → classifies → pushes to CleverTap.
     */
    fun loadAndTrackSims() {
        viewModelScope.launch {
            _isLoading.value = true

            val context = getApplication<Application>()

            val rawSims = withContext(Dispatchers.IO) {
                SimReader.readSims(context)
            }

            if (rawSims.isEmpty()) {
                _statusMessage.value = "No active SIM cards found or permission denied."
                _isLoading.value = false
                return@launch
            }

            val classifications = SimClassifier.classify(rawSims)
            _simClassifications.value = classifications

            // ── CleverTap pushes ──────────────────────────────────────────
            withContext(Dispatchers.IO) {
                // 1. Update user properties (profile)
                CleverTapManager.pushSimUserProperties(context, classifications)

                // 2. Fire "SIM Detected" event for each slot
                CleverTapManager.pushSimDetectedEvents(context, classifications)

                // 3. Fire role-specific usage events
                val primary = classifications.firstOrNull { it.role == SimRole.PRIMARY }
                val competitor = classifications.firstOrNull {
                    it.role == SimRole.COMPETITOR && !it.simInfo.isRoaming
                }

                primary?.let {
                    CleverTapManager.pushPrimaryOperatorUsed(context, it.simInfo, sessionId)
                }

                competitor?.let {
                    CleverTapManager.pushCompetitorSimUsed(context, it.simInfo, sessionId)
                }

                // 4. App open with full SIM context
                CleverTapManager.pushAppOpenWithSimContext(context, classifications, sessionId)
            }

            _statusMessage.value = buildSummaryMessage(classifications)
            _isLoading.value = false
        }
    }

    private fun buildSummaryMessage(classifications: List<SimClassification>): String {
        val sb = StringBuilder("Session: $sessionId\n\n")
        classifications.forEach { c ->
            sb.append("• ${c.displayLabel}\n")
        }
        val competitor = classifications.any { it.role == SimRole.COMPETITOR && !it.simInfo.isRoaming }
        if (competitor) {
            sb.append("\n✅ Competitor SIM detected in secondary slot — event pushed to CleverTap.")
        } else {
            sb.append("\nℹ️ No relevant competitor SIM in secondary slot.")
        }
        return sb.toString()
    }
}