package dev.sebastianrn.portfolioapp.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import dev.sebastianrn.portfolioapp.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Using Inter - a modern, open-source alternative with similar aesthetics to Google Sans Flex
// Inter has excellent readability, geometric design, and variable font support
private val interFont = GoogleFont("Inter")

val GoogleSansFlexFamily = FontFamily(
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Light
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold
    ),
    Font(
        googleFont = interFont,
        fontProvider = googleFontProvider,
        weight = FontWeight.Black
    )
)
