package uk.ac.tees.mad.coffeequest.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteShop::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteShopDao(): FavoriteShopDao

    companion object {
        const val DATABASE_NAME = "coffee_shop_db"
    }
}