package com.project.integrationsdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.project.integrationsdk.databinding.CustomAppInboxCardLayoutBinding

class CustomAIAdapter(var caiList: ArrayList<CTInboxMessage>, var context: Context) : RecyclerView.Adapter<CustomAIHolder>() {

    private var arrList: ArrayList<CTInboxMessage> = caiList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAIHolder {
        return CustomAIHolder(CustomAppInboxCardLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomAIHolder, position: Int) {
        val list = caiList[position]
        holder.binding.customAiMessage.text = list.inboxMessageContents[0].message
        holder.binding.customAiTitle.text = list.inboxMessageContents[0].title
    }

    override fun getItemCount() = caiList.size
}

class CustomAIHolder(val binding: CustomAppInboxCardLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
}
