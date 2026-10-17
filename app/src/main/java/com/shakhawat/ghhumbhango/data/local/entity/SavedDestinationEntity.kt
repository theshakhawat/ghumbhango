package com.shakhawat.ghhumbhango.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shakhawat.ghhumbhango.domain.model.Destination

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val isFavorite: Boolean = false,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null
) {
    fun toDomain(): Destination = Destination(
        id = id.toString(),
        name = name,
        latitude = latitude,
        longitude = longitude,
        address = address,
        isSaved = true,
        category = category
    )

    companion object {
        fun fromDomain(destination: Destination, isFavorite: Boolean = true, category: String = destination.category): SavedDestinationEntity =
            SavedDestinationEntity(
                id = destination.id.toLongOrNull() ?: 0L,
                name = destination.name,
                latitude = destination.latitude,
                longitude = destination.longitude,
                address = destination.address,
                isFavorite = isFavorite,
                category = category,
                lastUsedAt = System.currentTimeMillis()
            )
    }
}

