package dev.sebastianrn.portfolioapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = OnGold,
    primaryContainer = Color(0xFF3A2E0E),
    onPrimaryContainer = GoldBright,
    secondary = EmeraldDark,
    onSecondary = Color(0xFF04271A),
    secondaryContainer = Color(0xFF12382A),
    onSecondaryContainer = Color(0xFF8CEFC5),
    tertiary = BronzeAccent,
    onTertiary = Color(0xFF2A1C06),
    tertiaryContainer = Color(0xFF3C2B10),
    onTertiaryContainer = Color(0xFFF2CF9A),
    error = RubyDark,
    onError = Color(0xFF330A0A),
    errorContainer = Color(0xFF4A1616),
    onErrorContainer = Color(0xFFFFC9C9),
    background = ObsidianBackground,
    onBackground = DarkTextPrimary,
    surface = ObsidianSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = Color(0xFF0A0906),
    surfaceContainerLow = Color(0xFF13110B),
    surfaceContainer = ObsidianSurface,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    surfaceContainerHighest = ObsidianSurfaceHighest,
    outline = DarkTextTertiary,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = ObsidianBackground,
    inversePrimary = GoldDeep
)

private val LightColorScheme = lightColorScheme(
    primary = GoldOnLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF4E3B2),
    onPrimaryContainer = Color(0xFF3C2E06),
    secondary = EmeraldLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDEEDF),
    onSecondaryContainer = Color(0xFF05341F),
    tertiary = Color(0xFF8A5A18),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6DFC0),
    onTertiaryContainer = Color(0xFF2E1D02),
    error = RubyLight,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBDADA),
    onErrorContainer = Color(0xFF3F0A0A),
    background = IvoryBackground,
    onBackground = LightTextPrimary,
    surface = IvorySurface,
    onSurface = LightTextPrimary,
    surfaceVariant = IvorySurfaceHigh,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF9F1),
    surfaceContainer = IvorySurface,
    surfaceContainerHigh = IvorySurfaceHigh,
    surfaceContainerHighest = IvorySurfaceHighest,
    outline = LightTextTertiary,
    outlineVariant = LightOutlineVariant,
    inverseSurface = Color(0xFF262014),
    inverseOnSurface = IvoryBackground,
    inversePrimary = GoldChampagne
)

@Composable
fun PortfolioAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Edge-to-edge is enabled in MainActivity; only the system bar icon
    // appearance needs to follow the app theme (which can differ from the
    // system theme).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
