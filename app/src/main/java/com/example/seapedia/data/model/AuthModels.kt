package com.example.seapedia.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: User,
    val token: String
)

data class SwitchRoleRequest(
    val role: String
)

data class SwitchRoleResponse(
    val user: User
)

data class User(
    val id: Int,
    @SerializedName("full_name") val fullName: String,
    val email: String,
    @SerializedName("active_role_id") val activeRoleId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("active_role") val activeRole: Role,
    val roles: List<UserRole>
)

data class Role(
    val id: Int,
    val name: String
)

data class UserRole(
    val id: Int,
    val name: String,
    val pivot: RolePivot
)

data class RolePivot(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("role_id") val roleId: Int
)