package com.project.integrationsdk.model

sealed class RecyclerViewItem {
    data class Merchant(val imageUrl: String, val name: String) : RecyclerViewItem()
    data class Restaurant(
        val imageUrl: String,
        val name: String,
        val rating: String,
        val distance: String,
        val offer: String
    ) : RecyclerViewItem()

    data class Carousel(val imageUrl: String, val title: String, val subtitle: String) :
        RecyclerViewItem()
//
    data class Capsule(val isSelected: Boolean) :
        RecyclerViewItem()
}
