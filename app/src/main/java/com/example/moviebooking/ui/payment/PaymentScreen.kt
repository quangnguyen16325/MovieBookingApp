package com.example.moviebooking.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moviebooking.ui.components.MovieButton
import com.example.moviebooking.ui.theme.AccentColor
import com.example.moviebooking.ui.theme.DarkNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClick: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    viewModel: PaymentViewModel
) {
    val paymentState by viewModel.paymentState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val movieTitle by viewModel.movieTitle.collectAsState()
    val cinemaName by viewModel.cinemaName.collectAsState()
    val formattedSeats by viewModel.formattedSeats.collectAsState()
    val formattedTotalAmount by viewModel.formattedTotalAmount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Define gradients
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Black,
            DarkNavy
        )
    )

    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.7f),
            Color.Black.copy(alpha = 0.9f)
        )
    )

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentViewModel.PaymentState.Success -> {
                val bookingId = (paymentState as PaymentViewModel.PaymentState.Success).bookingId
                onPaymentSuccess(bookingId)
            }
            is PaymentViewModel.PaymentState.Error -> {
                val message = (paymentState as PaymentViewModel.PaymentState.Error).message
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Payment",
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Payment Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardGradient)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Payment Summary",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Movie Info
                            InfoRow(
                                icon = Icons.Default.MovieFilter,
                                label = "Movie",
                                value = movieTitle
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Cinema Info
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "Cinema",
                                value = cinemaName
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Seats Info
                            InfoRow(
                                icon = Icons.Default.EventSeat,
                                label = "Seats",
                                value = formattedSeats
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Divider(color = Color.White.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(16.dp))

                            // Total Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = formattedTotalAmount,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Payment Methods
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Credit Card Option
                PaymentMethodCard(
                    title = "Credit Card",
                    subtitle = "Pay with your credit card",
                    icon = Icons.Default.CreditCard,
                    isSelected = selectedPaymentMethod == PaymentViewModel.PaymentMethod.CREDIT_CARD,
                    onClick = { viewModel.selectPaymentMethod(PaymentViewModel.PaymentMethod.CREDIT_CARD) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile Payment Option
                PaymentMethodCard(
                    title = "Mobile Payment",
                    subtitle = "Pay with your mobile wallet",
                    icon = Icons.Default.PhoneAndroid,
                    isSelected = selectedPaymentMethod == PaymentViewModel.PaymentMethod.MOBILE_PAYMENT,
                    onClick = { viewModel.selectPaymentMethod(PaymentViewModel.PaymentMethod.MOBILE_PAYMENT) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bank Transfer Option
                PaymentMethodCard(
                    title = "Bank Transfer",
                    subtitle = "Pay via bank transfer",
                    icon = Icons.Default.Payment,
                    isSelected = selectedPaymentMethod == PaymentViewModel.PaymentMethod.BANK_TRANSFER,
                    onClick = { viewModel.selectPaymentMethod(PaymentViewModel.PaymentMethod.BANK_TRANSFER) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Pay Now Button
                MovieButton(
                    text = "Pay Now",
                    onClick = { viewModel.processPayment() },
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AccentColor
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AccentColor else Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 