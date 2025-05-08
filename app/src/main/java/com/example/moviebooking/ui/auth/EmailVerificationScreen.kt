package com.example.moviebooking.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.moviebooking.R
import com.example.moviebooking.ui.theme.*
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

    // Animation states
    var iconVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        targetValue = if (iconVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "iconScale"
    )

    // Launch animation sequence
    LaunchedEffect(Unit) {
        iconVisible = true
        delay(300)
        formVisible = true
    }

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

    // Define gradients and colors
    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            DarkNavy.copy(alpha = 0.9f),
            DarkNavy
        ),
        center = Offset(0f, 0f),
        radius = 1500f
    )

    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            DarkNavyLight.copy(alpha = 0.7f),
            DarkNavyLight.copy(alpha = 0.9f)
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image with overlay
            Image(
                painter = painterResource(id = R.drawable.cinema_background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(6.dp)
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
            )

            // Main content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top section - Email icon with animation
                    AnimatedVisibility(
                        visible = iconVisible,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.35f)
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                AccentColor.copy(alpha = 0.7f),
                                                DarkNavyLight.copy(alpha = 0.8f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Verify Email",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Middle section - Verification form with card layout
                    AnimatedVisibility(
                        visible = formVisible,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.65f)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(cardGradient)
                                        .padding(horizontal = 16.dp, vertical = 24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Verify Your Email",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "We've sent a verification email to your address. Please check your inbox and click the verification link to activate your account.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                viewModel.resendVerificationEmail()
                                            },
                                            text = "Resend Verification Email",
                                            isLoading = isLoading
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    viewModel.refreshUserEmailVerificationStatus()
                                                }
                                            },
                                            text = "I've Verified My Email",
                                            isLoading = isLoading
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        TextButton(
                                            onClick = {
                                                viewModel.logout()
                                                onBackToLogin()
                                            }
                                        ) {
                                            Text(
                                                text = "Back to Login",
                                                color = AccentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}