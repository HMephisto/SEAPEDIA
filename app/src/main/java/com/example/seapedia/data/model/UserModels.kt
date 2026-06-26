package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    val id: Int,
    @SerializedName("full_name") val fullName: String,
    val email: String,
    @SerializedName("active_role_id") val activeRoleId: Int,
    @SerializedName("active_role") val activeRole: Role,
    val roles: List<UserRole>
)
