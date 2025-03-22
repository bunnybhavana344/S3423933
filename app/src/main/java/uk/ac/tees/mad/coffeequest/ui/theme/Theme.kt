package uk.ac.tees.mad.coffeequest.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CoffeeColorScheme = lightColorScheme(
    primary = Color(0xFF6F4E37), // Coffee brown
    secondary = Color(0xFFD4A373), // Light coffee accent
    background = Color(0xFFF5E6CC) // Cream background
)

@Composable
fun CoffeeQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {


    MaterialTheme(
        colorScheme = CoffeeColorScheme,
        typography = Typography,
        content = content
    )
}