package com.project.integrationsdk.simtracker



/**
 * Represents information about a single SIM card.
 *
 * @param slotIndex     Physical SIM slot index (0 = SIM1, 1 = SIM2)
 * @param operatorName  Human-readable carrier name (e.g. "Jio", "Airtel")
 * @param mccMnc        MCC+MNC code string e.g. "40486" (used for roaming detection)
 * @param countryIso    ISO country code returned by TelephonyManager e.g. "in"
 * @param simState      Raw SIM state integer from TelephonyManager
 * @param isRoaming     True if this SIM is currently roaming
 */
data class SimInfo(
    val slotIndex: Int,
    val operatorName: String,
    val mccMnc: String,
    val countryIso: String,
    val simState: Int,
    val isRoaming: Boolean
) {
    val slotLabel: String get() = if (slotIndex == 0) "Primary SIM" else "Secondary SIM"
    val isActive: Boolean get() = simState == android.telephony.TelephonyManager.SIM_STATE_READY
}