package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.AddToCartRequest
import com.example.seapedia.data.model.UpdateCartRequest
import com.example.seapedia.data.network.api.ApiService

class CartRepository(private val api: ApiService) {
    suspend fun getCart(): ApiResult<com.example.seapedia.data.model.CartResponse> {
        return try {
            val response = api.getCart()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Empty response")
            } else {
                ApiResult.Error("Failed to load cart: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun addToCart(productId: Int, quantity: Int): ApiResult<Unit> {
        return try {
            val response = api.addToCart(AddToCartRequest(productId, quantity))
            if (response.isSuccessful) ApiResult.Success(Unit)
            else {
                val errorBody = response.errorBody()?.string()
                val message = if (errorBody?.contains("another store") == true)
                    "Your cart already contains products from another store."
                else
                    "Failed to add to cart: ${response.code()}"
                ApiResult.Error(message)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateCartItem(itemId: Int, quantity: Int): ApiResult<Unit> {
        return try {
            val response = api.updateCartItem(itemId, UpdateCartRequest(quantity))
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Failed to update item: ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun removeCartItem(itemId: Int): ApiResult<Unit> {
        return try {
            val response = api.removeCartItem(itemId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Failed to remove item: ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun clearCart(): ApiResult<Unit> {
        return try {
            val response = api.clearCart()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Failed to clear cart: ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}