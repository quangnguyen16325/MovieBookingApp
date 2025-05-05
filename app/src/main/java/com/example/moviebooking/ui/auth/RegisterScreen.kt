package com.example.moviebooking.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.auth.AuthViewModel.AuthState
import com.example.moviebooking.ui.components.MovieButton
import com.example.moviebooking.ui.components.MovieTextField
import com.example.moviebooking.ui.components.PasswordTextField
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.util.Utils

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
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

    // Handle authentication state changes
    LaunchedEffect(viewModel.authState) {
        when (viewModel.authState) {
            AuthState.EMAIL_NOT_VERIFIED -> {
                onNavigateToHome()
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
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                MovieTextField(
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

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = ""
                    },
                    label = "Password",
                    imeAction = ImeAction.Next,
                    isError = passwordError.isNotEmpty(),
                    errorMessage = passwordError
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    label = "Confirm Password",
                    isError = confirmPasswordError.isNotEmpty(),
                    errorMessage = confirmPasswordError
                )

                Spacer(modifier = Modifier.height(32.dp))

                MovieButton(
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
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = AccentColor)) {
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