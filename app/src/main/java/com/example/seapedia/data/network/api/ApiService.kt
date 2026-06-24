package com.example.seapedia.data.network.api

import com.example.seapedia.data.model.AddReviewRequest
import com.example.seapedia.data.model.LoginRequest
import com.example.seapedia.data.model.LoginResponse
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.model.ProductResponse
import com.example.seapedia.data.model.RegisterRequest
import com.example.seapedia.data.model.ReviewResponse
import com.example.seapedia.data.model.SwitchRoleRequest
import com.example.seapedia.data.model.SwitchRoleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @POST("switch-role")
    suspend fun switchRole(@Body request: SwitchRoleRequest): Response<SwitchRoleResponse>

    @GET("products")
    suspend fun getProducts(@Query("search") search: String? = null): Response<ProductResponse>

    @GET("reviews")
    suspend fun getReviews(): Response<ReviewResponse>

    @POST("reviews")
    suspend fun addReview(@Body request: AddReviewRequest): Response<Unit>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") productId: Int): Response<ProductDetail>
}