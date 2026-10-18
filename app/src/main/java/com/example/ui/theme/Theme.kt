package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class VeyloraThemeId(
    val title: String,
    val isDark: Boolean,
    val previewPrimary: Color,
    val previewBg: Color
) {
    WARM_SUNSET(
        title = "Warm Sunset (ডিফল্ট)",
        isDark = true,
        previewPrimary = Color(0xFFF59E0B),
        previewBg = Color(0xFF161210)
    ),
    WARM_CHALK(
        title = "Warm Amber Day",
        isDark = false,
        previewPrimary = Color(0xFFD97706),
        previewBg = Color(0xFFFAF6F0)
    ),
    COSMIC_MIDNIGHT(
        title = "Cosmic Midnight",
        isDark = true,
        previewPrimary = Color(0xFF00D2FF),
        previewBg = Color(0xFF090D1A)
    ),
    OLED_BLACK(
        title = "OLED Pure Black",
        isDark = true,
        previewPrimary = Color(0xFF10B981),
        previewBg = Color(0xFF000000)
    ),
    NORDIC_FROST(
        title = "Nordic Frost Day",
        isDark = false,
        previewPrimary = Color(0xFF2563EB),
        previewBg = Color(0xFFF8FAFC)
    ),
    CYBER_NEON(
        title = "Cyber Violet",
        isDark = true,
        previewPrimary = Color(0xFFA855F7),
        previewBg = Color(0xFF0B0914)
    ),
    EMERALD_SCHOLAR(
        title = "Emerald Scholar",
        isDark = true,
        previewPrimary = Color(0xFF10B981),
        previewBg = Color(0xFF04140F)
    );

    fun getColorScheme(): ColorScheme {
        return when (this) {
            WARM_SUNSET -> darkColorScheme(
                primary = WarmDarkPrimary,
                onPrimary = Color(0xFF1C1309),
                primaryContainer = Color(0xFF452B0E),
                onPrimaryContainer = Color(0xFFFDE68A),
                secondary = WarmDarkSecondary,
                onSecondary = Color(0xFF1C1309),
                background = WarmDarkBg,
                onBackground = WarmDarkTextPrimary,
                surface = WarmDarkSurface,
                onSurface = WarmDarkTextPrimary,
                surfaceVariant = WarmDarkSurfaceVariant,
                onSurfaceVariant = WarmDarkTextSecondary,
                outline = WarmDarkBorder,
                error = VeyloraRed,
                onError = Color.White
            )
            WARM_CHALK -> lightColorScheme(
                primary = WarmLightPrimary,
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFEF3C7),
                onPrimaryContainer = Color(0xFF78350F),
                secondary = WarmLightSecondary,
                onSecondary = Color.White,
                background = WarmLightBg,
                onBackground = WarmLightTextPrimary,
                surface = WarmLightSurface,
                onSurface = WarmLightTextPrimary,
                surfaceVariant = WarmLightSurfaceVariant,
                onSurfaceVariant = WarmLightTextSecondary,
                outline = WarmLightBorder,
                error = Color(0xFFDC2626),
                onError = Color.White
            )
            COSMIC_MIDNIGHT -> darkColorScheme(
                primary = Color(0xFF00D2FF),
                onPrimary = Color(0xFF031024),
                primaryContainer = Color(0xFF0E385D),
                onPrimaryContainer = Color(0xFFCFFAFE),
                secondary = Color(0xFF818CF8),
                onSecondary = Color.White,
                background = DarkBg,
                onBackground = DarkTextPrimary,
                surface = DarkSurface,
                onSurface = DarkTextPrimary,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkTextSecondary,
                outline = DarkBorder,
                error = VeyloraRed,
                onError = Color.White
            )
            OLED_BLACK -> darkColorScheme(
                primary = Color(0xFF10B981),
                onPrimary = Color.Black,
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF06B6D4),
                onSecondary = Color.Black,
                background = Color(0xFF000000),
                onBackground = Color(0xFFFFFFFF),
                surface = Color(0xFF0E0E12),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF18181F),
                onSurfaceVariant = Color(0xFFA1A1AA),
                outline = Color(0xFF27272A),
                error = VeyloraRed,
                onError = Color.White
            )
            NORDIC_FROST -> lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFDBEAFE),
                onPrimaryContainer = Color(0xFF1E3A8A),
                secondary = Color(0xFF4F46E5),
                onSecondary = Color.White,
                background = LightBg,
                onBackground = LightTextPrimary,
                surface = LightSurface,
                onSurface = LightTextPrimary,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightTextSecondary,
                outline = LightBorder,
                error = Color(0xFFDC2626),
                onError = Color.White
            )
            CYBER_NEON -> darkColorScheme(
                primary = Color(0xFFA855F7),
                onPrimary = Color.White,
                primaryContainer = Color(0xFF3B0764),
                onPrimaryContainer = Color(0xFFF3E8FF),
                secondary = Color(0xFFEC4899),
                onSecondary = Color.White,
                background = Color(0xFF0B0914),
                onBackground = Color(0xFFFAF5FF),
                surface = Color(0xFF141026),
                onSurface = Color(0xFFFAF5FF),
                surfaceVariant = Color(0xFF1F193C),
                onSurfaceVariant = Color(0xFFC084FC),
                outline = Color(0xFF2D2254),
                error = VeyloraRed,
                onError = Color.White
            )
            EMERALD_SCHOLAR -> darkColorScheme(
                primary = Color(0xFF10B981),
                onPrimary = Color(0xFF022C22),
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF34D399),
                onSecondary = Color(0xFF022C22),
                background = Color(0xFF04140F),
                onBackground = Color(0xFFECFDF5),
                surface = Color(0xFF0A221A),
                onSurface = Color(0xFFECFDF5),
                surfaceVariant = Color(0xFF103328),
                onSurfaceVariant = Color(0xFF6EE7B7),
                outline = Color(0xFF163E30),
                error = VeyloraRed,
                onError = Color.White
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    themeId: VeyloraThemeId = VeyloraThemeId.WARM_SUNSET,
    darkTheme: Boolean = themeId.isDark,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themeId.getColorScheme(),
        typography = Typography,
        content = content
    )
}
