package com.example.moviebooking.ui.admin.showtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.ui.theme.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShowtimeFormScreen(
    showtimeId: String,
    onNavigateBack: () -> Unit,
    onShowtimeSaved: () -> Unit = {},
    viewModel: AdminShowtimeFormViewModel = viewModel(
        factory = AdminShowtimeFormViewModel.Factory(showtimeId, LocalContext.current)
    )
) {
    val context = LocalContext.current
    val showtime by viewModel.showtime.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val cinemas by viewModel.cinemas.collectAsState()
    val screens by viewModel.screens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showError by viewModel.showError.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    var selectedMovieId by remember { mutableStateOf("") }
    var selectedCinemaId by remember { mutableStateOf("") }
    var selectedScreenId by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("2D") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // State cho các dropdown
    var movieExpanded by remember { mutableStateOf(false) }
    var cinemaExpanded by remember { mutableStateOf(false) }
    var screenExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }

    // State cho DateTimePicker
    var showStartDateTimePicker by remember { mutableStateOf(false) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    val formats = listOf("2D", "3D", "IMAX")

    val nowShowingMovies = movies.filter { it.isNowShowing }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showtime) {
        showtime?.let {
            selectedMovieId = it.movieId
            selectedCinemaId = it.cinemaId
            selectedScreenId = it.screenId
            selectedFormat = it.format
            startTime = formatTimestamp(it.startTime)
            endTime = formatTimestamp(it.endTime)
            price = it.price.toString()
            
            // Parse start and end time
            startDate = it.startTime?.toDate()
            endDate = it.endTime?.toDate()
        }
    }

    LaunchedEffect(showtimeId) {
        if (showtimeId == "new") {
            selectedMovieId = ""
            selectedCinemaId = ""
            selectedScreenId = ""
            selectedFormat = "2D"
            startTime = ""
            endTime = ""
            price = ""
            startDate = null
            endDate = null
        }
    }

    LaunchedEffect(saveResult) {
        if (saveResult?.isSuccess == true) {
            onShowtimeSaved()
            onNavigateBack()
        }
    }

    LaunchedEffect(selectedMovieId, startDate) {
        if (selectedMovieId.isNotBlank() && startDate != null) {
            val movie = movies.find { it.id == selectedMovieId }
            if (movie != null) {
                val calendar = Calendar.getInstance()
                calendar.time = startDate!!
                calendar.add(Calendar.MINUTE, movie.duration)
                endDate = calendar.time
                endTime = formatTime(calendar.time)
            }
        }
    }

    LaunchedEffect(selectedCinemaId) {
        if (selectedCinemaId.isNotBlank()) {
            viewModel.loadScreens(selectedCinemaId)
        }
    }

    // Show error message in Snackbar
    LaunchedEffect(showError, errorMessage) {
        if (showError && errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                duration = SnackbarDuration.Short,
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    // Custom Time Picker Dialog
    @Composable
    fun TimePickerDialog(
        onDismiss: () -> Unit,
        onTimeSelected: (hour: Int, minute: Int) -> Unit
    ) {
        var selectedHour by remember { mutableStateOf(8) }
        var selectedMinute by remember { mutableStateOf(0) }
        var isAM by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Time") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hour Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                selectedHour = if (selectedHour > 1) selectedHour - 1 else 12
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Decrease Hour")
                        }
                        
                        Text(
                            text = String.format("%02d", selectedHour),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        IconButton(
                            onClick = {
                                selectedHour = if (selectedHour < 12) selectedHour + 1 else 1
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, "Increase Hour")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minute Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                selectedMinute = if (selectedMinute > 0) selectedMinute - 15 else 45
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Decrease Minute")
                        }
                        
                        Text(
                            text = String.format("%02d", selectedMinute),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        IconButton(
                            onClick = {
                                selectedMinute = if (selectedMinute < 45) selectedMinute + 15 else 0
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, "Increase Minute")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AM/PM Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = isAM,
                            onClick = { isAM = true },
                            label = { Text("AM") }
                        )
                        FilterChip(
                            selected = !isAM,
                            onClick = { isAM = false },
                            label = { Text("PM") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = if (isAM) {
                            if (selectedHour == 12) 0 else selectedHour
                        } else {
                            if (selectedHour == 12) 12 else selectedHour + 12
                        }
                        onTimeSelected(hour, selectedMinute)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    // DateTimePicker Dialog
    if (showStartDateTimePicker) {
        val datePickerState = rememberDatePickerState()
        var showTimePicker by remember { mutableStateOf(false) }
        
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onTimeSelected = { hour, minute ->
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        startDate = calendar.time
                        startTime = formatTime(calendar.time)
                    }
                    showStartDateTimePicker = false
                }
            )
        } else {
            DatePickerDialog(
                onDismissRequest = { showStartDateTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (datePickerState.selectedDateMillis != null) {
                            showTimePicker = true
                        }
                    }) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDateTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    if (showEndDateTimePicker) {
        val datePickerState = rememberDatePickerState()
        var showTimePicker by remember { mutableStateOf(false) }
        
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onTimeSelected = { hour, minute ->
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        endDate = calendar.time
                        endTime = formatTime(calendar.time)
                    }
                    showEndDateTimePicker = false
                }
            )
        } else {
            DatePickerDialog(
                onDismissRequest = { showEndDateTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        if (datePickerState.selectedDateMillis != null) {
                            showTimePicker = true
                        }
                    }) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDateTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showtimeId == "new") "Add New Showtime" else "Edit Showtime") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                // Movie Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Movie",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        ExposedDropdownMenuBox(
                            expanded = movieExpanded,
                            onExpandedChange = { movieExpanded = it },
                        ) {
                            TextField(
                                value = movies.find { it.id == selectedMovieId }?.title ?: "Select Movie",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = movieExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = DarkNavyLight,
                                    unfocusedContainerColor = DarkNavyLight,
                                    cursorColor = AccentColor,
                                    focusedIndicatorColor = AccentColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = movieExpanded,
                                onDismissRequest = { movieExpanded = false },
                                modifier = Modifier.background(DarkNavyLight)
                            ) {
                                nowShowingMovies.forEach { movie ->
                                    DropdownMenuItem(
                                        text = { Text(movie.title, color = Color.White) },
                                        onClick = { 
                                            selectedMovieId = movie.id
                                            movieExpanded = false
                                        },
                                        modifier = Modifier.background(DarkNavyLight)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cinema Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cinema",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        ExposedDropdownMenuBox(
                            expanded = cinemaExpanded,
                            onExpandedChange = { cinemaExpanded = it },
                        ) {
                            TextField(
                                value = cinemas.find { it.id == selectedCinemaId }?.name ?: "Select Cinema",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cinemaExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = DarkNavyLight,
                                    unfocusedContainerColor = DarkNavyLight,
                                    cursorColor = AccentColor,
                                    focusedIndicatorColor = AccentColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = cinemaExpanded,
                                onDismissRequest = { cinemaExpanded = false },
                                modifier = Modifier.background(DarkNavyLight)
                            ) {
                                cinemas.forEach { cinema ->
                                    DropdownMenuItem(
                                        text = { Text(cinema.name, color = Color.White) },
                                        onClick = { 
                                            selectedCinemaId = cinema.id
                                            selectedScreenId = "" // Reset screen selection
                                            cinemaExpanded = false
                                        },
                                        modifier = Modifier.background(DarkNavyLight)
                                    )
                                }
                            }
                        }
                    }
                }

                // Screen Selection
                if (selectedCinemaId.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Screen",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            ExposedDropdownMenuBox(
                                expanded = screenExpanded,
                                onExpandedChange = { screenExpanded = it },
                            ) {
                                TextField(
                                    value = screens.find { it.id == selectedScreenId }?.let { "Screen ${it.screenNumber}" } ?: "Select Screen",
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = screenExpanded) },
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = DarkNavyLight,
                                        unfocusedContainerColor = DarkNavyLight,
                                        cursorColor = AccentColor,
                                        focusedIndicatorColor = AccentColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = screenExpanded,
                                    onDismissRequest = { screenExpanded = false },
                                    modifier = Modifier.background(DarkNavyLight)
                                ) {
                                    screens.forEach { screen ->
                                        DropdownMenuItem(
                                            text = { Text("Screen ${screen.screenNumber}", color = Color.White) },
                                            onClick = { 
                                                selectedScreenId = screen.id
                                                screenExpanded = false
                                            },
                                            modifier = Modifier.background(DarkNavyLight)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Format Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Format",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        ExposedDropdownMenuBox(
                            expanded = formatExpanded,
                            onExpandedChange = { formatExpanded = it },
                        ) {
                            TextField(
                                value = selectedFormat,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = DarkNavyLight,
                                    unfocusedContainerColor = DarkNavyLight,
                                    cursorColor = AccentColor,
                                    focusedIndicatorColor = AccentColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = formatExpanded,
                                onDismissRequest = { formatExpanded = false },
                                modifier = Modifier.background(DarkNavyLight)
                            ) {
                                formats.forEach { format ->
                                    DropdownMenuItem(
                                        text = { Text(format, color = Color.White) },
                                        onClick = { 
                                            selectedFormat = format
                                            formatExpanded = false
                                        },
                                        modifier = Modifier.background(DarkNavyLight)
                                    )
                                }
                            }
                        }
                    }
                }

                // Time Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        
                        // Start Time
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { },
                            label = { Text("Start Time", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showStartDateTimePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date and Time", tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = AccentColor,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = AccentColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // End Time
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { },
                            label = { Text("End Time", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showEndDateTimePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Select Date and Time", tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = AccentColor,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = AccentColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                // Price Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Price",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price", color = Color.White) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = AccentColor,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = AccentColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        if (startDate != null && endDate != null) {
                            val priceDouble = price.toDoubleOrNull() ?: 0.0
                            viewModel.saveShowtime(
                                movieId = selectedMovieId,
                                cinemaId = selectedCinemaId,
                                screenId = selectedScreenId,
                                format = selectedFormat,
                                startTime = startDate!!,
                                endTime = endDate!!,
                                price = priceDouble
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        disabledContainerColor = AccentColor.copy(alpha = 0.5f)
                    ),
                    enabled = !isLoading && 
                        selectedMovieId.isNotBlank() &&
                        selectedCinemaId.isNotBlank() && 
                        selectedScreenId.isNotBlank() &&
                        startDate != null && 
                        endDate != null &&
                        price.isNotBlank() &&
                        price.toDoubleOrNull() != null &&
                        price.toDoubleOrNull()!! > 0
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (showtimeId == "new") "Add Showtime" else "Update Showtime",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    return formatTime(date)
}

private fun formatTime(date: Date): String {
    val timeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
    return timeFormat.format(date)
}

private fun parseTime(time: String): Date? {
    return try {
        val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        timeFormat.parse(time)
    } catch (e: Exception) {
        null
    }
} 