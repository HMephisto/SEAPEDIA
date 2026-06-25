package com.example.seapedia.data.network.api

import com.example.seapedia.data.model.AddReviewRequest
import com.example.seapedia.data.model.AddRoleRequest
import com.example.seapedia.data.model.AddRoleResponse
import com.example.seapedia.data.model.CreateProductRequest
import com.example.seapedia.data.model.CreateProductResponse
import com.example.seapedia.data.model.CreateStoreRequest
import com.example.seapedia.data.model.CreateStoreResponse
import com.example.seapedia.data.model.LoginRequest
import com.example.seapedia.data.model.LoginResponse
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.model.ProductResponse
import com.example.seapedia.data.model.RegisterRequest
import com.example.seapedia.data.model.ReviewResponse
import com.example.seapedia.data.model.SellerStoreResponse
import com.example.seapedia.data.model.StoreCheckResponse
import com.example.seapedia.data.model.SwitchRoleRequest
import com.example.seapedia.data.model.SwitchRoleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @POST("add-role")
    suspend fun addRole(@Body request: AddRoleRequest): Response<AddRoleResponse>

    @GET("products")
    suspend fun getProducts(@Query("search") search: String? = null): Response<ProductResponse>

    @GET("reviews")
    suspend fun getReviews(): Response<ReviewResponse>

    @POST("reviews")
    suspend fun addReview(@Body request: AddReviewRequest): Response<Unit>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") productId: Int): Response<ProductDetail>

    @GET("seller/store/check")
    suspend fun checkStore(): Response<StoreCheckResponse>

    @POST("seller/store")
    suspend fun createStore(@Body request: CreateStoreRequest): Response<CreateStoreResponse>

    @GET("seller/store")
    suspend fun getSellerStore(): Response<SellerStoreResponse>

    @PUT("seller/store")
    suspend fun updateStore(@Body request: CreateStoreRequest): Response<SellerStoreResponse>

    @GET("products")
    suspend fun getProductsByStore(@Query("store_id") storeId: Int): Response<ProductResponse>

    @POST("seller/products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<CreateProductResponse>

    @PUT("seller/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Int,
        @Body request: CreateProductRequest
    ): Response<CreateProductResponse>

    @DELETE("seller/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): Response<Unit>

    @Multipart
    @POST("seller/products/{id}/image")
    suspend fun uploadProductImage(
        @Path("id") productId: Int,
        @Part image: MultipartBody.Part
    ): Response<Unit>
}