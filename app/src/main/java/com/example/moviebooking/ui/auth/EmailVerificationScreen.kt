package com.example.moviebooking.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.components.MovieButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Set up periodic refresh
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.refreshUserEmailVerificationStatus()
            delay(5000) // Check every 5 seconds
        }
    }

    // Handle state changes
    LaunchedEffect(viewModel.authState) {
        when (viewModel.authState) {
            AuthViewModel.AuthState.AUTHENTICATED -> {
                onNavigateToHome()
            }
            else -> {}
        }
    }

    // Display error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Display success messages
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Verify Email",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We've sent a verification email to your address. Please check your inbox and click the verification link to activate your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                MovieButton(
                    onClick = {
                        viewModel.resendVerificationEmail()
                    },
                    text = "Resend Verification Email",
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                MovieButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.refreshUserEmailVerificationStatus()
                        }
                    },
                    text = "I've Verified My Email",
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    viewModel.logout()
                    onBackToLogin()
                }) {
                    Text("Back to Login")
                }
            }
        }
    }
}