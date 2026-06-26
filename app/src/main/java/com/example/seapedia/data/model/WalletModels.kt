package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class WalletResponse(
    val success: Boolean,
    val data: Wallet
)

data class Wallet(
    val id: Int,
    val balance: Double,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class TopUpRequest(
    val amount: Double
)

data class TopUpResponse(
    val success: Boolean,
    val message: String,
    val data: TopUpData
)

data class TopUpData(
    @SerializedName("new_balance") val newBalance: Double,
    @SerializedName("topped_up") val toppedUp: Double
)

data class TransactionResponse(
    val success: Boolean,
    val data: List<Transaction>,
    val meta: TransactionMeta
)

data class Transaction(
    val id: Int,
    @SerializedName("wallet_id") val walletId: Int,
    val type: String,
    val amount: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String
)

data class TransactionMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int
)