package com.example.moviebooking.ui.admin.cinema

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCinemaFormScreen(
    cinemaId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminCinemaFormViewModel = viewModel(
        factory = AdminCinemaFormViewModel.Factory(cinemaId, LocalContext.current)
    )
) {
    val cinema by viewModel.cinema.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var numberOfScreens by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var selectedFacilities by remember { mutableStateOf(setOf<String>()) }

    val availableFacilities = listOf(
        "Parking",
        "Food & Beverages",
        "Wheelchair Access",
        "3D Projection",
        "IMAX",
        "Dolby Atmos",
        "VIP Seating",
        "Online Booking"
    )

    LaunchedEffect(cinema) {
        cinema?.let {
            name = it.name
            address = it.address
            city = it.city
            numberOfScreens = it.numberOfScreens.toString()
            latitude = it.location?.latitude?.toString() ?: ""
            longitude = it.location?.longitude?.toString() ?: ""
            selectedFacilities = it.facilities.toSet()
        }
    }

    LaunchedEffect(cinemaId) {
        if (cinemaId == "new") {
            name = ""
            address = ""
            city = ""
            numberOfScreens = ""
            latitude = ""
            longitude = ""
            selectedFacilities = emptySet()
        }
    }

    LaunchedEffect(saveResult) {
        saveResult?.onSuccess {
            onNavigateBack()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cinemaId == "new") "Add New Cinema" else "Edit Cinema") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyLight)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Cinema Image",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Cinema Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = if (imageUrl == null) Icons.Default.Add else Icons.Default.Edit,
                                    contentDescription = "Select Image",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Cinema Details Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Cinema Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = numberOfScreens,
                            onValueChange = { numberOfScreens = it },
                            label = { Text("Number of Screens") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Location Section
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = { latitude = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = longitude,
                                onValueChange = { longitude = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // Facilities Section
                        Text(
                            text = "Facilities",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableFacilities.forEach { facility ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedFacilities.contains(facility),
                                        onCheckedChange = { checked ->
                                            selectedFacilities = if (checked) {
                                                selectedFacilities + facility
                                            } else {
                                                selectedFacilities - facility
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AccentColor,
                                            uncheckedColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                    Text(
                                        text = facility,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val numberOfScreensInt = numberOfScreens.toIntOrNull() ?: 0
                        val latitudeDouble = latitude.toDoubleOrNull() ?: 0.0
                        val longitudeDouble = longitude.toDoubleOrNull() ?: 0.0
                        viewModel.saveCinema(
                            name = name,
                            address = address,
                            city = city,
                            numberOfScreens = numberOfScreensInt,
                            facilities = selectedFacilities.toList(),
                            latitude = latitudeDouble,
                            longitude = longitudeDouble
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    enabled = !isLoading && name.isNotBlank() && address.isNotBlank() &&
                            city.isNotBlank() && numberOfScreens.isNotBlank() &&
                            latitude.isNotBlank() && longitude.isNotBlank() &&
                            imageUrl != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Save Cinema")
                    }
                }
            }

            if (errorMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(errorMessage ?: "")
                }
            }
        }
    }
} 