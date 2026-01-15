package dev.sebastianrn.portfolioapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Map Dark Colors to M3 Slots
private val DarkColorScheme = darkColorScheme(
    primary = GoldStart,
    background = PremiumBlack,
    surface = Charcoal,          // Card Background
    surfaceVariant = SurfaceGray,// Secondary Cards
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray
)

// Map Light Colors to M3 Slots
private val LightColorScheme = lightColorScheme(
    primary = GoldStart,
    background = LuxuryCream,
    surface = PearlWhite,        // Card Background
    surfaceVariant = SoftBeige,  // Secondary Cards
    onBackground = TextBlack,
    onSurface = TextBlack,
    onSurfaceVariant = TextDarkGray
)

// Add this new color scheme
private val ExpressiveColorScheme = darkColorScheme(
    primary = ExpressivePrimaryStart,
    secondary = ExpressiveSecondary,
    tertiary = ExpressiveTertiary,
    error = ExpressiveError,
    background = ExpressiveSurfaceContainer,
    surface = ExpressiveSurfaceHigh,
    surfaceVariant = ExpressiveSurfaceContainer,
    onBackground = ExpressiveOnSurface,
    onSurface = ExpressiveOnSurface,
    onSurfaceVariant = ExpressiveOnSurface.copy(alpha = 0.7f)
)

@Composable
fun PortfolioAppTheme(
    darkTheme: Boolean = true, // We will pass this dynamically
    expressiveMode: Boolean = true, // Add this parameter
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        expressiveMode -> ExpressiveColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Fix Status Bar Icons (Clock, Battery, etc.)
            // If Dark Theme -> We want Light Icons (White) -> isAppearanceLightStatusBars = false
            // If Light Theme -> We want Dark Icons (Black) -> isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightStatusBars = !darkTheme

            // Fix Navigation Bar Icons (Bottom bar)
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}