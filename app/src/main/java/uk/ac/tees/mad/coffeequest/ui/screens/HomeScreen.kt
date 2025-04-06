package uk.ac.tees.mad.coffeequest.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.coffeequest.domain.Shop

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onViewMapClick: () -> Unit = {},
    onShopClick: (Shop) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val notificationPermissionState =
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    var userLocationString by remember { mutableStateOf("Fetching location...") }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var shops by remember { mutableStateOf(listOf<Shop>()) }
    var filteredShops by remember { mutableStateOf(listOf<Shop>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Fetch location when permission is granted
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            scope.launch {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val geocoder = Geocoder(context)
                            val addresses: List<Address>? =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            userLocation = location

                            fetchShopsFromFirestore(
                                userLocation!!,
                                onShopsFetched = { fetchedShops ->
                                    shops = fetchedShops
                                },
                                onError = { error ->
                                    Toast.makeText(
                                        context,
                                        "Error fetching shops: $error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                })

                            userLocationString = if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val addressLine = address.getAddressLine(0)

                                if (!addressLine.isNullOrEmpty()) {
                                    addressLine
                                } else {
                                    "Lat: ${location.latitude}, Lon: ${location.longitude}"
                                }
                            } else {
                                "Lat: ${location.latitude}, Lon: ${location.longitude}"
                            }
                        } else {
                            userLocationString = "Location unavailable"
                        }
                    }
                } catch (e: SecurityException) {
                    userLocationString = "Permission error"
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Filter shops based on search query
    LaunchedEffect(searchQuery, shops) {

        filteredShops = if (searchQuery.isEmpty()) {
            shops // Show all nearby shops when query is empty

        } else {
            shops.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Show daily deal notification
    LaunchedEffect(Unit) {
        if (notificationPermissionState.status.isGranted) {
            showDailyDealNotification(context)
        } else {
            notificationPermissionState.launchPermissionRequest()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Coffee Shops") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display user's location
            Text(
                text = "Your Location: $userLocationString",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search shops by name or location") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            // Shop list from Firestore
            if (filteredShops.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No shops found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                ShopList(
                    shops = filteredShops,
                    onShopClick = onShopClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "All the shops within 100 km range will only be shown.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // "View on Map" button

                Button(
                    onClick = onViewMapClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("View on Map")
                }
            }
        }
    }
}

// Fetch shops from Firestore and filter by proximity
fun fetchShopsFromFirestore(
    userLocation: Location,
    onShopsFetched: (List<Shop>) -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("shops")
        .get()
        .addOnSuccessListener { result ->

            val shopList = mutableListOf<Shop>()
            for (document in result) {
                val shop = document.toObject<Shop>()
                val shopLocation = Location("").apply {
                    latitude = shop.latitude
                    longitude = shop.longitude
                }
                val distance =
                    userLocation.distanceTo(shopLocation) / 1000 // Distance in kilometers
                Log.d("DITA", distance.toString())

                // Filter shops within 1000km
                if (distance <= 1000) {
                    shopList.add(shop)
                }
            }
            onShopsFetched(shopList.sortedBy { shop ->
                val shopLoc = Location("").apply {
                    latitude = shop.latitude
                    longitude = shop.longitude
                }
                userLocation.distanceTo(shopLoc)
            }) // Sort by distance
        }
        .addOnFailureListener { exception ->
            // Handle error
            exception.localizedMessage?.let { onError(it) }
        }
}

// Composable for the shop list
@Composable
fun ShopList(shops: List<Shop>, onShopClick: (Shop) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(shops) { shop ->
            ShopItem(shop, onShopClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        }
    }
}

// Composable for individual shop item
@Composable
fun ShopItem(shop: Shop, onShopClick: (Shop) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShopClick(shop) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = shop.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rating: ${shop.rating}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

// Function to show daily deal notification
private fun showDailyDealNotification(context: Context) {
    val channelId = "daily_deal_channel"
    val notificationId = 1

    // Create notification channe
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Daily Deals",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for daily coffee shop deals"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Building the notification
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a default icon; replace with your app icon
        .setContentTitle("Daily Deal")
        .setContentText("Today's Deal: 20% off all lattes!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, notification)
    }
}