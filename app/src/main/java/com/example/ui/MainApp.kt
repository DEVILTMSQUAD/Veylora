package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundPlayer
import com.example.auth.AuthManager
import com.example.data.VeyloraRepository
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.ThemePickerDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppScreen(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    SUBJECTS("Subjects", Icons.Default.MenuBook),
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    TASKS("Tasks", Icons.Default.CheckCircleOutline),
    REVISION("Revision", Icons.Default.Psychology),
    POMODORO("Focus", Icons.Default.Timer),
    FLASHCARDS("Cards", Icons.Default.Style),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    REMINDERS("Reminders", Icons.Default.Notifications),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    repository: VeyloraRepository,
    soundPlayer: SoundPlayer,
    authManager: AuthManager
) {
    var isSplashVisible by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var addTaskDatePreset by remember { mutableStateOf("") }
    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showAuthModal by remember { mutableStateOf(false) }
    var showThemePickerModal by remember { mutableStateOf(false) }

    val profile by repository.profile.collectAsState()
    var showOnboarding by remember(profile.isOnboardingCompleted, profile.name) {
        mutableStateOf(!profile.isOnboardingCompleted && profile.name.isBlank())
    }

    // Intercept hardware/system back button
    BackHandler(enabled = !isSplashVisible && !showOnboarding && (currentScreen != AppScreen.HOME || showAuthModal)) {
        if (showAuthModal) {
            showAuthModal = false
        } else {
            currentScreen = AppScreen.HOME
        }
    }

    if (isSplashVisible) {
        SplashScreen(
            onFinished = { isSplashVisible = false }
        )
    } else if (showOnboarding) {
        com.example.ui.screens.OnboardingSetupScreen(
            repository = repository,
            authManager = authManager,
            onComplete = {
                showOnboarding = false
            }
        )
    } else if (showAuthModal) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AuthScreen(
            authManager = authManager,
            onAuthSuccess = { _ ->
                repository.restoreUserDataFromCloud { success ->
                    if (success) {
                        android.widget.Toast.makeText(
                            context,
                            "ক্লাউড থেকে পূর্বের সব পড়ার ডেটা রিস্টোর হয়েছে! ☁️✅",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
                showAuthModal = false
            },
            onSkip = { showAuthModal = false }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("veylora_main_scaffold"),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                VeyloraBottomBar(
                    currentScreen = currentScreen,
                    onSelectScreen = { screen ->
                        currentScreen = screen
                    },
                    onOpenMore = { showMoreMenuSheet = true }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        repository = repository,
                        onNavigateToTasks = { currentScreen = AppScreen.TASKS },
                        onNavigateToCalendar = { currentScreen = AppScreen.CALENDAR },
                        onNavigateToPomodoro = { currentScreen = AppScreen.POMODORO },
                        onNavigateToRevision = { currentScreen = AppScreen.REVISION },
                        onNavigateToReminders = { currentScreen = AppScreen.REMINDERS },
                        onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                        onOpenAddTask = {
                            addTaskDatePreset = ""
                            showAddTaskDialog = true
                        },
                        onToggleDarkDayMode = { repository.toggleDarkMode() },
                        onOpenThemePicker = { showThemePickerModal = true },
                        onOpenAuth = { showAuthModal = true }
                    )
                    AppScreen.SUBJECTS -> SubjectsScreen(repository = repository)
                    AppScreen.CALENDAR -> CalendarScreen(
                        repository = repository,
                        onOpenAddTaskForDate = { dateStr ->
                            addTaskDatePreset = dateStr
                            showAddTaskDialog = true
                        }
                    )
                    AppScreen.TASKS -> DailyTasksScreen(
                        repository = repository,
                        onOpenAddTask = {
                            addTaskDatePreset = ""
                            showAddTaskDialog = true
                        }
                    )
                    AppScreen.REVISION -> RevisionScreen(repository = repository)
                    AppScreen.POMODORO -> PomodoroScreen(
                        repository = repository,
                        soundPlayer = soundPlayer
                    )
                    AppScreen.FLASHCARDS -> FlashcardsScreen(repository = repository)
                    AppScreen.ANALYTICS -> AnalyticsScreen(repository = repository)
                    AppScreen.REMINDERS -> RemindersScreen(repository = repository)
                    AppScreen.HISTORY -> HistoryScreen(repository = repository)
                    AppScreen.SETTINGS -> SettingsScreen(
                        repository = repository,
                        authManager = authManager,
                        onOpenAuth = { showAuthModal = true },
                        onNavigateToReminders = { currentScreen = AppScreen.REMINDERS },
                        onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                        onOpenThemePicker = { showThemePickerModal = true }
                    )
                }

                // Add Task Dialog Modal
                if (showAddTaskDialog) {
                    AddTaskDialog(
                        repository = repository,
                        initialDate = addTaskDatePreset,
                        onDismiss = { showAddTaskDialog = false }
                    )
                }

                // Theme Picker Modal
                if (showThemePickerModal) {
                    ThemePickerDialog(
                        currentThemeId = profile.selectedTheme,
                        onSelectTheme = { themeId ->
                            repository.updateSelectedTheme(themeId.name)
                        },
                        onDismiss = { showThemePickerModal = false }
                    )
                }

                // More Options Bottom Sheet
                if (showMoreMenuSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showMoreMenuSheet = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সব ফিচারসমূহ (All Features)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = {
                                        repository.toggleDarkMode()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (profile.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Grid of features
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MoreMenuGridItem(
                                    title = "Tasks",
                                    icon = Icons.Default.CheckCircle,
                                    color = VeyloraGreen,
                                    onClick = {
                                        currentScreen = AppScreen.TASKS
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Focus Timer",
                                    icon = Icons.Default.Timer,
                                    color = VeyloraCyan,
                                    onClick = {
                                        currentScreen = AppScreen.POMODORO
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Revision",
                                    icon = Icons.Default.Psychology,
                                    color = VeyloraPurple,
                                    onClick = {
                                        currentScreen = AppScreen.REVISION
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Flashcards",
                                    icon = Icons.Default.Style,
                                    color = VeyloraYellow,
                                    onClick = {
                                        currentScreen = AppScreen.FLASHCARDS
                                        showMoreMenuSheet = false
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MoreMenuGridItem(
                                    title = "Analytics",
                                    icon = Icons.Default.BarChart,
                                    color = VeyloraBlue,
                                    onClick = {
                                        currentScreen = AppScreen.ANALYTICS
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Reminders",
                                    icon = Icons.Default.Notifications,
                                    color = VeyloraFlame,
                                    onClick = {
                                        currentScreen = AppScreen.REMINDERS
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "History",
                                    icon = Icons.Default.History,
                                    color = Color(0xFF10B981),
                                    onClick = {
                                        currentScreen = AppScreen.HISTORY
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Themes",
                                    icon = Icons.Default.Palette,
                                    color = Color(0xFFA855F7),
                                    onClick = {
                                        showMoreMenuSheet = false
                                        showThemePickerModal = true
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MoreMenuGridItem(
                                    title = "Settings",
                                    icon = Icons.Default.Settings,
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        currentScreen = AppScreen.SETTINGS
                                        showMoreMenuSheet = false
                                    }
                                )

                                MoreMenuGridItem(
                                    title = "Cloud Sync",
                                    icon = Icons.Default.CloudSync,
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = {
                                        showMoreMenuSheet = false
                                        showAuthModal = true
                                    }
                                )

                                Spacer(modifier = Modifier.width(72.dp))
                                Spacer(modifier = Modifier.width(72.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VeyloraBottomBar(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit,
    onOpenMore: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("veylora_bottom_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val primaryNavScreens = listOf(
                AppScreen.HOME,
                AppScreen.SUBJECTS,
                AppScreen.CALENDAR,
                AppScreen.POMODORO
            )

            primaryNavScreens.forEach { screen ->
                val isSelected = currentScreen == screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScreen(screen) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("nav_item_${screen.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = screen.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More / Menu button
            val isMoreActive = currentScreen !in listOf(
                AppScreen.HOME,
                AppScreen.SUBJECTS,
                AppScreen.CALENDAR,
                AppScreen.POMODORO
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenMore() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("nav_item_more")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isMoreActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "More",
                        tint = if (isMoreActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "More",
                    fontSize = 10.sp,
                    fontWeight = if (isMoreActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isMoreActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoreMenuGridItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(76.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
