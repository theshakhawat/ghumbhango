package com.shakhawat.ghhumbhango.domain.model

/**
 * Domain model representing a selected or saved travel destination.
 */
data class Destination(
    val id: String = "",
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val isSaved: Boolean = false,
    val category: String = "general"
)

