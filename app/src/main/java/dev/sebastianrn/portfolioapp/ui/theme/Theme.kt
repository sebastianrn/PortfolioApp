package dev.sebastianrn.portfolioapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    primaryContainer = GoldDark,
    secondary = TealAccent,
    secondaryContainer = TealGradientEnd,
    tertiary = PurpleAccent,
    error = CoralAccent,
    background = SurfaceDark,
    surface = SurfaceCardDark,
    surfaceVariant = SurfaceElevatedDark,
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextTertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    primaryContainer = GoldLight,
    secondary = TealAccent,
    secondaryContainer = TealGradientStart,
    tertiary = PurpleAccent,
    error = CoralAccent,
    background = SurfaceLight,
    surface = SurfaceCardLight,
    surfaceVariant = SurfaceElevatedLight,
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextTertiaryLight
)

@Composable
fun PortfolioAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

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