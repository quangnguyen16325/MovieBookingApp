package com.example.moviebooking.ui.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviebooking.R
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.ui.theme.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllMoviesScreen(
    movies: List<MovieModel>,
    onNavigateBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    title: String = "Now Showing"
) {
    // Luôn sử dụng màu tối
    val backgroundColor = DarkNavy
    val surfaceColor = DarkNavyLight
    val textPrimaryColor = Color.White
    val textSecondaryColor = Color.White.copy(alpha = 0.7f)

    // Đảm bảo danh sách phim không rỗng
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No movies available",
                style = MaterialTheme.typography.titleLarge,
                color = textPrimaryColor
            )
        }
        return
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { Int.MAX_VALUE }
            )

            // Tính toán actualPage an toàn
            val actualPage = (pagerState.currentPage % movies.size).coerceAtLeast(0)

            // Background Blur Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movies[actualPage].posterUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.15f
                        scaleX = 1.5f
                        scaleY = 1.5f
                    }
            )

            // Dark Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 24.dp
            ) { page ->
                // Tính toán actualIndex an toàn
                val actualIndex = (page % movies.size).coerceAtLeast(0)
                val pageOffset = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                ).absoluteValue

                val scale = lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )

                val rotation = lerp(
                    start = 15f,
                    stop = 0f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ) * if (page < pagerState.currentPage) -1 else 1

                val alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )

                MovieCard(
                    movie = movies[actualIndex],
                    onClick = { onMovieClick(movies[actualIndex].id) },
                    textPrimaryColor = textPrimaryColor,
                    textSecondaryColor = textSecondaryColor,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationZ = rotation
                            this.alpha = alpha
                        }
                )
            }

            // Page Indicator
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(movies.size) { iteration ->
                    val color = if (actualPage == iteration) AccentColor else Color.White.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieModel,
    onClick: () -> Unit,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavyLight
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Movie Poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_movie)
                    .error(R.drawable.placeholder_movie)
                    .build(),
                contentDescription = "Movie Poster",
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
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 400f,
                            endY = 1200f
                        )
                    )
            )

            // Movie Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Rating Badge
                Card(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .align(Alignment.Start),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = movie.rating.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Movie Title
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 3f
                        )
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Genre with improved visibility
                Text(
                    text = movie.genres.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration with improved visibility
                Text(
                    text = "${movie.duration} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Booking Button with improved visibility
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "Book Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
} 