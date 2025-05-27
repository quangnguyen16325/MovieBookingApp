package com.example.moviebooking.ui.admin.movie

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMovieFormScreen(
    movieId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminMovieFormViewModel = viewModel(
        factory = AdminMovieFormViewModel.Factory(movieId, LocalContext.current)
    )
) {
    val movie by viewModel.movie.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val posterUrl by viewModel.posterUrl.collectAsState()
    val backdropUrl by viewModel.backdropUrl.collectAsState()

    var title by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var genres by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var cast by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var trailerUrl by remember { mutableStateOf("") }
    var isNowShowing by remember { mutableStateOf(false) }
    var isComingSoon by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(movie) {
        movie?.let {
            title = it.title
            overview = it.overview
            duration = it.duration.toString()
            genres = it.genres.joinToString(", ")
            rating = it.rating.toString()
            cast = it.cast.joinToString(", ")
            director = it.director
            trailerUrl = it.trailerUrl
            isNowShowing = it.isNowShowing
            isComingSoon = it.isComingSoon
        }
    }

    LaunchedEffect(movieId) {
        if (movieId == "new") {
            title = ""
            overview = ""
            duration = ""
            genres = ""
            rating = ""
            cast = ""
            director = ""
            trailerUrl = ""
            isNowShowing = false
            isComingSoon = false
        }
    }

    LaunchedEffect(saveResult) {
        saveResult?.onSuccess {
            onNavigateBack()
        }
    }

    val posterImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPosterImage(it) }
    }

    val backdropImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadBackdropImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (movieId == "new") "Add New Movie" else "Edit Movie") },
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
        }
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
                // Image Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Movie Images",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        // Poster Image
                        Column {
                            Text(
                                text = "Movie Poster",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                            ) {
                                if (posterUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(posterUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Movie Poster",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { posterImagePicker.launch("image/*") },
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Icon(
                                        imageVector = if (posterUrl == null) Icons.Default.Add else Icons.Default.Edit,
                                        contentDescription = "Select Poster",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Backdrop Image
                        Column {
                            Text(
                                text = "Backdrop Image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                            ) {
                                if (backdropUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(backdropUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Movie Backdrop",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { backdropImagePicker.launch("image/*") },
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Icon(
                                        imageVector = if (backdropUrl == null) Icons.Default.Add else Icons.Default.Edit,
                                        contentDescription = "Select Backdrop",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Movie Details Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Movie Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = overview,
                            onValueChange = { overview = it },
                            label = { Text("Overview") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (minutes)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = genres,
                            onValueChange = { genres = it },
                            label = { Text("Genres (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = rating,
                            onValueChange = { rating = it },
                            label = { Text("Rating (0-10)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = cast,
                            onValueChange = { cast = it },
                            label = { Text("Cast (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = director,
                            onValueChange = { director = it },
                            label = { Text("Director") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = trailerUrl,
                            onValueChange = { trailerUrl = it },
                            label = { Text("Trailer URL (YouTube)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isNowShowing,
                                    onCheckedChange = { isNowShowing = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentColor,
                                        uncheckedColor = Color.White
                                    )
                                )
                                Text(
                                    text = "Now Showing",
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isComingSoon,
                                    onCheckedChange = { isComingSoon = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentColor,
                                        uncheckedColor = Color.White
                                    )
                                )
                                Text(
                                    text = "Coming Soon",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val durationInt = duration.toIntOrNull() ?: 0
                        val ratingDouble = rating.toDoubleOrNull() ?: 0.0
                        val genresList = genres.split(",").map { it.trim() }
                        val castList = cast.split(",").map { it.trim() }

                        viewModel.saveMovie(
                            title = title,
                            overview = overview,
                            duration = durationInt,
                            genres = genresList,
                            rating = ratingDouble,
                            cast = castList,
                            director = director,
                            trailerUrl = trailerUrl,
                            isNowShowing = isNowShowing,
                            isComingSoon = isComingSoon
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    enabled = !isLoading && title.isNotBlank() && overview.isNotBlank() &&
                            duration.isNotBlank() && genres.isNotBlank() && rating.isNotBlank() &&
                            cast.isNotBlank() && director.isNotBlank() && trailerUrl.isNotBlank() &&
                            posterUrl != null && backdropUrl != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Save Movie")
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