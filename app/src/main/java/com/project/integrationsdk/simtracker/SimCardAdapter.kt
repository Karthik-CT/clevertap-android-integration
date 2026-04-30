package com.project.integrationsdk.simtracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.integrationsdk.R
import com.project.integrationsdk.simtracker.SimClassification
import com.project.integrationsdk.simtracker.SimRole

class SimCardAdapter :
    ListAdapter<SimClassification, SimCardAdapter.SimViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sim_card, parent, false)
        return SimViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SimViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSlot: TextView        = itemView.findViewById(R.id.tvSlot)
        private val tvOperator: TextView    = itemView.findViewById(R.id.tvOperator)
        private val tvMccMnc: TextView      = itemView.findViewById(R.id.tvMccMnc)
        private val tvRole: TextView        = itemView.findViewById(R.id.tvRole)
        private val tvRoaming: TextView     = itemView.findViewById(R.id.tvRoaming)
        private val tvCountry: TextView     = itemView.findViewById(R.id.tvCountry)
        private val viewRoleIndicator: View = itemView.findViewById(R.id.viewRoleIndicator)

        fun bind(item: SimClassification) {
            tvSlot.text     = item.simInfo.slotLabel
            tvOperator.text = item.simInfo.operatorName.ifBlank { "Unknown Operator" }
            tvMccMnc.text   = "MCC+MNC: ${item.simInfo.mccMnc.ifBlank { "N/A" }}"
            tvCountry.text  = "Country: ${item.simInfo.countryIso.uppercase().ifBlank { "N/A" }}"
            tvRoaming.text  = if (item.simInfo.isRoaming) "🌍 Roaming" else "📍 Home Network"

            val (roleText, roleColor) = when (item.role) {
                SimRole.PRIMARY    -> "PRIMARY OPERATOR"      to 0xFF1A73E8.toInt()
                SimRole.COMPETITOR -> "COMPETITOR (Tracked)"  to 0xFFE53935.toInt()
                SimRole.ROAMING    -> "ROAMING (Excluded)"    to 0xFFFF8F00.toInt()
                SimRole.OTHER      -> "OTHER / UNKNOWN"       to 0xFF757575.toInt()
            }
            tvRole.text = roleText
            viewRoleIndicator.setBackgroundColor(roleColor)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SimClassification>() {
            override fun areItemsTheSame(a: SimClassification, b: SimClassification) =
                a.simInfo.slotIndex == b.simInfo.slotIndex

            override fun areContentsTheSame(a: SimClassification, b: SimClassification) =
                a == b
        }
    }
}