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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import uk.ac.tees.mad.coffeequest.ui.screens.HomeScreen
import uk.ac.tees.mad.coffeequest.ui.screens.MapScreen
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
                onViewMapClick = { navController.navigate("map") }
            )
        }
        composable("map") {
            MapScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
