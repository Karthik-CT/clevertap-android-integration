package com.project.integrationsdk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.integrationsdk.model.CarouselItem
import com.project.integrationsdk.R

class CarouselAdapter(private val carouselItems: List<CarouselItem>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val currentItem = carouselItems[position]
        holder.title.text = currentItem.title
        holder.subtitle.text = currentItem.subtitle

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return carouselItems.size
    }

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageView)
        var title: TextView = itemView.findViewById<TextView>(R.id.title)
        var subtitle: TextView = itemView.findViewById<TextView>(R.id.subtitle)
    }
}

