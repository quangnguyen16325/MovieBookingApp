package com.example.moviebooking.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToMovies: () -> Unit,
    onNavigateToCinemas: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToShowtimes: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToMembership: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavyLight),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Admin Panel",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your movie booking system",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Management Cards
            AdminCard(
                title = "Movies",
                description = "Manage movie listings and details",
                icon = Icons.Default.Movie,
                onClick = onNavigateToMovies
            )

            AdminCard(
                title = "Cinemas",
                description = "Manage cinema locations and halls",
                icon = Icons.Default.LocationOn,
                onClick = onNavigateToCinemas
            )

            AdminCard(
                title = "Users",
                description = "Manage user accounts and permissions",
                icon = Icons.Default.People,
                onClick = onNavigateToUsers
            )

            AdminCard(
                title = "Showtimes",
                description = "Manage movie showtimes and schedules",
                icon = Icons.Default.Schedule,
                onClick = onNavigateToShowtimes
            )

//            AdminCard(
//                title = "Bookings",
//                description = "View and manage booking records",
//                icon = Icons.Default.ConfirmationNumber,
//                onClick = onNavigateToBookings
//            )

            AdminCard(
                title = "Membership",
                description = "Manage membership levels and benefits",
                icon = Icons.Default.CardMembership,
                onClick = onNavigateToMembership
            )
        }
    }
}

@Composable
private fun AdminCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavyLight),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
} 