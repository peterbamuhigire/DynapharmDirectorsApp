package com.dynapharm.owner.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Standard envelope for all API responses.
 * Backend returns this structure for both success and error cases.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDto? = null,
    val meta: MetaDto? = null
)

@Serializable
data class ErrorDto(
    val code: String,
    val message: String,
    val field: String? = null,
    val details: String? = null
)

@Serializable
data class MetaDto(
    val timestamp: String? = null,
    val franchise_id: Int? = null,
    val franchise_name: String? = null,
    val request_id: String? = null
)

/**
 * Pagination metadata for paginated responses.
 */
@Serializable
data class PaginationDto(
    val current_page: Int,
    val per_page: Int,
    val total_items: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)
