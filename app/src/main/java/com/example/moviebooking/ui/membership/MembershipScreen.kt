package com.example.moviebooking.ui.membership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviebooking.data.model.MembershipLevel
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    onBackClick: () -> Unit,
    viewModel: MembershipViewModel = viewModel()
) {
    val userMembership by viewModel.userMembership.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserMembershipInfo()
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Membership",
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Membership Card
                    MembershipCard(userMembership, cardGradient)

//                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress to next level
                    NextLevelProgress(viewModel, cardGradient)

//                    Spacer(modifier = Modifier.height(12.dp))

                    // Membership Benefits
                    MembershipBenefits(
                        userMembership?.membershipLevel ?: MembershipLevel.BASIC,
                        cardGradient
                    )
                }
            }

            // Error and Success Messages
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(message)
                }
            }

            successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
fun MembershipCard(user: UserModel?, cardGradient: Brush) {
    val membershipLevel = user?.membershipLevel ?: MembershipLevel.BASIC
    val cardColor = getMembershipCardColor(membershipLevel)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            cardColor.copy(alpha = 0.8f),
            cardColor.copy(alpha = 0.6f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(24.dp)
        ) {
            // Membership Level Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = membershipLevel.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section
                Column {
                    Text(
                        text = "Member Card",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user?.fullName ?: "Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Bottom Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Points Section
                    Column {
                        Text(
                            text = "POINTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        if (user?.membershipLevel == MembershipLevel.PREMIUM) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = "Unlimited",
                                tint = Color.White,
                                modifier = Modifier.size(45.dp)
                            )
                        } else {
                            Text(
                                text = "${user?.membershipPoints ?: 0}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
//                        Text(
//                            text = "${user?.membershipPoints ?: 0}",
//                            style = MaterialTheme.typography.headlineMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
                    }

                    // Membership Level Icon
                    Icon(
                        imageVector = when (membershipLevel) {
                            MembershipLevel.PREMIUM -> Icons.Default.EmojiEvents
                            MembershipLevel.DIAMOND -> Icons.Default.Diamond
                            MembershipLevel.GOLD -> Icons.Default.Star
                            MembershipLevel.SILVER -> Icons.Default.Star
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun getMembershipCardColor(level: MembershipLevel): Color {
    return when (level) {
        MembershipLevel.BASIC -> Color(0xFF2C3E50) // Dark Blue
        MembershipLevel.SILVER -> Color(0xFF7F8C8D) // Silver
        MembershipLevel.GOLD -> Color(0xFFF1C40F) // Gold
        MembershipLevel.DIAMOND -> Color(0xFF3498DB) // Diamond Blue
        MembershipLevel.PREMIUM -> Color(	0xFFC0392B) // Crimson Red
    }
}

@Composable
fun NextLevelProgress(viewModel: MembershipViewModel, cardGradient: Brush) {
    val (currentPoints, nextLevelPoints) = viewModel.getNextLevelProgress()
    val progress = if (nextLevelPoints == Int.MAX_VALUE) 1f else currentPoints.toFloat() / nextLevelPoints

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Progress to Next Level",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AccentColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentPoints points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (nextLevelPoints == Int.MAX_VALUE) "MAX" else "$nextLevelPoints points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun MembershipBenefits(level: MembershipLevel, cardGradient: Brush) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Membership Benefits",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (level) {
                    MembershipLevel.BASIC -> {
                        BenefitItem("No benefits yet", "Collect points to upgrade your membership and receive incentives")
                    }
                    MembershipLevel.SILVER -> {
                        BenefitItem("Silver Discount", "10% off on all tickets")
                    }
                    MembershipLevel.GOLD -> {
                        BenefitItem("Gold Discount", "15% off on all tickets")
                        BenefitItem("Birthday Gift", "Special gift on your birthday")
                    }
                    MembershipLevel.DIAMOND -> {
                        BenefitItem("Diamond Discount", "25% off on all tickets")
                        BenefitItem("Birthday Package", "Special birthday package")
                        BenefitItem("Priority Support", "24/7 priority customer support")
                    }
                    MembershipLevel.PREMIUM -> {
                        BenefitItem("Free Tickets", "100% off on all tickets — unlimited access")
                        BenefitItem("Private Screenings", "Access to private and early screenings")
                        BenefitItem("Personal Assistant", "1-on-1 movie assistant for bookings and recommendations")
                        BenefitItem("Lifetime Membership", "No expiration — lifetime premium access")
                        BenefitItem("VIP Lounge Access", "Entry to exclusive VIP lounges at select theaters")
                    }

                }
            }
        }
    }
}

@Composable
fun BenefitItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun getMembershipLevelColor(level: MembershipLevel): Color {
    return when (level) {
        MembershipLevel.BASIC -> Color.White
        MembershipLevel.SILVER -> Color(0xFFC0C0C0)
        MembershipLevel.GOLD -> Color(0xFFFFD700)
        MembershipLevel.DIAMOND -> Color(0xFFB9F2FF)
        MembershipLevel.PREMIUM -> Color(0xFFC0392B)

    }
} 