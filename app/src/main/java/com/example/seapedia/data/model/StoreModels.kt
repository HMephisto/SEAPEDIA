package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class StoreCheckResponse(
    @SerializedName("has_store") val hasStore: Boolean
)

data class CreateStoreRequest(
    @SerializedName("store_name") val storeName: String,
    val description: String,
    @SerializedName("address_detail") val addressDetail: String
)

data class CreateStoreResponse(
    val store: StoreDetail
)

data class SellerStoreResponse(
    val store: SellerStore
)

data class SellerStore(
    val id: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("store_name") val storeName: String,
    val description: String,
    @SerializedName("address_detail") val addressDetail: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateProductRequest(
    @SerializedName("store_id") val storeId: Int,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class CreateProductResponse(
    val product: Product
)