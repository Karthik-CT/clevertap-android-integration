package com.project.integrationsdk.simtracker

import com.project.integrationsdk.simtracker.OperatorConfig
import com.project.integrationsdk.simtracker.SimInfo

/**
 * Classifies each detected SIM card as:
 *   PRIMARY   – belongs to our primary operator (Jio in POC / Ooredoo in production)
 *   COMPETITOR – a known competitor SIM (not roaming)
 *   OTHER      – unrecognised operator
 *   ROAMING    – any SIM that is currently roaming (excluded from counts)
 */
enum class SimRole { PRIMARY, COMPETITOR, OTHER, ROAMING }

data class SimClassification(
    val simInfo: SimInfo,
    val role: SimRole,
    val displayLabel: String
)

object SimClassifier {

    fun classify(sims: List<SimInfo>): List<SimClassification> {
        return sims.map { sim ->
            val role = determineRole(sim)
            SimClassification(
                simInfo = sim,
                role = role,
                displayLabel = buildLabel(sim, role)
            )
        }
    }

    private fun determineRole(sim: SimInfo): SimRole {
        // 1. Roaming check — always wins, excluded from usage counts
        if (sim.isRoaming) return SimRole.ROAMING

        // 2. Inactive / no SIM
        if (!sim.isActive) return SimRole.OTHER

        val normalizedName = sim.operatorName.trim().lowercase()
        val mccMnc = sim.mccMnc.trim()

        // 3. Primary operator match (MCC+MNC preferred, name fallback)
        val isPrimary = OperatorConfig.activePrimaryMccMnc.contains(mccMnc)
                || OperatorConfig.activePrimaryNames.any { normalizedName.contains(it) }
        if (isPrimary) return SimRole.PRIMARY

        // 4. Competitor match
        val isCompetitor = OperatorConfig.activeCompetitorMccMnc.contains(mccMnc)
                || OperatorConfig.activeCompetitorNames.any { normalizedName.contains(it) }
        if (isCompetitor) return SimRole.COMPETITOR

        return SimRole.OTHER
    }

    private fun buildLabel(sim: SimInfo, role: SimRole): String {
        val slotLabel = sim.slotLabel
        val opLabel = sim.operatorName.ifBlank { "Unknown" }
        return when (role) {
            SimRole.PRIMARY    -> "$slotLabel: $opLabel (Primary)"
            SimRole.COMPETITOR -> "$slotLabel: $opLabel (Competitor)"
            SimRole.ROAMING    -> "$slotLabel: $opLabel (Roaming – excluded)"
            SimRole.OTHER      -> "$slotLabel: $opLabel (Other)"
        }
    }
}