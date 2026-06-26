package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.MeResponse
import com.example.seapedia.data.network.api.ApiService

class UserRepository(private val apiService: ApiService) {
    suspend fun getMe(): ApiResult<MeResponse> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load profile")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}