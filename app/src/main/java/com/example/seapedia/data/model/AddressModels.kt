package com.example.seapedia.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import com.google.gson.annotations.SerializedName

data class AddressResponse(
    val success: Boolean,
    val data: List<Address>
)

data class AddressPostResponse(
    val success: Boolean,
    val data: Address
)

data class Address(
    val id: Int,
    @SerializedName("buyer_id") val buyerId: Int,
    @SerializedName("recipient_name") val recipientName: String,
    val phone: String,
    @SerializedName("address_detail") val addressDetail: String,
    @SerializedName("is_default") val isDefault: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(buyerId)
        parcel.writeString(recipientName)
        parcel.writeString(phone)
        parcel.writeString(addressDetail)
        parcel.writeByte(if (isDefault) 1 else 0)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<Address> {
        override fun createFromParcel(parcel: Parcel): Address {
            return Address(parcel)
        }

        override fun newArray(size: Int): Array<Address?> {
            return arrayOfNulls(size)
        }
    }
}

data class AddressRequest(
    @SerializedName("recipient_name") val recipientName: String,
    val phone: String,
    @SerializedName("address_detail") val addressDetail: String,
    @SerializedName("is_default") val isDefault: Boolean
)