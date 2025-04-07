package uk.ac.tees.mad.coffeequest.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_shops")
data class FavoriteShop(
    @PrimaryKey val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float
)