package uk.ac.tees.mad.coffeequest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteShop: FavoriteShop)

    @Query("DELETE FROM favorite_shops WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT * FROM favorite_shops WHERE name = :name")
    suspend fun getFavoriteShopByName(name: String): FavoriteShop?

    @Query("SELECT * FROM favorite_shops")
    fun getAllFavoriteShops(): Flow<List<FavoriteShop>>
}