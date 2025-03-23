package uk.ac.tees.mad.coffeequest.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import uk.ac.tees.mad.coffeequest.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var showLocationDialog by remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Animation for scaling the logo
    val scale = remember {
        Animatable(0.5f)
    }

    // Simulate loading delay
    LaunchedEffect(Unit) {
        val isLocationEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

        if (!isLocationEnabled) {
            showLocationDialog = true
        } else if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()

        } else {
            scale.animateTo(1f, animationSpec = tween(durationMillis = 1000))
            delay(2000) // 2-second delay for demo purposes
            // TODO: Navigate to Home Screen
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5E6CC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.ic_coffee_logo),
                contentDescription = "Coffee Shop Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value) // Rotate the logo
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading Indicator
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
    // Dialog to prompt user to enable location services
    if (showLocationDialog) {
        LocationServicesDialog(
            onDismiss = { showLocationDialog = false },
            onEnableLocation = {
                showLocationDialog = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        )
    }
}

@Composable
fun LocationServicesDialog(
    onDismiss: () -> Unit,
    onEnableLocation: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enable Location Services",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This app requires location services to find nearby coffee shops. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = onEnableLocation) {
                        Text("Enable")
                    }
                }
            }
        }
    }
}