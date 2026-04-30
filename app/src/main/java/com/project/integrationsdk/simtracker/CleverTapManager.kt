package com.project.integrationsdk.simtracker

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.simtracker.SimInfo
import com.project.integrationsdk.simtracker.SimClassification
import com.project.integrationsdk.simtracker.SimRole

/**
 * Handles all CleverTap data pushes for the SIM-tracking use case.
 *
 * ─── USER PROPERTIES (set once / updated on change) ──────────────────────
 *  sim_primary_operator         – e.g. "Jio"
 *  sim_primary_mccmnc           – e.g. "40486"
 *  sim_primary_country          – e.g. "in"
 *  sim_primary_roaming          – true/false
 *
 *  sim_secondary_operator       – e.g. "Airtel"  (blank if no relevant secondary)
 *  sim_secondary_mccmnc         – e.g. "40410"
 *  sim_secondary_country        – e.g. "in"
 *  sim_secondary_roaming        – true/false
 *  sim_secondary_is_competitor  – true/false
 *
 *  sim_dual_sim_device          – true/false
 *  sim_competitor_in_secondary  – true/false   (convenience flag for segmentation)
 *
 * ─── EVENTS ──────────────────────────────────────────────────────────────
 *  Event: "SIM Detected"
 *    Properties: slot, operator, mccmnc, role, roaming, country
 *
 *  Event: "Primary Operator Used"
 *    Properties: operator, mccmnc, slot, session_id
 *
 *  Event: "Competitor SIM Used"   ← the key counter event
 *    Properties: operator, mccmnc, slot, country, session_id
 *    (Only fired when a non-roaming competitor SIM is in the secondary slot)
 * ─────────────────────────────────────────────────────────────────────────
 */
object CleverTapManager {

    // ── Push User Properties ──────────────────────────────────────────────

    fun pushSimUserProperties(
        context: Context,
        classifications: List<SimClassification>
    ) {
        val ct = CleverTapAPI.getDefaultInstance(context) ?: return

        val profile = HashMap<String, Any>()

        val primary = classifications.firstOrNull { it.role == SimRole.PRIMARY }
        val competitor = classifications.firstOrNull { it.role == SimRole.COMPETITOR }

        // Primary SIM properties
        if (primary != null) {
            profile["sim_primary_operator"] = primary.simInfo.operatorName.ifBlank { "Unknown" }
            profile["sim_primary_mccmnc"]   = primary.simInfo.mccMnc
            profile["sim_primary_country"]  = primary.simInfo.countryIso.uppercase()
            profile["sim_primary_roaming"]  = primary.simInfo.isRoaming
        }

        // Secondary SIM properties
        // Only set if there is a non-roaming competitor in any secondary slot
        if (competitor != null && !competitor.simInfo.isRoaming) {
            profile["sim_secondary_operator"]      = competitor.simInfo.operatorName.ifBlank { "Unknown" }
            profile["sim_secondary_mccmnc"]        = competitor.simInfo.mccMnc
            profile["sim_secondary_country"]       = competitor.simInfo.countryIso.uppercase()
            profile["sim_secondary_roaming"]       = false
            profile["sim_secondary_is_competitor"] = true
        } else {
            // Clear secondary props if no relevant competitor SIM present
            profile["sim_secondary_operator"]      = ""
            profile["sim_secondary_mccmnc"]        = ""
            profile["sim_secondary_is_competitor"] = false
        }

        // Convenience flags for CleverTap segmentation
        profile["sim_dual_sim_device"]         = classifications.size > 1
        profile["sim_competitor_in_secondary"] = competitor != null && !competitor.simInfo.isRoaming

        ct.pushProfile(profile)
    }

    // ── Push SIM Detected Events ──────────────────────────────────────────

    fun pushSimDetectedEvents(
        context: Context,
        classifications: List<SimClassification>
    ) {
        val ct = CleverTapAPI.getDefaultInstance(context) ?: return

        classifications.forEach { classification ->
            val props = HashMap<String, Any>()
            props["slot"]       = classification.simInfo.slotLabel
            props["operator"]   = classification.simInfo.operatorName.ifBlank { "Unknown" }
            props["mccmnc"]     = classification.simInfo.mccMnc
            props["role"]       = classification.simInfo.role(classification.role)
            props["roaming"]    = classification.simInfo.isRoaming
            props["country"]    = classification.simInfo.countryIso.uppercase()

            ct.pushEvent("SIM Detected", props)
        }
    }

    // ── Push Primary Operator Used Event ─────────────────────────────────

    fun pushPrimaryOperatorUsed(context: Context, simInfo: SimInfo, sessionId: String) {
        val ct = CleverTapAPI.getDefaultInstance(context) ?: return

        val props = HashMap<String, Any>()
        props["operator"]   = simInfo.operatorName.ifBlank { "Unknown" }
        props["mccmnc"]     = simInfo.mccMnc
        props["slot"]       = simInfo.slotLabel
        props["session_id"] = sessionId

        ct.pushEvent("Primary Operator Used", props)
    }

    // ── Push Competitor SIM Used Event ────────────────────────────────────
    // This is the key event that tracks "how many times competitor used as secondary"

    fun pushCompetitorSimUsed(context: Context, simInfo: SimInfo, sessionId: String) {
        val ct = CleverTapAPI.getDefaultInstance(context) ?: return

        // Guard: never fire if roaming
        if (simInfo.isRoaming) return

        val props = HashMap<String, Any>()
        props["operator"]   = simInfo.operatorName.ifBlank { "Unknown" }
        props["mccmnc"]     = simInfo.mccMnc
        props["slot"]       = simInfo.slotLabel
        props["country"]    = simInfo.countryIso.uppercase()
        props["session_id"] = sessionId

        ct.pushEvent("Competitor SIM Used", props)
    }

    // ── Convenience: Push App Open with SIM context ───────────────────────

    fun pushAppOpenWithSimContext(
        context: Context,
        classifications: List<SimClassification>,
        sessionId: String
    ) {
        val ct = CleverTapAPI.getDefaultInstance(context) ?: return

        val primary    = classifications.firstOrNull { it.role == SimRole.PRIMARY }
        val competitor = classifications.firstOrNull {
            it.role == SimRole.COMPETITOR && !it.simInfo.isRoaming
        }

        val props = HashMap<String, Any>()
        props["session_id"]              = sessionId
        props["primary_operator"]        = primary?.simInfo?.operatorName ?: "None"
        props["competitor_in_secondary"] = competitor != null
        props["competitor_operator"]     = competitor?.simInfo?.operatorName ?: "None"
        props["total_sims_detected"]     = classifications.size

        ct.pushEvent("App Opened", props)
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private fun SimInfo.role(simRole: SimRole): String = when (simRole) {
        SimRole.PRIMARY    -> "primary"
        SimRole.COMPETITOR -> "competitor"
        SimRole.ROAMING    -> "roaming"
        SimRole.OTHER      -> "other"
    }
}