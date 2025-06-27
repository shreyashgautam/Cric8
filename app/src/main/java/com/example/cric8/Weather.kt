package com.example.cric8

import android.Manifest
import android.annotation.SuppressLint
import android.icu.text.Transliterator.Position
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Weather() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var city by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // State for map camera position
    val defaultLocation = LatLng(20.5937, 78.9629) // India center default
    var mapPosition by remember { mutableStateOf(defaultLocation) }

    // Google Map camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition, 5f)
    }

    // Animation states for background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gradient_offset"
    )

    // Dynamic gradient colors based on weather
    val gradientColors = remember(weather) {
        when {
            weather?.current?.weather_descriptions?.any { it.contains("sunny", ignoreCase = true) } == true ->
                listOf(Color(0xFFFFB347), Color(0xFFFF8C42), Color(0xFFFF6B35))
            weather?.current?.weather_descriptions?.any { it.contains("rain", ignoreCase = true) } == true ->
                listOf(Color(0xFF4A90E2), Color(0xFF357ABD), Color(0xFF1E3A8A))
            weather?.current?.weather_descriptions?.any { it.contains("cloud", ignoreCase = true) } == true ->
                listOf(Color(0xFF9CA3AF), Color(0xFF6B7280), Color(0xFF4B5563))
            else -> listOf(Color(0xFF667EEA), Color(0xFF764BA2), Color(0xFF6B73FF))
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Location permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLoading = true
            getUserLocation(
                fusedLocationClient,
                onLocationFound = { location ->
                    coroutineScope.launch {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addressList = withContext(Dispatchers.IO) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            }
                            val detectedCity = addressList?.firstOrNull()?.locality
                            if (!detectedCity.isNullOrBlank()) {
                                city = detectedCity
                                mapPosition = LatLng(location.latitude, location.longitude)
                                val result = RetrofitClient.weatherApi.getWeather(city = detectedCity)
                                weather = result
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(mapPosition, 10f)
                                )
                            } else {
                                showToast("Unable to detect city from location.")
                                weather = null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showToast("Error fetching weather: ${e.localizedMessage}")
                            weather = null
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onError = { errorMsg ->
                    isLoading = false
                    showToast(errorMsg)
                }
            )
        } else {
            showToast("Location permission denied")
        }
    }

    // Fetch weather by LatLng (used on map click)
    fun fetchWeatherForLatLng(latLng: LatLng) {
        coroutineScope.launch {
            isLoading = true
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addressList = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                }
                val detectedCity = addressList?.firstOrNull()?.locality
                if (!detectedCity.isNullOrBlank()) {
                    city = detectedCity
                    val result = RetrofitClient.weatherApi.getWeather(city = detectedCity)
                    weather = result
                    // Move camera to tapped location
                    mapPosition = latLng
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
                    )
                } else {
                    showToast("Could not find city for selected location.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error fetching weather: ${e.localizedMessage}")
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = gradientOffset * 1000,
                    endY = (1f - gradientOffset) * 1000
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Weather App",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Check weather anywhere in the world",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Enter City Name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00B894),
                            focusedLabelColor = Color(0xFF00B894)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (city.isNotBlank()) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val result = RetrofitClient.weatherApi.getWeather(city = city)
                                            weather = result
                                            // Update map position based on city
                                            val geocoder = Geocoder(context, Locale.getDefault())
                                            val addressList = withContext(Dispatchers.IO) {
                                                geocoder.getFromLocationName(city, 1)
                                            }
                                            addressList?.firstOrNull()?.let {
                                                val latLng = LatLng(it.latitude, it.longitude)
                                                mapPosition = latLng
                                                cameraPositionState.animate(
                                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
                                                )
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            showToast("Error fetching weather: ${e.localizedMessage}")
                                            weather = null
                                        }
                                        isLoading = false
                                    }
                                } else {
                                    showToast("Please enter a city name")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667EEA)
                            )
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Location", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Map Composable
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        // When user taps on map, update city & weather
                        fetchWeatherForLatLng(latLng)
                    }
                ) {
                    Marker(

                        title = city.ifBlank { "Selected Location" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading Animation
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF00B894),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Fetching weather data...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Weather Display
            AnimatedVisibility(
                visible = weather != null && !isLoading,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                weather?.let { WeatherDisplay(it) }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getUserLocation(
    client: FusedLocationProviderClient,
    onLocationFound: (Location) -> Unit,
    onError: (String) -> Unit
) {
    client.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationFound(location)
        } else {
            onError("Failed to get current location. Please enable GPS or try again.")
        }
    }.addOnFailureListener { e ->
        onError("Failed to get location: ${e.localizedMessage}")
    }
}

@Composable
fun WeatherDisplay(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location Header
            Text(
                text = "${weather.location.name}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
            Text(
                text = weather.location.country,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = weather.location.localtime,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Weather Icon and Temperature
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                weather.current.weather_icons.firstOrNull()?.let { iconUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(iconUrl),
                        contentDescription = "Weather Icon",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${weather.current.temperature}°",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Feels like ${weather.current.feelslike}°",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Description
            Text(
                text = weather.current.weather_descriptions.joinToString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        Color(0xFF00B894).copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Weather Details Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    WeatherDetailCard(
                        icon = "💧",
                        title = "Humidity",
                        value = "${weather.current.humidity}%"
                    )
                }
                item {
                    WeatherDetailCard(
                        icon = "🌬️",
                        title = "Wind Speed",
                        value = "${weather.current.wind_speed} km/h"
                    )
                }
                item {
                    WeatherDetailCard(
                        icon = "🎯",
                        title = "Pressure",
                        value = "${weather.current.pressure} hPa"
                    )
                }
                item {
                    WeatherDetailCard(
                        icon = "👁",
                        title = "Visibility",
                        value = "${weather.current.pressure} km"
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )
        }
    }
}
