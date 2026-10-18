package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VeyloraRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    repository: VeyloraRepository,
    onOpenAddTaskForDate: (String) -> Unit
) {
    val tasks by repository.tasks.collectAsState()
    val exams by repository.exams.collectAsState()
    val revisions by repository.revisions.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val todayStr = remember { repository.todayStr() }
    var selectedDateStr by remember { mutableStateOf(todayStr) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthTitle = remember(currentMonthCalendar) {
        monthFormat.format(currentMonthCalendar.time)
    }

    // Generate days of month grid
    val daysInMonth = remember(currentMonthCalendar) {
        val cal = currentMonthCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val offset = firstDayOfWeek - 1
        Pair(offset, maxDays)
    }

    val selectedDayTasks = remember(tasks, selectedDateStr) {
        tasks.filter { it.dueDate == selectedDateStr }
    }
    val selectedDayExams = remember(exams, selectedDateStr) {
        exams.filter { it.date == selectedDateStr }
    }
    val selectedDayRevisions = remember(revisions, selectedDateStr) {
        revisions.filter { it.dueDate == selectedDateStr }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
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
                        text = "ক্যালেন্ডার ও সময়সূচি (Calendar)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "পরীক্ষা, রিভিশন ও দৈনিক সময়সূচি",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showAddExamDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Exam", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }

        // Calendar Card
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
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val next = currentMonthCalendar.clone() as Calendar
                                next.add(Calendar.MONTH, -1)
                                currentMonthCalendar = next
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Text(
                            text = monthTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                val next = currentMonthCalendar.clone() as Calendar
                                next.add(Calendar.MONTH, 1)
                                currentMonthCalendar = next
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of Week Header
                    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        dayNames.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val (offsetDays, totalMonthDays) = daysInMonth
                    val totalSlots = ((offsetDays + totalMonthDays + 6) / 7) * 7

                    for (row in 0 until (totalSlots / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            for (col in 0 until 7) {
                                val index = row * 7 + col
                                val dayNumber = index - offsetDays + 1

                                if (dayNumber in 1..totalMonthDays) {
                                    val year = currentMonthCalendar.get(Calendar.YEAR)
                                    val month = currentMonthCalendar.get(Calendar.MONTH) + 1
                                    val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNumber)

                                    val isSelected = dateKey == selectedDateStr
                                    val isToday = dateKey == todayStr
                                    val hasExam = exams.any { it.date == dateKey }
                                    val hasTask = tasks.any { it.dueDate == dateKey }
                                    val hasRevision = revisions.any { it.dueDate == dateKey }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDateStr = dateKey },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    isToday -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                maxLines = 1,
                                                softWrap = false
                                            )

                                            // Event indicator dots
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                if (hasExam) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(VeyloraRed))
                                                }
                                                if (hasTask) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary))
                                                }
                                                if (hasRevision) {
                                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(VeyloraGreen))
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = MaterialTheme.colorScheme.primary, label = "Today")
                        LegendItem(color = VeyloraRed, label = "Exam")
                        LegendItem(color = VeyloraGreen, label = "Revision")
                        LegendItem(color = MaterialTheme.colorScheme.secondary, label = "Task")
                    }
                }
            }
        }

        // Selected Date Agenda
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agenda • $selectedDateStr",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                TextButton(onClick = { onOpenAddTaskForDate(selectedDateStr) }) {
                    Text("+ Add Task", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, softWrap = false)
                }
            }
        }

        // Agenda Items for Selected Date
        if (selectedDayTasks.isEmpty() && selectedDayExams.isEmpty() && selectedDayRevisions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "এই তারিখে কোনো টাস্ক বা পরীক্ষা নেই।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Exams on this date
            items(selectedDayExams) { exam ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(VeyloraRed.copy(alpha = 0.5f)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = VeyloraRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EXAM: ${exam.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = exam.date,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    com.example.notifications.NotificationHelper.showNotification(
                                        context = context,
                                        channelId = com.example.notifications.NotificationHelper.CHANNEL_CALENDAR,
                                        notificationId = exam.id.hashCode(),
                                        title = "📅 পরীক্ষা রিমাইন্ডার: ${exam.name}",
                                        message = "পরীক্ষার তারিখ: ${exam.date} — রুটিন অনুযায়ী প্রস্তুতি নিন!"
                                    )
                                    android.widget.Toast.makeText(context, "পরীক্ষার রিমাইন্ডার সেট করা হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Reminder", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { repository.deleteExam(exam.id) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Tasks on this date
            items(selectedDayTasks) { task ->
                TaskCardRow(task = task, onToggle = { repository.toggleTask(task.id) })
            }

            // Revisions due on this date
            items(selectedDayRevisions) { rev ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(VeyloraGreen.copy(alpha = 0.5f)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = VeyloraGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Revision: ${rev.title}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Stage ${rev.stage + 1}/5 Spaced Repetition",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Exam Dialog
    if (showAddExamDialog) {
        var examTitle by remember { mutableStateOf("") }
        var examDate by remember { mutableStateOf(selectedDateStr) }

        AlertDialog(
            onDismissRequest = { showAddExamDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("নতুন Exam যোগ করুন", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = examTitle,
                        onValueChange = { examTitle = it },
                        label = { Text("পরীক্ষার নাম (e.g. Model Test, Board Exam)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = examDate,
                        onValueChange = { examDate = it },
                        label = { Text("তারিখ (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (examTitle.isNotBlank()) {
                            repository.addExam(examTitle.trim(), examDate)
                            com.example.notifications.NotificationHelper.showNotification(
                                context = context,
                                channelId = com.example.notifications.NotificationHelper.CHANNEL_CALENDAR,
                                notificationId = examTitle.hashCode(),
                                title = "📅 পরীক্ষার রিমাইন্ডার যোগ হয়েছে!",
                                message = "${examTitle.trim()} — তারিখ: $examDate এ পরীক্ষা।"
                            )
                            showAddExamDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সেভ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExamDialog = false }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
    }
}
