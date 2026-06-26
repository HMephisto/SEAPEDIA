package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.AddressPostResponse
import com.example.seapedia.data.model.AddressRequest
import com.example.seapedia.data.model.AddressResponse
import com.example.seapedia.data.network.api.ApiService

class AddressRepository(private val apiService: ApiService){
    suspend fun getAddresses(): ApiResult<AddressResponse> {
        return try {
            val response = apiService.getAddresses()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load addresses")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun createAddress(
        recipientName: String,
        phone: String,
        addressDetail: String,
        isDefault: Boolean
    ): ApiResult<AddressPostResponse> {
        return try {
            val response = apiService.createAddress(
                AddressRequest(recipientName, phone, addressDetail, isDefault)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to create address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun updateAddress(
        id: Int,
        recipientName: String,
        phone: String,
        addressDetail: String,
        isDefault: Boolean
    ): ApiResult<AddressPostResponse> {
        return try {
            val response = apiService.updateAddress(
                id, AddressRequest(recipientName, phone, addressDetail, isDefault)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun deleteAddress(id: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteAddress(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to delete address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun setDefaultAddress(id: Int): ApiResult<AddressResponse> {
        return try {
            val response = apiService.setDefaultAddress(id)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to set default")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}