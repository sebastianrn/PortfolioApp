package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.sebastianrn.portfolioapp.util.formatCurrency

/**
 * Currency text that counts toward its target value whenever it changes
 * (including the initial 0 → value sweep on first composition).
 *
 * The count-up animates through a Float for performance, but once the
 * animation settles the exact Double is rendered — Float precision (~7
 * significant digits) would otherwise show amounts off by a few cents.
 */
@Composable
fun AnimatedCounterText(
    value: Double,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    val target = value.toFloat()
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "counter"
    )

    val displayValue = if (animated == target) value else animated.toDouble()

    Text(
        text = displayValue.formatCurrency(),
        style = style,
        color = color,
        modifier = modifier
    )
}
