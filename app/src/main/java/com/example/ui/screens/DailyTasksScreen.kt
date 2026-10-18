package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VeyloraRepository
import com.example.data.model.TaskItem
import com.example.ui.theme.*

@Composable
fun DailyTasksScreen(
    repository: VeyloraRepository,
    onOpenAddTask: () -> Unit
) {
    val tasks by repository.tasks.collectAsState()
    val profile by repository.profile.collectAsState()
    var selectedTab by remember { mutableStateOf("today") } // "today", "tomorrow", "all"
    var searchQuery by remember { mutableStateOf("") }

    var celebratedTaskTitle by remember { mutableStateOf<String?>(null) }
    var showLevelUpModal by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    val todayStr = repository.todayStr()
    val tomorrowStr = remember { repository.addDaysToStr(todayStr, 1) }

    LaunchedEffect(celebratedTaskTitle) {
        if (celebratedTaskTitle != null) {
            kotlinx.coroutines.delay(2600)
            celebratedTaskTitle = null
        }
    }

    val filteredTasks = remember(tasks, selectedTab, searchQuery) {
        tasks.filter { task ->
            val matchesTab = when (selectedTab) {
                "today" -> task.dueDate == todayStr || (!task.done && task.dueDate < todayStr)
                "tomorrow" -> task.dueDate == tomorrowStr
                else -> true
            }
            val matchesSearch = if (searchQuery.isNotBlank()) {
                task.title.contains(searchQuery, ignoreCase = true) ||
                        task.subject.contains(searchQuery, ignoreCase = true)
            } else true
            matchesTab && matchesSearch
        }
    }

    val pendingTasks = remember(filteredTasks) { filteredTasks.filter { !it.done } }
    val completedTasks = remember(filteredTasks) { filteredTasks.filter { it.done } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_tasks_screen_content"),
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
                        text = "দৈনিক টাস্ক (Daily Tasks)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${pendingTasks.size}টি কাজ বাকি আছে",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Add Task Button
                Button(
                    onClick = onOpenAddTask,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_task_header_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("টাস্ক খুঁজুন...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }

        // Tabs: Today | Tomorrow | All
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabPill(
                    title = "আজ (Today)",
                    isSelected = selectedTab == "today",
                    onClick = { selectedTab = "today" }
                )
                TabPill(
                    title = "আগামীকাল (Tomorrow)",
                    isSelected = selectedTab == "tomorrow",
                    onClick = { selectedTab = "tomorrow" }
                )
                TabPill(
                    title = "সবগুলো (All)",
                    isSelected = selectedTab == "all",
                    onClick = { selectedTab = "all" }
                )
            }
        }

        // Pending Tasks Section
        if (pendingTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "কোনো বাকি থাকা টাস্ক নেই! 🌟",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(pendingTasks) { task ->
                TaskFullRow(
                    task = task,
                    onToggle = {
                        val wasDone = task.done
                        repository.toggleTask(task.id)
                        if (!wasDone) {
                            celebratedTaskTitle = task.title
                            showConfetti = true
                            val nextXp = profile.xp + 20
                            if (nextXp / 100 > profile.xp / 100) {
                                showLevelUpModal = true
                            }
                        }
                    },
                    onDelete = { repository.deleteTask(task.id) }
                )
            }
        }

        // Completed Section
        if (completedTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "সম্পন্ন হয়েছে (${completedTasks.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VeyloraGreen
                )
            }

            items(completedTasks) { task ->
                TaskFullRow(
                    task = task,
                    onToggle = { repository.toggleTask(task.id) },
                    onDelete = { repository.deleteTask(task.id) }
                )
            }
        }
    }

        // Confetti Burst Overlay
        com.example.ui.components.ConfettiOverlay(
            isActive = showConfetti,
            onFinished = { showConfetti = false }
        )

        // Floating Task Completed Banner
        celebratedTaskTitle?.let { title ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                com.example.ui.components.TaskCompletedBanner(
                    visible = true,
                    xpGained = 20,
                    taskTitle = title
                )
            }
        }

        // Level Up Dialog
        if (showLevelUpModal) {
            val levelNum = (profile.xp + 20) / 100 + 1
            com.example.ui.components.LevelUpCelebrationDialog(
                newLevel = "Level $levelNum Scholar 🌟",
                xpTotal = profile.xp + 20,
                onDismiss = { showLevelUpModal = false }
            )
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ),
            width = 1.dp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun TaskFullRow(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        2 -> VeyloraRed
        1 -> VeyloraYellow
        else -> VeyloraGreen
    }
    val priorityText = when (task.priority) {
        2 -> "High"
        1 -> "Med"
        else -> "Low"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.done) VeyloraGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline
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
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (task.done) VeyloraGreen else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (task.done) VeyloraGreen else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.done) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${task.subject}${if (task.timeSlot.isNotBlank()) " • ${task.timeSlot}" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = priorityColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = priorityText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
