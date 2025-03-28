package uk.ac.tees.mad.coffeequest.domain

// Data class for shop
data class Shop(
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Float = 0f
)