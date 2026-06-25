package com.example.seapedia.data.model

import android.os.Parcel
import android.os.Parcelable
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Store::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(storeId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(price)
        parcel.writeInt(stock)
        parcel.writeString(imageUrl)
        parcel.writeString(createdAt)
        parcel.writeParcelable(store, flags)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

data class Store(
    val id: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("address_detail") val addressDetail: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(storeName)
        parcel.writeString(addressDetail)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<Store> {
        override fun createFromParcel(parcel: Parcel): Store {
            return Store(parcel)
        }

        override fun newArray(size: Int): Array<Store?> {
            return arrayOfNulls(size)
        }
    }
}

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