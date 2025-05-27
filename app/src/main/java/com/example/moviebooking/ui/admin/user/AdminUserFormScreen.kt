package com.example.moviebooking.ui.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.MembershipLevel
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.ui.theme.DarkNavy
import com.example.moviebooking.ui.theme.DarkNavyLight
import com.example.moviebooking.ui.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserFormScreen(
    userId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: AdminUsersViewModel = viewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var membershipPoints by remember { mutableStateOf("0") }
    var membershipLevel by remember { mutableStateOf(MembershipLevel.BASIC) }

    // Load user data when screen is first displayed
    LaunchedEffect(userId) {
        if (userId != "new") {
            viewModel.loadUser(userId)
        }
    }

    // Update form fields when user data is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            fullName = user.fullName
            phoneNumber = user.phoneNumber ?: ""
            membershipPoints = user.membershipPoints.toString()
            membershipLevel = user.membershipLevel
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (userId == "new") "Add User" else "Edit User", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val updatedUser = currentUser?.copy(
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    membershipPoints = membershipPoints.toIntOrNull() ?: 0,
                                    membershipLevel = membershipLevel
                                ) ?: UserModel(
                                    uid = userId,
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    membershipPoints = membershipPoints.toIntOrNull() ?: 0,
                                    membershipLevel = membershipLevel
                                )
                                viewModel.updateUser(updatedUser)
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyLight)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentColor
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // User Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "User Information",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Name",
                                        tint = AccentColor
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    cursorColor = AccentColor,
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedLabelColor = AccentColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        tint = AccentColor
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    cursorColor = AccentColor,
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedLabelColor = AccentColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    // Membership Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Membership Information",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )

                            OutlinedTextField(
                                value = membershipPoints,
                                onValueChange = { membershipPoints = it },
                                label = { Text("Membership Points", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Points",
                                        tint = AccentColor
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    cursorColor = AccentColor,
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedLabelColor = AccentColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Text(
                                text = "Membership Level",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MembershipLevel.values().take(3).forEach { level ->
                                        FilterChip(
                                            selected = membershipLevel == level,
                                            onClick = { membershipLevel = level },
                                            label = { Text(level.toString(), color = if (membershipLevel == level) DarkNavy else Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AccentColor,
                                                selectedLabelColor = DarkNavy,
                                                containerColor = DarkNavy,
                                                labelColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MembershipLevel.values().drop(3).forEach { level ->
                                        FilterChip(
                                            selected = membershipLevel == level,
                                            onClick = { membershipLevel = level },
                                            label = { Text(level.toString(), color = if (membershipLevel == level) DarkNavy else Color.White) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AccentColor,
                                                selectedLabelColor = DarkNavy,
                                                containerColor = DarkNavy,
                                                labelColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Membership Level Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Membership Level Information",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Silver",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "SILVER: > 200 points",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Gold",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "GOLD: > 600 points",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Diamond",
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "DIAMOND: > 1200 points",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "PREMIUM: Limited",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 