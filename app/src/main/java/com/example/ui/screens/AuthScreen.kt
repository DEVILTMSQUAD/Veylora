package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.AuthManager
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthScreen(
    authManager: AuthManager,
    onAuthSuccess: (FirebaseUser) -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060B17),
                        Color(0xFF0C142B),
                        Color(0xFF131F3F)
                    )
                )
            )
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF0B1428),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(VeyloraCyan, VeyloraPurple)),
                    width = 2.dp
                ),
                shadowElevation = 18.dp,
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Veylora",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Plan. Learn. Achieve.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = VeyloraCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "তোমার স্বপ্নের পথে, আমরা আছি পাশে।\nFirebase Cloud Sync দিয়ে সব ডিভাইসে ডেটা সুরক্ষিত রাখুন।",
                fontSize = 13.sp,
                color = VeyloraTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0E1A33),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(Color(0xFF263D68), Color(0xFF15223E))),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "লগইন বা সাইন-আপ করুন",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Features pills (single-line, non-breaking pills)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AuthPill(icon = Icons.Default.CloudSync, text = "Cloud Backup", modifier = Modifier.weight(1f))
                        AuthPill(icon = Icons.Default.Timer, text = "Bg Timer", modifier = Modifier.weight(1f))
                        AuthPill(icon = Icons.Default.Palette, text = "7 Themes", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // "Continue with Google" Button
                    Button(
                        onClick = {
                            if (activity != null) {
                                isLoading = true
                                authManager.signInWithGoogle(
                                    activity = activity,
                                    onSuccess = { user ->
                                        isLoading = false
                                        Toast.makeText(context, "স্বাগতম ${user.displayName ?: ""}!", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess(user)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        Toast.makeText(context, "লগইন ব্যর্থ: $err", Toast.LENGTH_LONG).show()
                                    },
                                    onCancelled = {
                                        isLoading = false
                                    }
                                )
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_signin_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF2563EB),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google Colorful 'G' Icon
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color(0xFF4285F4)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                    }

                    // Continue as guest / offline button
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color(0xFF2E4670), Color(0xFF1D2E4D))),
                            width = 1.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("skip_auth_button")
                    ) {
                        Text(
                            text = "অফলাইনে ব্যবহার চালিয়ে যান",
                            color = VeyloraTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF142444),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = VeyloraCyan, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
