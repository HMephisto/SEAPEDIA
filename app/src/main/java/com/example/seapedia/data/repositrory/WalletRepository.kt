package com.example.seapedia.data.repositrory

import com.example.seapedia.data.model.TopUpRequest
import com.example.seapedia.data.model.TopUpResponse
import com.example.seapedia.data.model.TransactionResponse
import com.example.seapedia.data.model.WalletResponse
import com.example.seapedia.data.network.api.ApiService

class WalletRepository(private val apiService: ApiService) {
    suspend fun getWallet(): ApiResult<WalletResponse> {
        return try {
            val response = apiService.getWallet()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load wallet")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun topUp(amount: Double): ApiResult<TopUpResponse> {
        return try {
            val response = apiService.topUp(TopUpRequest(amount))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Top up failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }

    suspend fun getTransactions(): ApiResult<TransactionResponse> {
        return try {
            val response = apiService.getTransactions()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Failed to load transactions")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Something went wrong")
        }
    }
}