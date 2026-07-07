package dev.sebastianrn.portfolioapp.ui.theme

import androidx.compose.ui.graphics.Color

// --- GOLD / CHAMPAGNE PALETTE ---
val GoldBright = Color(0xFFF7E2A6)   // Specular highlight
val GoldChampagne = Color(0xFFEECF7F) // Light champagne
val Gold = Color(0xFFE3B94F)          // Core gold
val GoldDeep = Color(0xFFC08F2C)      // Deep gold
val GoldBronze = Color(0xFF8F6A1E)    // Bronze shadow

// Ink used on top of gold surfaces (same in both themes)
val OnGold = Color(0xFF241A03)
val OnGoldMuted = Color(0xB3241A03)   // 70% alpha espresso

// Gain/loss inks tuned for readability on gold surfaces
val GainOnGold = Color(0xFF14532D)
val LossOnGold = Color(0xFF8F1D1D)

// --- SEMANTIC ---
val EmeraldDark = Color(0xFF3DDC97)   // Positive (dark theme)
val EmeraldLight = Color(0xFF0B9663)  // Positive (light theme)
val RubyDark = Color(0xFFF07373)      // Negative (dark theme)
val RubyLight = Color(0xFFC94040)     // Negative (light theme)
val BronzeAccent = Color(0xFFD8A25A)  // Tertiary (bars, API records)

// --- DARK THEME: WARM OBSIDIAN ---
val ObsidianBackground = Color(0xFF0D0B08)
val ObsidianSurface = Color(0xFF16130D)
val ObsidianSurfaceHigh = Color(0xFF1F1B12)
val ObsidianSurfaceHighest = Color(0xFF282218)
val DarkTextPrimary = Color(0xFFF4EEDF)
val DarkTextSecondary = Color(0xFFAFA48C)
val DarkTextTertiary = Color(0xFF756C58)
val DarkOutlineVariant = Color(0xFF2E2919)

// --- LIGHT THEME: IVORY ---
val IvoryBackground = Color(0xFFF8F4EA)
val IvorySurface = Color(0xFFFFFDF7)
val IvorySurfaceHigh = Color(0xFFF1EBDC)
val IvorySurfaceHighest = Color(0xFFEAE2CE)
val LightTextPrimary = Color(0xFF211B10)
val LightTextSecondary = Color(0xFF6E6552)
val LightTextTertiary = Color(0xFF9C9280)
val LightOutlineVariant = Color(0xFFE2D9C4)

// Gold tuned for light surfaces (needs more contrast than pure champagne)
val GoldOnLight = Color(0xFF9A7517)
