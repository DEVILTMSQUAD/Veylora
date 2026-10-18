package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "পড়াশোনার সময় হয়েছে!"
        val message = intent.getStringExtra("EXTRA_MESSAGE")
            ?: "Veylora অ্যাপ খুলুন এবং আজকের নির্ধারিত পাঠ সম্পন্ন করুন।"
        val channel = intent.getStringExtra("EXTRA_CHANNEL") ?: NotificationHelper.CHANNEL_REMINDERS
        val id = intent.getIntExtra("EXTRA_ID", 1001)
        val reminderId = intent.getStringExtra("EXTRA_REMINDER_ID") ?: ""
        val hour24 = intent.getIntExtra("EXTRA_HOUR", -1)
        val minute = intent.getIntExtra("EXTRA_MINUTE", -1)

        NotificationHelper.showNotification(
            context = context,
            channelId = channel,
            notificationId = id,
            title = title,
            message = message
        )

        // Automatically reschedule next day's alarm for recurring reminders
        if (reminderId.isNotBlank() && hour24 in 0..23 && minute in 0..59) {
            NotificationHelper.scheduleStudyAlarm(
                context = context,
                reminderId = reminderId,
                title = title,
                message = message,
                hour24 = hour24,
                minute = minute
            )
        }
    }
}
