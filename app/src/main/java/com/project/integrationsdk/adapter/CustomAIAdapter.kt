package com.project.integrationsdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.clevertap.android.sdk.inbox.CTInboxMessageContent
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.CustomAppInboxCardLayoutBinding

class CustomAIAdapter(var caiList: ArrayList<CTInboxMessage>, var context: Context, private val onItemClick: (CTInboxMessage) -> Unit) : RecyclerView.Adapter<CustomAIHolder>() {

    private var arrList: ArrayList<CTInboxMessage> = caiList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAIHolder {
        return CustomAIHolder(CustomAppInboxCardLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomAIHolder, position: Int) {
        val list = caiList[position]
        holder.binding.customAiMessage.text = list.inboxMessageContents[0].message
        holder.binding.customAiTitle.text = list.inboxMessageContents[0].title

        Glide
            .with(context)
            .load(list.inboxMessageContents[0].media)
            .into(holder.binding.customAiImage)
        print("KK Tags: ${list.inboxMessageContents[0].message} and ${list.inboxMessageContents[0].message}")
//        holder.binding.customAiLinks.text = list.inboxMessageContents[0].links
//
//        list.inboxMessageContents[0].links

        print("KK Tags: ${list.inboxMessageContents[0].links.toString()}")

        holder.bind(list, onItemClick)
    }


    override fun getItemCount() = caiList.size
}

class CustomAIHolder(val binding: CustomAppInboxCardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    private val cardView: CardView = itemView.findViewById(R.id.custom_ai_card)
    fun bind(msg: CTInboxMessage, onItemClick: (CTInboxMessage) -> Unit) {
        cardView.setOnClickListener {
            onItemClick(msg)
        }
    }
}
