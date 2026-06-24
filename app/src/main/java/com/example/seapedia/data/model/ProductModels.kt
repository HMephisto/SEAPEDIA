package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<Product>,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)

data class Product(
    val id: Int,
    @SerializedName("store_id") val storeId: Int,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("created_at") val createdAt: String,
    val store: Store
)

data class Store(
    val id: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("address_detail") val addressDetail: String
)

data class ProductDetail(
    val id: Int,
    @SerializedName("store_id") val storeId: Int,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val store: StoreDetail
)

data class StoreDetail(
    val id: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("address_detail") val addressDetail: String,
    val description: String,
    val seller: Seller
)

data class Seller(
    val id: Int,
    @SerializedName("full_name") val fullName: String
)