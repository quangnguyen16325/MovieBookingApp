package com.example.moviebooking.ui.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.ui.theme.DarkNavy
import com.example.moviebooking.ui.theme.DarkNavyLight
import com.example.moviebooking.ui.theme.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    onNavigateBack: () -> Unit,
    onEditUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    val viewModel: AdminUsersViewModel = viewModel()
    val users by viewModel.users.collectAsState()
    val filteredUsers by viewModel.filteredUsers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

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
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Manage Users", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavy,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
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
                        .background(DarkNavyLight)
                        .padding(16.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        placeholder = { Text("Search users..") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = AccentColor
                        ),
                        singleLine = true
                    )

                    // User List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserItem(
                                user = user,
                                onEditClick = { onEditUser(user.uid) },
                                onDeleteClick = { onDeleteUser(user.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: UserModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Phone: ${user.phoneNumber ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Membership: ${user.membershipLevel} (${user.membershipPoints} points)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User", color = Color.White) },
            text = { Text("Are you sure you want to delete this user?", color = Color.White) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = DarkNavy
        )
    }
} 