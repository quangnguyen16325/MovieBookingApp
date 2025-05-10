package com.example.moviebooking.ui.cinema

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.moviebooking.R
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.BackgroundLight
import com.example.moviebooking.ui.theme.DarkNavy
import com.example.moviebooking.ui.theme.DarkNavyLight
import com.example.moviebooking.ui.theme.SurfaceLight
import com.example.moviebooking.ui.theme.TextPrimaryLight
import com.example.moviebooking.ui.theme.TextSecondaryLight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaScreen(
    onNavigateBack: () -> Unit,
    onCinemaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CinemaViewModel = viewModel()
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (!isDarkTheme) DarkNavy else BackgroundLight
    val surfaceColor = if (!isDarkTheme) DarkNavyLight else SurfaceLight
    val textPrimaryColor = if (!isDarkTheme) Color.White else TextPrimaryLight
    val textSecondaryColor = if (!isDarkTheme) Color.White.copy(alpha = 0.7f) else TextSecondaryLight

    val cinemas by viewModel.cinemas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cinemas",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = textPrimaryColor,
                    navigationIconContentColor = AccentColor
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AccentColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(backgroundColor),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cinemas) { cinema ->
                    CinemaCard(
                        cinema = cinema,
//                        onClick = { onCinemaClick(cinema.id) },
                        onClick = {  },
                        textPrimaryColor = textPrimaryColor,
                        textSecondaryColor = textSecondaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun CinemaCard(
    cinema: CinemaModel,
    onClick: () -> Unit,
    textPrimaryColor: Color,
    textSecondaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isSystemInDarkTheme()) DarkNavyLight else SurfaceLight
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Cinema Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cinema.imageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_cinema)
                    .error(R.drawable.placeholder_cinema)
                    .build(),
                contentDescription = "Cinema Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Cinema Name
                Text(
                    text = cinema.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimaryColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = AccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cinema.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Facilities
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Facilities",
                        tint = AccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cinema.facilities.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
} 