package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.VeyloraRepository
import com.example.data.model.ChapterItem
import com.example.data.model.SubjectItem
import com.example.ui.theme.*

@Composable
fun SubjectsScreen(
    repository: VeyloraRepository
) {
    val context = LocalContext.current
    val subjects by repository.subjects.collectAsState()
    val profile by repository.profile.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // "all" or "progress"
    var expandedSubjectId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showClass910ConfirmDialog by remember { mutableStateOf(false) }
    var selectedGroupInDialog by remember(profile.group) { mutableStateOf(profile.group) }
    var showAddChapterDialogForSubjectId by remember { mutableStateOf<String?>(null) }
    var activeNoteModal by remember { mutableStateOf<Pair<String, ChapterItem>?>(null) }

    val filteredSubjects = remember(subjects, selectedFilter, searchQuery) {
        subjects.filter {
            if (searchQuery.isNotBlank()) {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.chapters.any { c -> c.name.contains(searchQuery, ignoreCase = true) }
            } else true
        }.sortedByDescending { if (selectedFilter == "progress") it.progressPercent else 0 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("subjects_screen_content"),
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
                        text = "বিষয় ও সিলেবাস (Subjects)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "সকল বিষয় ও অধ্যায়ের বিস্তারিত অগ্রগতি",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Class 9-10 Syllabus Preset Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            width = 1.dp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showClass910ConfirmDialog = true }
                            .testTag("load_class_9_10_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📚", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "৯ম-১০ম সিলেবাস",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAddSubjectDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("add_subject_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subject",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("বিষয় বা অধ্যায় খুঁজুন...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
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

        // Filter Pills: All Subjects vs My Progress
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("সব বিষয় (All)", maxLines = 1, softWrap = false) }
                )

                FilterChip(
                    selected = selectedFilter == "progress",
                    onClick = { selectedFilter = "progress" },
                    label = { Text("অগ্রগতি অনুযায়ী (Top)", maxLines = 1, softWrap = false) }
                )
            }
        }

        // Subject Items
        items(filteredSubjects) { subject ->
            SubjectCard(
                subject = subject,
                isExpanded = expandedSubjectId == subject.id,
                onToggleExpand = {
                    expandedSubjectId = if (expandedSubjectId == subject.id) null else subject.id
                },
                onToggleChapter = { chapterId ->
                    repository.toggleChapter(subject.id, chapterId)
                },
                onAddChapter = {
                    showAddChapterDialogForSubjectId = subject.id
                },
                onOpenNotes = { chapter ->
                    activeNoteModal = Pair(subject.id, chapter)
                },
                onDeleteSubject = {
                    repository.deleteSubject(subject.id)
                }
            )
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("📚") }
        val emojis = listOf(
            "📚", "⚛️", "🔬", "📐", "🧪", "🧬",
            "🧮", "💻", "📖", "📜", "🎨", "🌍",
            "✍️", "🏛️", "🧠", "📊", "💼", "🎵",
            "🔭", "➗", "🤖", "🗣️", "🏥", "⚖️"
        )

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("নতুন Subject যোগ করুন", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("বিষয়ের নাম (Subject Name)") },
                        placeholder = { Text("যেমন: তথ্য ও যোগাযোগ প্রযুক্তি") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("আইকন পছন্দ করুন (${emojis.size}টি আইকন):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    // 4 rows of 6 icons
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        emojis.chunked(6).forEach { rowEmojis ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowEmojis.forEach { em ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (emoji == em) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (emoji == em) CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                            width = 1.5.dp
                                        ) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clickable { emoji = em }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) { Text(em, fontSize = 18.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            repository.addSubject(name.trim(), emoji, "#3B82F6")
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("যোগ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showClass910ConfirmDialog) {
        val groupPresets = listOf(
            Triple(com.example.data.Class910Curriculum.GROUP_SCIENCE, "বিজ্ঞান বিভাগ (Science)", "⚡"),
            Triple(com.example.data.Class910Curriculum.GROUP_COMMERCE, "ব্যবসায় শিক্ষা (Commerce)", "💼"),
            Triple(com.example.data.Class910Curriculum.GROUP_ARTS, "মানবিক বিভাগ (Arts)", "🏛️"),
            Triple("ALL", "সকল ১৮টি বিষয় একসাথে (All Subjects)", "📚")
        )

        AlertDialog(
            onDismissRequest = { showClass910ConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Text("📚", fontSize = 32.sp) },
            title = {
                Text(
                    text = "৯ম-১০ম শ্রেণী সিলেবাস ও গ্রুপ নির্বাচন",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "আপনার গ্রুপ অনুযায়ী বিষয় ও সম্পূর্ণ অধ্যায় সিলেবাস লোড করতে গ্রুপ বাছাই করুন:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    groupPresets.forEach { (valKey, labelText, emoji) ->
                        val isSelected = selectedGroupInDialog == valKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                width = 1.5.dp
                            ) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGroupInDialog = valKey }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                    Text(
                                        text = labelText,
                                        fontSize = 12.sp,
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

                    Text(
                        text = "💡 এতে প্রতিটি অধ্যায় টিক দিয়ে ট্র্যাকিং ও অটো রিভিশন সুবিধা পাবেন।",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedGroupInDialog == "ALL") {
                            repository.loadAllClass910Subjects()
                            Toast.makeText(context, "৯ম-১০ম শ্রেণীর সকল ১৮টি বিষয় লোড হয়েছে! 🎓", Toast.LENGTH_LONG).show()
                        } else {
                            repository.loadClass910Preset(selectedGroupInDialog)
                            Toast.makeText(context, "$selectedGroupInDialog এর সিলেবাস সফলভাবে লোড হয়েছে! 🎓", Toast.LENGTH_LONG).show()
                        }
                        showClass910ConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("লোড করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClass910ConfirmDialog = false }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Add Chapter Dialog
    showAddChapterDialogForSubjectId?.let { subjectId ->
        var chapterTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddChapterDialogForSubjectId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("নতুন Chapter যোগ করুন", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("অধ্যায়ের নাম (Chapter Title)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitle.isNotBlank()) {
                            repository.addChapter(subjectId, chapterTitle.trim())
                            showAddChapterDialogForSubjectId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সেভ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialogForSubjectId = null }) {
                    Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Notes Editor Dialog
    activeNoteModal?.let { (subjectId, chapter) ->
        var noteText by remember { mutableStateOf(chapter.notes) }
        AlertDialog(
            onDismissRequest = { activeNoteModal = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("📝 সূত্র ও নোট — ${chapter.name}", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("গুরুত্বপূর্ণ সূত্র, শর্টকাট বা পয়েন্ট লিখে রাখুন...") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateChapterNote(subjectId, chapter.id, noteText)
                        activeNoteModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("সংরক্ষণ করুন", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeNoteModal = null }) {
                    Text("বন্ধ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleChapter: (String) -> Unit,
    onAddChapter: () -> Unit,
    onOpenNotes: (ChapterItem) -> Unit,
    onDeleteSubject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            width = 1.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = subject.icon, fontSize = 22.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subject.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${subject.completedChapters} / ${subject.chapters.size} অধ্যায় সম্পন্ন",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${subject.progressPercent}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (subject.progressPercent > 50) MaterialTheme.colorScheme.primary else VeyloraYellow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (subject.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Expanded Chapter List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.8.dp)

                    subject.chapters.forEach { chapter ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (chapter.done) VeyloraGreen else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (chapter.done) VeyloraGreen else MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(5.dp)
                                            )
                                            .clickable { onToggleChapter(chapter.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (chapter.done) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = chapter.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (chapter.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        if (chapter.notes.isNotBlank()) {
                                            Text(
                                                text = "📝 ${chapter.notes}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onOpenNotes(chapter) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Notes",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Chapter Action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onAddChapter) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Chapter", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(onClick = onDeleteSubject) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = VeyloraRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছে ফেলুন", color = VeyloraRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
