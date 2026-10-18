package com.example.data.model

import java.util.UUID

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
    val dueDate: String = "", // YYYY-MM-DD
    val timeSlot: String = "",
    val done: Boolean = false,
    val completedDate: String = ""
)

data class ChapterItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val done: Boolean = false,
    val notes: String = ""
)

data class SubjectItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "📚",
    val chapters: List<ChapterItem> = emptyList(),
    val colorHex: String = "#3B82F6"
) {
    val completedChapters: Int
        get() = chapters.count { it.done }

    val progressPercent: Int
        get() = if (chapters.isNotEmpty()) ((completedChapters.toDouble() / chapters.size) * 100).toInt() else 0
}

data class RevisionItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val stage: Int = 0, // 0..4 (1, 3, 7, 14, 30 days)
    val dueDate: String,
    val chapterKey: String = ""
)

data class FlashcardItem(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val question: String,
    val answer: String,
    val box: Int = 0, // 0..4 for Leitner box
    val dueDate: String
)

data class ExamItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: String
)

data class MockTestItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val subject: String,
    val score: Int,
    val total: Int = 100,
    val date: String
)

data class MistakeItem(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val subject: String,
    val errorReason: String,
    val fixed: Boolean = false
)

data class ActivityRecord(
    val minutes: Int = 0,
    val tasksCount: Int = 0,
    val sessionsCount: Int = 0
)

data class StudyReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val time: String,
    val repeatDays: String = "প্রতিদিন",
    val isEnabled: Boolean = true,
    val type: String = "STUDY" // STUDY, REVISION, EXAM
)

data class StudyHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val durationMinutes: Int = 0,
    val type: String = "FOCUS", // FOCUS, TASK, REVISION
    val timestampFormatted: String,
    val xpEarned: Int = 25,
    val date: String
)

data class UserProfile(
    val name: String = "",
    val studentClass: String = "",
    val group: String = "বিজ্ঞান (Science)",
    val email: String = "",
    val photoUrl: String = "",
    val title: String = "Scholar",
    val xp: Int = 0,
    val streak: Int = 1,
    val dailyGoalMinutes: Int = 120,
    val isDarkMode: Boolean = true,
    val selectedTheme: String = "WARM_SUNSET",
    val language: String = "bn",
    val isCloudSynced: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)
