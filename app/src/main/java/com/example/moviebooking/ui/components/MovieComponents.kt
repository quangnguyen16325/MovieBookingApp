package com.example.moviebooking.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.moviebooking.R
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.DarkNavyLight
import com.example.moviebooking.ui.theme.ErrorColor
import com.example.moviebooking.ui.theme.PrimaryColor
import com.example.moviebooking.ui.theme.SurfaceDark
import com.example.moviebooking.ui.theme.SurfaceLight
import com.example.moviebooking.ui.theme.TextPrimaryDark
import com.example.moviebooking.ui.theme.TextPrimaryLight
import com.example.moviebooking.ui.theme.TextSecondaryDark
import com.example.moviebooking.ui.theme.TextSecondaryLight
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MovieCard(
    movie: MovieModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Luôn sử dụng màu tối
    val cardBackground = DarkNavyLight.copy(alpha = 0.7f)
    val textPrimaryColor = Color.White
    val textSecondaryColor = Color.White.copy(alpha = 0.7f)

    Card(
        modifier = modifier
            .width(160.dp)
            .height(290.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Movie Poster với chiều cao cố định
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Movie poster for ${movie.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val state = painter.state
                    if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state is AsyncImagePainter.State.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = PrimaryColor
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_error_outline),
                                    contentDescription = "Error loading image",
                                    tint = ErrorColor
                                )
                            }
                        }
                    } else {
                        SubcomposeAsyncImageContent()
                    }
                }

                // Gradient ở dưới poster
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Badge "NOW SHOWING" hoặc "COMING SOON"
                if (movie.isNowShowing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NOW SHOWING",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                } else if (movie.isComingSoon) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentColor.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMING SOON",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                }

                // Rating với star icon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", movie.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Movie details với chiều cao cố định
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Chiều cao cố định cho phần details
                    .background(cardBackground)
                    .padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = textPrimaryColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .height(40.dp) // Giới hạn chiều cao tiêu đề
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thể loại
                    if (movie.genres.isNotEmpty()) {
                        Text(
                            text = movie.genres.first(),
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Thời lượng
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = textSecondaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${movie.duration}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCarousel(
    title: String,
    movies: List<MovieModel>,
    isLoading: Boolean,
    onMovieClick: (MovieModel) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = true
    val textPrimaryColor =  Color.White
    val textSecondaryColor = Color.White.copy(alpha = 0.7f)

    Column(modifier = modifier.fillMaxWidth()) {
        // Header với title và nút See All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )

            TextButton(
                onClick = onSeeAllClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AccentColor
                )
            ) {
                Text(
                    text = "See All",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Movie list hoặc loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = if (!isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No movies available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (!isDarkTheme) TextSecondaryDark else TextSecondaryLight
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(movies.size) { index ->
                    MovieCard(
                        movie = movies[index],
                        onClick = { onMovieClick(movies[index]) }
                    )
                }
            }
        }
    }
}



