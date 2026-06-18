package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.LoginRequest
import com.example.seapedia.data.model.LoginResponse
import com.example.seapedia.data.network.api.ApiService

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun logout(): ApiResult<Unit> {
        return try {
            val response = apiService.logout()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Logout failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}