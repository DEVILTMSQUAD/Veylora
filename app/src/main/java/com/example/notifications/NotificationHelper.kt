package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.RevisionItem
import com.example.data.model.StudyReminderItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_REMINDERS = "veylora_study_reminders"
    const val CHANNEL_MOTIVATION = "veylora_daily_motivation"
    const val CHANNEL_CALENDAR = "veylora_calendar_alerts"

    val motivationalQuotes = listOf(
        "\"সাফল্যের কোনো গোপন সূত্র নেই, এটা হলো প্রস্তুতি, কঠোর পরিশ্রম এবং ব্যর্থতা থেকে শেখার ফলাফল।\"",
        "\"আজকের এক ঘণ্টার গভীর পড়াশোনা তোমাকে আগামীকালের সাফল্যের এক ধাপ এগিয়ে নিয়ে যাবে।\"",
        "\"যে স্বপ্ন দেখে এবং সে অনুযায়ী কাজ করে, সফলতা তার কাছে আসতে বাধ্য। চলো শুরু করি!\"",
        "\"ছোট ছোট দৈনিক অগ্রগতি সময়ের সাথে সাথে বিশাল ফলাফলে রূপান্তরিত হয়। Keep going!\"",
        "\"কঠিন অধ্যায়গুলোই তোমার মেধা শাণিত করে। ফোকাস রাখো এবং এগিয়ে চলো!\"",
        "\"পড়াশোনাকে বোঝা নয়, নিজের ভবিষ্যতের সেরা ইনভেস্টমেন্ট হিসেবে গ্রহণ করো।\"",
        "\"তোমার লক্ষ্য যদি স্পষ্ট থাকে, কোনো বাধাই তোমাকে থামাতে পারবে না।\""
    )

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val studyChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Study Reminders (পড়াশোনার রিমাইন্ডার)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ডেইলি স্টাডি রুটিন ও পড়ার সময়সূচির অ্যালার্ট"
                enableVibration(true)
            }

            val motivationChannel = NotificationChannel(
                CHANNEL_MOTIVATION,
                "Daily Motivation (দৈনিক অনুপ্রেরণা)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "প্রতিদিনের শিক্ষণীয় উক্তি ও অনুপ্রেরণামূলক নোটিফিকেশন"
            }

            val calendarChannel = NotificationChannel(
                CHANNEL_CALENDAR,
                "Exam & Calendar Alerts (পরীক্ষা ও রিভিশন)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "আসন্ন পরীক্ষা ও সময়সূচির আগাম নোটিফিকেশন"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(studyChannel, motivationChannel, calendarChannel)
            )
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_veylora_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun triggerDailyMotivation(context: Context) {
        val quote = motivationalQuotes.random()
        showNotification(
            context = context,
            channelId = CHANNEL_MOTIVATION,
            notificationId = 8888,
            title = "✨ আজকের স্টাডি মোটিভেশন",
            message = quote
        )
    }

    fun parseTimeString(timeStr: String): Pair<Int, Int> {
        val trimmed = timeStr.trim()
        return try {
            if (trimmed.contains("AM", ignoreCase = true) || trimmed.contains("PM", ignoreCase = true)) {
                val df = SimpleDateFormat("hh:mm a", Locale.US).apply { isLenient = false }
                val date = df.parse(trimmed)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                } else Pair(20, 0)
            } else {
                val parts = trimmed.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].trim().toIntOrNull() ?: 20
                    val m = parts[1].trim().take(2).toIntOrNull() ?: 0
                    Pair(h, m)
                } else Pair(20, 0)
            }
        } catch (_: Exception) {
            try {
                val parts = trimmed.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].trim().take(2).toIntOrNull() ?: 20
                    val m = parts[1].trim().take(2).toIntOrNull() ?: 0
                    Pair(h, m)
                } else Pair(20, 0)
            } catch (_: Exception) {
                Pair(20, 0)
            }
        }
    }

    /**
     * Schedules a real system alarm using AlarmManager to wake up and notify the user on time.
     */
    fun scheduleStudyAlarm(
        context: Context,
        reminderId: String,
        title: String,
        message: String,
        hour24: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
            putExtra("EXTRA_CHANNEL", CHANNEL_REMINDERS)
            putExtra("EXTRA_ID", reminderId.hashCode())
            putExtra("EXTRA_REMINDER_ID", reminderId)
            putExtra("EXTRA_HOUR", hour24)
            putExtra("EXTRA_MINUTE", minute)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If time already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback for devices restricting exact alarms
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelStudyAlarm(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Automatic Reminder Engine:
     * Automatically registers alarms for all enabled study reminders and spaced repetition revisions.
     * Also checks for due revisions on launch and notifies the student immediately if due.
     */
    fun scheduleAllRemindersAndRevisions(
        context: Context,
        reminders: List<StudyReminderItem>,
        revisions: List<RevisionItem>
    ) {
        // 1. Schedule all enabled study reminders
        reminders.filter { it.isEnabled }.forEach { reminder ->
            val (hour, minute) = parseTimeString(reminder.time)
            scheduleStudyAlarm(
                context = context,
                reminderId = reminder.id,
                title = "🔔 ${reminder.title}",
                message = "পড়ার নির্ধারিত সময় (${reminder.time}) হয়েছে। Veylora খুলে আজকের পাঠ শেষ করুন।",
                hour24 = hour,
                minute = minute
            )
        }

        // 2. Schedule automatic daily revision reminder every day at 8:30 PM (20:30)
        scheduleStudyAlarm(
            context = context,
            reminderId = "auto_revision_daily",
            title = "📖 আজকের রিভিশন সময়!",
            message = "আপনার রিভিশন তালিকায় পড়া বাকি রয়েছে। স্পেসড রিপিটেশন রিভিশন সম্পন্ন করুন।",
            hour24 = 20,
            minute = 30
        )

        // 3. Auto-check on launch: if revisions are due today, trigger automatic alert
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val dueRevisions = revisions.filter { it.dueDate <= today }
        if (dueRevisions.isNotEmpty()) {
            val prefs = context.getSharedPreferences("veylora_notifs", Context.MODE_PRIVATE)
            val lastNotifiedDate = prefs.getString("last_revision_notif_date", "")
            if (lastNotifiedDate != today) {
                prefs.edit().putString("last_revision_notif_date", today).apply()
                showNotification(
                    context = context,
                    channelId = CHANNEL_CALENDAR,
                    notificationId = 5555,
                    title = "🎯 আজকের রিভিশন বাকি!",
                    message = "আজকে আপনার ${dueRevisions.size}টি বিষয়ের রিভিশন দেওয়ার সময়। Veylora খুলে রিভিশন সম্পন্ন করুন।"
                )
            }
        }
    }
}
