package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.CreateProductRequest
import com.example.seapedia.data.model.CreateProductResponse
import com.example.seapedia.data.model.CreateStoreRequest
import com.example.seapedia.data.model.CreateStoreResponse
import com.example.seapedia.data.model.ProductResponse
import com.example.seapedia.data.model.SellerStoreResponse
import com.example.seapedia.data.model.StoreCheckResponse
import com.example.seapedia.data.network.api.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class StoreRepository(private val apiService: ApiService) {

    suspend fun checkStore(): ApiResult<StoreCheckResponse> {
        return try {
            val response = apiService.checkStore()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to check store")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun createStore(
        storeName: String,
        description: String,
        addressDetail: String
    ): ApiResult<CreateStoreResponse> {
        return try {
            val response = apiService.createStore(
                CreateStoreRequest(storeName, description, addressDetail)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to create store")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun getSellerStore(): ApiResult<SellerStoreResponse> {
        return try {
            val response = apiService.getSellerStore()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load store")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun getProductsByStore(storeId: Int): ApiResult<ProductResponse> {
        return try {
            val response = apiService.getProductsByStore(storeId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load products")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun createProduct(
        storeId: Int,
        name: String,
        description: String,
        price: String,
        stock: Int,
        imageUrl: String? = null
    ): ApiResult<CreateProductResponse> {
        return try {
            val response = apiService.createProduct(
                CreateProductRequest(storeId, name, description, price, stock, imageUrl)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to create product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun updateProduct(
        productId: Int,
        storeId: Int,
        name: String,
        description: String,
        price: String,
        stock: Int,
        imageUrl: String? = null
    ): ApiResult<CreateProductResponse> {
        return try {
            val response = apiService.updateProduct(
                productId,
                CreateProductRequest(storeId, name, description, price, stock, imageUrl)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun deleteProduct(productId: Int): ApiResult<Unit> {
        return try {
            val response = apiService.deleteProduct(productId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to delete product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun uploadProductImage(productId: Int, imageFile: File): ApiResult<Unit> {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            val response = apiService.uploadProductImage(productId, part)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to upload image")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun updateStore(
        storeName: String,
        description: String,
        addressDetail: String
    ): ApiResult<SellerStoreResponse> {
        return try {
            val response = apiService.updateStore(
                CreateStoreRequest(storeName, description, addressDetail)
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to update store")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}