package dev.sebastianrn.portfolioapp.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Shared brushes for the gold "vault" visual language. The gold gradients are
 * intentionally theme-independent: gold surfaces always carry [OnGold] content.
 */
object AppGradients {

    /** Diagonal champagne sweep used on hero cards. */
    val goldCard = Brush.linearGradient(
        colors = listOf(GoldBright, GoldChampagne, Gold, GoldDeep),
        start = Offset(0f, 0f),
        end = Offset(1200f, 1400f)
    )

    /** Radial polish for round coin icons. */
    val goldCoin = Brush.radialGradient(
        colors = listOf(GoldBright, Gold, GoldDeep),
        radius = 90f
    )

    /** Vertical cast for bar/ingot icons. */
    val goldBar = Brush.verticalGradient(
        colors = listOf(GoldChampagne, Gold, GoldBronze)
    )

    /** Thin gold accent line (section markers, sheet headers). */
    val goldAccentLine = Brush.horizontalGradient(
        colors = listOf(GoldBright, GoldDeep)
    )

    /**
     * Moving specular highlight for the hero card shimmer.
     * [progress] should run from 0f to 1f on an infinite transition.
     */
    fun goldShimmer(progress: Float, width: Float = 2400f): Brush {
        val x = width * (progress * 2f - 0.5f)
        return Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent
            ),
            start = Offset(x - 500f, 0f),
            end = Offset(x + 500f, 900f)
        )
    }

    /** Soft gold glow used behind screen headers. */
    fun goldHalo(centerX: Float, alpha: Float = 0.10f) = Brush.radialGradient(
        colors = listOf(Gold.copy(alpha = alpha), Color.Transparent),
        center = Offset(centerX, 0f),
        radius = 900f
    )
}
