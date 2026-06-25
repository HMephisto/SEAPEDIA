package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.AddRoleRequest
import com.example.seapedia.data.model.AddRoleResponse
import com.example.seapedia.data.model.LoginRequest
import com.example.seapedia.data.model.LoginResponse
import com.example.seapedia.data.model.RegisterRequest
import com.example.seapedia.data.model.SwitchRoleRequest
import com.example.seapedia.data.model.SwitchRoleResponse
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

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): ApiResult<LoginResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password,
                    passwordConfirmation = passwordConfirmation
                )
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Registration failed")
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

    suspend fun switchRole(role: String): ApiResult<SwitchRoleResponse> {
        return try {
            val response = apiService.switchRole(SwitchRoleRequest(role))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to switch role")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun addRole(role: String): ApiResult<AddRoleResponse> {
        return try {
            val response = apiService.addRole(AddRoleRequest(role))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to add role")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}