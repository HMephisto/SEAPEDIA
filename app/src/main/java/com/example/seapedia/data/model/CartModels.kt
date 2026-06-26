package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class CartResponse(
    @SerializedName("store") val store: CartStore?,
    @SerializedName("items") val items: List<CartItem>,
    @SerializedName("total") val total: Long
)

data class CartStore(
    @SerializedName("id") val id: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("address_detail") val addressDetail: String?
)

data class CartItem(
    @SerializedName("id") val id: Int,
    @SerializedName("product_id") val productId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("price") val price: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("subtotal") val subtotal: Long
)

data class AddToCartRequest(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class UpdateCartRequest(
    @SerializedName("quantity") val quantity: Int
)