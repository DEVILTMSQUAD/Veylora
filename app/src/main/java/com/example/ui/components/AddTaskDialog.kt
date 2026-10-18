package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VeyloraRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    repository: VeyloraRepository,
    initialDate: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val subjects by repository.subjects.collectAsState()
    val today = remember { repository.todayStr() }
    val tomorrow = remember { repository.addDaysToStr(today, 1) }
    val dayAfterTomorrow = remember { repository.addDaysToStr(today, 2) }

    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "পদার্থবিজ্ঞান (Physics)") }
    var priority by remember { mutableIntStateOf(1) } // 0=Low, 1=Med, 2=High
    var dueDate by remember { mutableStateOf(initialDate.ifBlank { today }) }
    var timeSlot by remember { mutableStateOf("08:30 PM - 10:00 PM") }
    var isSubjectDropdownOpen by remember { mutableStateOf(false) }

    // Quick Time Slot presets for single-tap convenience
    val timePresets = remember {
        listOf(
            "🌅 সকাল (07:00 AM - 08:30 AM)" to "07:00 AM - 08:30 AM",
            "☀️ সকাল (09:00 AM - 10:30 AM)" to "09:00 AM - 10:30 AM",
            "🌤 দুপুর (11:30 AM - 01:00 PM)" to "11:30 AM - 01:00 PM",
            "🌇 বিকাল (04:00 PM - 05:30 PM)" to "04:00 PM - 05:30 PM",
            "🌆 সন্ধ্যা (06:30 PM - 08:00 PM)" to "06:30 PM - 08:00 PM",
            "🌙 রাত (08:30 PM - 10:00 PM)" to "08:30 PM - 10:00 PM",
            "🌌 রাত (10:30 PM - 12:00 AM)" to "10:30 PM - 12:00 AM"
        )
    }

    // Custom time picker helper
    fun showClockTimePicker() {
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                val endCal = (startCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, 90) // default 1.5 hr study slot
                }
                val format = SimpleDateFormat("hh:mm a", Locale.US)
                val newSlot = "${format.format(startCal.time)} - ${format.format(endCal.time)}"
                timeSlot = newSlot
            },
            currentHour,
            currentMin,
            false
        ).show()
    }

    fun showCalendarDatePicker() {
        val cal = Calendar.getInstance()
        try {
            val parts = dueDate.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                dueDate = chosen
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "নতুন Task যোগ করুন",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task শিরোনাম (যেমন: অধ্যায় ৩ রিভিশন)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownOpen,
                    onExpandedChange = { isSubjectDropdownOpen = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("বিষয় (Subject)") },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownOpen) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isSubjectDropdownOpen,
                        onDismissRequest = { isSubjectDropdownOpen = false }
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.icon} ${s.name}") },
                                onClick = {
                                    selectedSubject = s.name
                                    isSubjectDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Priority Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Priority (গুরুত্ব):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(0, "Low", VeyloraGreen),
                            Triple(1, "Medium", VeyloraYellow),
                            Triple(2, "High", VeyloraRed)
                        ).forEach { (pIndex, pName, pColor) ->
                            val isSelected = priority == pIndex
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) pColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) pColor else Color.Transparent),
                                    width = 1.dp
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { priority = pIndex }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Date Selection with Quick Chips & Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("তারিখ (Date):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(
                            onClick = { showCalendarDatePicker() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ক্যালেন্ডার", fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "আজকে" to today,
                            "আগামীকাল" to tomorrow,
                            "পরশু" to dayAfterTomorrow
                        ).forEach { (label, dateVal) ->
                            val isSelected = dueDate == dateVal
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { dueDate = dateVal }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("বাস্তব তারিখ (YYYY-MM-DD)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Time Slot with Easy One-Tap Chips & Clock Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("পড়ার সময় (Time Slot):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Button(
                            onClick = { showClockTimePicker() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ঘড়ি দিয়ে বাছুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("সহজ এক-ট্যাপ স্লট:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Scrollable Horizontal Quick Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timePresets.forEach { (label, slotVal) ->
                            val isSelected = timeSlot == slotVal
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { timeSlot = slotVal }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Editable input field for fine-tuning
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("নির্বাচিত সময় স্লট") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        repository.addTask(
                            title = title.trim(),
                            subject = selectedSubject,
                            priority = priority,
                            dueDate = dueDate,
                            timeSlot = timeSlot
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("সেভ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
