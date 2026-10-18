package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.GeminiAiService
import com.example.ai.SmartReminderResult
import com.example.data.VeyloraRepository
import com.example.data.model.StudyReminderItem
import com.example.notifications.NotificationHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RemindersScreen(
    repository: VeyloraRepository
) {
    val reminders by repository.reminders.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val exams by repository.exams.collectAsState()
    val revisions by repository.revisions.collectAsState()
    val profile by repository.profile.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var isGeneratingAiReminder by remember { mutableStateOf(false) }
    var latestAiReminder by remember { mutableStateOf<SmartReminderResult?>(null) }
    var showAiResultDialog by remember { mutableStateOf(false) }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val df = SimpleDateFormat("hh:mm a", Locale.US)
            val date = df.parse(timeStr.trim())
            if (date != null) {
                val cal = java.util.Calendar.getInstance().apply { time = date }
                Pair(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
            } else Pair(20, 0)
        } catch (_: Exception) {
            Pair(20, 0)
        }
    }

    // Android 13+ Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "নোটিফিকেশন পারমিশন সক্রিয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "নোটিফিকেশন পেতে পারমিশন দেওয়া প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminders_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "রিমাইন্ডার ও অ্যালার্ট",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "নিয়মিত পড়ার অভ্যাস ও সিস্টেম নোটিফিকেশন",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showAddReminderDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddAlarm, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Reminder", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ==========================================
        // Gemini AI Smart Reminder Card (Highlight)
        // ==========================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    ),
                    width = 1.5.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AI স্মার্ট রিমাইন্ডার মেন্টর",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Gemini",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "পড়ার গতি ও বাকি কাজ বিশ্লেষণ করে সঠিক সময়ে রিমাইন্ডার",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (isGeneratingAiReminder) return@Button
                            isGeneratingAiReminder = true
                            coroutineScope.launch {
                                val pendingTitles = tasks.filter { !it.done }.map { "${it.subject}: ${it.title}" }
                                val lowProgressSubs = subjects
                                    .filter { it.progressPercent < 60 }
                                    .map { "${it.name} (${it.progressPercent}%)" }
                                val examNames = exams.map { "${it.name} (${it.date})" }

                                val result = GeminiAiService.generateSmartReminder(
                                    studentName = profile.name,
                                    studentClass = profile.studentClass,
                                    pendingTasks = pendingTitles,
                                    subjectsWithLowProgress = lowProgressSubs,
                                    upcomingExams = examNames
                                )

                                isGeneratingAiReminder = false
                                result.fold(
                                    onSuccess = { smartReminder ->
                                        latestAiReminder = smartReminder
                                        showAiResultDialog = true
                                        NotificationHelper.showNotification(
                                            context = context,
                                            channelId = NotificationHelper.CHANNEL_REMINDERS,
                                            notificationId = 7777,
                                            title = smartReminder.title,
                                            message = smartReminder.message
                                        )
                                    },
                                    onFailure = { _ ->
                                        val fallback = SmartReminderResult(
                                            title = "📚 পড়ার নিখুঁত সময়!",
                                            message = "আপনার আজকের লক্ষ্য পূরণে এখনই বই নিয়ে বসুন। ছোট ছোট পদক্ষেপে বড় সফলতা নিশ্চিত হয়।",
                                            suggestedAction = "আজকের অমীমাংসিত অধ্যায়গুলো শেষ করুন"
                                        )
                                        latestAiReminder = fallback
                                        showAiResultDialog = true
                                        NotificationHelper.showNotification(
                                            context = context,
                                            channelId = NotificationHelper.CHANNEL_REMINDERS,
                                            notificationId = 7777,
                                            title = fallback.title,
                                            message = fallback.message
                                        )
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGeneratingAiReminder
                    ) {
                        if (isGeneratingAiReminder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI রিমাইন্ডার বিশ্লেষণ করছে...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI দিয়ে সঠিক রিমাইন্ডার তৈরি ও পাঠান 🤖", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        // Auto Reminder & Spaced Repetition Revision Status Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    ),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "সার্বক্ষণিক অটো রিমাইন্ডার সক্রিয়",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "AUTO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "বাটন চাপ দেওয়া ছাড়া স্বয়ংক্রিয়ভাবে পড়ার ও রিভিশনের অ্যালার্ট আসবে",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "⏰ অটো অ্যালার্ট শিডিউল:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "প্রতিটি সক্রিয় রিমাইন্ডার + প্রতিদিন রাত ৮:৩০ এ রিভিশন অ্যালার্ট",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    NotificationHelper.scheduleAllRemindersAndRevisions(context, reminders, revisions)
                                    Toast.makeText(context, "সকল অটো রিমাইন্ডার ও রিভিশন অ্যালার্ট আপডেট হয়েছে! 🔔", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync Alarms",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Motivation Reminder Banner
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "প্রতিদিনের মোটিভেশন রিমাইন্ডার",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "প্রতিদিন অনুপ্রেরণামূলক স্টাডি উক্তি ও টিপস",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            NotificationHelper.triggerDailyMotivation(context)
                            Toast.makeText(context, "মোটিভেশন নোটিফিকেশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আজকের মোটিভেশন পাঠান ✨", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reminders List
        item {
            Text(
                text = "নির্ধারিত রিমাইন্ডার তালিকা",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (reminders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "কোনো রিমাইন্ডার তৈরি করা নেই। উপরে থাকা '+ Reminder' বাটন চেপে রুটিন যোগ করুন।",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(reminders) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onToggle = {
                        val isNowEnabled = repository.toggleReminder(reminder.id)
                        if (isNowEnabled) {
                            val (h, m) = parseTime(reminder.time)
                            NotificationHelper.scheduleStudyAlarm(
                                context = context,
                                reminderId = reminder.id,
                                title = "🔔 ${reminder.title}",
                                message = "পড়ার সময় হয়েছে (${reminder.time})। Veylora অ্যাপে আজকের পড়া সম্পন্ন করুন।",
                                hour24 = h,
                                minute = m
                            )
                            NotificationHelper.showNotification(
                                context = context,
                                channelId = NotificationHelper.CHANNEL_REMINDERS,
                                notificationId = reminder.id.hashCode(),
                                title = "🔔 রিমাইন্ডার সক্রিয়: ${reminder.title}",
                                message = "সময়: ${reminder.time} — Veylora এলার্ম সেট করা হয়েছে।"
                            )
                        } else {
                            NotificationHelper.cancelStudyAlarm(context, reminder.id)
                        }
                    },
                    onDelete = {
                        NotificationHelper.cancelStudyAlarm(context, reminder.id)
                        repository.deleteReminder(reminder.id)
                    }
                )
            }
        }
    }

    // AI Generated Reminder Detail Dialog
    if (showAiResultDialog && latestAiReminder != null) {
        val rem = latestAiReminder!!
        AlertDialog(
            onDismissRequest = { showAiResultDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Text("🤖", fontSize = 32.sp) },
            title = {
                Text(
                    text = rem.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = rem.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পরামর্শ: ${rem.suggestedAction}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.addReminder(
                            title = rem.title,
                            time = "08:00 PM",
                            repeatDays = "প্রতিদিন",
                            type = "AI_STUDY"
                        )
                        showAiResultDialog = false
                        Toast.makeText(context, "রুটিনে সেভ ও ডিভাইসে নোটিফিকেশন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("রুটিনে সেভ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiResultDialog = false }) {
                    Text("ঠিক আছে", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Add Reminder Dialog
    if (showAddReminderDialog) {
        var title by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("08:00 PM") }
        var repeatDays by remember { mutableStateOf("প্রতিদিন") }

        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("নতুন রিমাইন্ডার যোগ করুন", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("রিমাইন্ডারের নাম (Title)") },
                        placeholder = { Text("যেমন: রাতের গণিত প্র্যাকটিস") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("সময় (Time):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(
                                onClick = {
                                    val (currH, currM) = parseTime(time)
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val cal = java.util.Calendar.getInstance().apply {
                                                set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                                set(java.util.Calendar.MINUTE, minute)
                                            }
                                            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                                            time = sdf.format(cal.time)
                                        },
                                        currH,
                                        currM,
                                        false
                                    ).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ঘড়িতে বাছুন", fontSize = 11.sp)
                            }
                        }

                        // Quick Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "সকাল ০৬:০০" to "06:00 AM",
                                "সকাল ০৭:৩০" to "07:30 AM",
                                "বিকাল ০৪:৩০" to "04:30 PM",
                                "সন্ধ্যা ০৭:০০" to "07:00 PM",
                                "রাত ০৮:৩০" to "08:30 PM",
                                "রাত ১০:০০" to "10:00 PM"
                            ).forEach { (label, tVal) ->
                                val isSelected = time == tVal
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { time = tVal }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("নির্বাচিত সময়") },
                            placeholder = { Text("08:00 PM বা 06:30 AM") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = repeatDays,
                        onValueChange = { repeatDays = it },
                        label = { Text("দিন (Repeat)") },
                        placeholder = { Text("প্রতিদিন / শনি-বুধ") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            repository.addReminder(title.trim(), time.trim(), repeatDays.trim(), "STUDY")
                            val (h, m) = parseTime(time.trim())
                            NotificationHelper.scheduleStudyAlarm(
                                context = context,
                                reminderId = title.trim(),
                                title = "🔔 ${title.trim()}",
                                message = "পড়ার সময় হয়েছে ($time) — Veylora খুলে আজকের পাঠ সম্পন্ন করুন।",
                                hour24 = h,
                                minute = m
                            )
                            NotificationHelper.showNotification(
                                context = context,
                                channelId = NotificationHelper.CHANNEL_REMINDERS,
                                notificationId = title.hashCode(),
                                title = "✅ রিমাইন্ডার সেট করা হয়েছে!",
                                message = "${title.trim()} — প্রতিদিন $time এ অ্যালার্ট আসবে।"
                            )
                            showAddReminderDialog = false
                            Toast.makeText(context, "রিমাইন্ডার ও নোটিফিকেশন সেট সম্পন্ন!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সেভ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: StudyReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (reminder.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
            ),
            width = 1.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (reminder.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = reminder.time,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (reminder.isEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reminder.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "🗓 ${reminder.repeatDays}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
