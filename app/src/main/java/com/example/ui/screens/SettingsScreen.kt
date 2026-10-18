package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.data.VeyloraRepository
import com.example.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun SettingsScreen(
    repository: VeyloraRepository,
    authManager: AuthManager,
    onOpenAuth: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenThemePicker: () -> Unit = {}
) {
    val profile by repository.profile.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val firebaseUser = Firebase.auth.currentUser

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Profile & Google Account Banner Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (firebaseUser == null) onOpenAuth() else showEditProfileDialog = true
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = firebaseUser?.displayName ?: profile.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (firebaseUser != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VeyloraGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Synced",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VeyloraGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (firebaseUser != null) (firebaseUser.email ?: "Google Account") else "${profile.title} • ${profile.xp} XP",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (firebaseUser == null) {
                        Button(
                            onClick = onOpenAuth,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("লগইন", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                authManager.signOut {
                                    repository.syncAuthProfile()
                                    Toast.makeText(context, "লগআউট সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Sign Out", tint = VeyloraRed)
                        }
                    }
                }
            }
        }

        // Appearance & Tools Group
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Dark Mode / Day Mode Switch
                    SettingsRow(
                        icon = if (profile.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        title = "অ্যাপ মোড (Appearance)",
                        subtitle = if (profile.isDarkMode) "Dark Mode (ডার্ক মোড সক্রিয়)" else "Day Mode (দিন মোড সক্রিয়)",
                        trailing = {
                            Switch(
                                checked = profile.isDarkMode,
                                onCheckedChange = { repository.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Theme Palette Selection
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = "কালার প্যালেট থিম (Theme Palette)",
                        subtitle = "Cosmic Midnight, OLED Black, Nordic Day...",
                        onClick = onOpenThemePicker
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Reminders Shortcut
                    SettingsRow(
                        icon = Icons.Default.Alarm,
                        title = "স্টাডি রিমাইন্ডার (Reminders)",
                        subtitle = "দৈনিক পড়ার সময় ও অ্যালার্ম সেট করুন",
                        onClick = onNavigateToReminders
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // History Shortcut
                    SettingsRow(
                        icon = Icons.Default.History,
                        title = "স্টাডি হিস্ট্রি ও টাইমলাইন (History)",
                        subtitle = "সব সম্পন্ন সেশন ও টাস্কের বিস্তারিত রেকর্ড",
                        onClick = onNavigateToHistory
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Daily Goal
                    SettingsRow(
                        icon = Icons.Default.Flag,
                        title = "দৈনিক লক্ষ্য (Daily Goal)",
                        subtitle = "${profile.dailyGoalMinutes / 60} hours (${profile.dailyGoalMinutes} min)",
                        onClick = { showGoalDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // Group / Stream Selector
                    SettingsRow(
                        icon = Icons.Default.School,
                        title = "গ্রুপ / বিভাগ (Study Group)",
                        subtitle = "${profile.group} • পরিবর্তন করতে ট্যাপ করুন",
                        onClick = { showGroupDialog = true }
                    )
                }
            }
        }

        // Cloud & Storage Group
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRow(
                        icon = Icons.Default.CloudSync,
                        title = "Firebase Cloud Sync",
                        subtitle = if (firebaseUser != null) "Connected: ${firebaseUser.email}" else "Not Synced (Login with Google)",
                        onClick = { if (firebaseUser == null) onOpenAuth() }
                    )

                    if (firebaseUser != null) {
                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                        SettingsRow(
                            icon = Icons.Default.CloudDownload,
                            title = "ক্লাউড থেকে রিস্টোর (Restore Cloud Data)",
                            subtitle = "পূর্বের একাউন্টে সংরক্ষিত সকল পড়ার ডেটা ও প্রোগ্রেস আনুন",
                            onClick = {
                                repository.restoreUserDataFromCloud { ok ->
                                    if (ok) {
                                        Toast.makeText(context, "ক্লাউড থেকে সফলভাবে সব ডেটা রিস্টোর হয়েছে! ☁️✅", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "ক্লাউড ডেটা রিস্টোর ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                        SettingsRow(
                            icon = Icons.Default.CloudUpload,
                            title = "এখনই ক্লাউডে ব্যাকআপ দিন (Backup to Cloud)",
                            subtitle = "বর্তমান সব অগ্রগতি ও রুটিন ক্লাউডে সিঙ্ক রাখুন",
                            onClick = {
                                repository.pushToCloud { ok ->
                                    if (ok) {
                                        Toast.makeText(context, "ক্লাউডে সব ডেটা সংরক্ষিত হয়েছে! ☁️✅", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "ক্লাউড ব্যাকআপ ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    SettingsRow(
                        icon = Icons.Default.Backup,
                        title = "Export / Import Data",
                        subtitle = "JSON Backup, Restore & Reset",
                        onClick = { showBackupModal = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "About Veylora",
                        subtitle = "v2.0.0 (Premium Day/Dark Edition)",
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(profile.name) }
        var title by remember { mutableStateOf(profile.title) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Profile সম্পাদনা করুন", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("আপনার নাম") })
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("টাইটেল বা শ্রেণি (যেমন: Level 3 Scholar)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            repository.updateProfile(name.trim(), title.trim())
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সংরক্ষণ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showEditProfileDialog = false }) { Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }

    // Goal Dialog
    if (showGoalDialog) {
        var goalMins by remember { mutableStateOf("${profile.dailyGoalMinutes}") }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("দৈনিক লক্ষ্য নির্ধারণ (মিনিট)", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = goalMins,
                    onValueChange = { goalMins = it },
                    label = { Text("লক্ষ্য মিনিট (e.g. 120, 180, 240)") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = goalMins.toIntOrNull() ?: 120
                        repository.updateDailyGoal(m.coerceAtLeast(15))
                        showGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সেভ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showGoalDialog = false }) { Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        )
    }

    // Group / Stream Selection Dialog
    if (showGroupDialog) {
        val groups = listOf(
            Triple(com.example.data.Class910Curriculum.GROUP_SCIENCE, "বিজ্ঞান বিভাগ (Science)", "⚡"),
            Triple(com.example.data.Class910Curriculum.GROUP_COMMERCE, "ব্যবসায় শিক্ষা (Commerce)", "💼"),
            Triple(com.example.data.Class910Curriculum.GROUP_ARTS, "মানবিক বিভাগ (Arts)", "🏛️")
        )
        var selectedGrp by remember { mutableStateOf(profile.group) }

        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Text("🎓", fontSize = 28.sp) },
            title = { Text("গ্রুপ / বিভাগ পরিবর্তন", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "নতুন বিভাগ নির্বাচন করলে সেই বিভাগের সব অধ্যায় ও বিষয় অ্যাপে স্বয়ংক্রিয়ভাবে লোড হবে:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    groups.forEach { (grpKey, grpLabel, emoji) ->
                        val isSelected = selectedGrp == grpKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                width = 1.5.dp
                            ) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGrp = grpKey }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                    Text(
                                        text = grpLabel,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.loadCurriculumForGroup(selectedGrp)
                        showGroupDialog = false
                        Toast.makeText(context, "$selectedGrp এর সিলেবাস সফলভাবে লোড হয়েছে! 🎓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সংরক্ষণ ও লোড করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Backup & Restore Modal
    if (showBackupModal) {
        val currentJson = remember { repository.exportDataJson() }
        var importInputText by remember { mutableStateOf(currentJson) }

        AlertDialog(
            onDismissRequest = { showBackupModal = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("💾 Backup & Restore", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "সব তথ্য JSON ফরম্যাটে কপি করে রাখুন বা পূর্বের ব্যাকআপ পেস্ট করে Import করুন।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        minLines = 6
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentJson))
                                Toast.makeText(context, "JSON কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy JSON", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = {
                                repository.resetToDemo()
                                showBackupModal = false
                                Toast.makeText(context, "ডেমো ডেটা রিস্টোর হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VeyloraRed.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Demo", fontSize = 11.sp, color = VeyloraRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = repository.importDataJson(importInputText)
                        if (ok) {
                            showBackupModal = false
                            Toast.makeText(context, "Import সফল হয়েছে! ✅", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "JSON ফরম্যাট সঠিক নয়! ❌", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Import করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupModal = false }) {
                    Text("বন্ধ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Veylora", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("v2.0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plan. Learn. Achieve.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text("তোমার স্বপ্নের পথে, আমরা আছি পাশে।", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("রিমাইন্ডার, হিস্ট্রি লগ, ডার্ক ও ডে মোড, ব্যাকগ্রাউন্ড টাইমার এবং ক্লাউড সিঙ্ক সম্পন্ন প্রিমিয়াম স্টাডি অ্যাপ।", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("ঠিক আছে", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(1.dp))
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
