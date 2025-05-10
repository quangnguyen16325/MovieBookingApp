package com.example.moviebooking.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviebooking.R
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.components.MovieCarousel
import kotlinx.coroutines.delay
import com.example.moviebooking.ui.theme.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToMovieDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToCinemas: () -> Unit,
    onNavigateToSeeAllMovies: (List<MovieModel>, String) -> Unit,
    onNavigateToSeeAllComingSoon: (List<MovieModel>, String) -> Unit,
    onNavigationIconClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (!isDarkTheme) DarkNavy else BackgroundLight
    val surfaceColor = if (!isDarkTheme) DarkNavyLight else SurfaceLight
    val textPrimaryColor = if (!isDarkTheme) Color.White else TextPrimaryLight
    val textSecondaryColor = if (!isDarkTheme) Color.White.copy(alpha = 0.7f) else TextSecondaryLight

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val nowShowingMovies by homeViewModel.nowShowingMovies.collectAsState()
    val comingSoonMovies by homeViewModel.comingSoonMovies.collectAsState()
    val isNowShowingLoading by homeViewModel.isNowShowingLoading.collectAsState()
    val isComingSoonLoading by homeViewModel.isComingSoonLoading.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()

    val currentUser by authViewModel.currentUser.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            homeViewModel.clearError()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            homeViewModel.refreshAllMovies()
            isRefreshing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.cineai_1),
//                            contentDescription = "Cine AI",
//                            modifier = Modifier.height(32.dp)
//                        )
                        Text(
                            text = "CINE AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = AccentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AccentColor
                        )
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = AccentColor
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        val userState by authViewModel.currentUser.collectAsState()
                        val profileImage = userState?.profileImage

                        if (profileImage.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp),
                                tint = AccentColor
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImage)
                                    .crossfade(true)
                                    .placeholder(R.drawable.ic_default_user)
                                    .error(R.drawable.ic_default_user)
                                    .build(),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = textPrimaryColor,
                    actionIconContentColor = textPrimaryColor
                )
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { isRefreshing = true },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .verticalScroll(rememberScrollState())
            ) {
                // Featured Slider / Carousel
                FeaturedMovieBanner(
                    movies = nowShowingMovies.take(5),
                    onMovieClick = { onNavigateToMovieDetail(it.id) }
                )

                // Chào mừng người dùng
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = textSecondaryColor
                        )
                    )
                    Text(
                        text = currentUser?.fullName ?: "Guest",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor
                        )
                    )
                }

                // Quick Access Card
                QuickAccessCard(
                    onBookingsClick = onNavigateToBookings,
                    onProfileClick = onNavigateToProfile,
                    onCinemasClick = onNavigateToCinemas
                )

                Spacer(modifier = Modifier.height(0.dp))

                // Now Showing Movies
                MovieCarousel(
                    title = "Now Showing",
                    movies = nowShowingMovies,
                    isLoading = isNowShowingLoading,
                    onMovieClick = { onNavigateToMovieDetail(it.id) },
                    onSeeAllClick = { onNavigateToSeeAllMovies(nowShowingMovies, "Now Showing") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Coming Soon Movies
                MovieCarousel(
                    title = "Coming Soon",
                    movies = comingSoonMovies,
                    isLoading = isComingSoonLoading,
                    onMovieClick = { onNavigateToMovieDetail(it.id) },
                    onSeeAllClick = { onNavigateToSeeAllComingSoon(comingSoonMovies, "Coming Soon") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FeaturedMovieBanner(
    movies: List<MovieModel>,
    onMovieClick: (MovieModel) -> Unit
) {
    val pagerState = rememberPagerState { movies.size }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (movies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No featured movies",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSystemInDarkTheme()) TextSecondaryDark else TextSecondaryLight
                    )
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onMovieClick(movies[page]) }
                    ) {
                        // Backdrop Image
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(movies[page].backdropUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            DarkNavy.copy(alpha = 0.8f)
                                        ),
                                        startY = 0f,
                                        endY = 500f
                                    )
                                )
                        )

                        // Movie Info
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = movies[page].title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Genre and Duration
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (movies[page].genres.isNotEmpty()) {
                                    Text(
                                        text = movies[page].genres.joinToString(", ").take(20),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(Color.White.copy(alpha = 0.5f), CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Text(
                                    text = "${movies[page].duration} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Indicators
                Row(
                    Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(movies.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) AccentColor else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }
    }

    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (movies.isNotEmpty()) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % movies.size)
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCinemasClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cardBackgroundColor = if (!isDarkTheme) DarkNavyLight else SurfaceLight
    val textPrimaryColor = if (!isDarkTheme) Color.White else TextPrimaryLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // My Bookings
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBookingsClick)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "My Bookings",
                            tint = AccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "My Bookings",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = textPrimaryColor
                    )
                }

                // My Profile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onProfileClick)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "My Profile",
                            tint = AccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "My Profile",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = textPrimaryColor
                    )
                }

                // Find Cinemas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCinemasClick)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Find Cinemas",
                            tint = AccentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Cinemas",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = textPrimaryColor
                    )
                }
            }
        }
    }
}