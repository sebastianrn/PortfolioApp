package dev.sebastianrn.portfolioapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun PortfolioAppTheme(
    darkTheme: Boolean = true, // We will pass this dynamically
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}