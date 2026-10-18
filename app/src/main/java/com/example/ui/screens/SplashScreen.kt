package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    // Stage 1: Initial Black Screen (কালো স্ক্রিন) for ~1.8s
    // Stage 2: Animated Loading Page (লোডিং পেজ) for ~1.5s
    var isBlackStage by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        // Stage 1: Pure sleek black screen (কালো screen)
        delay(1600L)
        isBlackStage = false

        // Stage 2: Loading screen progress animation
        val startTime = System.currentTimeMillis()
        val duration = 1500L
        while (System.currentTimeMillis() - startTime < duration) {
            val elapsed = System.currentTimeMillis() - startTime
            progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            delay(16)
        }
        progress = 1f
        delay(150)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap to skip
                onFinished()
            }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (isBlackStage) {
            // ==========================================
            // Phase 1: Pure Deep Black Screen (কালো screen)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                            ),
                            width = 1.5.dp
                        ),
                        shadowElevation = 12.dp,
                        modifier = Modifier
                            .size(88.dp)
                            .scale(pulseScale)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_veylora_logo),
                            contentDescription = "Veylora Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Veylora",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            // ==========================================
            // Phase 2: Animated Loading Page (লোডিং পেজ)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Logo Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                    ) {
                        // Background soft glow halo
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            VeyloraCyan.copy(alpha = 0.4f),
                                            VeyloraPurple.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Logo Image
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFF0B1428),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(VeyloraCyan, VeyloraPurple)
                                ),
                                width = 2.dp
                            ),
                            shadowElevation = 16.dp,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_veylora_logo),
                                contentDescription = "Veylora Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Title
                    Text(
                        text = "Veylora",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    // Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Plan.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VeyloraPink)
                        Text(text = "Learn.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VeyloraCyan)
                        Text(text = "Achieve.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bengali Tagline
                    Text(
                        text = "তোমার স্বপ্নের পথে, আমরা আছি পাশে।",
                        fontSize = 15.sp,
                        color = VeyloraTextSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feature Highlights
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F1A33).copy(alpha = 0.7f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(Color(0xFF1E2E4E), Color(0xFF283D66))),
                            width = 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SplashFeatureRow(icon = Icons.Default.CheckCircle, title = "Smart Daily Planner", color = VeyloraCyan)
                            SplashFeatureRow(icon = Icons.Default.MenuBook, title = "Auto Spaced Repetition Revision", color = VeyloraPurple)
                            SplashFeatureRow(icon = Icons.Default.AutoAwesome, title = "Gemini AI Smart Reminder", color = VeyloraPink)
                            SplashFeatureRow(icon = Icons.Default.School, title = "Class 9-10 & SSC Complete Syllabus", color = VeyloraGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Progress bar
                    Column(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = VeyloraCyan,
                            trackColor = Color(0xFF1E2E4E)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (progress < 0.5f) "পড়ার রুটিন ও রিভিশন ডেটা লোড হচ্ছে... ${(progress * 100).toInt()}%"
                            else "AI অটো রিমাইন্ডার ইঞ্জিন প্রস্তুত হচ্ছে... ${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = VeyloraTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
        )
    }
}
