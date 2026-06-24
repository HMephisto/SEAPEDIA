package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.AddReviewRequest
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.model.ProductResponse
import com.example.seapedia.data.model.ReviewResponse
import com.example.seapedia.data.network.api.ApiService

class GuestRepository (private val apiService: ApiService) {
    suspend fun getProducts(search: String? = null): ApiResult<ProductResponse> {
        return try {
            val response = apiService.getProducts(search)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load products")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun getReviews(): ApiResult<ReviewResponse> {
        return try {
            val response = apiService.getReviews()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load reviews")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun addReview(rating: Int, comment: String): ApiResult<Unit> {
        return try {
            val response = apiService.addReview(AddReviewRequest(rating, comment))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to submit review")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun getProductDetail(productId: Int): ApiResult<ProductDetail> {
        return try {
            val response = apiService.getProductDetail(productId)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load product")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}