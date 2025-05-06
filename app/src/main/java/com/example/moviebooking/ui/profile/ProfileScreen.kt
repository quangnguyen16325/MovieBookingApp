package com.example.moviebooking.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.moviebooking.R
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.components.MovieButton
import com.example.moviebooking.ui.components.MovieTextField
import com.example.moviebooking.ui.components.PasswordTextField

@Composable
fun ProfileScreenWrapper(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        GuestProfileScreen(
            onBackClick = onBackClick,
            onLoginClick = onNavigateToLogin,
            onAboutClick = onNavigateToAbout
        )
    } else {
        ProfileScreen(
            onBackClick = onBackClick,
            onNavigateToBookings = onNavigateToBookings,
            onNavigateToAbout = onNavigateToAbout,
            onLogout = onLogout
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Edit profile form state
    var editFullName by remember { mutableStateOf("") }
    var editPhoneNumber by remember { mutableStateOf("") }

    // Change password form state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Initialize edit form with current values when profile is loaded
    LaunchedEffect(userProfile) {
        userProfile?.let {
            editFullName = it.fullName
            editPhoneNumber = it.phoneNumber ?: ""
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && userProfile == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading profile...")
                }
            } else if (userProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Image
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(data = userProfile?.profileImage ?: R.drawable.ic_default_user)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                // Edit Icon (decorative only)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // User Name
                            Text(
                                text = userProfile?.fullName ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Email
                            Text(
                                text = userProfile?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Account Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Email
                            ProfileInfoItem(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = userProfile?.email ?: ""
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Phone
                            ProfileInfoItem(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = userProfile?.phoneNumber ?: "Not set"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Member Since
                            ProfileInfoItem(
                                icon = Icons.Default.CalendarToday,
                                label = "Member Since",
                                value = userProfile?.createdAt?.let {
                                    viewModel.formatDate(it)
                                } ?: "Unknown"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // My Bookings
                            ActionItem(
                                icon = Icons.Default.ConfirmationNumber,
                                title = "My Bookings",
                                onClick = onNavigateToBookings
                            )

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            // Edit Profile
                            ActionItem(
                                icon = Icons.Default.Person,
                                title = "Edit Profile",
                                onClick = { showEditProfileDialog = true }
                            )

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            // Change Password
                            ActionItem(
                                icon = Icons.Default.Lock,
                                title = "Change Password",
                                onClick = { showChangePasswordDialog = true }
                            )

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            // Settings
                            ActionItem(
                                icon = Icons.Default.Settings,
                                title = "Settings",
                                onClick = { /* Navigate to settings */ }
                            )

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            ActionItem(
                                icon = Icons.Default.Info,
                                title = "About",
                                onClick = onNavigateToAbout
                            )

                            Divider(modifier = Modifier.padding(horizontal = 16.dp))

                            // Logout
                            ActionItem(
                                icon = Icons.Default.ExitToApp,
                                title = "Logout",
                                onClick = { showLogoutConfirmDialog = true },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    MovieTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MovieTextField(
                        value = editPhoneNumber,
                        onValueChange = { editPhoneNumber = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProfile(editFullName, editPhoneNumber)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditProfileDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    PasswordTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = "Current Password"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm New Password"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                        if (errorMessage == null) {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            showChangePasswordDialog = false
                        }
                    }
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangePasswordDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutConfirmDialog = false
                        onLogout()
                    }
                ) {
                    Text("Yes, Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}