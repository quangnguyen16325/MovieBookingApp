package com.example.moviebooking.ui.admin.showtime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.ui.theme.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShowtimeListScreen(
    onNavigateToForm: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSeats: (String) -> Unit,
    viewModel: AdminShowtimeListViewModel = viewModel()
) {
    val showtimes by viewModel.showtimes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cinemas by viewModel.cinemas.collectAsState()
    val movies by viewModel.movies.collectAsState()
    var refreshTrigger by remember { mutableStateOf(0) }

    // Load showtimes when screen is first shown or when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        viewModel.loadShowtimes()
    }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Filtered showtimes
    val filteredShowtimes = showtimes.filter { showtime ->
        showtime.movieName.contains(searchQuery, ignoreCase = true) ||
        showtime.cinemaName.contains(searchQuery, ignoreCase = true)
    }

    // Group filtered showtimes by date
    val groupedShowtimes = filteredShowtimes.groupBy { showtime ->
        showtime.startTime?.toDate()?.let { date ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } ?: ""
    }.toSortedMap(reverseOrder())

    // State to track expanded dates
    var expandedDates by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Manage Showtimes") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToForm("new") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Showtime")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyLight)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentColor
                )
            } else if (filteredShowtimes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) 
                            "No matching showtimes found" 
                        else 
                            "No showtimes found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedShowtimes.forEach { (date, showtimesForDate) ->
                        item {
                            DateHeader(
                                date = date,
                                isExpanded = expandedDates.contains(date),
                                onClick = {
                                    expandedDates = if (expandedDates.contains(date)) {
                                        expandedDates - date
                                    } else {
                                        expandedDates + date
                                    }
                                }
                            )
                        }
                        item {
                            AnimatedVisibility(
                                visible = expandedDates.contains(date),
                                enter = expandVertically(animationSpec = tween(300)),
                                exit = shrinkVertically(animationSpec = tween(300))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    showtimesForDate.forEach { showtime ->
                                        ShowtimeCard(
                                            showtime = showtime,
                                            onEdit = { onNavigateToForm(showtime.id) },
                                            onDelete = { viewModel.deleteShowtime(showtime.id) },
                                            onNavigateToSeats = { onNavigateToSeats(showtime.id) }
                                        )
                                    }
                                }
                            }
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search showtimes") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
            cursorColor = AccentColor,
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f)
        )
    )
}

@Composable
fun DateHeader(
    date: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowtimeCard(
    showtime: ShowtimeModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToSeats: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = showtime.movieName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = showtime.cinemaName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Screen ${showtime.screenId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Row {
                    IconButton(onClick = onNavigateToSeats) {
                        Icon(
                            Icons.Default.EventSeat,
                            contentDescription = "View Seats",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatTime(showtime.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatTime(showtime.endTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Format",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = showtime.format,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatPrice(showtime.price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Showtime") },
            text = { Text("Are you sure you want to delete this showtime?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        val date = inputFormat.parse(dateStr)
        date?.let { outputFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatTime(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("hh:mm a", Locale.US)
    return formatter.format(date)
}

private fun formatPrice(price: Double): String {
    return String.format("%,.0f VND", price)
} 