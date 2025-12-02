package com.example.portfolioapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldStart,
    onPrimary = Color.Black,
    secondary = GoldEnd,
    onSecondary = Color.Black,
    background = PremiumBlack,
    onBackground = TextWhite,
    surface = Charcoal,
    onSurface = TextWhite,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextGray
)

@Composable
fun PortfolioAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography, // You can customize typography here if needed
        content = content
    )
}