package com.project.agrione.utilities

interface CartItemBuy {
    fun addToOrders(productId: String, quantity: Int, itemCost: Int, deliveryCost: Int)
}
