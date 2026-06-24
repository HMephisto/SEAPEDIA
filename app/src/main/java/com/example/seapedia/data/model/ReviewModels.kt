package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<Review>,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)

data class Review(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("reviewer_name") val reviewerName: String,
    val rating: Int,
    val comment: String,
    @SerializedName("created_at") val createdAt: String,
    val user: ReviewUser
)

data class ReviewUser(
    val id: Int,
    @SerializedName("full_name") val fullName: String
)

data class AddReviewRequest(
    val rating: Int,
    val comment: String
)