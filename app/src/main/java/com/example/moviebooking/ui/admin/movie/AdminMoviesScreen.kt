package com.example.moviebooking.ui.admin.movie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMoviesScreen(
    onNavigateBack: () -> Unit,
    onAddMovie: () -> Unit,
    onEditMovie: (String) -> Unit,
    onDeleteMovie: (String) -> Unit,
    viewModel: AdminMoviesViewModel = viewModel()
) {
    val movies by viewModel.filteredMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadMovies()
    }

    LaunchedEffect(searchQuery) {
        viewModel.searchMovies(searchQuery)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Manage Movies") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddMovie) {
                            Icon(Icons.Default.Add, contentDescription = "Add Movie")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search movies...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    singleLine = true
                )
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentColor
                )
            } else if (movies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No movies found" else "No movies available",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            onEdit = { onEditMovie(movie.id) },
                            onDelete = { showDeleteDialog = movie.id }
                        )
                    }
                }
            }

            errorMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    },
                    containerColor = DarkNavyLight,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

            showDeleteDialog?.let { movieId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete Movie") },
                    text = { Text("Are you sure you want to delete this movie?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteMovie(movieId)
                                showDeleteDialog = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Cancel")
                        }
                    },
                    containerColor = DarkNavyLight,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavyLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Poster
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = movie.genres.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${movie.duration} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AccentColor
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
} 