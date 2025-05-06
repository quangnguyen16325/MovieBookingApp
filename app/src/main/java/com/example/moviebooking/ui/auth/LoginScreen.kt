package com.example.moviebooking.ui.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.moviebooking.R
import com.example.moviebooking.ui.auth.AuthViewModel.AuthState
import com.example.moviebooking.ui.components.FacebookSignInButton
import com.example.moviebooking.ui.components.GoogleSignInButton
import com.example.moviebooking.ui.components.HeadingText
import com.example.moviebooking.ui.components.MovieButton
import com.example.moviebooking.ui.components.MovieTextField
import com.example.moviebooking.ui.components.PasswordTextField
import com.example.moviebooking.ui.components.SocialLoginDivider
import com.example.moviebooking.ui.components.SubheadingText
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.util.Utils

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
    val configuration = LocalConfiguration.current

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section - Logo and App name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.35f)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    HeadingText(
                        text = "Cine AI",
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Smart Ticketing Made Easy",
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Image(
                        painter = painterResource(id = R.drawable.cineai_1),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(220.dp)
                    )
                }

                // Middle section - Login form
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.4f)
                ) {
                    SubheadingText(
                        text = "Login to your account",
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email/Password Login
                    MovieTextField(
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

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = ""
                        },
                        label = "Password",
                        isError = passwordError.isNotEmpty(),
                        errorMessage = passwordError
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot password text
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val forgotPasswordText = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
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

                    Spacer(modifier = Modifier.height(16.dp))

                    MovieButton(
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

                // Bottom section - Social login and register link
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.25f) // Giảm trọng số cho phần dưới để tạo thêm không gian cho logo
                ) {
                    SocialLoginDivider()

                    // Social login buttons in a row to save space
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GoogleSignInButton(
                            onClick = onGoogleSignInClick,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FacebookSignInButton(
                            onClick = onFacebookSignInClick,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val registerText = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(style = SpanStyle(color = AccentColor)) {
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