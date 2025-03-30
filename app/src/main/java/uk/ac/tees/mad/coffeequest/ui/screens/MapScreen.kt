package uk.ac.tees.mad.coffeequest.ui.screens

import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.coffeequest.domain.Shop
import  com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var shops by remember { mutableStateOf(listOf<Shop>()) }
    val cameraPositionState = rememberCameraPositionState()

    // Fetch user location and shops
    LaunchedEffect(Unit) {
        scope.launch {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = location
                        fetchShopsFromFirestore(userLocation!!, onShopsFetched = { fetchedShops ->
                            shops = fetchedShops
                            // Center map on user location
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 12f
                            )
                        }, onError = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            } catch (e: SecurityException) {
                userLocation = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coffee Shops Map") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState
        ) {
            userLocation?.let {
                val userMarker = rememberMarkerState(
                    position = LatLng(it.latitude, it.longitude)
                )
                // Marker for user's location
                Marker(
                    state = userMarker,
                    title = "You are here",
                    snippet = "Your current location"
                )
            }

            // Markers for coffee shops
            shops.forEach { shop ->

                Marker(
                    state = MarkerState(position = LatLng(shop.latitude, shop.longitude)),
                    title = shop.name,
                    snippet = shop.address
                )
            }
        }
    }
}
