package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VeyloraRepository
import com.example.data.model.MockTestItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    repository: VeyloraRepository
) {
    val activity by repository.activity.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val revisions by repository.revisions.collectAsState()
    val flashcards by repository.flashcards.collectAsState()
    val mockTests by repository.mockTests.collectAsState()
    val profile by repository.profile.collectAsState()

    var selectedChartTab by remember { mutableStateOf("hours") } // "hours", "tasks", "syllabus"
    var showAddTestDialog by remember { mutableStateOf(false) }

    val todayStr = remember { repository.todayStr() }

    // Real syllabus calculations (no dummy offsets)
    val totalChapters = remember(subjects) { subjects.sumOf { it.chapters.size } }
    val completedChapters = remember(subjects) { subjects.sumOf { it.completedChapters } }
    val overallSyllabusPercent = remember(totalChapters, completedChapters) {
        if (totalChapters > 0) ((completedChapters.toDouble() / totalChapters) * 100).toInt() else 0
    }

    // Real task stats
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.done }
    val taskRate = if (totalTasks > 0) ((completedTasks.toDouble() / totalTasks) * 100).toInt() else 0

    // Real 7-day data preparation for Recharts-style charts
    val last7DaysData = remember(activity, tasks, todayStr) {
        val list = mutableListOf<DayAnalyticsData>()
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 6 downTo 0) {
            val d = repository.addDaysToStr(todayStr, -i)
            val dayName = try {
                val dateObj = parseFormat.parse(d) ?: Date()
                dayNameFormat.format(dateObj)
            } catch (_: Exception) {
                "D${7 - i}"
            }

            val dayRecord = activity[d]
            val studyMinutes = dayRecord?.minutes ?: 0
            val studyHours = studyMinutes / 60.0

            val tasksDueOrDone = tasks.filter { it.dueDate == d || it.completedDate == d }
            val doneCount = tasksDueOrDone.count { it.done }
            val totalCount = tasksDueOrDone.size
            val completionRate = if (totalCount > 0) {
                (doneCount.toFloat() / totalCount * 100f).coerceIn(0f, 100f)
            } else if (studyMinutes > 0) {
                100f
            } else {
                0f
            }

            list.add(
                DayAnalyticsData(
                    dateStr = d,
                    dayLabel = dayName,
                    studyMinutes = studyMinutes,
                    studyHours = studyHours,
                    tasksDone = doneCount,
                    tasksTotal = totalCount,
                    completionRatePercent = completionRate
                )
            )
        }
        list
    }

    // Real total minutes from activity (no fake +2800)
    val totalMinutes = remember(activity) {
        activity.values.sumOf { it.minutes }
    }
    val totalHoursStr = String.format(Locale.US, "%.1f", totalMinutes / 60.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "প্রোগ্রেস ও অ্যানালিটিক্স",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "রিয়েল টাইম স্টাডি ও টাস্ক পারফরম্যান্স চার্ট",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary 4 Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "মোট পড়াশোনা",
                    value = "${totalHoursStr}h",
                    icon = Icons.Default.Timer,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "টাস্ক রেট",
                    value = "$taskRate%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "সিলেবাস",
                    value = "$overallSyllabusPercent%",
                    icon = Icons.Default.MenuBook,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "স্ট্রিক",
                    value = "${profile.streak} দিন",
                    icon = Icons.Default.LocalFireDepartment,
                    color = VeyloraFlame,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recharts Data Visualization Card
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
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header with chart mode pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Data Visualization",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedChartTab == "hours") "সাপ্তাহিক স্টাডি আওয়ার অগ্রগতি" else "দৈনিক টাস্ক সম্পন্ন করার হার (%)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Chart Switcher
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChartTabButton(
                                title = "ঘণ্টা",
                                selected = selectedChartTab == "hours",
                                onClick = { selectedChartTab = "hours" }
                            )
                            ChartTabButton(
                                title = "টাস্ক %",
                                selected = selectedChartTab == "tasks",
                                onClick = { selectedChartTab = "tasks" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Recharts Render
                    if (selectedChartTab == "hours") {
                        RechartsStudyHoursBarChart(
                            data = last7DaysData,
                            dailyGoalHours = profile.dailyGoalMinutes / 60.0
                        )
                    } else {
                        RechartsTaskCompletionAreaChart(
                            data = last7DaysData
                        )
                    }
                }
            }
        }

        // Grand Overall Progress Card
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
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সম্পূর্ণ সিলেবাস অগ্রগতি",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$completedChapters / $totalChapters অধ্যায় সম্পন্ন হয়েছে",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$overallSyllabusPercent%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = (overallSyllabusPercent / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "progress"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Subjects Progress Breakdown
        item {
            Text(
                text = "বিষয়ভিত্তিক সিলেবাস বিস্তার",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (subjects.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "কোনো বিষয় এখনো যোগ করা হয়নি। Subjects ট্যাব থেকে বিষয় যুক্ত করুন।",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(subjects) { subject ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
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
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = subject.icon, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = subject.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${subject.completedChapters}/${subject.chapters.size} অধ্যায়",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${subject.progressPercent}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DayAnalyticsData(
    val dateStr: String,
    val dayLabel: String,
    val studyMinutes: Int,
    val studyHours: Double,
    val tasksDone: Int,
    val tasksTotal: Int,
    val completionRatePercent: Float
)

@Composable
private fun ChartTabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            width = 1.dp
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Recharts-style Animated Bar Chart for Study Hours with touch-to-inspect Tooltip
 */
@Composable
fun RechartsStudyHoursBarChart(
    data: List<DayAnalyticsData>,
    dailyGoalHours: Double
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    val maxHours = remember(data, dailyGoalHours) {
        val observed = data.maxOfOrNull { it.studyHours } ?: 1.0
        val target = if (dailyGoalHours > 0) dailyGoalHours else 2.0
        maxOf(observed, target, 3.0)
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Selected Tooltip Bar Info
        val activeItem = selectedIndex?.let { data.getOrNull(it) } ?: data.lastOrNull()
        if (activeItem != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    width = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${activeItem.dateStr} (${activeItem.dayLabel})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "⏱ ${activeItem.studyMinutes} মিনিট (${String.format(Locale.US, "%.1f", activeItem.studyHours)} ঘণ্টা)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Canvas Recharts Bar Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / data.size
                            val tappedIndex = (offset.x / slotWidth).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val w = size.width
                val h = size.height - 30f // room for day labels
                val count = data.size
                val slotWidth = w / count
                val barWidth = slotWidth * 0.45f

                // Horizontal Guide Lines (Grid)
                val gridSteps = 3
                for (s in 0..gridSteps) {
                    val y = h - (h / gridSteps * s)
                    drawLine(
                        color = outlineColor.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Reference line for Daily Goal
                val goalY = (h - (dailyGoalHours / maxHours * h).toFloat()).coerceIn(0f, h)
                drawLine(
                    color = Color(0xFFEF4444).copy(alpha = 0.6f),
                    start = Offset(0f, goalY),
                    end = Offset(w, goalY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )

                // Draw Bars
                data.forEachIndexed { i, item ->
                    val isSelected = selectedIndex == i
                    val xCenter = slotWidth * i + slotWidth / 2f
                    val barHeight = ((item.studyHours / maxHours) * h * animProgress.value).toFloat().coerceAtLeast(6f)
                    val topY = h - barHeight

                    // Bar gradient brush
                    val barBrush = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(Color(0xFFFBBF24), primaryColor)
                        } else {
                            listOf(primaryColor, secondaryColor)
                        },
                        startY = topY,
                        endY = h
                    )

                    // Draw rounded bar
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(xCenter - barWidth / 2f, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Glowing top cap if non-zero
                    if (item.studyMinutes > 0) {
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(xCenter, topY)
                        )
                    }
                }
            }
        }

        // Day Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Text(
                    text = item.dayLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedIndex = index }
                )
            }
        }
    }
}

/**
 * Recharts-style Area / Spline Chart for Task Completion Rates
 */
@Composable
fun RechartsTaskCompletionAreaChart(
    data: List<DayAnalyticsData>
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = Color(0xFF10B981) // Emerald Green
    val outlineColor = MaterialTheme.colorScheme.outline

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        val activeItem = selectedIndex?.let { data.getOrNull(it) } ?: data.lastOrNull()
        if (activeItem != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.12f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(primaryColor.copy(alpha = 0.3f)),
                    width = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${activeItem.dateStr} (${activeItem.dayLabel})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🎯 টাস্ক সম্পন্ন: ${activeItem.completionRatePercent.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / (data.size - 1).coerceAtLeast(1)
                            val tappedIndex = ((offset.x + slotWidth / 2f) / slotWidth).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val w = size.width
                val h = size.height - 25f
                val count = data.size
                if (count < 2) return@Canvas

                val stepX = w / (count - 1)

                // Horizontal Grid lines (0%, 50%, 100%)
                for (step in 0..2) {
                    val y = h - (h / 2f * step)
                    drawLine(
                        color = outlineColor.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                val points = data.mapIndexed { i, d ->
                    val x = i * stepX
                    val y = (h - (d.completionRatePercent / 100f * h * animProgress.value)).coerceIn(0f, h)
                    Offset(x, y)
                }

                // Smooth Path
                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                // Fill Area Under Curve
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, h)
                    lineTo(points.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = h
                    )
                )

                // Draw Line
                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Dots
                points.forEachIndexed { idx, pt ->
                    val isSelected = selectedIndex == idx
                    drawCircle(
                        color = if (isSelected) Color.White else primaryColor,
                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                        center = pt
                    )
                    if (isSelected) {
                        drawCircle(
                            color = primaryColor,
                            radius = 7.dp.toPx(),
                            center = pt,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }

        // Day Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                Text(
                    text = item.dayLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedIndex = index }
                )
            }
        }
    }
}
