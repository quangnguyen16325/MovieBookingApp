package com.example.moviebooking.ui.movie

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moviebooking.data.model.SeatModel
import com.example.moviebooking.data.model.SeatType
import com.example.moviebooking.ui.components.MovieButton
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.SeatAvailable
import com.example.moviebooking.ui.theme.SeatSelected
import com.example.moviebooking.ui.theme.SeatUnavailable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSeatScreen(
    viewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onBookingComplete: (String) -> Unit
) {
    val showtime by viewModel.showtime.collectAsState()
    val movie by viewModel.movie.collectAsState()
    val cinema by viewModel.cinema.collectAsState()
    val seats by viewModel.seats.collectAsState()
    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val bookingResult by viewModel.bookingResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(bookingResult) {
        bookingResult?.onSuccess { booking ->
            onBookingComplete(booking.id)
        }
    }

    var showCheckoutPanel by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSeats) {
        showCheckoutPanel = selectedSeats.isNotEmpty()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Select Seats") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && movie == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentColor)
                }
            } else if (movie != null && showtime != null && cinema != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Movie and Showtime Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = movie!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                Text(
                                    text = "${viewModel.formatDate(showtime!!.date)} • " +
                                            "${viewModel.formatTime(showtime!!.startTime)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = cinema!!.name,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = showtime!!.format,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Screen ${showtime!!.screenId.substringAfterLast("_")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Screen indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )

                        Text(
                            text = "SCREEN",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Seats Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeatLegendItem(
                            color = SeatAvailable,
                            text = "Available"
                        )

                        SeatLegendItem(
                            color = SeatSelected,
                            text = "Selected"
                        )

                        SeatLegendItem(
                            color = SeatUnavailable,
                            text = "Unavailable"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seat Type Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SeatTypeLegendItem(
                            seatType = SeatType.STANDARD,
                            text = "Standard"
                        )

                        SeatTypeLegendItem(
                            seatType = SeatType.PREMIUM,
                            text = "Premium"
                        )

                        SeatTypeLegendItem(
                            seatType = SeatType.VIP,
                            text = "VIP"
                        )

                        SeatTypeLegendItem(
                            seatType = SeatType.COUPLE,
                            text = "Couple"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Seats Grid
                    val seatsByRow = seats.groupBy { it.row }
                    val rows = seatsByRow.keys.sorted()

                    for (row in rows) {
                        val rowSeats = seatsByRow[row] ?: continue

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Row label
                            Text(
                                text = row,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )

                            // Seats in this row
                            rowSeats.sortedBy { it.number }.forEach { seat ->
                                SeatItem(
                                    seat = seat,
                                    onSeatClick = { viewModel.toggleSeatSelection(seat) }
                                )
                            }
                        }
                    }

                    // Add extra space at bottom to account for checkout panel
                    if (showCheckoutPanel) {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Checkout Panel
                AnimatedVisibility(
                    visible = showCheckoutPanel,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${selectedSeats.size} Seats",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = viewModel.formatPrice(totalPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = selectedSeats.joinToString(", ") { "${it.row}${it.number}" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MovieButton(
                                text = "Proceed to Payment",
                                onClick = { viewModel.confirmBooking() },
                                isLoading = isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeatLegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SeatTypeLegendItem(
    seatType: SeatType,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(
                    width = when (seatType) {
                        SeatType.STANDARD -> 1.dp
                        SeatType.PREMIUM -> 1.dp
                        SeatType.VIP -> 2.dp
                        SeatType.COUPLE -> 3.dp
                    },
                    color = when (seatType) {
                        SeatType.STANDARD -> Color.Gray
                        SeatType.PREMIUM -> AccentColor
                        SeatType.VIP -> Color.Red
                        SeatType.COUPLE -> Color.Green
                    },
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SeatItem(
    seat: SeatModel,
    onSeatClick: () -> Unit
) {
    val backgroundColor = when {
        !seat.isAvailable -> SeatUnavailable
        seat.isSelected -> SeatSelected
        else -> SeatAvailable
    }

    val borderColor = when (seat.type) {
        SeatType.STANDARD -> Color.Gray
        SeatType.PREMIUM -> AccentColor
        SeatType.VIP -> Color.Red
        SeatType.COUPLE -> Color.Green
    }

    val borderWidth = when (seat.type) {
        SeatType.STANDARD -> 1.dp
        SeatType.PREMIUM -> 1.dp
        SeatType.VIP -> 2.dp
        SeatType.COUPLE -> 2.dp
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
            .clickable(enabled = seat.isAvailable) { onSeatClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.number.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (seat.isSelected) Color.White else Color.DarkGray
        )
    }
}