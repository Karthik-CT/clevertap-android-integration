package com.project.integrationsdk.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.integrationsdk.R
import com.project.integrationsdk.model.RecyclerViewItem

class GenericAdapter(
    private val items: List<RecyclerViewItem>,
    private val onClick: (RecyclerViewItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MERCHANT = 1
        private const val VIEW_TYPE_RESTAURANT = 2
        private const val VIEW_TYPE_CAROUSEL = 3
        private const val VIEW_TYPE_CAPSULE = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is RecyclerViewItem.Merchant -> VIEW_TYPE_MERCHANT
            is RecyclerViewItem.Restaurant -> VIEW_TYPE_RESTAURANT
            is RecyclerViewItem.Carousel -> VIEW_TYPE_CAROUSEL
            is RecyclerViewItem.Capsule -> VIEW_TYPE_CAPSULE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MERCHANT -> MerchantViewHolder(
                inflater.inflate(R.layout.merchants_recycler_item, parent, false)
            )
            VIEW_TYPE_RESTAURANT -> RestaurantViewHolder(
                inflater.inflate(R.layout.restaurant_carousel_item, parent, false)
            )
            VIEW_TYPE_CAROUSEL -> CarouselViewHolder(
                inflater.inflate(R.layout.carousel_item, parent, false)
            )
            VIEW_TYPE_CAPSULE -> CapsuleViewHolder(
                inflater.inflate(R.layout.item_capsule, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is MerchantViewHolder -> holder.bind(item as RecyclerViewItem.Merchant, onClick)
            is RestaurantViewHolder -> holder.bind(item as RecyclerViewItem.Restaurant, onClick)
            is CarouselViewHolder -> holder.bind(item as RecyclerViewItem.Carousel, onClick)
            is CapsuleViewHolder -> holder.bind(item as RecyclerViewItem.Capsule, onClick)
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder for Merchant
    class MerchantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: RecyclerViewItem.Merchant, onClick: (RecyclerViewItem) -> Unit) {
            itemView.findViewById<TextView>(R.id.merchant_name).text = item.name
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(itemView.findViewById(R.id.merchants_circular_image_view))
            itemView.setOnClickListener { onClick(item) }
        }
    }

    // ViewHolder for Restaurant
    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: RecyclerViewItem.Restaurant, onClick: (RecyclerViewItem) -> Unit) {
            itemView.findViewById<TextView>(R.id.item_title).text = item.name
            itemView.findViewById<TextView>(R.id.item_rating).text = item.rating
            itemView.findViewById<TextView>(R.id.km_value).text = item.distance
            itemView.findViewById<TextView>(R.id.item_offer).text = item.offer
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(itemView.findViewById(R.id.item_image))
            itemView.setOnClickListener { onClick(item) }
        }
    }

    // ViewHolder for Carousel
    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: RecyclerViewItem.Carousel, onClick: (RecyclerViewItem) -> Unit) {
            itemView.findViewById<TextView>(R.id.title).text = item.title
            itemView.findViewById<TextView>(R.id.subtitle).text = item.subtitle
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(itemView.findViewById(R.id.imageView))
            itemView.setOnClickListener { onClick(item) }
        }
    }

    // ViewHolder for Capsule
    class CapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: RecyclerViewItem.Capsule, onClick: (RecyclerViewItem) -> Unit) {
            val capsuleView: View = itemView.findViewById(R.id.capsuleView)
            capsuleView.backgroundTintList = ColorStateList.valueOf(
                if (item.isSelected) Color.GREEN else Color.GRAY
            )
            itemView.setOnClickListener { onClick(item) }
        }
    }
}

