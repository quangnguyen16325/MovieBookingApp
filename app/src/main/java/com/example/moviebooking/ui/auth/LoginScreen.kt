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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.moviebooking.R
import com.example.moviebooking.ui.auth.AuthViewModel.AuthState
import com.example.moviebooking.ui.components.FacebookSignInButton
import com.example.moviebooking.ui.components.GoogleSignInButton
import com.example.moviebooking.ui.components.SocialLoginDivider
import com.example.moviebooking.ui.theme.*
import com.example.moviebooking.util.Utils
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onFacebookSignInClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // State variables
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    // Animation states
    var logoVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var socialVisible by remember { mutableStateOf(false) }

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
        delay(300)
        socialVisible = true
    }

    // Handle authentication state changes
    LaunchedEffect(viewModel.authState) {
        when (viewModel.authState) {
            AuthState.AUTHENTICATED -> {
                onNavigateToHome()
                viewModel.resetAuthState()
            }
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
        // Background image with overlay
        Box(modifier = Modifier.fillMaxSize()) {
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

                    // Middle section - Login form with card layout
                    AnimatedVisibility(
                        visible = formVisible,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.45f)
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
                                            text = "Welcome Back",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Login to your account",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(alpha = 0.7f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

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

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Forgot password text
                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val forgotPasswordText = buildAnnotatedString {
                                                withStyle(style = SpanStyle(color = AccentColor)) {
                                                    pushStringAnnotation(tag = "FORGOT_PASSWORD", annotation = "forgot_password")
                                                    append("Forgot Password?")
                                                    pop()
                                                }
                                            }

                                            ClickableText(
                                                text = forgotPasswordText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                onClick = { offset ->
                                                    forgotPasswordText.getStringAnnotations(
                                                        tag = "FORGOT_PASSWORD",
                                                        start = offset,
                                                        end = offset
                                                    ).firstOrNull()?.let {
                                                        onNavigateToForgotPassword()
                                                    }
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Button(
                                            onClick = {
                                                // Validate inputs
                                                when {
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
                                                    else -> {
                                                        // All inputs valid, try login
                                                        viewModel.login(email, password)
                                                    }
                                                }
                                            },
                                            text = "Login",
                                            isLoading = isLoading
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom section - Social login and register link
                    AnimatedVisibility(
                        visible = socialVisible,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.2f)
                        ) {
//                            SocialLoginDivider()
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Social login buttons in a row
//                            Row(
//                                horizontalArrangement = Arrangement.SpaceEvenly,
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                GoogleSignInButton(
//                                    onClick = onGoogleSignInClick,
//                                    enabled = !isLoading,
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .shadow(4.dp, RoundedCornerShape(12.dp))
//                                )
//
//                                Spacer(modifier = Modifier.width(16.dp))
//
//                                FacebookSignInButton(
//                                    onClick = onFacebookSignInClick,
//                                    enabled = !isLoading,
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .shadow(4.dp, RoundedCornerShape(12.dp))
//                                )
//                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val registerText = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                                    append("Don't have an account? ")
                                }
                                withStyle(style = SpanStyle(color = AccentColor, fontWeight = FontWeight.Bold)) {
                                    pushStringAnnotation(tag = "REGISTER", annotation = "register")
                                    append("Register")
                                    pop()
                                }
                            }

                            ClickableText(
                                text = registerText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textAlign = TextAlign.Center
                                ),
                                onClick = { offset ->
                                    registerText.getStringAnnotations(
                                        tag = "REGISTER",
                                        start = offset,
                                        end = offset
                                    ).firstOrNull()?.let {
                                        onNavigateToRegister()
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

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.8f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else AccentColor
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
        )

        if (isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.8f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error else AccentColor
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
        )

        if (isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean = false
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}