package uk.ac.tees.mad.coffeequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import uk.ac.tees.mad.coffeequest.database.FavoriteShop
import uk.ac.tees.mad.coffeequest.domain.Shop
import uk.ac.tees.mad.coffeequest.ui.screens.FavoritesScreen
import uk.ac.tees.mad.coffeequest.ui.screens.HomeScreen
import uk.ac.tees.mad.coffeequest.ui.screens.MapScreen
import uk.ac.tees.mad.coffeequest.ui.screens.ShopDetailsScreen
import uk.ac.tees.mad.coffeequest.ui.screens.SplashScreen
import uk.ac.tees.mad.coffeequest.ui.theme.CoffeeQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            CoffeeQuestTheme {
                AppNavigation()

            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                onViewMapClick = { navController.navigate("map") },
                onShopClick = { shop ->
                    val shopJson = Gson().toJson(shop)
                    navController.navigate("shopDetails/$shopJson")
                },
                onFavoritesClick = { navController.navigate("favorites") }
            )
        }
        composable("map") {
            MapScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "shopDetails/{shopJson}",
            arguments = listOf(navArgument("shopJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopJson = backStackEntry.arguments?.getString("shopJson") ?: ""
            val shop = Gson().fromJson(shopJson, Shop::class.java)
            ShopDetailsScreen(
                shop = shop,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            FavoritesScreen(
                favoriteShops = listOf(
                    FavoriteShop(
                        name = "Coffee Shop 1",
                        address = "123 Main St",
                        latitude = 40.7128,
                        longitude = -74.0060,
                        rating = 4.5f
                    ),
                    FavoriteShop(
                        name = "Coffee Shop 2",
                        address = "456 Elm St",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        rating = 4.2f
                    )

                ), // in future
                onBackClick = { navController.popBackStack() },
                onFavoriteShopClick = { shop ->
                    val shopJson = Gson().toJson(
                        Shop(
                            shop.name,
                            shop.address,
                            shop.latitude,
                            shop.longitude,
                            shop.rating
                        )
                    )
                    navController.navigate("shopDetails/$shopJson")
                }
            )
        }
    }
}
