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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.moviebooking.R
import com.example.moviebooking.ui.auth.AuthViewModel.AuthState
import com.example.moviebooking.ui.theme.*
import com.example.moviebooking.util.Utils
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEmailVerification: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    // Animation states
    var logoVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    // Launch animation sequence
    LaunchedEffect(Unit) {
        logoVisible = true
        delay(300)
        formVisible = true
    }

    // Handle authentication state changes
    LaunchedEffect(viewModel.authState) {
        when (viewModel.authState) {
            AuthState.EMAIL_NOT_VERIFIED -> {
                onNavigateToEmailVerification()
                viewModel.resetAuthState()
            }
            AuthState.ERROR -> {
                errorMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearError()
                }
            }
            else -> {}
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
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top section - Logo and App name with animation
                    AnimatedVisibility(
                        visible = logoVisible,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.35f)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Cine AI",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Smart Ticketing Made Easy",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = AccentColor
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(170.dp)
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
                                Image(
                                    painter = painterResource(id = R.drawable.cineai_1),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(160.dp)
                                        .graphicsLayer {
                                            scaleX = logoScale
                                            scaleY = logoScale
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Middle section - Register form with card layout
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
                                            text = "Create Account",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Join our community",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        TextField(
                                            value = fullName,
                                            onValueChange = {
                                                fullName = it
                                                fullNameError = ""
                                            },
                                            label = "Full Name",
                                            leadingIcon = Icons.Default.Person,
                                            isError = fullNameError.isNotEmpty(),
                                            errorMessage = fullNameError
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        TextField(
                                            value = email,
                                            onValueChange = {
                                                email = it
                                                emailError = ""
                                            },
                                            label = "Email",
                                            leadingIcon = Icons.Default.Email,
                                            keyboardType = KeyboardType.Email,
                                            isError = emailError.isNotEmpty(),
                                            errorMessage = emailError
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        PasswordField(
                                            value = password,
                                            onValueChange = {
                                                password = it
                                                passwordError = ""
                                            },
                                            label = "Password",
                                            leadingIcon = Icons.Rounded.Lock,
                                            isError = passwordError.isNotEmpty(),
                                            errorMessage = passwordError
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        PasswordField(
                                            value = confirmPassword,
                                            onValueChange = {
                                                confirmPassword = it
                                                confirmPasswordError = ""
                                            },
                                            label = "Confirm Password",
                                            leadingIcon = Icons.Rounded.Lock,
                                            isError = confirmPasswordError.isNotEmpty(),
                                            errorMessage = confirmPasswordError
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                // Validate inputs
                                                when {
                                                    fullName.isEmpty() -> {
                                                        fullNameError = "Full name is required"
                                                    }
                                                    email.isEmpty() -> {
                                                        emailError = "Email is required"
                                                    }
                                                    !Utils.isValidEmail(email) -> {
                                                        emailError = "Enter a valid email"
                                                    }
                                                    password.isEmpty() -> {
                                                        passwordError = "Password is required"
                                                    }
                                                    password.length < 6 -> {
                                                        passwordError = "Password must be at least 6 characters"
                                                    }
                                                    confirmPassword.isEmpty() -> {
                                                        confirmPasswordError = "Please confirm your password"
                                                    }
                                                    password != confirmPassword -> {
                                                        confirmPasswordError = "Passwords do not match"
                                                    }
                                                    else -> {
                                                        // All inputs valid, try register
                                                        viewModel.register(email, password, fullName)
                                                    }
                                                }
                                            },
                                            text = "Register",
                                            isLoading = isLoading
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        val loginText = buildAnnotatedString {
                                            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                                                append("Already have an account? ")
                                            }
                                            withStyle(style = SpanStyle(color = AccentColor, fontWeight = FontWeight.Bold)) {
                                                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                                                append("Login")
                                                pop()
                                            }
                                        }

                                        ClickableText(
                                            text = loginText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                textAlign = TextAlign.Center
                                            ),
                                            onClick = { offset ->
                                                loginText.getStringAnnotations(
                                                    tag = "LOGIN",
                                                    start = offset,
                                                    end = offset
                                                ).firstOrNull()?.let {
                                                    onNavigateToLogin()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
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