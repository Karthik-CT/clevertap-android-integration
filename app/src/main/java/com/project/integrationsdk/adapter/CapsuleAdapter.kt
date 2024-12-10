package com.project.integrationsdk.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.integrationsdk.model.CapsuleItem
import com.project.integrationsdk.R

class CapsuleAdapter(
    private val items: List<CapsuleItem>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    inner class CapsuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val capsuleView: View = view.findViewById(R.id.capsuleView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val item = items[position]
        holder.capsuleView.backgroundTintList = ColorStateList.valueOf(
            if (item.isSelected) Color.GREEN else Color.GRAY
        )
        holder.itemView.setOnClickListener { onItemClicked(position) }
    }

    override fun getItemCount() = items.size
}
