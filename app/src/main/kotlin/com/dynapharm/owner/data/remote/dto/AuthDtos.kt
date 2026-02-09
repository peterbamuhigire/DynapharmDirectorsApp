package com.dynapharm.owner.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for owner login.
 * Accepts either email or username for backward compatibility.
 */
@Serializable
data class LoginRequestDto(
    @SerialName("email")
    val username: String,  // Field name is username in code, but serialized as email for API
    @SerialName("password")
    val password: String
)

/**
 * Response DTO for successful login (data payload).
 * Contains user information, tokens, and associated franchises.
 */
@Serializable
data class LoginResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("user")
    val user: UserDto,
    @SerialName("franchises")
    val franchises: List<FranchiseDto>
)

/**
 * User information DTO.
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("role")
    val role: String,
    @SerialName("phone")
    val phone: String? = null
)

/**
 * Franchise information DTO.
 */
@Serializable
data class FranchiseDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("branch_count")
    val branchCount: Int
)

/**
 * Response DTO for token refresh.
 */
@Serializable
data class TokenResponseDto(
    @SerialName("access_token")
    val accessToken: String
)
