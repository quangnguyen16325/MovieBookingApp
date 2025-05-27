package com.example.moviebooking.ui.admin.showtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.SeatModel
import com.example.moviebooking.data.model.SeatType
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShowtimeSeatsScreen(
    showtimeId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminShowtimeSeatsViewModel = viewModel(
        factory = AdminShowtimeSeatsViewModel.Factory(showtimeId)
    )
) {
    val showtime by viewModel.showtime.collectAsState()
    val movie by viewModel.movie.collectAsState()
    val cinema by viewModel.cinema.collectAsState()
    val seats by viewModel.seats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Showtime Seats", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentColor
                )
            } else if (movie != null && showtime != null && cinema != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Movie and Showtime Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = movie!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${viewModel.formatDate(showtime!!.date)} • " +
                                        "${viewModel.formatTime(showtime!!.startTime)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = cinema!!.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = showtime!!.format,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Screen ${showtime!!.screenId.substringAfterLast("_")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

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
                            color = SeatUnavailable,
                            text = "Booked"
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
                                modifier = Modifier.width(20.dp),
                                color = Color.White
                            )

                            // Seats in this row
                            rowSeats.sortedBy { it.number }.forEach { seat ->
                                SeatItem(seat = seat)
                            }
                        }
                    }

                    // Booking Summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Booking Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Seats",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )

                                Text(
                                    text = "${seats.count { !it.isAvailable }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Available Seats",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )

                                Text(
                                    text = "${seats.count { it.isAvailable }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
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
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
fun SeatItem(
    seat: SeatModel
) {
    val backgroundColor = if (seat.isAvailable) SeatAvailable else SeatUnavailable
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

    val textColor = if (seat.isAvailable) Color.Black else Color.Gray

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seat.number.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
} 