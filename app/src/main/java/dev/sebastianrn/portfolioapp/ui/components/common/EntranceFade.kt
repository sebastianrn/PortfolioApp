package dev.sebastianrn.portfolioapp.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Staggered entrance: fades and floats content in on first appearance.
 * [index] shifts the delay so consecutive items cascade.
 */
@Composable
fun EntranceFade(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 450, delayMillis = index * 90)) +
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = index * 90,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = { it / 6 }
                )
    ) {
        content()
    }
}
