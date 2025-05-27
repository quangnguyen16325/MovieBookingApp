package com.example.moviebooking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.navigation.AppNavigation
import com.example.moviebooking.ui.navigation.Screen
import com.example.moviebooking.ui.theme.MovieBookingTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil.request.CachePolicy
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.DarkNavy
import com.example.moviebooking.ui.theme.DarkNavyLight
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieBookingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MovieBookingApp()
                }
            }
        }
    }
}

@Composable
fun MovieBookingApp() {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    val userState by authViewModel.currentUser.collectAsState()
    val profileImage = userState?.profileImage

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    // Variable to track which screen we're on
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkNavy,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
            ) {
                // User Profile Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkNavy)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Image
                        if (profileImage != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(80.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // User Name
                        if (currentUser != null) {
                            Text(
                                text = currentUser?.fullName ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "Guest User",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Drawer Items
                DrawerItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen == Screen.Home,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Movie,
                    label = "Cinemas",
                    isSelected = currentScreen == Screen.Cinemas,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(Screen.Cinemas.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                DrawerItem(
                    icon = Icons.Default.ConfirmationNumber,
                    label = "My Bookings",
                    isSelected = currentScreen == Screen.Bookings,
                    onClick = {
                        navigateToScreen(navController, Screen.Bookings.route, scope) {
                            drawerState.close()
                        }
                    },
                    tint = Color.White
                )

                DrawerItem(
                    icon = Icons.Default.Star,
                    label = "Membership",
                    isSelected = currentScreen == Screen.Membership,
                    onClick = {
                        navigateToScreen(navController, Screen.Membership.route, scope) {
                            drawerState.close()
                        }
                    },
                    tint = Color.White
                )

                DrawerItem(
                    icon = Icons.Default.AccountCircle,
                    label = "Profile",
                    isSelected = currentScreen == Screen.Profile,
                    onClick = {
                        navigateToScreen(navController, Screen.Profile.route, scope) {
                            drawerState.close()
                        }
                    },
                    tint = Color.White
                )

                DrawerItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = false,
                    onClick = {
                        // TODO: Implement settings screen
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    tint = Color.White
                )

                DrawerItem(
                    icon = Icons.Default.Info,
                    label = "About",
                    isSelected = currentScreen == Screen.About,
                    onClick = {
                        navigateToScreen(navController, Screen.About.route, scope) {
                            drawerState.close()
                        }
                    },
                    tint = Color.White
                )

                // Add Admin System item for admin users
                if (isAdmin == true) {
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    DrawerItem(
                        icon = Icons.Default.Settings,
                        label = "Admin System",
                        isSelected = currentScreen == Screen.AdminDashboard,
                        onClick = {
                            navigateToScreen(navController, Screen.AdminDashboard.route, scope) {
                                drawerState.close()
                            }
                        },
                        tint = AccentColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Divider(color = Color.White.copy(alpha = 0.2f))

                // Logout Button
                if (currentUser != null) {
                    DrawerItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Logout",
                        isSelected = false,
                        onClick = {
                            scope.launch {
                                authViewModel.logout()
                                drawerState.close()
                                // Navigate to login screen
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }
                        },
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        content = {
            AppNavigation(
                navController = navController,
                startGoogleSignIn = { /* Implement Google Sign-In */ },
                startFacebookSignIn = { /* Implement Facebook Sign-In */ },
                authViewModel = authViewModel,
                onNavigationIconClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onScreenChange = { screen ->
                    currentScreen = screen
                }
            )
        }
    )
}

// Helper function to navigate to a screen and run an optional action afterwards
private fun navigateToScreen(
    navController: NavHostController,
    route: String,
    scope: kotlinx.coroutines.CoroutineScope,
    afterNavigate: suspend () -> Unit
) {
    scope.launch {
        navController.navigate(route) {
            // Pop up to the start destination of the graph to avoid building up
            // a large stack of destinations on the back stack
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when navigating back to a previously visited screen
            restoreState = true
        }
        afterNavigate()
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    val backgroundColor = if (isSelected) {
        AccentColor.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        AccentColor
    } else {
        tint
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

fun addSampleMovies() {
    val firestore = FirebaseFirestore.getInstance()

    // Phim đang chiếu
    val nowShowingMovie = hashMapOf(
        "title" to "Avengers: Endgame",
        "overview" to "After the devastating events of Avengers: Infinity War, the universe is in ruins...",
        "posterUrl" to "https://m.media-amazon.com/images/M/MV5BMTc5MDE2ODcwNV5BMl5BanBnXkFtZTgwMzI2NzQ2NzM@._V1_.jpg",
        "backdropUrl" to "https://images.hdqwalls.com/wallpapers/avengers-endgame-2019-movie-poster-y7.jpg",
        "duration" to 181,
        "genres" to listOf("Action", "Adventure", "Sci-Fi"),
        "rating" to 8.4,
        "cast" to listOf("Robert Downey Jr.", "Chris Evans", "Mark Ruffalo"),
        "director" to "Russo Brothers",
        "trailerUrl" to "https://www.youtube.com/watch?v=TcMBFSGVi1c",
        "isNowShowing" to true,
        "isComingSoon" to false
    )

    // Phim sắp chiếu
    val comingSoonMovie = hashMapOf(
        "title" to "Deadpool 3",
        "overview" to "The Merc with a Mouth teams up with Wolverine in this highly anticipated sequel...",
        "posterUrl" to "https://m.media-amazon.com/images/M/MV5BMGI1ZTFmY2YtZGYxNi00MjM0LTlhZDYtMzVlODllYTRjOTc1XkEyXkFqcGdeQXVyMDM2NDM2MQ@@._V1_.jpg",
        "backdropUrl" to "https://i0.wp.com/thefutureoftheforce.com/wp-content/uploads/2022/09/deadpool-wolverine.jpg",
        "duration" to 120,
        "genres" to listOf("Action", "Comedy", "Adventure"),
        "rating" to 9.0,
        "cast" to listOf("Ryan Reynolds", "Hugh Jackman", "Emma Corrin"),
        "director" to "Shawn Levy",
        "trailerUrl" to "https://www.youtube.com/watch?v=LKCkDp_QBTE",
        "isNowShowing" to false,
        "isComingSoon" to true
    )

    // Thêm phim vào Firestore
    firestore.collection("movies").add(nowShowingMovie)
        .addOnSuccessListener { documentReference ->
            Log.d("Firebase", "Added now showing movie with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error adding now showing movie", e)
        }

    firestore.collection("movies").add(comingSoonMovie)
        .addOnSuccessListener { documentReference ->
            Log.d("Firebase", "Added coming soon movie with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error adding coming soon movie", e)
        }
}

@Composable
fun MainScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit
) {
    val isAdmin by viewModel.isAdmin.collectAsState()
    val authState by remember { mutableStateOf(viewModel.authState) }

    LaunchedEffect(authState) {
        when (authState) {
            AuthViewModel.AuthState.AUTHENTICATED -> {
                if (isAdmin) {
                    onNavigateToAdminDashboard()
                }
            }
            AuthViewModel.AuthState.UNAUTHENTICATED -> {
                onNavigateToLogin()
            }
            AuthViewModel.AuthState.EMAIL_NOT_VERIFIED -> {
                onNavigateToEmailVerification()
            }
            else -> {}
        }
    }
}