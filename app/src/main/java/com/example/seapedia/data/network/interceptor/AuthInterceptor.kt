package com.example.seapedia.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor (private val tokenProvider: () -> String?) : Interceptor{

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept", "application/json")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
        }
        return chain.proceed(request)
    }
}