package com.example.moviebooking.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.BookingStatus
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.DarkNavy
import com.example.moviebooking.ui.theme.ErrorColor
import com.example.moviebooking.ui.theme.SuccessColor
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*
import com.example.moviebooking.data.repository.MovieRepository
import com.example.moviebooking.data.repository.CinemaRepository
import kotlinx.coroutines.launch
import com.example.moviebooking.util.DateFormats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onBackClick: () -> Unit,
    onBookingClick: (String) -> Unit,
    viewModel: BookingsViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

    // Define gradients
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Black,
            DarkNavy
        )
    )

    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.7f),
            Color.Black.copy(alpha = 0.9f)
        )
    )

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "My Bookings",
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.loadUserBookings() }
            ) {
                if (isLoading && bookings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentColor)
                    }
                } else if (bookings.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(0.3f))

                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Bookings Found",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Book a movie ticket to see your bookings here",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.weight(0.7f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(bookings.size) { index ->
                            val booking = bookings[index]

                            BookingItem(
                                booking = booking,
                                onClick = { onBookingClick(booking.id) }
                            )

                            if (index < bookings.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItem(
    booking: BookingModel,
    onClick: () -> Unit
) {
    val movieRepository = remember { MovieRepository() }
    val cinemaRepository = remember { CinemaRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var movieTitle by remember { mutableStateOf("Loading...") }
    var cinemaName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(booking.movieId, booking.cinemaId) {
        coroutineScope.launch {
            // Lấy thông tin phim
            movieRepository.getMovieById(booking.movieId)
                .onSuccess { movie ->
                    movieTitle = movie.title
                }
                .onFailure {
                    movieTitle = "Unknown Movie"
                }

            // Lấy thông tin rạp
            cinemaRepository.getCinemaById(booking.cinemaId)
                .onSuccess { cinema ->
                    cinemaName = cinema.name
                }
                .onFailure {
                    cinemaName = "Unknown Cinema"
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Status indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (booking.status) {
                                BookingStatus.CONFIRMED -> SuccessColor
                                BookingStatus.PENDING -> AccentColor
                                BookingStatus.CANCELLED -> ErrorColor
                                BookingStatus.COMPLETED -> Color.Gray
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Movie Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = movieTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cinema
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = cinemaName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date and Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = booking.bookingDate?.toDate()?.let {
                            DateFormats.FULL_DATE_TIME.format(it)
                        } ?: "Unknown Date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seats and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = AccentColor
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${booking.seats.size} seats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    Text(
                        text = String.format("%,.0f VND", booking.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor
                    )
                }
            }
        }
    }
}