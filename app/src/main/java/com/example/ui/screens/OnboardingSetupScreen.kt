package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.data.VeyloraRepository
import com.example.ui.theme.*

enum class OnboardingStep {
    ACCOUNT,
    PROFILE_DETAILS
}

@Composable
fun OnboardingSetupScreen(
    repository: VeyloraRepository,
    authManager: AuthManager,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var currentStep by remember { mutableStateOf(OnboardingStep.ACCOUNT) }
    var nameInput by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("৯ম-১০ম শ্রেণী / SSC") }
    var selectedGroup by remember { mutableStateOf(com.example.data.Class910Curriculum.GROUP_SCIENCE) }
    var selectedGoalMinutes by remember { mutableIntStateOf(120) }
    var isLoadingGoogle by remember { mutableStateOf(false) }
    var googleAccountEmail by remember { mutableStateOf("") }

    val classOptions = listOf(
        "৯ম-১০ম শ্রেণী / SSC",
        "একাদশ-দ্বাদশ / HSC",
        "বিশ্ববিদ্যালয় ভর্তি (Admission)",
        "অনার্স / ডিগ্রি (University)",
        "বিসিএস ও চাকুরীর প্রস্তুতি",
        "অন্যান্য"
    )

    val groupOptions = listOf(
        Triple(com.example.data.Class910Curriculum.GROUP_SCIENCE, "বিজ্ঞান", "⚡"),
        Triple(com.example.data.Class910Curriculum.GROUP_COMMERCE, "ব্যবসায় শিক্ষা", "💼"),
        Triple(com.example.data.Class910Curriculum.GROUP_ARTS, "মানবিক", "🏛️")
    )

    val goalOptions = listOf(
        Pair("১ ঘণ্টা", 60),
        Pair("২ ঘণ্টা", 120),
        Pair("৩ ঘণ্টা", 180),
        Pair("৪+ ঘণ্টা", 240)
    )

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
            )
            .testTag("onboarding_setup_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Brand Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Veylora Study Planner",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Step Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            width = 1.dp
                        )
                    ) {
                        Text(
                            text = if (currentStep == OnboardingStep.ACCOUNT) "ধাপ ১/২ • অ্যাকাউন্ট সাইন-ইন" else "ধাপ ২/২ • নাম ও লক্ষ্য নির্ধারণ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Step 1: Account Creation & Sign-In Page
            if (currentStep == OnboardingStep.ACCOUNT) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.outline
                                )
                            ),
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
                                text = "অ্যাকাউন্ট তৈরি বা লগইন করুন",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "সব ডিভাইসে আপনার রুটিন ও পড়ার ডেটা সিঙ্ক রাখতে ক্লাউড অ্যাকাউন্ট দিয়ে শুরু করুন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            // Feature Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OnboardingMiniPill(
                                    icon = Icons.Default.CloudSync,
                                    text = "ক্লাউড সিঙ্ক",
                                    modifier = Modifier.weight(1f)
                                )
                                OnboardingMiniPill(
                                    icon = Icons.Default.Timeline,
                                    text = "প্রোগ্রেস ট্র্যাকার",
                                    modifier = Modifier.weight(1f)
                                )
                                OnboardingMiniPill(
                                    icon = Icons.Default.NotificationsActive,
                                    text = "অ্যালার্ট",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Google Sign-In Button
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        isLoadingGoogle = true
                                        authManager.signInWithGoogle(
                                            activity = activity,
                                            onSuccess = { user ->
                                                val displayName = user.displayName?.ifBlank { "শিক্ষার্থী" } ?: "শিক্ষার্থী"
                                                nameInput = displayName
                                                googleAccountEmail = user.email ?: ""

                                                // Automatically check & restore cloud data for returning users safely
                                                repository.restoreUserDataFromCloud { hasRestored ->
                                                    isLoadingGoogle = false
                                                    val prof = repository.profile.value
                                                    if (hasRestored && prof.name.isNotBlank()) {
                                                        nameInput = prof.name
                                                        if (prof.studentClass.isNotBlank()) {
                                                            selectedClass = prof.studentClass
                                                        }
                                                        if (prof.group.isNotBlank()) {
                                                            selectedGroup = prof.group
                                                        }
                                                        if (prof.dailyGoalMinutes > 0) {
                                                            selectedGoalMinutes = prof.dailyGoalMinutes
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "স্বাগতম ${prof.name}! আপনার শ্রেণী ও বিভাগ নিশ্চিত করুন।",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "অ্যাকাউন্ট সফল: $displayName! আপনার বিভাগ ও তথ্য নির্বাচন করুন।",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    currentStep = OnboardingStep.PROFILE_DETAILS
                                                }
                                            },
                                            onError = { err ->
                                                isLoadingGoogle = false
                                                Toast.makeText(context, "লগইন ব্যর্থ: $err", Toast.LENGTH_SHORT).show()
                                            },
                                            onCancelled = {
                                                isLoadingGoogle = false
                                            }
                                        )
                                    }
                                },
                                enabled = !isLoadingGoogle,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF1F2937)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_google_button")
                            ) {
                                if (isLoadingGoogle) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("G", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF4285F4))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                }
                            }

                            // Divider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Text("বা সরাসরি প্রোফাইল তৈরি", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            }

                            // Next Page Button (Go to Step 2)
                            OutlinedButton(
                                onClick = {
                                    currentStep = OnboardingStep.PROFILE_DETAILS
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                    width = 1.2.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("next_to_profile_step_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "পরবর্তী ধাপে নাম ও তথ্য দিন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Separate Profile Details Page ("আর নাম .... ঐ সব গুলো অন্য পেজে নাও পরের পেজে একাউন্ট এর পর এটা আসবে")
            if (currentStep == OnboardingStep.PROFILE_DETAILS) {
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                            width = 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Back to Step 1 & Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { currentStep = OnboardingStep.ACCOUNT },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "আপনার পরিচিতি ও পড়ার লক্ষ্য",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (googleAccountEmail.isNotBlank()) {
                                        Text(
                                            text = "গুগল অ্যাকাউন্ট: $googleAccountEmail",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            // 1. Name Input (Real Name - never Tamim!)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "১. আপনার সম্পূর্ণ নাম (Full Name):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    placeholder = { Text("আপনার নাম লিখুন...") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_name_input")
                                )
                            }

                            // 2. Class / Exam Target Selection
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "২. শ্রেণী বা পড়াশোনার লক্ষ্য:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    classOptions.forEach { opt ->
                                        val isSelected = selectedClass == opt
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                                width = 1.5.dp
                                            ) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedClass = opt }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Group Selection (Science, Commerce, Arts)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "৩. গ্রুপ / বিভাগ নির্বাচন (Group):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    groupOptions.forEach { (groupVal, groupShort, iconEmoji) ->
                                        val isSelected = selectedGroup == groupVal
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                                width = 1.5.dp
                                            ) else null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedGroup = groupVal }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(text = iconEmoji, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = groupShort,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Daily Goal Selection
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "৪. দৈনিক পড়ার লক্ষ্য:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    goalOptions.forEach { (label, minutes) ->
                                        val isSelected = selectedGoalMinutes == minutes
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedGoalMinutes = minutes }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Submit & Enter App Button
                            Button(
                                onClick = {
                                    val trimmed = nameInput.trim()
                                    if (trimmed.isBlank()) {
                                        Toast.makeText(context, "অনুগ্রহ করে আপনার নাম লিখুন", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    repository.updateUserProfile(
                                        name = trimmed,
                                        studentClass = selectedClass,
                                        group = selectedGroup,
                                        dailyGoalMinutes = selectedGoalMinutes
                                    )
                                    if (repository.subjects.value.isEmpty()) {
                                        repository.loadCurriculumForGroup(selectedGroup)
                                    }
                                    repository.pushToCloud()
                                    Toast.makeText(context, "স্বাগতম $trimmed! পড়াশোনা শুরু করুন। 🎓", Toast.LENGTH_SHORT).show()
                                    onComplete()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("complete_onboarding_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "পড়াশোনা শুরু করুন 🚀",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
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

@Composable
private fun OnboardingMiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
