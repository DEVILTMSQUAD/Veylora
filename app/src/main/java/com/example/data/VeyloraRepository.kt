package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VeyloraRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("veylora_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault())
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectItem>>(emptyList())
    val subjects: StateFlow<List<SubjectItem>> = _subjects.asStateFlow()

    private val _revisions = MutableStateFlow<List<RevisionItem>>(emptyList())
    val revisions: StateFlow<List<RevisionItem>> = _revisions.asStateFlow()

    private val _flashcards = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val flashcards: StateFlow<List<FlashcardItem>> = _flashcards.asStateFlow()

    private val _exams = MutableStateFlow<List<ExamItem>>(emptyList())
    val exams: StateFlow<List<ExamItem>> = _exams.asStateFlow()

    private val _mockTests = MutableStateFlow<List<MockTestItem>>(emptyList())
    val mockTests: StateFlow<List<MockTestItem>> = _mockTests.asStateFlow()

    private val _mistakes = MutableStateFlow<List<MistakeItem>>(emptyList())
    val mistakes: StateFlow<List<MistakeItem>> = _mistakes.asStateFlow()

    private val _reminders = MutableStateFlow<List<StudyReminderItem>>(emptyList())
    val reminders: StateFlow<List<StudyReminderItem>> = _reminders.asStateFlow()

    private val _history = MutableStateFlow<List<StudyHistoryItem>>(emptyList())
    val history: StateFlow<List<StudyHistoryItem>> = _history.asStateFlow()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _activity = MutableStateFlow<Map<String, ActivityRecord>>(emptyMap())
    val activity: StateFlow<Map<String, ActivityRecord>> = _activity.asStateFlow()

    init {
        loadData()
        syncAuthProfile()
        try {
            com.example.notifications.NotificationHelper.scheduleAllRemindersAndRevisions(
                context,
                _reminders.value,
                _revisions.value
            )
        } catch (_: Exception) {}
    }

    fun syncAuthProfile() {
        try {
            val user = Firebase.auth.currentUser
            if (user != null) {
                val curr = _profile.value
                _profile.value = curr.copy(
                    name = user.displayName?.ifBlank { curr.name } ?: curr.name,
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    isCloudSynced = true
                )
                restoreUserDataFromCloud()
            }
        } catch (_: Exception) {}
    }

    fun todayStr(): String = dateFormat.format(Date())

    fun nowFormatted(): String = dateTimeFormat.format(Date())

    fun addDaysToStr(dateStr: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(dateStr) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, days)
            dateFormat.format(cal.time)
        } catch (_: Exception) {
            todayStr()
        }
    }

    fun daysDiff(date1: String, date2: String): Int {
        return try {
            val d1 = dateFormat.parse(date1) ?: Date()
            val d2 = dateFormat.parse(date2) ?: Date()
            val diffMs = d1.time - d2.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) {
            0
        }
    }

    // --- Background Wall-Clock Persistent Pomodoro Timer ---

    fun savePomodoroRunningState(isRunning: Boolean, targetEndTimeMs: Long, mode: Int, workMinutes: Int, linkedTaskId: String?) {
        prefs.edit()
            .putBoolean("pomo_running", isRunning)
            .putLong("pomo_target_end_ms", targetEndTimeMs)
            .putInt("pomo_mode", mode)
            .putInt("pomo_duration_mins", workMinutes)
            .putString("pomo_linked_task", linkedTaskId ?: "")
            .apply()
    }

    data class PomodoroRestoredState(
        val isRunning: Boolean,
        val secondsLeft: Int,
        val mode: Int,
        val totalSeconds: Int,
        val didFinishWhileAway: Boolean,
        val linkedTaskId: String?
    )

    fun getRestoredPomodoroState(): PomodoroRestoredState {
        val isRunning = prefs.getBoolean("pomo_running", false)
        val targetEndTimeMs = prefs.getLong("pomo_target_end_ms", 0L)
        val mode = prefs.getInt("pomo_mode", 0)
        val durationMins = prefs.getInt("pomo_duration_mins", 25)
        val linkedTask = prefs.getString("pomo_linked_task", null)?.ifBlank { null }
        val totalSeconds = durationMins * 60

        if (!isRunning || targetEndTimeMs <= 0L) {
            return PomodoroRestoredState(
                isRunning = false,
                secondsLeft = totalSeconds,
                mode = mode,
                totalSeconds = totalSeconds,
                didFinishWhileAway = false,
                linkedTaskId = linkedTask
            )
        }

        val now = System.currentTimeMillis()
        val remainingMillis = targetEndTimeMs - now

        return if (remainingMillis <= 0) {
            savePomodoroRunningState(false, 0L, mode, durationMins, null)
            if (mode == 0) {
                recordPomodoroSession(durationMins)
                addHistoryEntry(
                    title = "Focus Session Completed",
                    subject = "General Study",
                    durationMinutes = durationMins,
                    type = "FOCUS",
                    xpEarned = durationMins
                )
            }
            PomodoroRestoredState(
                isRunning = false,
                secondsLeft = totalSeconds,
                mode = mode,
                totalSeconds = totalSeconds,
                didFinishWhileAway = true,
                linkedTaskId = linkedTask
            )
        } else {
            val remainingSecs = (remainingMillis / 1000).toInt()
            PomodoroRestoredState(
                isRunning = true,
                secondsLeft = remainingSecs,
                mode = mode,
                totalSeconds = totalSeconds,
                didFinishWhileAway = false,
                linkedTaskId = linkedTask
            )
        }
    }

    private fun loadData() {
        val json = prefs.getString("data_backup", null)
        if (json.isNullOrBlank()) {
            loadSeedData()
        } else {
            try {
                parseAndApplyJson(json)
            } catch (_: Exception) {
                loadSeedData()
            }
        }
    }

    private fun loadSeedData() {
        val today = todayStr()
        val tomorrow = addDaysToStr(today, 1)

        val seedSubjects = Class910Curriculum.getClass910Subjects()

        val seedTasks = listOf(
            TaskItem(
                title = "Higher Math - Vector (Chapter 5)",
                subject = "Higher Math",
                priority = 2,
                dueDate = today,
                timeSlot = "9:00 AM - 10:30 AM",
                done = false
            ),
            TaskItem(
                title = "Physics - Chapter 11 (Thermodynamics)",
                subject = "Physics",
                priority = 1,
                dueDate = today,
                timeSlot = "11:00 AM - 12:30 PM",
                done = false
            ),
            TaskItem(
                title = "English - Poem (Sonnet Revision)",
                subject = "English",
                priority = 0,
                dueDate = today,
                timeSlot = "2:00 PM - 3:00 PM",
                done = false
            ),
            TaskItem(
                title = "Biology - Cell Structure Notes",
                subject = "Biology",
                priority = 1,
                dueDate = tomorrow,
                timeSlot = "4:00 PM - 5:00 PM",
                done = false
            ),
            TaskItem(
                title = "ICT - HTML Table & Form Design",
                subject = "ICT",
                priority = 0,
                dueDate = tomorrow,
                timeSlot = "6:00 PM - 7:00 PM",
                done = false
            ),
            TaskItem(
                title = "Bangla - Poem Analysis (Complete)",
                subject = "Bangla",
                priority = 1,
                dueDate = today,
                timeSlot = "1:00 PM",
                done = true,
                completedDate = today
            ),
            TaskItem(
                title = "Chemistry - Chapter 8 Reactions",
                subject = "Chemistry",
                priority = 2,
                dueDate = today,
                timeSlot = "10:00 AM",
                done = true,
                completedDate = today
            )
        )

        val seedRevisions = listOf(
            RevisionItem(
                title = "Physics - Thermodynamics Formula",
                subject = "Physics",
                stage = 0,
                dueDate = today
            ),
            RevisionItem(
                title = "Higher Math - Vector Dot & Cross",
                subject = "Higher Math",
                stage = 1,
                dueDate = addDaysToStr(today, 1)
            ),
            RevisionItem(
                title = "Chemistry - Chapter 8 Mineral Resources",
                subject = "Chemistry",
                stage = 2,
                dueDate = addDaysToStr(today, 3)
            ),
            RevisionItem(
                title = "Biology - Cell Division Phases",
                subject = "Biology",
                stage = 3,
                dueDate = addDaysToStr(today, 7)
            ),
            RevisionItem(
                title = "English - Poem Rhetorical Devices",
                subject = "English",
                stage = 4,
                dueDate = addDaysToStr(today, 14)
            )
        )

        val seedFlashcards = listOf(
            FlashcardItem(
                subject = "Physics",
                question = "কার্নোর ইঞ্জিনের দক্ষতার সূত্রটি কী?",
                answer = "η = 1 - (T₂ / T₁)\nঅথবা η = (W / Q₁) × 100%",
                box = 0,
                dueDate = today
            ),
            FlashcardItem(
                subject = "Physics",
                question = "নিউটনের ২য় সূত্রের গাণিতিক রূপ কী?",
                answer = "F⃗ = ma⃗ (বল = ভর × ত্বরণ)",
                box = 1,
                dueDate = today
            ),
            FlashcardItem(
                subject = "Higher Math",
                question = "দুটি ভেক্টর লম্ব হওয়ার শর্ত কী?",
                answer = "তাদের স্কেলার (ডট) গুণন শূন্য হতে হবে:\nA⃗ · B⃗ = 0",
                box = 0,
                dueDate = today
            ),
            FlashcardItem(
                subject = "Chemistry",
                question = "pH < 7 হলে দ্রবণ কেমন হয়?",
                answer = "দ্রবণটি অম্লীয় (Acidic) প্রকৃতির হয়।",
                box = 2,
                dueDate = today
            ),
            FlashcardItem(
                subject = "Biology",
                question = "কোষের শক্তিঘর (Power House) কাকে বলে?",
                answer = "মাইটোকন্ড্রিয়া (Mitochondria)",
                box = 0,
                dueDate = today
            ),
            FlashcardItem(
                subject = "ICT",
                question = "বাইনারি (1011)₂ এর ডেসিমালে মান কত?",
                answer = "11 (1×8 + 0×4 + 1×2 + 1×1 = 11)",
                box = 1,
                dueDate = today
            )
        )

        val seedExams = listOf(
            ExamItem(name = "SSC 2026 Board Final", date = addDaysToStr(today, 128)),
            ExamItem(name = "Physics & Chemistry Model Test", date = addDaysToStr(today, 14)),
            ExamItem(name = "Higher Math Weekly Quiz", date = addDaysToStr(today, 5))
        )

        val seedMockTests = listOf(
            MockTestItem(name = "Weekly Test 1", subject = "Physics", score = 62, total = 100, date = addDaysToStr(today, -14)),
            MockTestItem(name = "Weekly Test 2", subject = "Chemistry", score = 71, total = 100, date = addDaysToStr(today, -7)),
            MockTestItem(name = "Weekly Test 3", subject = "Physics", score = 78, total = 100, date = addDaysToStr(today, -2)),
            MockTestItem(name = "Math Assessment", subject = "Higher Math", score = 85, total = 100, date = addDaysToStr(today, -1))
        )

        val seedMistakes = listOf(
            MistakeItem(topic = "Carnot engine efficiency সূত্রে তাপমাত্রা Kelvin এ নিতে ভুলেছি", subject = "Physics", errorReason = "Celsius থেকে Kelvin রূপান্তর না করে T1, T2 বসানো", fixed = false),
            MistakeItem(topic = "ম্যাট্রিক্স গুণনে কলাম সংখ্যা ও সারি সংখ্যার ক্রম মেলেনি", subject = "Higher Math", errorReason = "A(m×n) এবং B(n×p) ক্রম যাচাই না করা", fixed = true)
        )

        val seedReminders = listOf(
            StudyReminderItem(title = "সকালের রিভিশন (Morning Focus)", time = "08:00 AM", repeatDays = "প্রতিদিন", isEnabled = true, type = "STUDY"),
            StudyReminderItem(title = "বিকেলের প্র্যাকটিস ও পড়া (Afternoon Session)", time = "04:30 PM", repeatDays = "প্রতিদিন", isEnabled = true, type = "STUDY"),
            StudyReminderItem(title = "রাতের রিভিশন ও পড়া সমাপনী (Night Wrap-up)", time = "09:30 PM", repeatDays = "প্রতিদিন", isEnabled = true, type = "REVISION")
        )

        // Clean, authentic initial state (no dummy records)
        _subjects.value = seedSubjects
        _tasks.value = emptyList()
        _revisions.value = emptyList()
        _flashcards.value = emptyList()
        _exams.value = emptyList()
        _mockTests.value = emptyList()
        _mistakes.value = emptyList()
        _reminders.value = emptyList()
        _history.value = emptyList()
        _activity.value = emptyMap()
        _profile.value = UserProfile(
            name = "",
            studentClass = "",
            title = "Scholar",
            xp = 0,
            streak = 1,
            dailyGoalMinutes = 120,
            isDarkMode = true,
            selectedTheme = "WARM_SUNSET",
            language = "bn",
            isOnboardingCompleted = false
        )

        saveData()
    }

    fun saveData() {
        try {
            val json = exportDataJson()
            prefs.edit().putString("data_backup", json).apply()
            pushToCloud()
        } catch (_: Exception) {}
    }

    fun pushToCloud(onComplete: ((Boolean) -> Unit)? = null) {
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (user == null) {
            onComplete?.invoke(false)
            return
        }
        repositoryScope.launch {
            try {
                val prof = _profile.value
                val fullJson = exportDataJson()
                val userDoc = mapOf(
                    "uid" to user.uid,
                    "displayName" to prof.name,
                    "email" to (user.email ?: prof.email),
                    "photoUrl" to prof.photoUrl,
                    "studentClass" to prof.studentClass,
                    "group" to prof.group,
                    "xp" to prof.xp,
                    "streak" to prof.streak,
                    "dailyGoalMinutes" to prof.dailyGoalMinutes,
                    "isDarkMode" to prof.isDarkMode,
                    "selectedTheme" to prof.selectedTheme,
                    "language" to prof.language,
                    "dataJson" to fullJson,
                    "tasksCount" to _tasks.value.size,
                    "subjectsCount" to _subjects.value.size,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(user.uid)
                    .set(userDoc, SetOptions.merge()).await()
                withContext(Dispatchers.Main) {
                    _profile.value = _profile.value.copy(isCloudSynced = true)
                }
                onComplete?.invoke(true)
            } catch (_: Exception) {
                onComplete?.invoke(false)
            }
        }
    }

    /**
     * Restores all study data, tasks, completed chapters, notes, reminders,
     * and revisions from Firestore when a previous user signs in.
     */
    fun restoreUserDataFromCloud(onComplete: ((Boolean) -> Unit)? = null) {
        val user = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        if (user == null) {
            repositoryScope.launch(Dispatchers.Main) {
                onComplete?.invoke(false)
            }
            return
        }

        repositoryScope.launch {
            try {
                val doc = firestore.collection("users").document(user.uid).get().await()
                if (doc.exists()) {
                    val cloudJson = doc.getString("dataJson")
                    if (!cloudJson.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            try {
                                parseAndApplyJson(cloudJson)
                                prefs.edit().putString("data_backup", cloudJson).apply()

                                val curr = _profile.value
                                val finalName = if (curr.name.isNotBlank()) curr.name else (user.displayName ?: "")
                                _profile.value = curr.copy(
                                    name = finalName,
                                    email = user.email ?: curr.email,
                                    isCloudSynced = true,
                                    isOnboardingCompleted = true
                                )
                                prefs.edit().putString("data_backup", exportDataJson()).apply()

                                try {
                                    com.example.notifications.NotificationHelper.scheduleAllRemindersAndRevisions(
                                        context,
                                        _reminders.value,
                                        _revisions.value
                                    )
                                } catch (_: Exception) {}
                            } catch (parseErr: Exception) {
                                android.util.Log.e("VeyloraRepository", "Error parsing cloud data: ${parseErr.message}")
                            }
                            onComplete?.invoke(true)
                        }
                        return@launch
                    }
                }

                // If document does not exist yet in cloud, upload current data as baseline
                withContext(Dispatchers.Main) {
                    try {
                        val curr = _profile.value
                        _profile.value = curr.copy(
                            name = if (curr.name.isNotBlank()) curr.name else (user.displayName ?: ""),
                            email = user.email ?: curr.email,
                            isCloudSynced = true
                        )
                        pushToCloud()
                    } catch (_: Exception) {}
                    onComplete?.invoke(true)
                }
            } catch (e: Exception) {
                android.util.Log.e("VeyloraRepository", "Cloud restore failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(false)
                }
            }
        }
    }

    fun exportDataJson(): String {
        val root = JSONObject()
        val prof = _profile.value
        val profJson = JSONObject().apply {
            put("name", prof.name)
            put("studentClass", prof.studentClass)
            put("group", prof.group)
            put("email", prof.email)
            put("photoUrl", prof.photoUrl)
            put("title", prof.title)
            put("xp", prof.xp)
            put("streak", prof.streak)
            put("dailyGoalMinutes", prof.dailyGoalMinutes)
            put("isDarkMode", prof.isDarkMode)
            put("selectedTheme", prof.selectedTheme)
            put("language", prof.language)
            put("isOnboardingCompleted", prof.isOnboardingCompleted)
        }
        root.put("profile", profJson)

        val tasksArr = JSONArray()
        _tasks.value.forEach { t ->
            tasksArr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("subject", t.subject)
                put("priority", t.priority)
                put("dueDate", t.dueDate)
                put("timeSlot", t.timeSlot)
                put("done", t.done)
                put("completedDate", t.completedDate)
            })
        }
        root.put("tasks", tasksArr)

        val subjArr = JSONArray()
        _subjects.value.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("icon", s.icon)
                put("colorHex", s.colorHex)
                val chArr = JSONArray()
                s.chapters.forEach { c ->
                    chArr.put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("done", c.done)
                        put("notes", c.notes)
                    })
                }
                put("chapters", chArr)
            }
            subjArr.put(sObj)
        }
        root.put("subjects", subjArr)

        val remArr = JSONArray()
        _reminders.value.forEach { r ->
            remArr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("time", r.time)
                put("repeatDays", r.repeatDays)
                put("isEnabled", r.isEnabled)
                put("type", r.type)
            })
        }
        root.put("reminders", remArr)

        val histArr = JSONArray()
        _history.value.forEach { h ->
            histArr.put(JSONObject().apply {
                put("id", h.id)
                put("title", h.title)
                put("subject", h.subject)
                put("durationMinutes", h.durationMinutes)
                put("type", h.type)
                put("timestampFormatted", h.timestampFormatted)
                put("xpEarned", h.xpEarned)
                put("date", h.date)
            })
        }
        root.put("history", histArr)

        val revArr = JSONArray()
        _revisions.value.forEach { r ->
            revArr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("subject", r.subject)
                put("stage", r.stage)
                put("dueDate", r.dueDate)
                put("chapterKey", r.chapterKey)
            })
        }
        root.put("revisions", revArr)

        val flashArr = JSONArray()
        _flashcards.value.forEach { f ->
            flashArr.put(JSONObject().apply {
                put("id", f.id)
                put("subject", f.subject)
                put("question", f.question)
                put("answer", f.answer)
                put("box", f.box)
                put("dueDate", f.dueDate)
            })
        }
        root.put("flashcards", flashArr)

        val examArr = JSONArray()
        _exams.value.forEach { e ->
            examArr.put(JSONObject().apply {
                put("id", e.id)
                put("name", e.name)
                put("date", e.date)
            })
        }
        root.put("exams", examArr)

        val testArr = JSONArray()
        _mockTests.value.forEach { mt ->
            testArr.put(JSONObject().apply {
                put("id", mt.id)
                put("name", mt.name)
                put("subject", mt.subject)
                put("score", mt.score)
                put("total", mt.total)
                put("date", mt.date)
            })
        }
        root.put("mockTests", testArr)

        val mistArr = JSONArray()
        _mistakes.value.forEach { m ->
            mistArr.put(JSONObject().apply {
                put("id", m.id)
                put("topic", m.topic)
                put("subject", m.subject)
                put("errorReason", m.errorReason)
                put("fixed", m.fixed)
            })
        }
        root.put("mistakes", mistArr)

        val actObj = JSONObject()
        _activity.value.forEach { (date, act) ->
            actObj.put(date, JSONObject().apply {
                put("minutes", act.minutes)
                put("tasksCount", act.tasksCount)
                put("sessionsCount", act.sessionsCount)
            })
        }
        root.put("activity", actObj)

        return root.toString(2)
    }

    fun importDataJson(jsonStr: String): Boolean {
        return try {
            parseAndApplyJson(jsonStr)
            saveData()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun parseAndApplyJson(jsonStr: String) {
        val root = JSONObject(jsonStr)
        if (root.has("profile")) {
            val p = root.getJSONObject("profile")
            val rawName = p.optString("name", "")
            val finalName = if (rawName == "Tamim Islam") "" else rawName
            val isOnboarded = if (rawName == "Tamim Islam") false else p.optBoolean("isOnboardingCompleted", finalName.isNotBlank())
            _profile.value = UserProfile(
                name = finalName,
                studentClass = p.optString("studentClass", ""),
                group = p.optString("group", Class910Curriculum.GROUP_SCIENCE),
                email = p.optString("email", ""),
                photoUrl = p.optString("photoUrl", ""),
                title = p.optString("title", "Scholar"),
                xp = p.optInt("xp", 0),
                streak = p.optInt("streak", 1),
                dailyGoalMinutes = p.optInt("dailyGoalMinutes", 120),
                isDarkMode = p.optBoolean("isDarkMode", true),
                selectedTheme = p.optString("selectedTheme", "WARM_SUNSET"),
                language = p.optString("language", "bn"),
                isCloudSynced = try { Firebase.auth.currentUser != null } catch (_: Exception) { false },
                isOnboardingCompleted = isOnboarded
            )
        }

        if (root.has("tasks")) {
            val arr = root.getJSONArray("tasks")
            val list = mutableListOf<TaskItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    TaskItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        subject = o.optString("subject", ""),
                        priority = o.optInt("priority", 1),
                        dueDate = o.optString("dueDate", todayStr()),
                        timeSlot = o.optString("timeSlot", ""),
                        done = o.optBoolean("done", false),
                        completedDate = o.optString("completedDate", "")
                    )
                )
            }
            _tasks.value = list
        }

        if (root.has("subjects")) {
            val arr = root.getJSONArray("subjects")
            val list = mutableListOf<SubjectItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val chArr = o.optJSONArray("chapters") ?: JSONArray()
                val chList = mutableListOf<ChapterItem>()
                for (j in 0 until chArr.length()) {
                    val c = chArr.getJSONObject(j)
                    chList.add(
                        ChapterItem(
                            id = c.optString("id", UUID.randomUUID().toString()),
                            name = c.optString("name", ""),
                            done = c.optBoolean("done", false),
                            notes = c.optString("notes", "")
                        )
                    )
                }
                list.add(
                    SubjectItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", ""),
                        icon = o.optString("icon", "📚"),
                        chapters = chList,
                        colorHex = o.optString("colorHex", "#3B82F6")
                    )
                )
            }
            _subjects.value = list
        }

        if (root.has("reminders")) {
            val arr = root.getJSONArray("reminders")
            val list = mutableListOf<StudyReminderItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    StudyReminderItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        time = o.optString("time", "08:00 AM"),
                        repeatDays = o.optString("repeatDays", "প্রতিদিন"),
                        isEnabled = o.optBoolean("isEnabled", true),
                        type = o.optString("type", "STUDY")
                    )
                )
            }
            _reminders.value = list
        }

        if (root.has("history")) {
            val arr = root.getJSONArray("history")
            val list = mutableListOf<StudyHistoryItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    StudyHistoryItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        subject = o.optString("subject", ""),
                        durationMinutes = o.optInt("durationMinutes", 25),
                        type = o.optString("type", "FOCUS"),
                        timestampFormatted = o.optString("timestampFormatted", ""),
                        xpEarned = o.optInt("xpEarned", 25),
                        date = o.optString("date", todayStr())
                    )
                )
            }
            _history.value = list
        }

        if (root.has("revisions")) {
            val arr = root.getJSONArray("revisions")
            val list = mutableListOf<RevisionItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    RevisionItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", ""),
                        subject = o.optString("subject", ""),
                        stage = o.optInt("stage", 0),
                        dueDate = o.optString("dueDate", todayStr()),
                        chapterKey = o.optString("chapterKey", "")
                    )
                )
            }
            _revisions.value = list
        }

        if (root.has("flashcards")) {
            val arr = root.getJSONArray("flashcards")
            val list = mutableListOf<FlashcardItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    FlashcardItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        subject = o.optString("subject", ""),
                        question = o.optString("question", ""),
                        answer = o.optString("answer", ""),
                        box = o.optInt("box", 0),
                        dueDate = o.optString("dueDate", todayStr())
                    )
                )
            }
            _flashcards.value = list
        }

        if (root.has("exams")) {
            val arr = root.getJSONArray("exams")
            val list = mutableListOf<ExamItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    ExamItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", ""),
                        date = o.optString("date", todayStr())
                    )
                )
            }
            _exams.value = list
        }

        if (root.has("mockTests")) {
            val arr = root.getJSONArray("mockTests")
            val list = mutableListOf<MockTestItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    MockTestItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", ""),
                        subject = o.optString("subject", ""),
                        score = o.optInt("score", 0),
                        total = o.optInt("total", 100),
                        date = o.optString("date", todayStr())
                    )
                )
            }
            _mockTests.value = list
        }

        if (root.has("mistakes")) {
            val arr = root.getJSONArray("mistakes")
            val list = mutableListOf<MistakeItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    MistakeItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        topic = o.optString("topic", ""),
                        subject = o.optString("subject", ""),
                        errorReason = o.optString("errorReason", ""),
                        fixed = o.optBoolean("fixed", false)
                    )
                )
            }
            _mistakes.value = list
        }

        if (root.has("activity")) {
            val obj = root.getJSONObject("activity")
            val map = mutableMapOf<String, ActivityRecord>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val a = obj.getJSONObject(k)
                map[k] = ActivityRecord(
                    minutes = a.optInt("minutes", 0),
                    tasksCount = a.optInt("tasksCount", 0),
                    sessionsCount = a.optInt("sessionsCount", 0)
                )
            }
            _activity.value = map
        }
    }

    fun resetToDemo() {
        prefs.edit().clear().apply()
        loadSeedData()
    }

    // --- State mutation methods ---

    fun addXP(points: Int) {
        val curr = _profile.value
        val newXp = (curr.xp + points).coerceAtLeast(0)
        val levelNum = (newXp / 250) + 1
        val levelTitle = "Level $levelNum Scholar"
        _profile.value = curr.copy(xp = newXp, title = levelTitle)
        saveData()
    }

    fun toggleTask(taskId: String): Boolean {
        val today = todayStr()
        var markedDone = false
        var completedTaskTitle = ""
        var completedTaskSub = ""

        val updated = _tasks.value.map { task ->
            if (task.id == taskId) {
                val nextState = !task.done
                markedDone = nextState
                completedTaskTitle = task.title
                completedTaskSub = task.subject
                task.copy(
                    done = nextState,
                    completedDate = if (nextState) today else ""
                )
            } else task
        }
        _tasks.value = updated

        val curAct = _activity.value[today] ?: ActivityRecord()
        if (markedDone) {
            addXP(20)
            _activity.value = _activity.value + (today to curAct.copy(tasksCount = curAct.tasksCount + 1))
            addHistoryEntry(
                title = "Task সম্পন্ন: $completedTaskTitle",
                subject = completedTaskSub,
                durationMinutes = 0,
                type = "TASK",
                xpEarned = 20
            )
        } else {
            addXP(-20)
            _activity.value = _activity.value + (today to curAct.copy(tasksCount = (curAct.tasksCount - 1).coerceAtLeast(0)))
        }
        saveData()
        return markedDone
    }

    fun addTask(title: String, subject: String, priority: Int, dueDate: String, timeSlot: String) {
        val item = TaskItem(
            title = title,
            subject = subject,
            priority = priority,
            dueDate = dueDate.ifBlank { todayStr() },
            timeSlot = timeSlot,
            done = false
        )
        _tasks.value = listOf(item) + _tasks.value
        saveData()
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        saveData()
    }

    fun toggleChapter(subjectId: String, chapterId: String): Boolean {
        var isNowDone = false
        var subName = ""
        var chName = ""

        val updatedSubjects = _subjects.value.map { sub ->
            if (sub.id == subjectId) {
                subName = sub.name
                val updatedChapters = sub.chapters.map { ch ->
                    if (ch.id == chapterId) {
                        isNowDone = !ch.done
                        chName = ch.name
                        ch.copy(done = isNowDone)
                    } else ch
                }
                sub.copy(chapters = updatedChapters)
            } else sub
        }
        _subjects.value = updatedSubjects

        if (isNowDone) {
            addXP(25)
            val rev = RevisionItem(
                title = "$subName — $chName",
                subject = subName,
                stage = 0,
                dueDate = addDaysToStr(todayStr(), 1),
                chapterKey = chapterId
            )
            _revisions.value = listOf(rev) + _revisions.value.filter { it.chapterKey != chapterId }
            addHistoryEntry(
                title = "অধ্যায় সম্পন্ন: $subName — $chName",
                subject = subName,
                durationMinutes = 0,
                type = "REVISION",
                xpEarned = 25
            )
        } else {
            addXP(-25)
            _revisions.value = _revisions.value.filter { it.chapterKey != chapterId }
        }
        saveData()
        return isNowDone
    }

    fun addSubject(name: String, icon: String, colorHex: String) {
        val newSub = SubjectItem(
            name = name,
            icon = icon.ifBlank { "📚" },
            colorHex = colorHex.ifBlank { "#3B82F6" },
            chapters = emptyList()
        )
        _subjects.value = _subjects.value + newSub
        saveData()
    }

    fun deleteSubject(subjectId: String) {
        _subjects.value = _subjects.value.filter { it.id != subjectId }
        saveData()
    }

    fun addChapter(subjectId: String, chapterName: String) {
        val updated = _subjects.value.map { sub ->
            if (sub.id == subjectId) {
                val newCh = ChapterItem(name = chapterName, done = false)
                sub.copy(chapters = sub.chapters + newCh)
            } else sub
        }
        _subjects.value = updated
        saveData()
    }

    fun loadClass910Preset(groupName: String? = null) {
        val targetGroup = groupName ?: _profile.value.group
        val preset = Class910Curriculum.getSubjectsForGroup(targetGroup)
        _subjects.value = preset
        if (groupName != null) {
            _profile.value = _profile.value.copy(group = groupName)
        }
        saveData()
    }

    fun loadAllClass910Subjects() {
        _subjects.value = Class910Curriculum.getClass910Subjects()
        saveData()
    }

    fun loadCurriculumForGroup(groupName: String) {
        loadClass910Preset(groupName)
    }

    fun updateChapterNote(subjectId: String, chapterId: String, notes: String) {
        val updated = _subjects.value.map { sub ->
            if (sub.id == subjectId) {
                val updatedCh = sub.chapters.map { ch ->
                    if (ch.id == chapterId) ch.copy(notes = notes) else ch
                }
                sub.copy(chapters = updatedCh)
            } else sub
        }
        _subjects.value = updated
        saveData()
    }

    val intervals = intArrayOf(1, 3, 7, 14, 30)

    fun reviewRevision(revisionId: String, remembered: Boolean) {
        val today = todayStr()
        val list = _revisions.value.toMutableList()
        val index = list.indexOfFirst { it.id == revisionId }
        if (index != -1) {
            val item = list[index]
            if (remembered) {
                addXP(15)
                val nextStage = item.stage + 1
                if (nextStage >= intervals.size) {
                    addXP(30)
                    list.removeAt(index)
                    addHistoryEntry(
                        title = "Mastered: ${item.title}",
                        subject = item.subject,
                        durationMinutes = 0,
                        type = "REVISION",
                        xpEarned = 30
                    )
                } else {
                    val nextDue = addDaysToStr(today, intervals[nextStage])
                    list[index] = item.copy(stage = nextStage, dueDate = nextDue)
                }
            } else {
                addXP(2)
                list[index] = item.copy(stage = 0, dueDate = addDaysToStr(today, 1))
            }
            _revisions.value = list
            saveData()
        }
    }

    fun answerFlashcard(flashcardId: String, remembered: Boolean) {
        val today = todayStr()
        val leitnerIntervals = intArrayOf(1, 2, 4, 8, 16)
        val list = _flashcards.value.toMutableList()
        val idx = list.indexOfFirst { it.id == flashcardId }
        if (idx != -1) {
            val item = list[idx]
            val nextBox = if (remembered) (item.box + 1).coerceAtMost(4) else 0
            val nextDue = addDaysToStr(today, leitnerIntervals[nextBox])
            list[idx] = item.copy(box = nextBox, dueDate = nextDue)
            _flashcards.value = list
            addXP(if (remembered) 15 else 2)
            saveData()
        }
    }

    fun addFlashcard(subject: String, question: String, answer: String) {
        val item = FlashcardItem(
            subject = subject,
            question = question,
            answer = answer,
            box = 0,
            dueDate = todayStr()
        )
        _flashcards.value = listOf(item) + _flashcards.value
        saveData()
    }

    fun deleteFlashcard(id: String) {
        _flashcards.value = _flashcards.value.filter { it.id != id }
        saveData()
    }

    fun addExam(name: String, date: String) {
        val item = ExamItem(name = name, date = date)
        _exams.value = listOf(item) + _exams.value
        saveData()
    }

    fun deleteExam(id: String) {
        _exams.value = _exams.value.filter { it.id != id }
        saveData()
    }

    fun addMockTest(name: String, subject: String, score: Int, total: Int) {
        val item = MockTestItem(
            name = name,
            subject = subject,
            score = score,
            total = total,
            date = todayStr()
        )
        _mockTests.value = listOf(item) + _mockTests.value
        addXP(15)
        addHistoryEntry(
            title = "Mock Test: $name ($score/$total)",
            subject = subject,
            durationMinutes = 0,
            type = "TASK",
            xpEarned = 15
        )
        saveData()
    }

    fun deleteMockTest(id: String) {
        _mockTests.value = _mockTests.value.filter { it.id != id }
        saveData()
    }

    fun addMistake(topic: String, subject: String, reason: String) {
        val item = MistakeItem(
            topic = topic,
            subject = subject,
            errorReason = reason,
            fixed = false
        )
        _mistakes.value = listOf(item) + _mistakes.value
        saveData()
    }

    fun toggleMistakeFixed(id: String) {
        _mistakes.value = _mistakes.value.map { m ->
            if (m.id == id) {
                val next = !m.fixed
                if (next) addXP(10)
                m.copy(fixed = next)
            } else m
        }
        saveData()
    }

    fun deleteMistake(id: String) {
        _mistakes.value = _mistakes.value.filter { it.id != id }
        saveData()
    }

    fun recordPomodoroSession(minutes: Int) {
        val today = todayStr()
        val curAct = _activity.value[today] ?: ActivityRecord()
        _activity.value = _activity.value + (today to curAct.copy(
            minutes = curAct.minutes + minutes,
            sessionsCount = curAct.sessionsCount + 1
        ))
        addXP(minutes)
        saveData()
    }

    // Reminders Management
    fun toggleReminder(id: String): Boolean {
        var isNowEnabled = false
        _reminders.value = _reminders.value.map { r ->
            if (r.id == id) {
                isNowEnabled = !r.isEnabled
                r.copy(isEnabled = isNowEnabled)
            } else r
        }
        saveData()
        return isNowEnabled
    }

    fun addReminder(title: String, time: String, repeatDays: String, type: String) {
        val item = StudyReminderItem(
            title = title,
            time = time,
            repeatDays = repeatDays,
            isEnabled = true,
            type = type
        )
        _reminders.value = _reminders.value + item
        saveData()
    }

    fun deleteReminder(id: String) {
        _reminders.value = _reminders.value.filter { it.id != id }
        saveData()
    }

    // History Tracking
    fun addHistoryEntry(title: String, subject: String, durationMinutes: Int, type: String, xpEarned: Int) {
        val item = StudyHistoryItem(
            title = title,
            subject = subject,
            durationMinutes = durationMinutes,
            type = type,
            timestampFormatted = nowFormatted(),
            xpEarned = xpEarned,
            date = todayStr()
        )
        _history.value = listOf(item) + _history.value
        saveData()
    }

    fun updateDailyGoal(goalMinutes: Int) {
        _profile.value = _profile.value.copy(dailyGoalMinutes = goalMinutes)
        saveData()
    }

    fun updateUserProfile(
        name: String,
        studentClass: String,
        group: String = Class910Curriculum.GROUP_SCIENCE,
        dailyGoalMinutes: Int
    ) {
        val curr = _profile.value
        val derivedTitle = if (studentClass.isNotBlank()) "$studentClass Scholar" else "Scholar"
        _profile.value = curr.copy(
            name = name,
            studentClass = studentClass,
            group = group,
            title = derivedTitle,
            dailyGoalMinutes = dailyGoalMinutes,
            isOnboardingCompleted = true
        )
        // Automatically set the subject curriculum to match chosen group
        val groupSubjects = Class910Curriculum.getSubjectsForGroup(group)
        if (groupSubjects.isNotEmpty()) {
            _subjects.value = groupSubjects
        }
        saveData()
    }

    fun updateGroup(group: String) {
        _profile.value = _profile.value.copy(group = group)
        saveData()
    }

    fun updateProfile(name: String, title: String) {
        _profile.value = _profile.value.copy(name = name, title = title)
        saveData()
    }

    fun toggleDarkMode() {
        val nextMode = !_profile.value.isDarkMode
        val nextTheme = if (nextMode) "WARM_SUNSET" else "WARM_CHALK"
        _profile.value = _profile.value.copy(
            isDarkMode = nextMode,
            selectedTheme = nextTheme
        )
        saveData()
    }

    fun updateSelectedTheme(themeId: String) {
        val isDark = when (themeId) {
            "NORDIC_FROST", "WARM_CHALK" -> false
            else -> true
        }
        _profile.value = _profile.value.copy(
            selectedTheme = themeId,
            isDarkMode = isDark
        )
        saveData()
    }

    fun setLanguage(lang: String) {
        _profile.value = _profile.value.copy(language = lang)
        saveData()
    }
}
