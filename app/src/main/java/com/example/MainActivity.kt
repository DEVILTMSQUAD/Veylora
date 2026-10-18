package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.audio.SoundPlayer
import com.example.auth.AuthManager
import com.example.data.VeyloraRepository
import com.example.ui.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VeyloraThemeId

class MainActivity : ComponentActivity() {
    private lateinit var repository: VeyloraRepository
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.notifications.NotificationHelper.createNotificationChannels(applicationContext)

        repository = VeyloraRepository(applicationContext)
        soundPlayer = SoundPlayer()
        authManager = AuthManager(applicationContext)

        com.example.notifications.NotificationHelper.scheduleAllRemindersAndRevisions(
            applicationContext,
            repository.reminders.value,
            repository.revisions.value
        )

        setContent {
            val profile by repository.profile.collectAsState()

            val selectedTheme = remember(profile.selectedTheme, profile.isDarkMode) {
                if (!profile.isDarkMode) {
                    try {
                        val t = VeyloraThemeId.valueOf(profile.selectedTheme)
                        if (!t.isDark) t else VeyloraThemeId.WARM_CHALK
                    } catch (_: Exception) {
                        VeyloraThemeId.WARM_CHALK
                    }
                } else {
                    try {
                        val t = VeyloraThemeId.valueOf(profile.selectedTheme)
                        if (t.isDark) t else VeyloraThemeId.WARM_SUNSET
                    } catch (_: Exception) {
                        VeyloraThemeId.WARM_SUNSET
                    }
                }
            }

            MyApplicationTheme(themeId = selectedTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainApp(
                        repository = repository,
                        soundPlayer = soundPlayer,
                        authManager = authManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundPlayer.isInitialized) {
            soundPlayer.stopAmbient()
        }
    }
}
