package com.example.agrione.model.data

data class EcommItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val shortDesc: String = "",
    val longDesc: String = "",
    val howtouse: String = "",
    val delCharge: String = "",
    val rating: Float = 0f,
    val retailer: String = "",
    val availability: String = "",
    val type: String = "",
    val attributes: Map<String, Any> = mapOf()
) 