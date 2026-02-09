package com.dynapharm.owner.domain.model

import com.dynapharm.owner.data.remote.dto.FranchiseDto

/**
 * Domain model representing a franchise.
 * Contains franchise information and branch count.
 */
data class Franchise(
    val id: Int,
    val name: String,
    val branchCount: Int
)

/**
 * Maps FranchiseDto from data layer to Franchise domain model.
 */
fun FranchiseDto.toDomain(): Franchise {
    return Franchise(
        id = id,
        name = name,
        branchCount = branchCount
    )
}
