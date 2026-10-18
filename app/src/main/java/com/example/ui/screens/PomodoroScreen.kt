package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundPlayer
import com.example.data.VeyloraRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PomodoroScreen(
    repository: VeyloraRepository,
    soundPlayer: SoundPlayer
) {
    val tasks by repository.tasks.collectAsState()
    val activity by repository.activity.collectAsState()
    val todayStr = remember { repository.todayStr() }
    val todayActivity = activity[todayStr]

    // Modes: Work = 0 (25m), Short Break = 1 (5m), Long Break = 2 (15m)
    var selectedMode by remember { mutableIntStateOf(0) }
    var workDurationMinutes by remember { mutableIntStateOf(25) }
    var breakDurationMinutes by remember { mutableIntStateOf(5) }
    var longBreakDurationMinutes by remember { mutableIntStateOf(15) }

    val currentTotalSeconds = remember(selectedMode, workDurationMinutes, breakDurationMinutes, longBreakDurationMinutes) {
        when (selectedMode) {
            0 -> workDurationMinutes * 60
            1 -> breakDurationMinutes * 60
            else -> longBreakDurationMinutes * 60
        }
    }

    var secondsLeft by remember { mutableIntStateOf(currentTotalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var targetEndTimeMs by remember { mutableLongStateOf(0L) }
    var linkedTaskId by remember { mutableStateOf<String?>(null) }
    var isAmbientPlaying by remember { mutableStateOf(soundPlayer.isPlayingAmbient) }
    var ambientVolume by remember { mutableFloatStateOf(soundPlayer.ambientVolume) }

    // Restore state from background/exit check (never loses time when leaving app)
    LaunchedEffect(Unit) {
        val restored = repository.getRestoredPomodoroState()
        selectedMode = restored.mode
        linkedTaskId = restored.linkedTaskId
        if (restored.didFinishWhileAway) {
            soundPlayer.playCompletionChime()
            isRunning = false
            secondsLeft = currentTotalSeconds
        } else if (restored.isRunning) {
            isRunning = true
            secondsLeft = restored.secondsLeft
            targetEndTimeMs = System.currentTimeMillis() + (restored.secondsLeft * 1000L)
        } else {
            secondsLeft = restored.secondsLeft
        }
    }

    // Precise Wall-Clock Timer countdown loop (Never loses time when app backgrounded/minimized)
    LaunchedEffect(isRunning, targetEndTimeMs) {
        while (isRunning) {
            val now = System.currentTimeMillis()
            val remMillis = targetEndTimeMs - now
            val remSecs = (remMillis / 1000L).toInt()

            if (remSecs <= 0) {
                secondsLeft = 0
                isRunning = false
                repository.savePomodoroRunningState(false, 0L, selectedMode, workDurationMinutes, null)
                soundPlayer.playCompletionChime()
                if (selectedMode == 0) {
                    repository.recordPomodoroSession(workDurationMinutes)
                }
                secondsLeft = currentTotalSeconds
                break
            } else {
                secondsLeft = remSecs
            }
            delay(500)
        }
    }

    val progress = remember(secondsLeft, currentTotalSeconds) {
        if (currentTotalSeconds > 0) (secondsLeft.toFloat() / currentTotalSeconds.toFloat()).coerceIn(0f, 1f) else 1f
    }

    val minutes = secondsLeft / 60
    val secs = secondsLeft % 60
    val timeFormatted = remember(minutes, secs) {
        String.format("%02d:%02d", minutes, secs)
    }

    val pendingTasks = remember(tasks) { tasks.filter { !it.done } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("pomodoro_screen_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "পোমোডোরো টাইমার (Focus)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "ব্যাকগ্রাউন্ডেও টাইমার সঠিকভাবে চলবে",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = {
                    if (!isRunning) {
                        workDurationMinutes = if (workDurationMinutes == 25) 30 else 25
                        secondsLeft = workDurationMinutes * 60
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = "Adjust", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Mode Selector: Work | Short Break | Long Break
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                width = 1.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModePill(
                    title = "Work",
                    isSelected = selectedMode == 0,
                    onClick = {
                        selectedMode = 0
                        isRunning = false
                        secondsLeft = workDurationMinutes * 60
                        repository.savePomodoroRunningState(false, 0L, 0, workDurationMinutes, linkedTaskId)
                    }
                )
                ModePill(
                    title = "Short Break",
                    isSelected = selectedMode == 1,
                    onClick = {
                        selectedMode = 1
                        isRunning = false
                        secondsLeft = breakDurationMinutes * 60
                        repository.savePomodoroRunningState(false, 0L, 1, breakDurationMinutes, linkedTaskId)
                    }
                )
                ModePill(
                    title = "Long Break",
                    isSelected = selectedMode == 2,
                    onClick = {
                        selectedMode = 2
                        isRunning = false
                        secondsLeft = longBreakDurationMinutes * 60
                        repository.savePomodoroRunningState(false, 0L, 2, longBreakDurationMinutes, linkedTaskId)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Animated Circular Progress Timer Ring
        val ringTrackColor = MaterialTheme.colorScheme.surfaceVariant
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("pomodoro_timer_circle"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                drawCircle(
                    color = ringTrackColor,
                    style = Stroke(width = strokeWidth)
                )

                val brush = Brush.sweepGradient(
                    colors = listOf(primaryColor, secondaryColor, primaryColor)
                )

                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeFormatted,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (selectedMode == 0) "Focus Time" else "Relax Break",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls: Reset | Start/Pause | Boost
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isRunning = false
                    secondsLeft = currentTotalSeconds
                    repository.savePomodoroRunningState(false, 0L, selectedMode, workDurationMinutes, null)
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurface)
            }

            // Start/Pause Button (Saves targetEndTimeMs so timer runs in background)
            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                        repository.savePomodoroRunningState(false, 0L, selectedMode, workDurationMinutes, linkedTaskId)
                    } else {
                        val target = System.currentTimeMillis() + (secondsLeft * 1000L)
                        targetEndTimeMs = target
                        isRunning = true
                        repository.savePomodoroRunningState(true, target, selectedMode, workDurationMinutes, linkedTaskId)
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("pomodoro_toggle_button"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = {
                    if (!isRunning) {
                        secondsLeft = (secondsLeft + 300).coerceAtMost(3600)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("+5m", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Stats Row: Sessions | Today | Focus Score
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                width = 1.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Sessions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${todayActivity?.sessionsCount ?: 3}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalMin = todayActivity?.minutes ?: 75
                    val hours = totalMin / 60
                    val remMin = totalMin % 60
                    Text(
                        text = "${hours}h ${remMin}m",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Focus Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "85%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VeyloraGreen
                    )
                }
            }
        }

        // Ambient Background Sound
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                width = 1.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                soundPlayer.toggleAmbient { isPlaying ->
                                    isAmbientPlaying = isPlaying
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isAmbientPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "বৃষ্টি ও অ্যাম্বিয়েন্ট সাউন্ড (Rain Sound)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAmbientPlaying) "🌧 শান্ত বৃষ্টির শব্দ চলছে (Playing)" else "বন্ধ (ট্যাপ করে রিলাক্সিং সাউন্ড চালু করুন)",
                                fontSize = 11.sp,
                                color = if (isAmbientPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAmbientPlaying,
                        onCheckedChange = {
                            soundPlayer.toggleAmbient { isPlaying ->
                                isAmbientPlaying = isPlaying
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (isAmbientPlaying) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ভলিউম: ${(ambientVolume * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Slider(
                        value = ambientVolume,
                        onValueChange = {
                            ambientVolume = it
                            soundPlayer.setVolume(it)
                        },
                        valueRange = 0.15f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Task Linking
        if (pendingTasks.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "কোন Task এ Focus করছ?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    pendingTasks.take(3).forEach { task ->
                        val isLinked = linkedTaskId == task.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLinked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { linkedTaskId = if (isLinked) null else task.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isLinked,
                                    onClick = { linkedTaskId = if (isLinked) null else task.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${task.title} (${task.subject})",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
