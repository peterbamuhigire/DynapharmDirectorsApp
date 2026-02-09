package com.dynapharm.owner.domain.model

import com.dynapharm.owner.data.remote.dto.UserDto

/**
 * Domain model representing a user in the system.
 * Contains essential user information.
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String?
)

/**
 * Maps UserDto from data layer to User domain model.
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        role = role,
        phone = phone
    )
}
