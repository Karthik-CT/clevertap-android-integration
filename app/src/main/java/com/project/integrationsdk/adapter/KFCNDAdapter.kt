package com.project.integrationsdk.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.integrationsdk.R
import com.project.integrationsdk.model.KFCNDModel

class KFCNDAdapter(
    private val items: List<KFCNDModel>,
    private val onAddToCart: (KFCNDModel) -> Unit,
    var context: Context
) : RecyclerView.Adapter<KFCNDAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.productImage)
        val title = view.findViewById<TextView>(R.id.productTitle)
        val desc = view.findViewById<TextView>(R.id.productDescription)
        val price = view.findViewById<TextView>(R.id.productPrice)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.kfc_native_display_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = items[position]

        Glide.with(context)
            .load(item.imageRes)
            .into(holder.img)
        holder.title.text = item.title
        holder.desc.text = item.description
        holder.price.text = item.price

        holder.btnAdd.setOnClickListener { onAddToCart(item) }
    }
}